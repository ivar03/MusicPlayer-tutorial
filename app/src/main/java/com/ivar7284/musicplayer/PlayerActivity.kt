package com.ivar7284.musicplayer

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File

class PlayerActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var playPauseButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var repeatButton: ImageButton
    private lateinit var songTitleTextView: TextView

    private var isPlaying = false
    private var isRepeating = false
    private var songPath: String? = null
    private lateinit var songList: List<String>
    private var currentSongIndex = 0

    private val handler = Handler(Looper.getMainLooper())
    private val updateSeekBar = object : Runnable {
        override fun run() {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                seekBar.progress = mediaPlayer.currentPosition
                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        songPath = intent.getStringExtra("SONG_PATH")
        songList = intent.getStringArrayListExtra("SONG_LIST") ?: listOf()
        currentSongIndex = songList.indexOf(songPath)

        if (currentSongIndex == -1 && songList.isNotEmpty()) {
            currentSongIndex = 0
            songPath = songList[currentSongIndex]
        }

        setupUI()

        mediaPlayer = MediaPlayer()

        if (songPath != null) {
            playSong()
        }
    }

    private fun setupUI() {
        songTitleTextView = findViewById(R.id.song_title)
        seekBar = findViewById(R.id.seekBar)
        playPauseButton = findViewById(R.id.btn_play_pause)
        previousButton = findViewById(R.id.btn_previous)
        nextButton = findViewById(R.id.btn_next)
        repeatButton = findViewById(R.id.btn_repeat)

        playPauseButton.setOnClickListener {
            togglePlayPause()
        }

        previousButton.setOnClickListener {
            playPreviousSong()
        }

        nextButton.setOnClickListener {
            playNextSong()
        }

        repeatButton.setOnClickListener {
            toggleRepeat()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playSong() {
        Log.d("PlayerActivity", "Playing song: $songPath")

        songTitleTextView.text = File(songPath ?: "").name

        mediaPlayer.apply {
            reset()
            try {
                setDataSource(songPath)
                prepare()
                start()
                seekBar.max = duration

                setOnCompletionListener {
                    if (isRepeating) {
                        playSong() // Play the same song again if repeating
                    } else {
                        playNextSong() // Play the next song
                    }
                }

                handler.post(updateSeekBar)

                this@PlayerActivity.isPlaying = true
                playPauseButton.setBackgroundResource(R.drawable.pause_icon)

            } catch (e: Exception) {
                Log.e("PlayerActivity", "Error playing song: ${e.message}", e)
            }
        }
    }

    private fun togglePlayPause() {
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            playPauseButton.setBackgroundResource(R.drawable.play_icon)
            isPlaying = false
        } else {
            mediaPlayer.start()
            playPauseButton.setBackgroundResource(R.drawable.pause_icon)
            isPlaying = true
        }
    }

    private fun playNextSong() {
        if (songList.isNotEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songList.size
            songPath = songList[currentSongIndex]
            playSong()
        }
    }

    private fun playPreviousSong() {
        if (songList.isNotEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songList.size) % songList.size
            songPath = songList[currentSongIndex]
            playSong()
        }
    }

    private fun toggleRepeat() {
        isRepeating = !isRepeating
        repeatButton.setBackgroundResource(
            if (isRepeating) R.drawable.repeat_off else R.drawable.repeat_icon
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.release()
        }
        handler.removeCallbacks(updateSeekBar)
    }
}
