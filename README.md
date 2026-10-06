# RASTA Universal Audio IR Remote v9

یک اپلیکیشن اندروید برای کنترل تجهیزات صوتی قدیمی از طریق **Infrared (IR)** با فرستنده‌های موجود در گوشی‌های Xiaomi و دستگاه‌های دارای IR Blaster.

## 📱 دستگاه‌های پشتیبانی‌شده

1. **Onkyo CR-185 / CR-185X** — Remote: RC-332S ✓ VERIFIED
2. **Sony CMT-E300HD** — Remote: RM-E02D ⚠ CANDIDATE (generic SIRC)
3. **Victor/JVC NX-TC5-B** — Remote: RM-SNXTC5-S ⚠ UNKNOWN (discovery required)
4. **Pioneer Private SELFIE P700 Mini** — Remote: CU-AP015 ⚠ UNKNOWN (discovery required)

## 🎯 ویژگی‌های اصلی

### ✓ Onkyo CR-185 (VERIFIED)
- کدهای تایید شده از documentation رسمی
- 15 دکمه مرکزی + کنترل صدا
- Protocol: NEC 38kHz

### ⚠ Sony CMT-E300HD (CANDIDATE)
- کدهای عمومی SIRC (Address 16, 40kHz)
- **نیاز به تست فیزیکی برای تأیید**
- Code Lab برای Discovery

### ⚠ Victor/JVC NX-TC5-B (UNKNOWN)
- Protocol JVC 37.9kHz معروف است
- **کدهای مدل‌محور NX-TC5 یافت نشدند**
- Code Lab برای تست و ثبت

### ⚠ Pioneer P700 (UNKNOWN)
- **CU-AP015 command table پیدا نشد**
- تمام کنترل‌ها از Code Lab شروع می‌شوند
- پس از تست و تأیید database ذخیره می‌شود

## 🔧 معماری

```
Android Native (Java)
        ↓
ConsumerIrManager API
        ↓
WireFormat Encoders (NEC, SIRC, JVC)
        ↓
IR Transmitter (Xiaomi/Samsung)
        ↓
IR LED → Device
        ↑
WebView/HTML UI ← JavaScript Bridge
        ↑
SharedPreferences JSON Database
```

## 📊 Database Schema

```json
{
  "id_1717...": {
    "id": "id_1717...",
    "time": "2024-06-01T10:30:00Z",
    "name": "POWER",
    "device": "SONY",
    "proto": "SONY",
    "address": 16,
    "bits": 12,
    "command": 21,
    "carrier": 40000,
    "status": "CANDIDATE|VERIFIED|FAILED|UNKNOWN",
    "source": "generic_sirc|user_tested|factory|unknown"
  }
}
```

## ⚠️ سیاست Verification

### VERIFIED فقط با یکی از:
1. **منبع معتبر** (official manual + source recorded)
2. **تست فیزیکی توسط کاربر** (گزینه VERIFIED در Code Lab)

### CANDIDATE:
- کدهای عمومی (generic protocol)
- کدهای آزمایشی
- **نباید به‌عنوان working code نمایش داده شود**

### UNKNOWN:
- Command دقیق پیدا نشده
- منتظر Code Lab discovery

### هیچ کد ساختگی وارد database نمی‌شود

## 🛠️ Code Lab — Discovery Mode

```
1. Protocol انتخاب کنید
2. Carrier frequency تنظیم کنید
3. Address / Custom / Data وارد کنید
4. TEST SEND را بزنید
5. بررسی کنید:
   ✓ Works → VERIFIED
   ✗ Failed → FAILED
   ❓ Unknown → CANDIDATE
6. Save
```

**اهمیت:** Discovery **کنترل‌شده** است، نه Rapid-Fire یا Scan بی‌نهایت.

## 🏗️ ساختار پروژه

```
Sony-E300HD-Remote-Control/
├── app/
│   ├── src/main/
│   │   ├── java/com/ehsan/onkyo185/
│   │   │   └── MainActivity.java (470 lines)
│   │   ├── assets/
│   │   │   └── index.html (HTML/CSS/JS UI)
│   │   ├── res/
│   │   │   └── values/
│   │   │       ├── strings.xml
│   │   │       └── themes.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle (root)
├── settings.gradle
├── gradle.properties
├── proguard-rules.pro
├── gradle/wrapper/gradle-wrapper.properties
└── README.md
```

## 🔴 نیازمندی‌های سخت‌افزار

- Android 6.0+ (API 23)
- **IR Emitter** (Xiaomi، Samsung، Nokia)
  - تست: `adb shell service call consumerir 1` (zero = no IR)

## 🔵 نیازمندی‌های نرم‌افزاری

```gradle
compileSdk 35
minSdk 23
targetSdk 35
java 17

Dependencies:
- androidx.appcompat:appcompat:1.6.1
- androidx.webkit:webkit:1.7.0
```

## ✅ Build & Install

```bash
# Sync
./gradlew sync

# Build APK
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Install
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## 🚀 استفاده

### تب Onkyo
```
کلیک مستقیم بر روی دکمه‌های معروف
Known codes: POWER, VOL±, MUTE, CD, TUNER, PLAY, STOP, etc.
```

### تب Sony
```
⚠ همه Candidates هستند
تست کنید و VERIFIED کنید
```

### تب JVC
```
❓ Unknown codes
تمام کنترل‌ها از Code Lab شروع می‌شوند
```

### تب Pioneer
```
❓ Unknown codes
Code Lab برای discovery
```

### Code Lab
```
Manual test for any device/button
Protocol: SONY, JVC, NEC
Carrier: 38-40 kHz (قابل تغییر)
Address/Custom/Data/Command/Bits/Repeat (قابل تغییر)
```

## 📤 Export / Import

```
1. Export: JSON file via Share
2. Copy: To clipboard
3. Import: Select JSON file
```

**Validation:**
- JSON schema check
- Protocol validation
- Carrier range check
- Duplicate handling

## 🔐 Safety & Accuracy

- ✓ No fabricated codes
- ✓ No fake verification
- ✓ No network dependency
- ✓ No AI mockup
- ✓ Full error handling
- ✓ RTL support (Persian)
- ✓ Persistent database (SharedPreferences)

## 🐛 Troubleshooting

| مشکل | راه‌حل |
|------|-------|
| "No IR Emitter" | دستگاه IR ندارد. Xiaomi/Samsung معمولاً دارند. |
| کدها کار نمی‌کنند | Code Lab → تست → mark VERIFIED |
| Database خراب شد | Export backup → Clear → Import |
| APK install نشود | `adb install -r app/build/outputs/apk/debug/app-debug.apk` |

## 📋 Protocols

### NEC (Onkyo)
- Carrier: 38 kHz
- Format: 32-bit (Address + NotAddress + Command + NotCommand)
- Timing: 9ms lead-in, 4.5ms space

### SIRC (Sony)
- Carrier: 40 kHz
- Format: 12/15/20-bit
- Timing: 2.4ms lead-in, 600µs bit

### JVC
- Carrier: 37.9 kHz
- Format: 16-bit (Custom 8-bit + Data 8-bit)
- Timing: 8.44ms lead-in, 4.22ms space

## 📝 License

MIT License - Feel free to use and modify.

---

**Version:** 9.0  
**Last Updated:** October 2026  
**Platform:** Android 6.0+ (API 23+)  
**Language:** Java 17 + HTML5/JS + Persian (RTL)

**Important Note:**
> No unverified model-specific IR command is presented as VERIFIED in this application.
> Sony RM-E02D, Victor/JVC RM-SNXTC5-S, and Pioneer CU-AP015 commands require physical testing.
> Code Lab discovery is the primary method for unknown devices.
