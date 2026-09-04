package com.example.smartexpensemanager.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.smartexpensemanager.data.mpesa.MpesaMessageParser
import com.example.smartexpensemanager.data.mpesa.PendingMpesaStore

/**
 * Listens for incoming notifications (M-Pesa app or the default SMS app) and
 * picks out M-Pesa transaction confirmations. Requires the user to grant
 * "notification access" for this app in system settings — deliberately used
 * instead of READ_SMS, which Play Store policy effectively restricts to
 * default-SMS-handler apps.
 */
class MpesaNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_TEXT)
            )?.toString() ?: return

        val combined = "$title $text"
        if (!combined.contains("Ksh")) return

        val parsed = MpesaMessageParser.parse(combined, sbn.postTime) ?: return
        PendingMpesaStore(applicationContext).add(parsed)
    }
}
