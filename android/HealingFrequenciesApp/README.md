# Curative App for Android

This project packages `https://infiniti-ia.com/english` in a native Android WebView for Google Play.

## Project details

- App name: **Curative App**
- Package: `com.apolosolinvictus.healingfrequencies`
- Privacy policy: `https://infiniti-ia.com/privacy`
- Minimum SDK: 26
- Compile and target SDK: 36
- Java: 17
- Billing: Google Play Billing 9.1.0
- Product ID: `healing_frequencies_full_access`

The product is a non-consumable one-time purchase. Configure it in Play Console at US$7.77, with no free trial, subscription, automatic renewal, or later charge. The app keeps the main WebView locked until Google Play confirms the purchase and supports restoring the purchase on another device using the same Google Play account.

## Open in Android Studio

1. Open Android Studio and choose **Open**.
2. Select this folder: `android/HealingFrequenciesApp`.
3. Use JDK 17 and install Android SDK API 36 if Android Studio asks for it.
4. Let Gradle sync, then run the `app` configuration on an emulator or physical Android device.

The web experience needs internet access. The app does not request microphone, camera, location, contacts, or storage permissions.

## Configure Play Console

1. Create the app with package `com.apolosolinvictus.healingfrequencies`.
2. Create a one-time in-app product with ID `healing_frequencies_full_access` and configure the price.
3. Upload an internal-testing Android App Bundle before testing the real billing flow.
4. Add license testers and verify purchase, pending purchase, restore, refund, and offline/error states.
5. Register `https://infiniti-ia.com/privacy` as the privacy policy and complete the Data safety form accurately.

## Build the release bundle

Use **Build > Generate Signed Bundle / APK > Android App Bundle** in Android Studio, select the `release` variant, and sign it with a private upload keystore. Keep the keystore and passwords outside Git. Increase `versionCode` in `app/build.gradle` for every update.

The signed bundle generated locally is `app/build/outputs/bundle/release/app-release.aab`. Upload that file to the Google Play internal-testing track after creating the app and the one-time product in Play Console. Back up the upload keystore and its password separately; losing them can prevent future updates.

The project is ready to open in Android Studio. Final signing, Play App Signing, product configuration, and publication are completed in your Google Play Console account.
