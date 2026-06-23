package com.example.sinalize.mediapipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.SystemClock
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class HandLandmarkerManager(
    context: Context,
    private val expectedGesture: String,
    private val onFeedback: (String, Boolean) -> Unit
) {
    private var handLandmarker: HandLandmarker

    init {
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("hand_landmarker.task")
            .build()

        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumHands(1)
            .setMinHandDetectionConfidence(0.6f)
            .setMinHandPresenceConfidence(0.6f)
            .setMinTrackingConfidence(0.6f)
            .setResultListener { result, _ ->
                val analysis = analyzeGesture(result)
                onFeedback(analysis.message, analysis.isCorrect)
            }
            .setErrorListener { error ->
                onFeedback("Erro no MediaPipe: ${error.message}", false)
            }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    @OptIn(ExperimentalGetImage::class)
    fun detect(imageProxy: ImageProxy) {
        try {
            val bitmap = imageProxyToBitmap(imageProxy)

            if (bitmap == null) {
                onFeedback("Não foi possível converter imagem da câmera.", false)
                imageProxy.close()
                return
            }

            val argbBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)

            val mpImage = BitmapImageBuilder(argbBitmap).build()
            val frameTime = SystemClock.uptimeMillis()

            handLandmarker.detectAsync(mpImage, frameTime)
        } catch (e: Exception) {
            onFeedback("Erro ao analisar imagem: ${e.message}", false)
        } finally {
            imageProxy.close()
        }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap? {
        val image = imageProxy.image ?: return null

        if (image.format != ImageFormat.YUV_420_888) {
            return null
        }

        val nv21 = yuv420ToNv21(imageProxy)

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            imageProxy.width,
            imageProxy.height,
            null
        )

        val outputStream = ByteArrayOutputStream()

        yuvImage.compressToJpeg(
            Rect(0, 0, imageProxy.width, imageProxy.height),
            90,
            outputStream
        )

        val imageBytes = outputStream.toByteArray()

        return BitmapFactory.decodeByteArray(
            imageBytes,
            0,
            imageBytes.size
        )
    }

    private fun yuv420ToNv21(imageProxy: ImageProxy): ByteArray {
        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)

        val chromaRowStride = imageProxy.planes[1].rowStride
        val chromaPixelStride = imageProxy.planes[1].pixelStride

        val width = imageProxy.width
        val height = imageProxy.height

        var offset = ySize

        val uBytes = uBuffer.toByteArray()
        val vBytes = vBuffer.toByteArray()

        for (row in 0 until height / 2) {
            for (col in 0 until width / 2) {
                val vuIndex = row * chromaRowStride + col * chromaPixelStride

                if (vuIndex < vBytes.size && vuIndex < uBytes.size && offset + 1 < nv21.size) {
                    nv21[offset++] = vBytes[vuIndex]
                    nv21[offset++] = uBytes[vuIndex]
                }
            }
        }

        return nv21
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        val data = ByteArray(remaining())
        get(data)
        return data
    }

    private fun analyzeGesture(result: HandLandmarkerResult): GestureAnalysis {
        if (result.landmarks().isEmpty()) {
            return GestureAnalysis(
                message = "Mão não detectada. Posicione sua mão na câmera.",
                isCorrect = false
            )
        }

        val hand = result.landmarks()[0]

        return when (expectedGesture) {
            "numero_1" -> analyzeNumberOne(hand)
            "numero_2" -> analyzeNumberTwo(hand)
            "letra_a" -> analyzeLetterA(hand)
            "letra_b" -> analyzeLetterB(hand)
            "ola" -> analyzeOpenHand(hand, "Mão aberta detectada! Pode seguir para o quiz.")
            "bom_dia" -> analyzeOpenHand(hand, "Mão aberta detectada! Pode seguir para o quiz.")
            "obrigado" -> analyzeOpenHand(hand, "Mão aberta detectada! Pode seguir para o quiz.")

            else -> GestureAnalysis(
                message = "Gesto detectado. Para esta aula, a validação automática ainda é simples.",
                isCorrect = true
            )
        }
    }

    private fun analyzeNumberOne(hand: List<NormalizedLandmark>): GestureAnalysis {
        val indexOpen = isFingerOpen(hand, 8, 6)
        val middleClosed = isFingerClosed(hand, 12, 10)
        val ringClosed = isFingerClosed(hand, 16, 14)
        val pinkyClosed = isFingerClosed(hand, 20, 18)

        return when {
            indexOpen && middleClosed && ringClosed && pinkyClosed -> {
                GestureAnalysis("Correto! Esse parece o sinal do número 1.", true)
            }

            !indexOpen -> {
                GestureAnalysis("Levante apenas o dedo indicador.", false)
            }

            else -> {
                GestureAnalysis("Feche os outros dedos e deixe só o indicador levantado.", false)
            }
        }
    }

    private fun analyzeNumberTwo(hand: List<NormalizedLandmark>): GestureAnalysis {
        val indexOpen = isFingerOpen(hand, 8, 6)
        val middleOpen = isFingerOpen(hand, 12, 10)
        val ringClosed = isFingerClosed(hand, 16, 14)
        val pinkyClosed = isFingerClosed(hand, 20, 18)

        return when {
            indexOpen && middleOpen && ringClosed && pinkyClosed -> {
                GestureAnalysis("Correto! Esse parece o sinal do número 2.", true)
            }

            !indexOpen || !middleOpen -> {
                GestureAnalysis("Levante o indicador e o dedo médio.", false)
            }

            else -> {
                GestureAnalysis("Feche o anelar e o mindinho.", false)
            }
        }
    }

    private fun analyzeLetterA(hand: List<NormalizedLandmark>): GestureAnalysis {
        val indexClosed = isFingerClosed(hand, 8, 6)
        val middleClosed = isFingerClosed(hand, 12, 10)
        val ringClosed = isFingerClosed(hand, 16, 14)
        val pinkyClosed = isFingerClosed(hand, 20, 18)

        return if (indexClosed && middleClosed && ringClosed && pinkyClosed) {
            GestureAnalysis("Correto! Esse parece a letra A.", true)
        } else {
            GestureAnalysis("Feche a mão para formar a letra A.", false)
        }
    }

    private fun analyzeLetterB(hand: List<NormalizedLandmark>): GestureAnalysis {
        val indexOpen = isFingerOpen(hand, 8, 6)
        val middleOpen = isFingerOpen(hand, 12, 10)
        val ringOpen = isFingerOpen(hand, 16, 14)
        val pinkyOpen = isFingerOpen(hand, 20, 18)

        return if (indexOpen && middleOpen && ringOpen && pinkyOpen) {
            GestureAnalysis("Correto! Esse parece a letra B.", true)
        } else {
            GestureAnalysis("Abra os dedos e mantenha a mão estendida para formar a letra B.", false)
        }
    }

    private fun analyzeOpenHand(
        hand: List<NormalizedLandmark>,
        successMessage: String
    ): GestureAnalysis {
        val indexOpen = isFingerOpen(hand, 8, 6)
        val middleOpen = isFingerOpen(hand, 12, 10)
        val ringOpen = isFingerOpen(hand, 16, 14)
        val pinkyOpen = isFingerOpen(hand, 20, 18)

        return if (indexOpen && middleOpen && ringOpen && pinkyOpen) {
            GestureAnalysis(successMessage, true)
        } else {
            GestureAnalysis("Abra a mão para praticar este sinal.", false)
        }
    }

    private fun isFingerOpen(
        landmarks: List<NormalizedLandmark>,
        tipIndex: Int,
        pipIndex: Int
    ): Boolean {
        val tip = landmarks[tipIndex]
        val pip = landmarks[pipIndex]

        return tip.y() < pip.y()
    }

    private fun isFingerClosed(
        landmarks: List<NormalizedLandmark>,
        tipIndex: Int,
        pipIndex: Int
    ): Boolean {
        val tip = landmarks[tipIndex]
        val pip = landmarks[pipIndex]

        return tip.y() > pip.y()
    }

    fun close() {
        handLandmarker.close()
    }
}

data class GestureAnalysis(
    val message: String,
    val isCorrect: Boolean
)