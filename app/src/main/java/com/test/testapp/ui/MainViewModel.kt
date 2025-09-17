package com.test.testapp.ui

import android.view.MotionEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.test.testapp.domain.repository.PositionRepository
import com.test.testapp.mvi.IconIntent
import com.test.testapp.mvi.IconState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max
import kotlin.math.min

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repo: PositionRepository
) : ViewModel() {

    private val intents = MutableSharedFlow<IconIntent>(
        extraBufferCapacity = 32,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private val _state = MutableStateFlow(IconState())
    val state: StateFlow<IconState> = _state.asStateFlow()

    private var savedXFraction: Float = 0.5f
    private var savedYFraction: Float = 0.5f
    private var fractionsLoaded = false

    init {
        // читаємо збережені фракції одноразово
        viewModelScope.launch {
            repo.positions.firstOrNull()?.let { f ->
                savedXFraction = f.xRatio
                savedYFraction = f.yRatio
            }
            fractionsLoaded = true
            tryRestore()
        }

        // головний редʼюсер
        viewModelScope.launch {
            intents.collect { intent -> reduce(intent) }
        }
    }

    fun send(intent: IconIntent) { intents.tryEmit(intent) }

    private fun reduce(intent: IconIntent) {
        when (intent) {
            is IconIntent.OnMeasured -> {
                val s = _state.value.copy(
                    containerW = intent.containerW,
                    containerH = intent.containerH,
                    iconW = intent.iconW,
                    iconH = intent.iconH
                )
                _state.value = s
                tryRestore()
            }

            is IconIntent.OnTouch -> {
                val s = _state.value
                if (!s.canInteract) return
                when (intent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        _state.value = s.copy(
                            isDragging = true,
                            grabDx = intent.xInContainer - s.xPx,
                            grabDy = intent.yInContainer - s.yPx
                        )
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!s.isDragging) return
                        val (cx, cy) = clamp(
                            intent.xInContainer - s.grabDx,
                            intent.yInContainer - s.grabDy,
                            s
                        )
                        _state.value = s.copy(xPx = cx, yPx = cy)
                    }
                    MotionEvent.ACTION_UP,
                    MotionEvent.ACTION_CANCEL -> {
                        if (!s.isDragging) return
                        val (cx, cy) = clamp(s.xPx, s.yPx, s)
                        val newS = s.copy(isDragging = false, xPx = cx, yPx = cy)
                        _state.value = newS
                        viewModelScope.launch {
                            val moveW = max(1, newS.containerW - newS.iconW).toFloat()
                            val moveH = max(1, newS.containerH - newS.iconH).toFloat()
                            val xf = (cx / moveW).coerceIn(0f, 1f)
                            val yf = (cy / moveH).coerceIn(0f, 1f)
                            repo.save(xf, yf)               // <-- через репозиторій
                            savedXFraction = xf
                            savedYFraction = yf
                        }
                    }
                }
            }

            else -> Unit
        }
    }

    private fun clamp(x: Float, y: Float, s: IconState): Pair<Float, Float> {
        val maxX = (s.containerW - s.iconW).toFloat()
        val maxY = (s.containerH - s.iconH).toFloat()
        val cx = min(max(0f, x), maxX)
        val cy = min(max(0f, y), maxY)
        return cx to cy
    }

    private fun tryRestore() {
        val s = _state.value
        if (!fractionsLoaded || !s.canInteract) return

        val moveW = max(1, s.containerW - s.iconW).toFloat()
        val moveH = max(1, s.containerH - s.iconH).toFloat()
        _state.value = s.copy(
            xPx = (savedXFraction * moveW).coerceIn(0f, moveW),
            yPx = (savedYFraction * moveH).coerceIn(0f, moveH)
        )
    }
}
