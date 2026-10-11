# StudyLens — offline Android study helper (no API key)

StudyLens reads textbook photos on the phone using bundled ML Kit text recognition. It then uses offline text rules to surface likely headings and key statements, and lets you search the scanned text by asking a question. No AI API key, server, account, or backend deployment is required. Photos stay on-device; the app does not upload them.

## What this version can and cannot do

- Select multiple textbook images.
- Read printed Latin-script text with on-device OCR. Results depend on image quality.
- Rank likely headings and key statements using simple offline rules.
- Answer questions by finding matching sentences in the extracted notes.
- Use the polished dark navy / blue / mint interface and subject selector.
- No cloud generative AI: it does not invent a fresh explanation or reliably translate content into Urdu/Roman Urdu. Urdu-script OCR is not included in this version.
- It cannot guarantee what will appear on an exam. Confirm priorities against your syllabus/teacher.

## Easiest way to build the APK (GitHub, no API key)

1. Download and extract this ZIP on your computer.
2. Open your GitHub repository: https://github.com/sdkplannars-art/StudyLens
3. Upload the contents of the `StudyLens` folder into the repository root (so `app/`, `build.gradle`, `settings.gradle`, and `.github/` appear at the top level). If GitHub says files already exist, replace them with this offline version.
4. Commit the changes to the `main` branch.
5. Open the repository's **Actions** tab.
6. Select **Build StudyLens Offline APK** on the left.
7. Click **Run workflow**, select `main`, then click the green **Run workflow** button. No secrets or API keys are needed.
8. Wait for the run to finish with a green check mark. Open that run and scroll to **Artifacts**.
9. Download **StudyLens-Offline-debug-APK** and extract the downloaded ZIP.
10. Copy `app-debug.apk` to your Android phone and tap it to install. If Android blocks it, allow your browser/file manager to install unknown apps for this one install, then install the APK.

## Troubleshooting

- If Actions fails, open the failed run and click the failed build step; copy the red error text for help.
- If OCR returns little text, retake the photo in good light, keep the page flat, and make the printed text sharp.
- The first build needs internet in GitHub Actions to download dependencies; after installation, page reading and matching work offline.

## Build locally (optional)

Install JDK 17, Android SDK platform/build tools 35, and Gradle 8.9, then run from this folder:

```bash
gradle assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`.
