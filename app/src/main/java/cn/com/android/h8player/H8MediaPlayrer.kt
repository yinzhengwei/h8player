package cn.com.android.h8player

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.RemoteException
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceView
import android.widget.Toast
import java.util.*

/**
 *  Created by yinzhengwei on 2018/12/2.
 *  @Function
 */
open class H8MediaPlayrer(context: Context, attribute: AttributeSet) : SurfaceView(context, attribute) {

    private val TAG = javaClass.name
    private lateinit var mMediaPlayer: MediaPlayer
    //private var mDisplayManager: DisplayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private var am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_SYSTEM)
    private var defaultVolume = am.getStreamVolume(AudioManager.STREAM_SYSTEM)

    private val mTrackAudioIndex = Vector<Int>()
    private var curAudioIndex = 0
    private var trackNum = 0

    private var mOnPreparedListener: MediaPlayer.OnPreparedListener? = null
    private var mOnCompletionListener: MediaPlayer.OnCompletionListener? = null
    private var mOnErrorListener: MediaPlayer.OnErrorListener? = null

    var isMuteVolume = false
    private var step = 2

    private var mIsPrepared: Boolean = false

    init {
        mIsPrepared = false
        mMediaPlayer = MediaPlayer().apply {
            setOnPreparedListener(mPreparedListener)
            setOnCompletionListener(mCompletionListener)
            setOnErrorListener(mErrorListener)
        }
    }

    fun isPlaying(): Boolean = mMediaPlayer.isPlaying

    fun start() {
        if (!isPlaying()) {
            mMediaPlayer.start()
        }
    }

    fun pause() {
        if (isPlaying()) {
            mMediaPlayer.pause()
        }
    }

    fun stop() {
        if (isPlaying()) {
            mMediaPlayer.stop()
        }
    }

//    fun upVolume() {
//        if (defaultVolume == maxVolume)
//            return
//        defaultVolume++
//
////        setVolume(AudioManager.ADJUST_LOWER)
//        setVolume()
//    }
//
//    fun downVolume() {
//        if (defaultVolume == 0)
//            return
//        defaultVolume--
//
////        setVolume(AudioManager.ADJUST_RAISE)
//        setVolume()
//    }

    fun setVolume(volume: Int) {
        this.defaultVolume = volume
        setVolume()
    }

    private fun setVolume() {
        try {
            if (defaultVolume == maxVolume || defaultVolume == 0)
                return
            //直接设置音量值
            am.setStreamVolume(AudioManager.STREAM_SYSTEM, defaultVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)

            //渐进式设置
//            am.adjustStreamVolume(AudioManager.STREAM_SYSTEM,
//                    type, AudioManager.FLAG_PLAY_SOUND)
        } catch (e: RemoteException) {
            Log.e(TAG, "Dead object in setStreamVolume", e)
        }
    }

    fun isMute() = isMuteVolume

    fun setMute(isMute: Boolean) {
        this.isMuteVolume = isMute
        if (isMute)
            setVolume()
        else
            setVolume()
    }

//    fun setmDefaultMusicVolume(mDefaultMusicVolume: Int) {
//        setVolume(mDefaultMusicVolume)
//    }

    fun setVolumeDuce() {
        if (defaultVolume >= step) {
            defaultVolume -= step
        } else {
            defaultVolume = 0
        }
        setVolume()
    }

    fun setVolumeAdd() {
        if (defaultVolume < 100) {
            defaultVolume += step
            setVolume()
        }
    }

    fun setStep(step: Int) {
        this.step = step
    }

    fun getmDefaultMusicVolume() = defaultVolume

    fun setVideoPath(path: String) {
        try {
            mIsPrepared = false

            mMediaPlayer.reset()

            mMediaPlayer.setDataSource(context, Uri.parse(path))
            mMediaPlayer.setDisplay(holder)
            mMediaPlayer.prepare()
            mMediaPlayer.start()

            getTrack()

            //上首歌如果是原唱，跟随上一首
            if (isTrack) {
                track()
            } else {
                orgin()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 升降调
     * 暂时取值范围是：-6.0f ----- 9.0f
     */
    fun soundChange(progress: Float) {
        if (progress < -6.0f || progress > 9.0f)
            return
        Thread {
            mMediaPlayer.setPitch(progress)
        }.start()
    }

    //默认是原唱
    var isTrack = true

    fun changeTrack() {
        //Log.e(TAG, "mTrackAudioIndex size = " + mTrackAudioIndex.size)
        Toast.makeText(context, "$trackNum", Toast.LENGTH_SHORT).show()

        if (isTrack) {
            orgin()
        } else {
            track()
        }

//        //如果是单音轨的歌，则切换左右声道
//        //左右声道(1左(伴唱)；2右(原唱)；0正常)
//        if (trackNum < 2) {
//            if (isTrack) {
//                mMediaPlayer.setParameter(1102, 2)
//            } else {
//                mMediaPlayer.setParameter(1102, 1)
//            }
//        } else {
//            mMediaPlayer.setParameter(1102, 0)
//
//            curAudioIndex = (curAudioIndex + 1) % trackNum
//            try {
//                mMediaPlayer.selectTrack(mTrackAudioIndex[curAudioIndex]);
//
//            } catch (e: IllegalStateException) {
//                Log.d(TAG, "setAudioTrack(): IllegalStateException: set audio track fail")
//            } catch (e: RuntimeException) {
//                Log.d(TAG, "setAudioTrack(): RuntimeException: set subtitle fail")
//            }
//        }
//        isTrack = !isTrack
    }

    //如果是单音轨的歌，则切换左右声道
    //左右声道(1左(伴唱)；2右(原唱)；0正常)
    fun track() {
        if (trackNum < 2) {
            mMediaPlayer.setParameter(1102, 2)
        } else {
//            curAudioIndex = (curAudioIndex + 1) % trackNum
//            try {
//                mMediaPlayer.selectTrack(mTrackAudioIndex[curAudioIndex]);
//            } catch (e: Exception) {
//                Log.d(TAG, "setAudioTrack(): IllegalStateException: set audio track fail")
//            }
            curAudioIndex = 0
            try {
                mMediaPlayer.selectTrack(mTrackAudioIndex[curAudioIndex]);
            } catch (e: Exception) {
                Log.d(TAG, "setAudioTrack(): IllegalStateException: set audio track fail")
            }
        }
        isTrack = true
    }

    //如果是单音轨的歌，则切换左右声道
    //左右声道(1左(伴唱)；2右(原唱)；0正常)
    fun orgin() {
        if (trackNum < 2) {
            mMediaPlayer.setParameter(1102, 1)
        } else {
            curAudioIndex = (curAudioIndex + 1) % trackNum
            try {
                mMediaPlayer.selectTrack(mTrackAudioIndex[curAudioIndex]);
            } catch (e: Exception) {
                Log.d(TAG, "setAudioTrack(): IllegalStateException: set audio track fail")
            }
        }
        isTrack = false
    }

    private fun getTrack() {
        val trackInfos = mMediaPlayer.trackInfo
        mTrackAudioIndex.clear()
        //curAudioIndex = 0

        mMediaPlayer.setParameter(1102, 0)

        trackNum = 0

        if (trackInfos != null && trackInfos.isNotEmpty()) {
            trackInfos.forEachIndexed { j, info ->
                println("******************track type = " + info.trackType)
                val typE_AUDIO = MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO
                println("MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO = $typE_AUDIO")
                if (info.trackType == typE_AUDIO) {
                    trackNum++
                    Log.e(TAG, "add track ********************** $j")
                    // mTrackInfosAudio.add(info);
                    mTrackAudioIndex.add(j)
                }
            }
        } else {
            if (trackInfos == null) {
                println("trackInfos = null")
            } else {
            }
            System.out.println("trackInfos.length = " + trackInfos?.size)
        }
    }

    fun setOnPreparedListener(l: MediaPlayer.OnPreparedListener) {
        mOnPreparedListener = l
    }

    fun setOnCompletionListener(l: MediaPlayer.OnCompletionListener) {
        mOnCompletionListener = l
    }

    fun setOnErrorListener(l: MediaPlayer.OnErrorListener) {
        mOnErrorListener = l
    }

    private var mPreparedListener: MediaPlayer.OnPreparedListener = MediaPlayer.OnPreparedListener { mp ->
        mIsPrepared = true
        if (mOnPreparedListener != null) {
            mOnPreparedListener?.onPrepared(mp)
        }
    }

    private val mCompletionListener = MediaPlayer.OnCompletionListener {
        if (mOnCompletionListener != null) {
            mOnCompletionListener?.onCompletion(it)
        }
    }

    private val mErrorListener = MediaPlayer.OnErrorListener { mp, framework_err, impl_err ->
        Log.d(TAG, "Error: $framework_err,$impl_err")
        if (mOnErrorListener != null) {
            mOnErrorListener?.onError(mp, framework_err, impl_err)
        }
        true
    }

    fun seekTo(msec: Int) {
        if (mIsPrepared) {
            mMediaPlayer.seekTo(msec)
        }
//        else {
//            mSeekWhenPrepared = msec
//        }
    }

}