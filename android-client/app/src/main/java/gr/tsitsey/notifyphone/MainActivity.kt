package gr.tsitsey.notifyphone

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var mSocket: Socket
    private val SERVER_URL = "http://192.168.2.212:5000"
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the content view to your layout
        setContentView(R.layout.activity_main)

        // Reference the TextView from the layout
        statusText = findViewById(R.id.statusText)

        try {
            // Configure options if needed
            val opts = IO.Options()
            opts.forceNew = true
            opts.reconnection = true

            mSocket = IO.socket(SERVER_URL, opts)
            mSocket.on(Socket.EVENT_CONNECT, onConnect)
            mSocket.on("new_notification", onNewNotification)
            mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect)
            mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError)
            mSocket.connect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val onConnect = Emitter.Listener {
        runOnUiThread {
            statusText.text = "Connected to the server"
        }
    }

    private val onNewNotification = Emitter.Listener { args ->
        runOnUiThread {
            val data = args[0] as JSONObject
            val message = data.getString("message")
            statusText.text = "Received message: $message"
            showNotification(message)
        }
    }

    private val onDisconnect = Emitter.Listener {
        runOnUiThread {
            statusText.text = "Disconnected from the server"
        }
    }

    private val onConnectError = Emitter.Listener { args ->
        runOnUiThread {
            statusText.text = "Connection error"
        }
    }

    private fun showNotification(message: String) {
        val channelId = "notifications"
        val channelName = "Notifications"
        val notificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create Notification Channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
                .setContentTitle("New Message")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("New Message")
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build()
        }

        notificationManager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket.disconnect()
        mSocket.off(Socket.EVENT_CONNECT, onConnect)
        mSocket.off("new_notification", onNewNotification)
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect)
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError)
    }
}