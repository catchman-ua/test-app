package com.test.testapp.ui

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.test.testapp.mvi.IconIntent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import androidx.lifecycle.lifecycleScope
import com.test.testapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding
    private val vm: MainViewModel by viewModels()   // <-- фабрика більше не потрібна

    private val tmp = IntArray(2)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.dragContainer.post {
            vm.send(
                IconIntent.OnMeasured(
                    binding.dragContainer.width,
                    binding.dragContainer.height,
                    binding.draggableIcon.width,
                    binding.draggableIcon.height
                )
            )
        }

        lifecycleScope.launch {
            vm.state.collectLatest { s ->
                binding.draggableIcon.x = s.xPx
                binding.draggableIcon.y = s.yPx
                binding.draggableIcon.scaleX = if (s.isDragging) 1.05f else 1f
                binding.draggableIcon.scaleY = if (s.isDragging) 1.05f else 1f
                binding.draggableIcon.alpha = if (s.isDragging) 0.95f else 1f
            }
        }


        binding.draggableIcon.setOnTouchListener { _, e ->
            val (cx, cy) = pointerInContainer(binding.dragContainer, e)
            vm.send(IconIntent.OnTouch(e.action, cx, cy))
            true
        }
    }

    private fun pointerInContainer(container: View, e: MotionEvent): Pair<Float, Float> {
        container.getLocationOnScreen(tmp)
        val containerX = tmp[0].toFloat()
        val containerY = tmp[1].toFloat()
        return (e.rawX - containerX) to (e.rawY - containerY)
    }
}
