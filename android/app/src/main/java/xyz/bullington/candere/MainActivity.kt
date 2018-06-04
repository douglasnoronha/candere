package xyz.bullington.candere

import android.Manifest
import android.arch.persistence.room.Room
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.os.Environment
import android.text.format.Formatter
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import com.nabinbhandari.android.permissions.Permissions
import java.io.File
import com.nabinbhandari.android.permissions.PermissionHandler

const val PORT = 27960
const val RTSP_URL = "xyz.bullington.candere.RTSP_URL"

class Bridge(private val activity: MainActivity) {
    @JavascriptInterface
    fun getHref(): String {
        val wm = activity.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)

        return "http://$ip:$PORT/"
    }

    @JavascriptInterface
    fun launchStreamActivity(rtspUrl: String) {
        activity.launchStreamActivity(rtspUrl)
    }
}

class MainActivity : AppCompatActivity() {
    private var mDeviceDatabase: DeviceDatabase? = null
    private var mUIHandler: Handler? = null

    private var mWebServer: WebServer? = null

    private var mWebView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Permissions.check(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                null,
                null,
                object : PermissionHandler() {
                    override fun onGranted() {
                        val directoryPath = Environment.getExternalStorageDirectory().absolutePath + "/candere"
                        val dir = File(directoryPath)
                        if (!dir.exists() || !dir.isDirectory) {
                            val decompress = Decompress(this@MainActivity, this@MainActivity.applicationContext.resources.openRawResource(R.raw.webapp), directoryPath)
                            decompress.unzip()
                        }

                        mDeviceDatabase = Room.databaseBuilder(applicationContext, DeviceDatabase::class.java, "devices").build()
                        mUIHandler = Handler(Looper.getMainLooper())

                        mWebServer = WebServer(this@MainActivity, mUIHandler!!, mDeviceDatabase!!, PORT, directoryPath)
                        mWebServer?.start()

                        WebView.setWebContentsDebuggingEnabled(true)

                        mWebView = findViewById(R.id.webview)

                        mWebView?.settings?.javaScriptEnabled = true
                        mWebView?.addJavascriptInterface(Bridge(this@MainActivity), "_candere_bridge")
                        mWebView?.webChromeClient = object: WebChromeClient() {
                            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                Log.d(TAG, consoleMessage?.message())
                                return true
                            }
                        }

                        mWebView?.loadUrl("http://127.0.0.1:$PORT")
                    }
                }
        )
    }

    override fun onDestroy() {
        super.onDestroy()

        mDeviceDatabase?.close()
        mWebServer?.stop()
    }

    override fun onBackPressed() {
        if (mWebView != null && mWebView!!.canGoBack()) {
            mWebView?.goBack()
        } else {
            super.onBackPressed()
        }
    }

    fun launchStreamActivity(rtspUrl: String) {
        val intent = Intent(this, StreamActivity::class.java).apply {
            putExtra(RTSP_URL, rtspUrl)
        }

        startActivity(intent)
    }
}
