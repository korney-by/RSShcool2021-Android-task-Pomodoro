package com.korneysoft.pomodoro.utils

import android.content.Context
import android.media.RingtoneManager
import android.widget.Toast

fun playFinishedSound(context: Context) {
    RingtoneManager.getRingtone(
        context,
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    ).play()
}

fun showToast(context: Context, text: String) {
    val duration = Toast.LENGTH_LONG
    Toast.makeText(context, text, duration).show();
}
