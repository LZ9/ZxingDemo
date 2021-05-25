package com.lodz.android.zxingdemo.main.media

import android.content.Context
import android.media.MediaPlayer
import android.os.Vibrator
import com.lodz.android.zxingdemo.R

/**
 * 提示音管理类
 * @author zhouL
 * @date 2021/4/9
 */

class BeepManager(val context: Context, val isVibrate: Boolean = false) {

    private val VIBRATE_DURATION = 200L

    private var mMediaPlayer: MediaPlayer? = null

    fun play() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(context, R.raw.beep)
        }
        mMediaPlayer?.start()
        if (isVibrate){
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE)
            if (vibrator is Vibrator){
                vibrator.vibrate(VIBRATE_DURATION)
            }
        }
    }

    fun release(){
        mMediaPlayer?.stop()
        mMediaPlayer?.release()
        mMediaPlayer = null
    }
}