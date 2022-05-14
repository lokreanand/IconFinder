package com.anand.iconfinder.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.anand.iconfinder.R
import com.anand.iconfinder.model.Download
import okhttp3.ResponseBody
import retrofit2.http.Streaming
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.pow
import kotlin.math.roundToInt

class DownloadService : Service() {

    private var mServiceLooper: Looper? = null
    private var mServiceHandler: ServiceHandler? = null

    private var totalFileSize: Int = 0
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    private lateinit var url: String

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            try {
                notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                notificationBuilder = NotificationCompat.Builder(applicationContext, "download")
                    .setContentTitle("Downloading")
                    .setContentText("Just a moment...")
                    .setSmallIcon(R.drawable.ic_logo)
                    .setOngoing(true)
                    .setAutoCancel(true)

                if (Build.VERSION.SDK_INT >= 26) {
                    val notificationChannel = NotificationChannel(
                        "download",
                        "Download Service",
                        NotificationManager.IMPORTANCE_LOW
                    )
                    notificationChannel.enableLights(false)
                    notificationChannel.enableVibration(false)
                    notificationManager.createNotificationChannel(notificationChannel)
                    notificationBuilder.setChannelId("download")

                    if (Build.VERSION.SDK_INT <= 21)
                        notificationBuilder.setSmallIcon(R.drawable.ic_logo)
                }

                val id = System.currentTimeMillis().toInt()

                notificationManager.notify(
                    id, notificationBuilder.build()
                )
                initDownload("$id.png", url, id)

            } catch (ex: Exception) {
                Log.d("Service", ex.message ?: "")
            }

        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        try {
            val thread = HandlerThread(
                "ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND
            )
            thread.start()

            mServiceLooper = thread.looper
            mServiceHandler = ServiceHandler(mServiceLooper!!)

            val msg = mServiceHandler!!.obtainMessage()

            val url = intent.getStringExtra("url")
            this.url = url ?: ""
            Log.d("Service", url ?: "")

            msg.arg1 = startId
            mServiceHandler!!.sendMessage(msg)

        } catch (e: java.lang.Exception) {
            Log.d("Service", e.message ?: "Exception occured")
        }

        return START_STICKY
    }

    private fun starting() {
        toast("Download Started!")
    }

    @Streaming
    private fun initDownload(filename: String, url: String, id: Int) {

        val handler = Handler(Looper.getMainLooper())
        handler.post { starting() }

        val request = retrofitClient.downloadFile(url)

        try {
            downloadFile(request.execute().body()!!, filename, id)
        } catch (e: Exception) {
            Toast.makeText(applicationContext, "Error: " + e.message, Toast.LENGTH_SHORT).show()
            val error = "There's some issue with Internet Connection. Please try again"
            notificationBuilder.setContentTitle("Download Failed!")
                .setProgress(0, 0, false)
                .setContentText(error)
                .setStyle(NotificationCompat.BigTextStyle().bigText(error))
                .setOngoing(false)

            notificationManager.notify(id, notificationBuilder.build())
        }
    }

    @Streaming
    @Throws(IOException::class)
    private fun downloadFile(body: ResponseBody, filename: String, id: Int) {
        var count: Int
        val data = ByteArray(1024 * 4)
        val fileSize = body.contentLength()
        val bis = BufferedInputStream(body.byteStream(), 1024 * 8)
        val mediaStorageDir = File(
            Environment.getExternalStorageDirectory(), "/"
        )

        try {
            mediaStorageDir.mkdirs()
        } catch (e: Exception) {
        }

        val outputFile = File(
            Environment.getExternalStorageDirectory().toString() +
                    File.separator, filename
        )
        val output = FileOutputStream(outputFile)

        var total: Long = 0
        val startTime = System.currentTimeMillis()
        var timeCount = 1

        count = bis.read(data)
        while (count != -1) {
            total += count.toLong()
            totalFileSize = (fileSize / 1.0.pow(2.0)).toInt() / 1000
            val current = ((total / 1.0.pow(2.0)).roundToInt() / 1000).toDouble()

            val progress = (total * 100 / fileSize).toInt()

            val currentTime = System.currentTimeMillis() - startTime

            val download = Download()
            download.totalFileSize = totalFileSize

            if (currentTime > 1000 * timeCount) {
                download.currentFileSize = current.toInt()
                download.progress = progress
                sendNotification(download, id, filename)
                timeCount++
            }

            output.write(data, 0, count)
            count = bis.read(data)
        }

        onDownloadComplete(filename, id)
        output.flush()
        output.close()
        bis.close()
    }

    private fun sendNotification(download: Download, id: Int, filename: String) {
        val progress = "Downloading " + download.currentFileSize + "/" + totalFileSize + " KB"

        notificationBuilder.setProgress(100, download.progress, false)
            .setContentTitle(filename)
            .setContentText(progress)
            .setStyle(NotificationCompat.BigTextStyle().bigText(progress))

        notificationManager.notify(id, notificationBuilder.build())
    }

    private fun onDownloadComplete(filename: String, id: Int) {
        try {
            val download = Download()
            download.progress = 100

            notificationBuilder.setProgress(0, 0, false)
                .setContentTitle("$filename Downloaded")
                .setContentText("Tap to open")
                .setOngoing(false)
                .setStyle(NotificationCompat.BigTextStyle().bigText("File Downloaded"))

            val path1 = Environment.getExternalStorageDirectory().toString() +
                    File.separator + "/" + filename

            val file = File(path1)
            val sharePath: Uri

            sharePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName + ".provider", file
                )
            else
                Uri.fromFile(file)

            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(path1))

            val intent = Intent(Intent.ACTION_VIEW)
                .setType(mimeType)
                .setDataAndType(sharePath, mimeType)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            val pIntent = PendingIntent.getActivity(this, id, intent, 0)

            notificationBuilder
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setContentTitle("$filename Downloaded")
                .setOngoing(false)

            notificationManager.notify(id, notificationBuilder!!.build())

        } catch (ex: Exception) {
            Log.d("Service", "${ex.message}")
        }
    }
}
