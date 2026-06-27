package com.example.sinalize.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.sinalize.mediapipe.HandLandmarkerManager
import com.example.sinalize.viewmodel.LibrasViewModel

@Composable
fun PracticeCameraScreen(
    viewModel: LibrasViewModel,
    onBackClick: () -> Unit,
    onFinishClick: () -> Unit
) {
    val context = LocalContext.current
    val lesson = viewModel.selectedLesson.value

    var feedback by remember {
        mutableStateOf("Posicione sua mão na câmera.")
    }

    var isGestureCorrect by remember {
        mutableStateOf(false)
    }

    var correctFrameCount by remember {
        mutableStateOf(0)
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val imageResId = remember(lesson?.imageName) {
        if (lesson != null) {
            context.resources.getIdentifier(
                lesson.imageName,
                "drawable",
                context.packageName
            )
        } else {
            0
        }
    }

    val expectedGesture = lesson?.imageName ?: ""

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedButton(onClick = onBackClick) {
            Text(text = "Voltar")
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Pratique o sinal: ${lesson?.title ?: ""}",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(10.dp))

        ReferenceGestureCard(
            imageResId = imageResId,
            lessonTitle = lesson?.title ?: "Gesto",
            movementTip = lesson?.movementTip ?: "Observe a imagem e repita o movimento."
        )

        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isGestureCorrect) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Text(
                    text = feedback,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isGestureCorrect) {
                        "Gesto confirmado. Você pode seguir para o quiz."
                    } else {
                        "Segure o gesto correto por alguns segundos para liberar."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Confirmação: $correctFrameCount/12",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (hasCameraPermission) {
            CameraPreviewWithHandDetection(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp),
                expectedGesture = expectedGesture,
                onFeedbackChanged = { newFeedback, correct ->
                    feedback = newFeedback

                    if (correct) {
                        correctFrameCount++
                    } else {
                        correctFrameCount = 0
                    }

                    isGestureCorrect = correctFrameCount >= 12
                }
            )
        } else {
            Text(text = "Permissão da câmera necessária para praticar os movimentos.")

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Permitir câmera")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onFinishClick,
            enabled = isGestureCorrect,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isGestureCorrect) {
                    "Ir para o quiz"
                } else {
                    "Faça o gesto correto para liberar"
                }
            )
        }
    }
}

@Composable
fun ReferenceGestureCard(
    imageResId: Int,
    lessonTitle: String,
    movementTip: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(120.dp)
                    .weight(1f)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = "Imagem de referência do sinal $lessonTitle",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(
                        text = "Sem imagem",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Gesto esperado",
                    style = MaterialTheme.typography.labelLarge
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = lessonTitle,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = movementTip,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun CameraPreviewWithHandDetection(
    modifier: Modifier = Modifier,
    expectedGesture: String,
    onFeedbackChanged: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val handLandmarkerManager = remember(expectedGesture) {
        HandLandmarkerManager(
            context = context,
            expectedGesture = expectedGesture,
            onFeedback = onFeedbackChanged
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            handLandmarkerManager.close()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        analysis.setAnalyzer(
                            ContextCompat.getMainExecutor(ctx)
                        ) { imageProxy ->
                            handLandmarkerManager.detect(imageProxy)
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }, ContextCompat.getMainExecutor(ctx))

            previewView
        }
    )
}