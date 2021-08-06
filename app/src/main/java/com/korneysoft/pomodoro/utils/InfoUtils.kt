package com.korneysoft.pomodoro.utils

import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.widget.Toast

fun playNotification(context: Context,notification:Int) {
    //val notification=RingtoneManager.TYPE_NOTIFICATION
    RingtoneManager.getRingtone(
        context,
        RingtoneManager.getDefaultUri(notification)
    ).play()
}

fun playSound(context: Context, resid: Int) {
    val mediaPlayer=MediaPlayer.create(context,resid)
    mediaPlayer.start()
}

fun showToast(context: Context, text: String) {
    val duration = Toast.LENGTH_LONG
    Toast.makeText(context, text, duration).show()
}
