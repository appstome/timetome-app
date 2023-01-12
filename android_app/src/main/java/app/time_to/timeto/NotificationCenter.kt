package app.time_to.timeto

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.net.Uri

/**
 * WARNING
 * DO NOT CHANGE FILE NAME FOR SOUND FILES. Otherwise they will stop working.
 *
 * Common docs: https://developer.android.com/guide/topics/ui/notifiers/notifications
 * Channel docs: https://developer.android.com/training/notify-user/channels
 */
object NotificationCenter {

    fun channelTimeToBreak() = upsertChannel("time_to_break", "Time to Break", "sound_time_to_break")
    fun channelTimerOverdue() = upsertChannel("timer_overdue", "Timer Overdue", null)

    fun getManager() =
        App.instance.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * According to documentation only first call affects. Second do nothing.
     *
     * @param soundName File name, id can be changed
     */
    fun upsertChannel(
        id: String,
        name: String,
        soundName: String?,
    ): NotificationChannel {
        /**
         * With IMPORTANCE_LOW device can show the notification only it the shade not in status bar.
         * todo https://developer.android.com/reference/android/app/NotificationManager#shouldHideSilentStatusBarIcons()
         */
        val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_DEFAULT)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        if (soundName != null)
            channel.setSound(
                Uri.parse("android.resource://${App.instance.packageName}/raw/$soundName"),
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
            )
        getManager().createNotificationChannel(channel)
        return channel
    }

    /**
     * At least on miui_12, if the "Badge -> Dot" is checked in the notification settings for
     * an app, when the application is opened, the notifications would removed automatically.
     */
    fun cleanAllPushes() {
        getManager().cancelAll()
    }
}
