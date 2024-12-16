# CMPT362-MyRuns
## Short Description
* This is a 3-month assignment to build a comprehensive app called `MyRuns` to track user location and speed when they are doing exercise, automatically detect what exercise are they training including Run, Walk, Climb, unknown; based on the **phone's accelerometer** (e.g how intense does user shake the phone?). All user's running history will be stored locally through `Room Database`. Additionally, there are some small adds-on feature from the app such as switching **distance preferences**(kilometer or miles), **take/upload photos**.
* The idea is designed and introduced by Professor of the class [Xingdong Yang](https://www.sfu.ca/~xingdong/)
## Short Demo
* **Location Tracking Service**:

https://github.com/user-attachments/assets/789b893f-29cd-4684-9e7a-a0155216276e
* **History Fragment**:
https://github.com/user-attachments/assets/3129c8ae-fd14-4f31-9e9b-3b65de905754
* **Settings Fragment**:
https://github.com/user-attachments/assets/5d4be2c1-fbc8-4617-a622-dc5c4c556828
## Usage Guidance:
* Download Android Studio, clone/download the repo, insert your own Google API key in `local properties file` from Google Console, and run the app!
* You can find the file on the root of the application:
```
📁 app/
├── 📁 manifests/
│   └── 📄 AndroidManifest.xml
├── 📁 kotlin+java/
│   └── 📁 com.example.tranquanngoc_cao_myruns2/
│       ├── 📁 automatic/
│       ├── 📁 database/
│       ├── 📁 googleMap/
│       ├── 📁 historyDetail/
│       ├── 📁 historyFragment/
│       ├── 📁 manualActivity/
│       ├── 📁 settingFragment/
│       ├── 📁 startFragment/
│       ├── 📁 userProfile/
│       └── 📁 util/
├── 📁 java (generated)/
├── 📁 res/
│   └── 📁 res (generated)/
└── 📁 Gradle Scripts/
    ├── 📄 build.gradle.kts
    ├── 📄 proguard-rules.pro
    ├── 📄 gradle.properties
    ├── 📄 local.properties <--- Insert your key here!
    └── 📄 settings.gradle.kts
```
Requirements:
- Minimum API Level: 33 (Android 13)
- Target API Level: 34 (Android 14)
- Tested on Physical Devices running Android 13 or higher
- Google Play Services required (for Maps functionality)

Permissions Required:
- Location (Fine and Coarse)
- Activity Recognition
- Foreground Service
- Post Notifications (Android 13+)


