package com.korneysoft.pomodoro.utils

import android.content.Context
import android.media.RingtoneManager

fun playFinishedSound(context: Context) {
    RingtoneManager.getRingtone(
        context,
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    ).play()
}