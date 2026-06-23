# Sinalize MediaPipe

App Android em Kotlin + Jetpack Compose para aprendizado básico de Libras.

## O que já vem implementado

- Home
- Categorias
- Aulas
- Imagens demonstrativas placeholder em drawable
- Prática com câmera frontal
- Integração preparada com MediaPipe Hand Landmarker
- Feedback em tempo real para:
  - Número 1
  - Número 2
  - Mão aberta
  - Mão fechada
- Quiz
- Progresso com XP

## Importar no Android Studio

1. Extraia o ZIP.
2. Abra o Android Studio.
3. Clique em **Open**.
4. Selecione a pasta `SinalizeMediaPipe`.
5. Espere o Gradle sincronizar.
6. Rode no celular físico, porque câmera/emulador pode falhar.

## Modelo MediaPipe necessário

Para ativar a análise real da mão, coloque o arquivo abaixo em:

```text
app/src/main/assets/hand_landmarker.task
```

O arquivo não está incluído no ZIP porque é um modelo externo do MediaPipe.

## Onde mexer nos gestos

Arquivo principal:

```text
app/src/main/java/com/example/sinalize/mediapipe/HandLandmarkerManager.kt
```

As regras atuais estão nestas funções:

```kotlin
analyzeNumberOne()
analyzeNumberTwo()
analyzeOpenHand()
analyzeClosedFist()
```

## Onde adicionar novas aulas

```text
app/src/main/java/com/example/sinalize/data/repository/LibrasRepository.kt
```

Cada aula tem um campo `expectedGesture`, usado pela tela de câmera.
