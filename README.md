# Revolution - AI Voice Assistant for Android

**Version 1.0** | Android 8.0+ (API 26+) | Built with Jetpack Compose & Material3

Revolution is a comprehensive AI-powered voice assistant for Android that can control your phone through natural voice commands, manage your communications, detect urgent messages, and learn from your behavior over time.

## Download & Install

The pre-built APK is available at the root of this repository:
- **Debug APK**: `Revolution-v1.0-debug.apk` (ready to install, no signing required)

To install:
1. Transfer the APK to your Android device
2. Enable "Install from Unknown Sources" in Settings > Security
3. Open the APK file and tap Install
4. Grant the requested permissions when prompted

## Features

### Voice Assistant Core
- **Always-listening mode** with toggle - keeps the mic active in the background via a foreground service
- **Customizable wake word** (default: "Nikhil") - say the wake word followed by your command
- **Nameable AI** - give your assistant any name you want
- **Voice interaction** via speaker or notifications (user-selectable mode)
- **Multiple AI voices** - choose male or female voice with adjustable pitch and speed

### Intelligent Command Execution
The assistant understands natural language commands for:

| Command | Examples |
|---------|----------|
| **Phone calls** | "Call Mom", "Call John on SIM 2" |
| **SMS/Messages** | "Send message to Dad", "Text Sarah hello" |
| **App messaging** | "WhatsApp Mom", "Send message on Telegram to..." |
| **Web search** | "Search for weather", "Google latest news" |
| **Media playback** | "Play Bohemian Rhapsody on YouTube", "Play music on Spotify" |
| **Settings toggle** | "Turn on WiFi", "Enable Bluetooth", "Turn off hotspot" |
| **Alarms** | "Set alarm for 7 AM", "Wake me up at 6:30" |
| **Email** | "Send email to boss about project update" |
| **Meetings** | "Schedule meeting tomorrow", "Join Zoom meeting" |
| **Camera** | "Take a photo", "Record a video" |
| **Navigation** | "Navigate to Central Park", "Directions to airport" |
| **Location sharing** | "Share my location" |
| **IoT control** | "Turn on smart bulb", "Control smart TV" |
| **Task scheduling** | "Remind me to call dentist at 3 PM" |
| **UPI payments** | "Pay via UPI" (requires authentication) |

### Learning & Memory
- Learns from user interactions and tracks command frequency
- Auto-tags behaviors as "Work" or "Personal" based on context
- Time-of-day based suggestions
- Cloud sync option for learning data

### App Permissions & Control
- Lists all installed apps on the device
- Per-app access control for the AI assistant
- Blocked keyword management for sensitive content (OTP, banking, etc.)
- Dynamic permission denial for restricted apps

### Urgency & Escalation Engine
- **Emergency contact whitelist** - always alert for these contacts
- **Multi-signal urgency detection**:
  - Message tone and keyword analysis (25+ urgent keywords)
  - Sender frequency and repetition tracking
  - Cross-app contact attempt detection
  - Time-of-day context (late night messages get higher urgency)
- **Configurable urgency rules** per contact/app/keyword
- **Voice alerts** for critical messages (AI calls your name)
- Four urgency levels: LOW, MEDIUM, HIGH, CRITICAL

### UI/UX
- Beautiful Material3 design with **dark/light theme** toggle
- Animated AI orb visualization on home screen
- Five main screens: Home, Settings, Logs, Permissions, User Guide
- In-app user guide with detailed feature explanations
- Customizable app name (rename from "Revolution" to anything)
- Custom app icon support

### Logs & Summaries
- All AI actions are logged with timestamps
- Filterable log viewer with category chips
- Log export functionality
- Daily summary email support (configurable recipient)

### Device & Media Controls
- Bluetooth device connection management
- Hands-free device selection
- GPS/maps integration for navigation

### Offline Support
- Phone calls and SMS work without internet
- Settings toggles (WiFi, Bluetooth, etc.) work offline
- Alarm management works offline
- Device-level actions don't require internet

## Project Structure

```
app/src/main/java/com/revolution/ai/
├── RevolutionApp.kt                    # Application class
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt             # Room database
│   │   └── Daos.kt                    # Data Access Objects
│   ├── model/
│   │   └── Models.kt                  # Data models & entities
│   ├── preferences/
│   │   └── UserPreferences.kt         # DataStore preferences
│   └── repository/
│       └── Repository.kt              # Data repository
├── engine/
│   ├── command/
│   │   ├── CommandParser.kt           # NLP command parsing
│   │   └── CommandExecutor.kt         # Android intent execution
│   ├── learning/
│   │   └── LearningEngine.kt          # Behavior learning
│   └── urgency/
│       └── UrgencyEngine.kt           # Urgency detection
├── receiver/
│   ├── AlarmReceiver.kt               # Scheduled task handler
│   └── BootReceiver.kt                # Auto-start on boot
├── service/
│   ├── NotificationListenerService.kt # Notification monitoring
│   └── VoiceAssistantService.kt       # Core voice service
├── ui/
│   ├── MainActivity.kt                # Main activity with navigation
│   ├── MainViewModel.kt               # Shared ViewModel
│   ├── theme/
│   │   └── Theme.kt                   # Material3 theme
│   └── screens/
│       ├── home/HomeScreen.kt          # Main home screen
│       ├── settings/SettingsScreen.kt  # Settings panel
│       ├── logs/LogsScreen.kt          # Action log viewer
│       ├── permissions/PermissionsScreen.kt  # App permissions
│       └── guide/GuideScreen.kt        # User guide
└── util/
    └── PermissionHelper.kt            # Permission utilities
```

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material3
- **Architecture**: MVVM with Repository pattern
- **Database**: Room (SQLite)
- **Preferences**: DataStore
- **Speech**: Android SpeechRecognizer + TextToSpeech APIs
- **Background**: Foreground Service with microphone
- **Scheduling**: WorkManager + AlarmManager
- **Camera**: CameraX
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target SDK**: 34 (Android 14)

## Building from Source

### Prerequisites
- JDK 17+
- Android SDK with API 34 and Build Tools 34.0.0

### Build Commands
```bash
# Debug build
./gradlew assembleDebug

# Release build (unsigned)
./gradlew assembleRelease
```

The APK will be generated at:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Permissions

The app requests the following permissions:
- **Microphone** - Voice recognition
- **Phone** - Making calls
- **SMS** - Sending text messages
- **Contacts** - Contact lookup for calls/messages
- **Location** - Navigation and location sharing
- **Camera** - Photo/video capture
- **Bluetooth** - Device connectivity
- **Notifications** - Alert display and notification monitoring
- **Storage** - File access for sending files
- **Alarm** - Setting alarms and reminders

## License

This project is proprietary. All rights reserved.
