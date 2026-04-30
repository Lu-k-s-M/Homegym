package com.example.homegym.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.homegym.databinding.ActivityPlaybackBinding
import com.example.homegym.data.api.RetrofitClient
import com.example.homegym.data.repository.HistorialRepository
import com.example.homegym.data.repository.EjercicioRepository
import com.example.homegym.util.CursorController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class PlaybackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaybackBinding
    private var player: ExoPlayer? = null
    private lateinit var cursorController: CursorController
    private val hideControlsHandler = Handler(Looper.getMainLooper())
    private val hideControlsRunnable = Runnable { hideControls() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaybackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoUrl = intent.getStringExtra("VIDEO_URL") ?: ""
        val title = intent.getStringExtra("VIDEO_TITLE") ?: "Ejercicio"

        binding.tvVideoTitle.text = title

        val ejercicioId = intent.getIntExtra("EJERCICIO_ID", -1)
        if (ejercicioId != -1) {
            registrarEnHistorial(ejercicioId)
        }

        setupPlayer(videoUrl)
        setupCursor()
        setupControls()

        showControls()
    }

    private fun setupPlayer(url: String) {
        val ejercicioId = intent.getIntExtra("EJERCICIO_ID", -1)
        
        player = ExoPlayer.Builder(this).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            
            if (url.isEmpty() && ejercicioId != -1) {
                // Si no tenemos URL pero sí ID, intentamos obtener el ejercicio para sacar su URL
                cargarYReproducir(ejercicioId, exoPlayer)
            } else {
                val finalUrl = if (url.isEmpty()) "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4" else url
                val mediaItem = MediaItem.fromUri(finalUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
            
            exoPlayer.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    updatePlayPauseIcon(isPlaying)
                }
            })
        }
    }

    private fun cargarYReproducir(ejercicioId: Int, exoPlayer: ExoPlayer) {
        val repository = EjercicioRepository(RetrofitClient.instance)
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val token = com.example.homegym.util.TokenManager.getToken(this@PlaybackActivity).first()
                if (token != null) {
                    val response = repository.getEjercicios(token)
                    if (response.isSuccessful) {
                        val ejercicio = response.body()?.find { it.id == ejercicioId }
                        val url = ejercicio?.videoUrl ?: "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                        val mediaItem = MediaItem.fromUri(url)
                        exoPlayer.setMediaItem(mediaItem)
                        exoPlayer.prepare()
                        exoPlayer.playWhenReady = true
                        binding.tvVideoTitle.text = ejercicio?.nombre ?: binding.tvVideoTitle.text
                    }
                }
            } catch (e: Exception) {
                // Fallback
                val mediaItem = MediaItem.fromUri("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
        }
    }

    private fun setupCursor() {
        cursorController = CursorController(this)
        cursorController.attach()
    }

    private fun setupControls() {
        binding.btnPlayPause.setOnClickListener {
            player?.let {
                if (it.isPlaying) it.pause() else it.play()
            }
            showControls()
        }

        binding.btnForward.setOnClickListener {
            player?.let {
                it.seekTo(it.currentPosition + 10000)
            }
            showControls()
        }

        binding.btnRewind.setOnClickListener {
            player?.let {
                it.seekTo(it.currentPosition - 10000)
            }
            showControls()
        }
    }

    private fun updatePlayPauseIcon(isPlaying: Boolean) {
        val icon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        binding.btnPlayPause.setImageResource(icon)
    }

    private fun showControls() {
        binding.controlsLayout.visibility = View.VISIBLE
        hideControlsHandler.removeCallbacks(hideControlsRunnable)
        hideControlsHandler.postDelayed(hideControlsRunnable, 3000)
    }

    private fun hideControls() {
        binding.controlsLayout.visibility = View.GONE
    }

    private fun registrarEnHistorial(ejercicioId: Int) {
        val repository = HistorialRepository(this, RetrofitClient.instance)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.agregarAlHistorial(ejercicioId)
            } catch (e: Exception) {
                // Silently fail or log
            }
        }
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN) {
            showControls()
        }
        return cursorController.handleKeyEvent(event) || super.dispatchKeyEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        hideControlsHandler.removeCallbacks(hideControlsRunnable)
    }
}