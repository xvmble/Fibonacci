package com.example.fibonaccinumbers

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.fibonaccinumbers.databinding.ActivityMainBinding
import java.util.concurrent.atomic.AtomicBoolean

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isStopped = AtomicBoolean(false)
    private var calcThread: Thread? = null
    private val uiHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeUI()
    }

    private fun initializeUI() {
        binding.toggleButton.setOnClickListener {
            if (binding.inputField.text.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_number), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isStopped.get()) {
                beginFiboSum()
            } else {
                stopFiboSum()
            }
            isStopped.set(!isStopped.get())
        }
    }

    private fun beginFiboSum() {
        isStopped.set(false)
        binding.toggleButton.text = getString(R.string.stop)
        binding.inputField.isEnabled = false
        val input = binding.inputField.text.toString().toInt()

        val mode = binding.modeSpinner.selectedItem.toString()
        calcThread = Thread {
            if (mode == "Sum") {
                computeFiboSum(input)
            } else {
                computeFiboValue(input)
            }
        }
        calcThread?.start()
    }

    private fun stopFiboSum() {
        isStopped.set(true)
        calcThread?.interrupt()
        binding.inputField.isEnabled = true
        uiHandler.removeCallbacksAndMessages(null)
        binding.toggleButton.text = getString(R.string.start)
        binding.outputText.text = ""
    }

    private fun computeFiboSum(n: Int) {
        if (n <= 0) return

        var result = 0
        val sequence = mutableListOf<Int>()

        for (i in 0 until n) {
            if (Thread.currentThread().isInterrupted) return

            val value = getFiboNumber(i)
            sequence.add(value)
            result += value

            val currentStep = i
            uiHandler.postDelayed({
                val stepText = getString(R.string.step_text, currentStep, value)
                binding.outputText.text = stepText
            }, i * 1000L)
        }

        uiHandler.postDelayed({
            val seqString = sequence.joinToString(", ")
            val finalText = getString(R.string.sum_result, seqString, result)
            binding.outputText.text = finalText
            binding.toggleButton.text = getString(R.string.start)
            binding.inputField.isEnabled = true
            isStopped.set(false)
        }, n * 1000L)
    }

    private fun computeFiboValue(n: Int) {
        if (n < 0) return

        var a = 0
        var b = 1
        var result = 0

        repeat(n + 1) {
            if (Thread.currentThread().isInterrupted) return
            val temp = a + b
            a = b
            b = temp
            result = temp
        }

        uiHandler.post {
            binding.outputText.text = getString(R.string.value_result, result)
            binding.toggleButton.text = getString(R.string.start)
            binding.inputField.isEnabled = true
            isStopped.set(false)
        }
    }

    private fun getFiboNumber(n: Int): Int {
        return if (n <= 1) n else getFiboNumber(n - 1) + getFiboNumber(n - 2)
    }
}
