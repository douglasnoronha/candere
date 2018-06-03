package xyz.bullington.candere

import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.widget.Toast
import android.widget.VideoView

/**
 * This activity helps us to show the live stream of an ONVIF camera thanks to VLC library.
 */
class StreamActivity : AppCompatActivity() {

    var videoView: VideoView? = null

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

        videoView = findViewById(R.id.videoView)
        videoView?.let { videoView ->
            val url = intent.getStringExtra(RTSP_URL)
            videoView.setVideoURI(Uri.parse(url))
            videoView.start()
        }
    }

    private fun stop() {
        videoView?.stopPlayback()
        finish()
    }
}

