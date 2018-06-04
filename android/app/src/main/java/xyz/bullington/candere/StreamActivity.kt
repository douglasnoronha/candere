package xyz.bullington.candere

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Rect
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.SurfaceView
import com.potterhsu.rtsplibrary.NativeCallback
import com.potterhsu.rtsplibrary.RtspClient

class RtspTask(val url: String) : AsyncTask<RtspClient, Unit, Unit>() {
    var mRtspClient: RtspClient? = null

    override fun doInBackground(vararg client: RtspClient?) {
        client.first()?.let { client ->
            mRtspClient = client
            mRtspClient?.play(url)
        }
    }

    override fun onCancelled() {
        super.onCancelled()

        mRtspClient?.stop()
        mRtspClient?.dispose()
    }
}

class StreamActivity : AppCompatActivity() {
    var mSurfaceView: SurfaceView? = null
    var mRtspClient: RtspClient? = null
    var mRtspTask: RtspTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stream)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)

            toolbar?.setNavigationOnClickListener {
                stop()
            }
        }

        mSurfaceView = findViewById(R.id.videoView)
        mSurfaceView?.let { mSurfaceView ->
            val url = intent.getStringExtra(RTSP_URL)

            mRtspClient = RtspClient(NativeCallback { frame, _, width, height ->
                mSurfaceView.holder?.lockCanvas()?.let { canvas ->
                    val area = width * height
                    val pixels = IntArray(area)

                    for (i in 0 until area) {
                        var r = frame[3 * i].toInt()
                        var g = frame[3 * i + 1].toInt()
                        var b = frame[3 * i + 2].toInt()
                        if (r < 0) r += 255
                        if (g < 0) g += 255
                        if (b < 0) b += 255
                        pixels[i] = Color.rgb(r, g, b)
                    }

                    val bmp = Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)

                    canvas.drawBitmap(
                            bmp,
                            null,
                            Rect(0, 0, canvas.width, height * canvas.width / width),
                            null
                    )

                    mSurfaceView.holder?.unlockCanvasAndPost(canvas)
                }
            })

            mRtspTask = RtspTask(url)
            mRtspTask?.execute(mRtspClient)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        mRtspTask?.cancel(true)
    }

    private fun stop() {
        finish()
    }
}

