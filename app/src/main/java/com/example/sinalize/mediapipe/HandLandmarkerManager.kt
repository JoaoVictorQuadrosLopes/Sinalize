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
            .setMinHandDetectionConfidence(0.7f)
            .setMinHandPresenceConfidence(0.7f)
            .setMinTrackingConfidence(0.7f)
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

                if (
                    vuIndex < vBytes.size &&
                    vuIndex < uBytes.size &&
                    offset + 1 < nv21.size
                ) {
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
            "numero_5" -> analyzeNumberFive(hand)
            "letra_a" -> analyzeLetterA(hand)
            "letra_b" -> analyzeLetterB(hand)

            "ola" -> analyzeOpenHand(
                hand,
                "Mão aberta detectada! Segure o gesto para liberar o quiz."
            )

            "bom_dia" -> analyzeOpenHand(
                hand,
                "Mão aberta detectada! Segure o gesto para liberar o quiz."
            )

            "obrigado" -> analyzeOpenHand(
                hand,
                "Mão aberta detectada! Segure o gesto para liberar o quiz."
            )

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

        val middleOpen = isFingerOpen(hand, 12, 10)
        val ringOpen = isFingerOpen(hand, 16, 14)
        val pinkyOpen = isFingerOpen(hand, 20, 18)

        val indexTip = hand[8]
        val middleTip = hand[12]
        val ringTip = hand[16]
        val pinkyTip = hand[20]

        val indexClearlyHighest =
            indexTip.y() < middleTip.y() - 0.04f &&
                    indexTip.y() < ringTip.y() - 0.04f &&
                    indexTip.y() < pinkyTip.y() - 0.04f

        return when {
            indexOpen &&
                    middleClosed &&
                    ringClosed &&
                    pinkyClosed &&
                    indexClearlyHighest -> {
                GestureAnalysis("Correto! Esse parece o sinal do número 1.", true)
            }

            !indexOpen -> {
                GestureAnalysis("Levante bem apenas o dedo indicador.", false)
            }

            middleOpen || ringOpen || pinkyOpen -> {
                GestureAnalysis("Feche bem os outros dedos. Deixe só o indicador levantado.", false)
            }

            !indexClearlyHighest -> {
                GestureAnalysis("Levante mais o indicador para destacar o número 1.", false)
            }

            else -> {
                GestureAnalysis("Ajuste a mão para formar melhor o número 1.", false)
            }
        }
    }

    private fun analyzeNumberTwo(hand: List<NormalizedLandmark>): GestureAnalysis {
        val indexOpen = isFingerOpen(hand, 8, 6)
        val middleOpen = isFingerOpen(hand, 12, 10)

        val ringClosed = isFingerClosed(hand, 16, 14)
        val pinkyClosed = isFingerClosed(hand, 20, 18)

        val ringOpen = isFingerOpen(hand, 16, 14)
        val pinkyOpen = isFingerOpen(hand, 20, 18)

        val indexTip = hand[8]
        val middleTip = hand[12]
        val ringTip = hand[16]
        val pinkyTip = hand[20]

        val indexAndMiddleClearlyHighest =
            indexTip.y() < ringTip.y() - 0.04f &&
                    indexTip.y() < pinkyTip.y() - 0.04f &&
                    middleTip.y() < ringTip.y() - 0.04f &&
                    middleTip.y() < pinkyTip.y() - 0.04f

        return when {
            indexOpen &&
                    middleOpen &&
                    ringClosed &&
                    pinkyClosed &&
                    indexAndMiddleClearlyHighest -> {
                GestureAnalysis("Correto! Esse parece o sinal do número 2.", true)
            }

            !indexOpen || !middleOpen -> {
                GestureAnalysis("Levante bem o indicador e o dedo médio.", false)
            }

            ringOpen || pinkyOpen -> {
                GestureAnalysis("Feche o anelar e o mindinho.", false)
            }

            else -> {
                GestureAnalysis("Ajuste os dedos para formar melhor o número 2.", false)
            }
        }
    }

    private fun analyzeNumberFive(hand: List<NormalizedLandmark>): GestureAnalysis {
        val indexOpen = isFingerOpen(hand, 8, 6)
        val middleOpen = isFingerOpen(hand, 12, 10)
        val ringOpen = isFingerOpen(hand, 16, 14)
        val pinkyOpen = isFingerOpen(hand, 20, 18)

        val fingersOpen = indexOpen && middleOpen && ringOpen && pinkyOpen
        val fingersSpread = areFingersSpread(hand)

        return when {
            fingersOpen && fingersSpread -> {
                GestureAnalysis("Correto! Esse parece o sinal do número 5.", true)
            }

            !fingersOpen -> {
                GestureAnalysis("Abra todos os dedos para formar o número 5.", false)
            }

            !fingersSpread -> {
                GestureAnalysis("Separe melhor os dedos para formar o número 5.", false)
            }

            else -> {
                GestureAnalysis("Ajuste a mão para formar melhor o número 5.", false)
            }
        }
    }

    private fun analyzeLetterA(hand: List<NormalizedLandmark>): GestureAnalysis {
        val indexClosed = isFingerClosed(hand, 8, 6)
        val middleClosed = isFingerClosed(hand, 12, 10)
        val ringClosed = isFingerClosed(hand, 16, 14)
        val pinkyClosed = isFingerClosed(hand, 20, 18)

        val allClosed = indexClosed && middleClosed && ringClosed && pinkyClosed

        return if (allClosed) {
            GestureAnalysis("Correto! Esse parece a letra A.", true)
        } else {
            GestureAnalysis("Feche bem a mão para formar a letra A.", false)
        }
    }

    private fun analyzeLetterB(hand: List<NormalizedLandmark>): GestureAnalysis {
        val indexOpen = isFingerOpen(hand, 8, 6)
        val middleOpen = isFingerOpen(hand, 12, 10)
        val ringOpen = isFingerOpen(hand, 16, 14)
        val pinkyOpen = isFingerOpen(hand, 20, 18)

        val fingersOpen = indexOpen && middleOpen && ringOpen && pinkyOpen
        val fingersTogether = areFingersTogether(hand)

        return when {
            fingersOpen && fingersTogether -> {
                GestureAnalysis("Correto! Esse parece a letra B.", true)
            }

            !fingersOpen -> {
                GestureAnalysis("Abra todos os dedos para formar a letra B.", false)
            }

            !fingersTogether -> {
                GestureAnalysis("Deixe os dedos mais juntos para formar a letra B.", false)
            }

            else -> {
                GestureAnalysis("Ajuste a mão para formar melhor a letra B.", false)
            }
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

        val fingersOpen = indexOpen && middleOpen && ringOpen && pinkyOpen

        return if (fingersOpen) {
            GestureAnalysis(successMessage, true)
        } else {
            GestureAnalysis("Abra bem a mão para praticar este sinal.", false)
        }
    }

    private fun isFingerOpen(
        landmarks: List<NormalizedLandmark>,
        tipIndex: Int,
        pipIndex: Int
    ): Boolean {
        val tip = landmarks[tipIndex]
        val pip = landmarks[pipIndex]

        val margin = 0.055f

        return tip.y() < pip.y() - margin
    }

    private fun isFingerClosed(
        landmarks: List<NormalizedLandmark>,
        tipIndex: Int,
        pipIndex: Int
    ): Boolean {
        val tip = landmarks[tipIndex]
        val pip = landmarks[pipIndex]

        val margin = 0.025f

        return tip.y() > pip.y() + margin
    }

    private fun areFingersTogether(hand: List<NormalizedLandmark>): Boolean {
        val indexTip = hand[8]
        val middleTip = hand[12]
        val ringTip = hand[16]
        val pinkyTip = hand[20]

        val indexMiddleDistance = kotlin.math.abs(indexTip.x() - middleTip.x())
        val middleRingDistance = kotlin.math.abs(middleTip.x() - ringTip.x())
        val ringPinkyDistance = kotlin.math.abs(ringTip.x() - pinkyTip.x())

        return indexMiddleDistance < 0.09f &&
                middleRingDistance < 0.09f &&
                ringPinkyDistance < 0.09f
    }

    private fun areFingersSpread(hand: List<NormalizedLandmark>): Boolean {
        val indexTip = hand[8]
        val middleTip = hand[12]
        val ringTip = hand[16]
        val pinkyTip = hand[20]

        val indexMiddleDistance = kotlin.math.abs(indexTip.x() - middleTip.x())
        val middleRingDistance = kotlin.math.abs(middleTip.x() - ringTip.x())
        val ringPinkyDistance = kotlin.math.abs(ringTip.x() - pinkyTip.x())

        return indexMiddleDistance > 0.025f &&
                middleRingDistance > 0.025f &&
                ringPinkyDistance > 0.025f
    }

    fun close() {
        handLandmarker.close()
    }
}

data class GestureAnalysis(
    val message: String,
    val isCorrect: Boolean
)