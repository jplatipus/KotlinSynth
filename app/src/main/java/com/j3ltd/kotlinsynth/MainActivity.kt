package com.j3ltd.kotlinsynth

import android.media.AudioFormat
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem

import kotlinx.android.synthetic.main.activity_main.*
import android.media.AudioFormat.ENCODING_PCM_16BIT
import android.media.AudioFormat.CHANNEL_OUT_MONO
import android.media.AudioTrack
import android.media.AudioManager
import android.widget.SeekBar
import kotlinx.android.synthetic.main.content_main.*


class MainActivity : AppCompatActivity() {
    var t : AudioThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        t = AudioThread()

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        frequency.setOnSeekBarChangeListener( object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar : SeekBar) { }
            override fun onStartTrackingTouch( seekBar : SeekBar) { }
            override fun onProgressChanged( seekBar : SeekBar, progress : Int, fromUser : Boolean) {
                if(fromUser) {
                    t?.fsliderVal = progress / seekBar.getMax().toDouble()
                    frequencyValue.text = "Frequency: " + t?.fsliderVal
                };
            }
        })

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
        t?.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
            t!!.isRunning = false

            try {
                t!!.join()
            } catch (e: InterruptedException) {
                print(e)
            }
            t = null;
    }
}

class AudioThread : Thread() {
    val sr = 44100
    var isRunning = true
    var fsliderVal : Double = 0.0

    override fun run() {
        priority = Thread.MAX_PRIORITY
        val buffsize = AudioTrack.getMinBufferSize(sr, AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT)
// create an audiotrack object
        val audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                sr,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                buffsize,
                AudioTrack.MODE_STREAM
        )
        val samples = ShortArray(buffsize)
        val amp = 10000
        val twopi = 8.0 * Math.atan(1.0)
        var fr = 440.0
        var ph = 0.0

        // start audio
        audioTrack.play()

        // synthesis loop
        while (isRunning) {
            fr = 440 + 440 * fsliderVal
            for (i in 0..buffsize - 1) {
                samples[i] = (amp * Math.sin(ph)).toShort()
                ph += twopi * fr / sr
            }
            audioTrack.write(samples, 0, buffsize)
        }

        audioTrack.stop()
        audioTrack.release()
    }
}