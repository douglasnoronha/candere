package xyz.bullington.candere

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
import android.text.format.Formatter

const val PORT = 27960
const val RTSP_URL = "xyz.bullington.candere.RTSP_URL"

class Bridge(private val activity: MainActivity) {
    @JavascriptInterface
    fun getHref(): String {
        val wm = activity.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ip = Formatter.formatIpAddress(wm.connectionInfo.ipAddress)

        return "http://$ip:$PORT/static/"
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

        mDeviceDatabase = Room.databaseBuilder(applicationContext, DeviceDatabase::class.java, "devices").build()
        mUIHandler = Handler(Looper.getMainLooper())

        mWebServer = WebServer(this, mUIHandler!!, mDeviceDatabase!!, PORT)
        mWebServer?.start()

        mWebView = findViewById(R.id.webview)
        mWebView?.settings?.javaScriptEnabled = true
        mWebView?.addJavascriptInterface(Bridge(this), "_candere_bridge")
    }

    override fun onDestroy() {
        super.onDestroy()

        mWebServer?.stop()
    }

    fun launchStreamActivity(rtspUrl: String) {
        val intent = Intent(this, StreamActivity::class.java).apply {
            putExtra(RTSP_URL, rtspUrl)
        }

        startActivity(intent)
    }
}
