# RASTA Universal Audio IR Remote v9

Android app for controlling:
- Onkyo CR-185 / RC-332S
- Sony CMT-E300HD / RM-E02D
- Victor/JVC NX-TC5-B / RM-SNXTC5-S
- Pioneer P700 / CU-AP015

## Important rule
No unverified device-specific IR command is presented as verified.
Only known/official mappings are marked VERIFIED.
Generic protocol codes remain CANDIDATE or UNKNOWN.

## Hardware
- Android IR blaster required: ConsumerIrManager
- Works on any Android device with IR transmitter, including Xiaomi devices like Redmi/Note series

## Build
- Android Studio
- compileSdk 35
- minSdk 23
- targetSdk 35
- Java 17

## Directory structure
- app/src/main/java/com/ehsan/onkyo185/MainActivity.java
- app/src/main/assets/index.html
- app/src/main/res/values/strings.xml
- app/src/main/res/values/themes.xml
- app/src/main/AndroidManifest.xml

## Notes
- This project uses a WebView-based UI and Android native IR transmission.
- It does not depend on the internet or AI APIs.
- Candidate discovery is done through the Code Lab.
