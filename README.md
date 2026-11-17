# BurgerBox

Application Android native consacrée à BurgerBox. Le projet est écrit en Java et construit avec Gradle ; son identifiant Android est `com.clem.burgerbox`.

## Prérequis

- Android Studio avec le SDK Android requis par le projet (compile SDK 36).
- JDK 11 ou plus récent.

## Ouvrir et lancer l'application

1. Clonez le dépôt et ouvrez-le dans Android Studio.
2. Laissez Gradle synchroniser les dépendances.
3. Lancez la configuration `app` sur un émulateur ou un appareil Android (minimum Android 7.0 / API 24).

En ligne de commande, vous pouvez également construire l'application avec le wrapper Gradle :

```bash
./gradlew assembleDebug
```

Sous Windows :

```bat
gradlew.bat assembleDebug
```

L'APK de débogage est généré dans `app/build/outputs/apk/debug/`.

## Structure

- `app/` : code source et ressources Android.
- `gradle/`, `build.gradle`, `settings.gradle` : configuration de construction Gradle.
