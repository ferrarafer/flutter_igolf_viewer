package com.filledstacks.plugins.flutter_igolf_viewer.channels

import android.os.Handler
import android.os.Looper
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.PluginRegistry.Registrar
import kotlin.concurrent.timerTask
import java.util.*

class CourseViewerEventChannel(binaryMessenger: BinaryMessenger) : StreamHandler {
    private val channel: EventChannel = EventChannel(
        binaryMessenger,
        "plugins.filledstacks.flutter_igolf_viewer/course_viewer_event"
    )
    private var eventSink: EventChannel.EventSink? = null

    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null

    init {
        channel.setStreamHandler(this)
        //startSendingMessages()
    }

    fun cleanup() {
        channel.setStreamHandler(null)
    }

    fun sendEvent(event: Any?) {
        handler.post {
            eventSink?.success(event)
        }
    }

    private fun startSendingMessages() {
        timer = Timer()
        timer?.scheduleAtFixedRate(timerTask {
            val randomString = UUID.randomUUID().toString()
            handler.post {
                sendEvent(randomString)
            }
        }, 0, 5000) // Send a random string every 5 seconds
    }

    override fun onListen(arguments: Any?, eventSink: EventChannel.EventSink?) {
        this.eventSink = eventSink
    }

    override fun onCancel(arguments: Any?) {
        eventSink = null
    }
}
