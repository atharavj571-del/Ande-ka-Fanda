package com.example.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.PodcastSegment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class PodcastSpeechEngine(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentSegmentIndex = MutableStateFlow(0)
    val currentSegmentIndex: StateFlow<Int> = _currentSegmentIndex.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private var playlist: List<PodcastSegment> = emptyList()

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (t: Throwable) {
            Log.e("PodcastTTS", "Failed to instantiate TextToSpeech engine on this device", t)
            tts = null
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            try {
                val result = tts?.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("PodcastTTS", "Language US not supported")
                } else {
                    isInitialized = true
                    setupUtteranceListener()
                }
            } catch (e: Exception) {
                Log.e("PodcastTTS", "Error during TTS onInit setup", e)
            }
        } else {
            Log.e("PodcastTTS", "TTS Initialization failed with status: $status")
        }
    }

    private fun setupUtteranceListener() {
        try {
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isPlaying.value = true
                }

                override fun onDone(utteranceId: String?) {
                    mainHandler.post {
                        try {
                            val nextIndex = _currentSegmentIndex.value + 1
                            if (nextIndex < playlist.size) {
                                _currentSegmentIndex.value = nextIndex
                                speakSegment(playlist[nextIndex])
                            } else {
                                _isPlaying.value = false
                            }
                        } catch (e: Exception) {
                            Log.e("PodcastTTS", "Error progressing to next segment", e)
                            _isPlaying.value = false
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    mainHandler.post {
                        _isPlaying.value = false
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("PodcastTTS", "Failed to set UtteranceProgressListener", e)
        }
    }

    fun loadPlaylist(segments: List<PodcastSegment>) {
        stop()
        playlist = segments
        _currentSegmentIndex.value = 0
    }

    fun play() {
        if (!isInitialized || playlist.isEmpty()) return
        val index = _currentSegmentIndex.value
        if (index in playlist.indices) {
            speakSegment(playlist[index])
        }
    }

    fun pause() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("PodcastTTS", "Error pausing TTS", e)
        } finally {
            _isPlaying.value = false
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("PodcastTTS", "Error stopping TTS", e)
        } finally {
            _isPlaying.value = false
        }
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        if (_isPlaying.value) {
            play()
        }
    }

    fun seekToSegment(index: Int) {
        if (index in playlist.indices) {
            stop()
            _currentSegmentIndex.value = index
            if (_isPlaying.value) {
                speakSegment(playlist[index])
            }
        }
    }

    fun nextSegment() {
        if (_currentSegmentIndex.value < playlist.size - 1) {
            seekToSegment(_currentSegmentIndex.value + 1)
        }
    }

    fun previousSegment() {
        if (_currentSegmentIndex.value > 0) {
            seekToSegment(_currentSegmentIndex.value - 1)
        }
    }

    private fun speakSegment(segment: PodcastSegment) {
        if (!isInitialized || tts == null) return

        try {
            if (segment.speaker == "FEMALE_HOST") {
                tts?.setPitch(1.25f)
            } else {
                tts?.setPitch(0.80f)
            }

            tts?.setSpeechRate(_playbackSpeed.value)

            val utteranceId = "SEGMENT_${segment.id}"
            tts?.speak(segment.dialogueText, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            _isPlaying.value = true
        } catch (e: Exception) {
            Log.e("PodcastTTS", "Error in speakSegment", e)
            _isPlaying.value = false
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("PodcastTTS", "Error shutting down TTS", e)
        }
    }
}
