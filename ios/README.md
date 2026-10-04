# Healing Frequencies App

This folder contains an iPhone app project named **Healing Frequencies App**. It uses SwiftUI for the native shell and `WKWebView` to present the live English synthesizer at:

`https://infiniti-ia.com/english`

The web experience remains the source of truth for the synthesizer UI and audio controls. The native shell adds navigation, loading and offline-error states, sharing, an in-app privacy-policy view, and a sound-safety panel. The public privacy URL is `https://infiniti-ia.com/privacy`.

## Open the project

1. Use a Mac with a current Apple-supported version of Xcode. iOS apps cannot be built or signed for the App Store from Windows alone.
2. Clone or download this repository on the Mac.
3. Open `ios/HealingFrequenciesApp/HealingFrequenciesApp.xcodeproj` in Xcode.
4. Select the `HealingFrequenciesApp` target, open **Signing & Capabilities**, select your Apple Developer Team, and replace the sample bundle identifier with one that is unique to your account, for example `com.yourcompany.healingfrequencies`.
5. Select an iPhone Simulator or a connected iPhone and run the app. Confirm that the page loads, the audio starts after tapping **Ignite Oscillators**, the keyboard/pad interactions work, and the app behaves correctly in portrait and landscape.

## App Store checklist

1. Enroll in the Apple Developer Program and accept the latest agreements.
2. Create an App ID in Certificates, Identifiers & Profiles using the exact bundle identifier from Xcode.
3. In App Store Connect, create a new iOS app named **Healing Frequencies App** and use the same bundle ID.
4. Complete the app metadata: subtitle, description, keywords, category, age rating, support URL, marketing URL if applicable, screenshots, app icon, and a real privacy policy URL.
5. Complete App Privacy accurately. The native shell does not add analytics or user accounts, but the remote web page loads third-party web resources. Review the live page and disclose any data collection, cookies, diagnostics, or third-party processing that actually occurs.
6. Test on a physical iPhone and through TestFlight. Test first launch, no-network behavior, audio permissions and volume, back/forward controls, the share action, and the Sound Safety panel.
7. In Xcode choose **Product > Archive**. In Organizer choose **Distribute App > App Store Connect > Upload**. Wait for Apple to process the build.
8. Select the processed build in the App Store Connect version record, answer export-compliance questions, complete the review information, and submit it to App Review.

## App Review risk

Apple Guideline 4.2 says an app should provide utility, content, and UI beyond a repackaged website. A remote `WKWebView` can still be considered a web wrapper, so approval cannot be guaranteed. The current project adds native controls and safety information, but the strongest submission would add more app-specific value, such as a bundled/offline version of the audio engine, native presets, local favorites, haptics, or other features that remain useful when the website is unavailable.

In App Review notes, explain that the app is an interactive sound synthesizer, provide the public URL, state that no login is required, and describe the native shell features. Do not describe the frequencies as medical treatment or claim that they reliably cause a psychological state.

## Versioning

- Marketing version: `1.0`
- Build: `1`
- Bundle identifier in the template: `com.apolosolinvictus.healingfrequencies`

Before each upload, increment the build number. Keep signing certificates, provisioning profiles, App Store Connect API keys, and private credentials outside this repository.
