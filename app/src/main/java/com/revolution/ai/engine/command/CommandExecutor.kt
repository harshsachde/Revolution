package com.revolution.ai.engine.command

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import com.revolution.ai.data.model.CommandResult

class CommandExecutor(private val context: Context) {

    fun execute(command: ParsedCommand): CommandResult {
        return try {
            when (command.type) {
                CommandType.CALL -> makeCall(command)
                CommandType.SMS -> sendSms(command)
                CommandType.MESSAGE_APP -> sendViaApp(command)
                CommandType.OPEN_APP -> openApp(command)
                CommandType.WEB_SEARCH -> webSearch(command)
                CommandType.PLAY_MEDIA -> playMedia(command)
                CommandType.TOGGLE_WIFI -> toggleWifi(command)
                CommandType.TOGGLE_BLUETOOTH -> toggleBluetooth(command)
                CommandType.TOGGLE_HOTSPOT -> toggleHotspot(command)
                CommandType.TOGGLE_GPS -> openGpsSettings(command)
                CommandType.SET_ALARM -> setAlarm(command)
                CommandType.SEND_EMAIL -> sendEmail(command)
                CommandType.SCHEDULE_MEETING -> scheduleMeeting(command)
                CommandType.TAKE_PHOTO -> takePhoto(command)
                CommandType.RECORD_VIDEO -> recordVideo(command)
                CommandType.NAVIGATE -> navigate(command)
                CommandType.SHARE_LOCATION -> shareLocation(command)
                CommandType.IOT_CONTROL -> iotControl(command)
                CommandType.UPI_PAYMENT -> upiPayment(command)
                CommandType.READ_NEWS -> readNews(command)
                CommandType.SEND_FILE -> sendFile(command)
                CommandType.SCHEDULE_TASK -> CommandResult(
                    success = true,
                    message = "Task scheduled: ${command.content}",
                    action = "schedule"
                )
                CommandType.UNKNOWN -> CommandResult(
                    success = false,
                    message = "I didn't understand that command",
                    action = "unknown"
                )
            }
        } catch (e: Exception) {
            CommandResult(
                success = false,
                message = "Error executing command: ${e.localizedMessage}",
                action = command.type.name.lowercase()
            )
        }
    }

    private fun makeCall(command: ParsedCommand): CommandResult {
        val target = command.target.ifBlank {
            return CommandResult(false, "No contact specified for the call", "call")
        }

        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:${Uri.encode(target)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val simSlot = command.extras["sim"]
        if (simSlot != null) {
            intent.putExtra("com.android.phone.extra.slot", simSlot.toIntOrNull()?.minus(1) ?: 0)
        }

        return CommandResult(
            success = true,
            message = "Calling $target",
            action = "call",
            requiresConfirmation = true,
            pendingAction = { context.startActivity(intent) }
        )
    }

    private fun sendSms(command: ParsedCommand): CommandResult {
        val target = command.target.ifBlank {
            return CommandResult(false, "No contact specified for SMS", "sms")
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${Uri.encode(target)}")
            putExtra("sms_body", command.content)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
        val msg = if (command.content.isNotBlank()) {
            "Opening SMS to $target with message"
        } else {
            "Opening SMS to $target"
        }
        return CommandResult(true, msg, "sms")
    }

    private fun sendViaApp(command: ParsedCommand): CommandResult {
        val target = command.target.ifBlank {
            return CommandResult(false, "No contact specified", "message_app")
        }

        val appPackage = command.appPackage
        if (appPackage.isBlank()) {
            return CommandResult(false, "Could not determine messaging app", "message_app")
        }

        val intent = when (appPackage) {
            "com.whatsapp" -> {
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://wa.me/?text=${Uri.encode(command.content)}")
                    setPackage(appPackage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            "org.telegram.messenger" -> {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, command.content)
                    setPackage(appPackage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            else -> {
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, command.content)
                    setPackage(appPackage)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        }

        return try {
            context.startActivity(intent)
            CommandResult(true, "Opening message to $target", "message_app")
        } catch (e: Exception) {
            CommandResult(false, "App not installed: $appPackage", "message_app")
        }
    }

    private fun openApp(command: ParsedCommand): CommandResult {
        val packageName = command.appPackage.ifBlank {
            return CommandResult(false, "Could not find app: ${command.target}", "open_app")
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            return CommandResult(true, "Opening ${command.target}", "open_app")
        }

        val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(fallbackIntent)
            CommandResult(false, "${command.target} is not installed. Opening Play Store.", "open_app")
        } catch (e: Exception) {
            CommandResult(false, "Could not open ${command.target}", "open_app")
        }
    }

    private fun webSearch(command: ParsedCommand): CommandResult {
        val query = command.content.ifBlank {
            return CommandResult(false, "No search query provided", "web_search")
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
        return CommandResult(true, "Searching for: $query", "web_search")
    }

    private fun playMedia(command: ParsedCommand): CommandResult {
        val query = command.content.ifBlank {
            return CommandResult(false, "No media specified to play", "play_media")
        }

        val appPackage = command.appPackage

        val intent = if (appPackage == "com.google.android.youtube") {
            Intent(Intent.ACTION_SEARCH).apply {
                setPackage(appPackage)
                putExtra("query", query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        } else if (appPackage.isNotBlank()) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(appPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                launchIntent
            } else {
                createYouTubeSearchIntent(query)
            }
        } else {
            createYouTubeSearchIntent(query)
        }

        return try {
            context.startActivity(intent)
            CommandResult(true, "Playing: $query", "play_media")
        } catch (e: Exception) {
            context.startActivity(createYouTubeSearchIntent(query))
            CommandResult(true, "Playing on YouTube: $query", "play_media")
        }
    }

    private fun createYouTubeSearchIntent(query: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    @Suppress("DEPRECATION")
    private fun toggleWifi(command: ParsedCommand): CommandResult {
        val state = command.extras["state"] ?: "toggle"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val intent = Intent(Settings.Panel.ACTION_WIFI).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return CommandResult(true, "Opening WiFi settings panel", "toggle_wifi")
        }

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            ?: return CommandResult(false, "WiFi not available", "toggle_wifi")

        val enable = when (state) {
            "on" -> true
            "off" -> false
            else -> !wifiManager.isWifiEnabled
        }

        wifiManager.isWifiEnabled = enable
        val stateStr = if (enable) "on" else "off"
        return CommandResult(true, "WiFi turned $stateStr", "toggle_wifi")
    }

    @Suppress("DEPRECATION")
    private fun toggleBluetooth(command: ParsedCommand): CommandResult {
        val state = command.extras["state"] ?: "toggle"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return CommandResult(true, "Opening Bluetooth settings", "toggle_bluetooth")
        }

        val adapter = BluetoothAdapter.getDefaultAdapter()
            ?: return CommandResult(false, "Bluetooth not available", "toggle_bluetooth")

        val enable = when (state) {
            "on" -> true
            "off" -> false
            else -> !adapter.isEnabled
        }

        if (enable) adapter.enable() else adapter.disable()
        val stateStr = if (enable) "on" else "off"
        return CommandResult(true, "Bluetooth turned $stateStr", "toggle_bluetooth")
    }

    private fun toggleHotspot(command: ParsedCommand): CommandResult {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            setClassName("com.android.settings", "com.android.settings.TetherSettings")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            CommandResult(true, "Opening hotspot settings", "toggle_hotspot")
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
            CommandResult(true, "Opening wireless settings", "toggle_hotspot")
        }
    }

    private fun openGpsSettings(command: ParsedCommand): CommandResult {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return CommandResult(true, "Opening location settings", "toggle_gps")
    }

    private fun setAlarm(command: ParsedCommand): CommandResult {
        val hour = command.extras["hour"]?.toIntOrNull()
            ?: return CommandResult(false, "Could not parse alarm time", "set_alarm")
        val minute = command.extras["minute"]?.toIntOrNull() ?: 0

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)

        val timeStr = String.format("%d:%02d", if (hour == 0) 12 else if (hour > 12) hour - 12 else hour, minute)
        val amPm = if (hour < 12) "AM" else "PM"
        return CommandResult(true, "Setting alarm for $timeStr $amPm", "set_alarm")
    }

    private fun sendEmail(command: ParsedCommand): CommandResult {
        val subject = command.extras["subject"] ?: ""

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${Uri.encode(command.target)}")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            if (command.content.isNotBlank()) {
                putExtra(Intent.EXTRA_TEXT, command.content)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            CommandResult(true, "Composing email to ${command.target}", "send_email")
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(command.target))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                if (command.content.isNotBlank()) {
                    putExtra(Intent.EXTRA_TEXT, command.content)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(fallback, "Send email").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            CommandResult(true, "Opening email chooser for ${command.target}", "send_email")
        }
    }

    private fun scheduleMeeting(command: ParsedCommand): CommandResult {
        val action = command.extras["action"]

        if (action == "join" && command.appPackage.isNotBlank()) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(command.appPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return CommandResult(true, "Opening meeting app", "schedule_meeting")
            }
            return CommandResult(false, "Meeting app not installed", "schedule_meeting")
        }

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = android.provider.CalendarContract.Events.CONTENT_URI
            if (command.target.isNotBlank()) {
                putExtra(android.provider.CalendarContract.Events.TITLE, "Meeting with ${command.target}")
            } else {
                putExtra(android.provider.CalendarContract.Events.TITLE, "Meeting")
            }
            val time = command.extras["time"]
            if (!time.isNullOrBlank()) {
                putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "Scheduled for: $time")
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(intent)
        return CommandResult(true, "Opening calendar to schedule meeting", "schedule_meeting")
    }

    private fun takePhoto(command: ParsedCommand): CommandResult {
        val facing = command.extras["facing"]

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            if (facing == "front") {
                putExtra("android.intent.extras.CAMERA_FACING", 1)
                putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
                putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            CommandResult(true, "Opening camera", "take_photo")
        } catch (e: Exception) {
            CommandResult(false, "Could not open camera", "take_photo")
        }
    }

    private fun recordVideo(command: ParsedCommand): CommandResult {
        val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            CommandResult(true, "Opening video camera", "record_video")
        } catch (e: Exception) {
            CommandResult(false, "Could not open video camera", "record_video")
        }
    }

    private fun navigate(command: ParsedCommand): CommandResult {
        val destination = command.target.ifBlank {
            return CommandResult(false, "No destination specified", "navigate")
        }

        val gmmIntentUri = Uri.parse("google.navigation:q=${Uri.encode(destination)}")
        val intent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            CommandResult(true, "Navigating to $destination", "navigate")
        } catch (e: Exception) {
            val fallback = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=${Uri.encode(destination)}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
            CommandResult(true, "Opening map to $destination", "navigate")
        }
    }

    private fun shareLocation(command: ParsedCommand): CommandResult {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Sharing my current location")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(Intent.createChooser(intent, "Share location via").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
        return CommandResult(true, "Opening location sharing", "share_location")
    }

    private fun iotControl(command: ParsedCommand): CommandResult {
        val device = command.target.ifBlank { "device" }
        val state = command.extras["state"]

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://home.google.com")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val googleHomePackage = "com.google.android.apps.chromecast.app"
        val launchIntent = context.packageManager.getLaunchIntentForPackage(googleHomePackage)

        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            val action = if (state != null) "Turning $state $device" else "Controlling $device"
            CommandResult(true, "$action via Google Home", "iot_control")
        } else {
            context.startActivity(intent)
            CommandResult(true, "Opening smart home controls for $device", "iot_control")
        }
    }

    private fun upiPayment(command: ParsedCommand): CommandResult {
        val target = command.target.ifBlank { "recipient" }
        val amount = command.extras["amount"] ?: ""
        val appPackage = command.appPackage

        if (appPackage.isNotBlank()) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(appPackage)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                val message = buildString {
                    append("Opening payment app to pay $target")
                    if (amount.isNotBlank()) append(" ₹$amount")
                }

                return CommandResult(
                    success = true,
                    message = message,
                    action = "upi_payment",
                    requiresConfirmation = true,
                    pendingAction = { context.startActivity(launchIntent) }
                )
            }
        }

        if (amount.isNotBlank()) {
            val upiUri = Uri.parse("upi://pay?pn=${Uri.encode(target)}&am=$amount&cu=INR")
            val intent = Intent(Intent.ACTION_VIEW, upiUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            return CommandResult(
                success = true,
                message = "Opening UPI payment of ₹$amount to $target",
                action = "upi_payment",
                requiresConfirmation = true,
                pendingAction = { context.startActivity(intent) }
            )
        }

        return CommandResult(false, "Could not process UPI payment", "upi_payment")
    }

    private fun readNews(command: ParsedCommand): CommandResult {
        val topic = command.content

        val query = if (topic.isNotBlank()) "news $topic" else "news headlines today"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://news.google.com/search?q=${Uri.encode(query)}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val gnewsPackage = "com.google.android.apps.magazines"
        val launchIntent = context.packageManager.getLaunchIntentForPackage(gnewsPackage)

        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            CommandResult(true, "Opening Google News", "read_news")
        } else {
            context.startActivity(intent)
            val msg = if (topic.isNotBlank()) "Showing news about $topic" else "Showing latest news"
            CommandResult(true, msg, "read_news")
        }
    }

    private fun sendFile(command: ParsedCommand): CommandResult {
        val target = command.target

        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(Intent.createChooser(intent, "Select file to send").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })

        val msg = if (target.isNotBlank()) "Select a file to send to $target" else "Select a file to send"
        return CommandResult(true, msg, "send_file")
    }
}
