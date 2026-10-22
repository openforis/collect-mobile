# CLAUDE.md

Collect Mobile is the Android data-entry client for [Open Foris Collect](https://openforis.org/tools/collect/).
Users import a survey (designed in Collect), fill in records offline, then export/submit them back to Collect.

## Project layout

Gradle multi-project (`settings.gradle`): `android` and `model`.

- `model/` – plain Java library, no Android dependencies.
  - `SurveyService` / `collectadapter/CollectModelBackedSurveyService` – main façade used by the UI
    (load survey, select record/node, add/update/remove nodes, export).
  - `collectadapter/` – bridge to the Open Foris Collect core model (`CollectModelManager`, `UiModelBuilder`, converters).
  - `viewmodel/` – UI-side node tree: `UiSurvey` → `UiRecordCollection` → `UiRecord` → entities/attributes.
  - `viewmodelmanager/` – `ViewModelManager` (current survey/record/node selection) and the SQLite-backed node repository.
  - Tests: Spock/Groovy in `model/src/test/groovy`.
- `android/` – the app (`applicationId` / package `org.openforis.collect`).
  - `gui/ServiceLocator` – lazily wires services; `init()` returns false if no survey is selected.
  - `gui/MainActivity` – launcher activity (survey import/selection).
  - `gui/SurveyNodeActivity` – data entry; hosts `pager/NodePagerFragment` with one `detail/NodeDetailFragment` per node.
  - `gui/detail/` – per-node-type fragments; `gui/input/` – attribute input components.

Java source level 9, target 11. Build uses Gradle 9 / AGP 9 (`gradle/wrapper`).

## Build and test

Requires the Android SDK: set `ANDROID_HOME` or create `local.properties` with `sdk.dir=/path/to/sdk`.

```bash
./gradlew :android:assembleDebug     # debug APK → android/build/outputs/apk/debug/
./gradlew :model:test                # unit tests (model module)
./gradlew clean build                # everything
```

Release builds are made with `./release [versionName]` (needs the signing keystore configured in
`local.properties`); it bumps `currentVersionCode` in `gradle.properties` and commits it.
Add user-visible changes to `CHANGELOG.md`.

## Running in the emulator from the command line

```bash
# 1. Start an emulator
$ANDROID_HOME/emulator/emulator -list-avds
$ANDROID_HOME/emulator/emulator -avd <avd_name> &
adb wait-for-device shell 'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 1; done'

# (no AVD yet? use arm64-v8a instead of x86_64 on Apple Silicon)
sdkmanager "system-images;android-35;google_apis;x86_64"
avdmanager create avd -n collect -k "system-images;android-35;google_apis;x86_64"

# 2. Build and install the debug APK
./gradlew :android:installDebug

# 3. Launch
adb shell am start -n org.openforis.collect/.android.gui.MainActivity
# or: adb shell monkey -p org.openforis.collect 1
```

- Logs: `adb logcat --pid=$(adb shell pidof org.openforis.collect)`; crashes only: `adb logcat AndroidRuntime:E *:S`.
- `INSTALL_FAILED_UPDATE_INCOMPATIBLE`: the Play Store build (different signature) is installed;
  `adb uninstall org.openforis.collect` first (this deletes its data).

### Testing activity recreation / process death

Many crash reports come from state restoration. To reproduce:

```bash
adb shell settings put global always_finish_activities 1   # "Don't keep activities" on (0 = off)
adb shell am kill org.openforis.collect                    # kill the backgrounded process
```

Then reopen the app from the recent apps list.

## Gotchas

- Node ids come from `IdGenerator`, seeded with `MAX(id)` of the persisted nodes when the repository is created.
  `UiRecordCollection` ids are generated on every survey load and never persisted, so they change across
  process restarts: never save them in bundles/arguments; use the record collection name instead
  (see `NodeDetailFragment.create`).
- Fragments are restored by `super.onCreate()` before `SurveyNodeActivity` selects its node, so
  `NodeDetailFragment.onCreate` must be able to resolve its node from its own arguments.
