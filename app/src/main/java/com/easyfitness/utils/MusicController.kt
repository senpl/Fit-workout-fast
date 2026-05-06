package com.easyfitness.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.easyfitness.R
import com.easyfitness.utils.FileChooserDialog.ChosenFileListener
import java.io.File
import java.io.IOException

class MusicController(activity: AppCompatActivity) {
    var mActivity: AppCompatActivity = activity
    var myNoisyAudioStreamReceiver: NoisyAudioStreamReceiver? = null

    // Music Controller
    private var musicPlay: ImageButton? = null
    private var musicReplay: ImageButton? = null
    private var barSongTitle: TextView? = null
    private var barSongTime: TextView? = null
    private var seekProgressBar: SeekBar? = null

    private val utils = UnitConverter()

    private var isStopped = true
    private var isPaused = false
    private var newSongSelected = false
    private var isReplayOn = false

    private var fileChooserDialog: FileChooserDialog? = null
    private var songList: MutableList<String>? = null
    private var currentFile = ""
    private var currentPath = ""
    private var currentIndexSongList = -1

    // Handler to update UI timer, progress bar etc,.
    private val mHandler = Handler()


    private var mediaPlayer: MediaPlayer? = null

    /*
     * Moves the cursor of the progress bar to accelerate a song.
     */
    private val seekBarTouch: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if (!isStopped && fromUser) {
                mediaPlayer!!.seekTo((mediaPlayer!!.getDuration() * (progress / 100.0)).toInt())
            }
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
        }
    }

    /**
     * Background Runnable thread
     */
    private val mUpdateTimeTask: Runnable = object : Runnable {
        override fun run() {
            if (mediaPlayer != null) {
                if (mediaPlayer!!.isPlaying()) {
                    val totalDuration = mediaPlayer!!.getDuration().toLong()
                    val currentDuration = mediaPlayer!!.getCurrentPosition().toLong()

                    // Displaying Total Duration time
                    barSongTime!!.setText(
                        "" + utils.milliSecondsToTimer(currentDuration) + "/" + utils.milliSecondsToTimer(
                            totalDuration
                        )
                    )

                    // Updating progress bar
                    val progress = utils.getProgressPercentage(currentDuration, totalDuration)
                    //Log.d("Progress", "" + progress);
                    seekProgressBar!!.setProgress(progress)

                    // Running this thread after 200 milliseconds
                    mHandler.postDelayed(this, 201)
                }
            }
        }
    }
    private var intentFilter: IntentFilter? = null

    //private OnFocusChangeListener touchRazEdit = new View.OnFocusChangeListener() {
    private val songCompletion: OnCompletionListener = object : OnCompletionListener {
        override fun onCompletion(mp: MediaPlayer?) {
            if (currentIndexSongList + 1 < songList!!.size) {
                Next()
            } else {
                if (isReplayOn) {
                    newSongSelected = true
                    currentIndexSongList = 0
                    Play()
                } else {
                    /* release mediaplayer */
                    Stop()
                }
            }
        }
    }

    //private OnFocusChangeListener touchRazEdit = new View.OnFocusChangeListener() {
    private val mediaplayerReady: OnPreparedListener = object : OnPreparedListener {
        override fun onPrepared(mp: MediaPlayer?) {
            mediaPlayer!!.start()
            mActivity!!.registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
            mediaPlayer!!.setOnCompletionListener(songCompletion)
            barSongTitle!!.setText(currentFile)
            musicPlay!!.setImageResource(R.drawable.ic_pause_black_24dp)
            updateProgressBar()
        }
    }
    private val playerClick: View.OnClickListener = object : View.OnClickListener {
        override fun onClick(v: View) {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer()
                mediaPlayer!!.setOnPreparedListener(mediaplayerReady)
                loadPreferences()
            }

            val id = v.getId()
            if (id == R.id.playerPlay) {
                if (mediaPlayer!!.isPlaying()) {
                    Pause()
                } else {
                    Play()
                }
            } else if (id == R.id.playerStop) {
                Stop()
            } else if (id == R.id.playerNext) {
                Next()
            } else if (id == R.id.playerPrevious) {
                Previous()
            } else if (id == R.id.playerList) {
                fileChooserDialog!!.chooseDirectory(currentPath)
            } else if (id == R.id.playerLoop) {
                if (isReplayOn) {
                    isReplayOn = false
                    musicReplay!!.setImageResource(R.drawable.ic_replay_blue_24dp)
                } else {
                    isReplayOn = true
                    musicReplay!!.setImageResource(R.drawable.ic_replay_black_24dp)
                }
            }
        }
    }

    init {

        // Create DirectoryChooserDialog and register a callback
//        fileChooserDialog =
//            FileChooserDialog(this.mActivity, ChosenFileListener { file: String ->
//                currentFile = file
//                currentPath = getParentDirPath(currentFile)
//                buildSongList(currentPath)
//                currentIndexSongList = songList!!.indexOf(getFileName(file))
//                newSongSelected = true
//                Play()
//                savePreferences()
//            })

//        fileChooserDialog!!.newFolderEnabled=(false)
//        fileChooserDialog!!.displayFolderOnly=(false)
//        fileChooserDialog!!.fileFilter=("mp3;3gp;mp4;aac;ts;flac;mid;ogg;mkv;wav")
    }

    fun initView() {
        // Music controller
        musicPlay = mActivity.findViewById<ImageButton?>(R.id.playerPlay)
        val musicStop = mActivity.findViewById<ImageButton>(R.id.playerStop)
        val musicNext = mActivity.findViewById<ImageButton>(R.id.playerNext)
        val musicPrevious = mActivity.findViewById<ImageButton>(R.id.playerPrevious)
        val musicList = mActivity.findViewById<ImageButton>(R.id.playerList)
        musicReplay = mActivity.findViewById<ImageButton?>(R.id.playerLoop)

        //playerTopLayout = (LinearLayout) mActivity.findViewById(R.id.playerTopLayout);
        barSongTitle = mActivity.findViewById<TextView?>(R.id.playerSongTitle)
        barSongTitle!!.setSingleLine(true)
        barSongTitle!!.ellipsize = TextUtils.TruncateAt.MARQUEE
        barSongTitle!!.setHorizontallyScrolling(true)
        barSongTitle!!.setSelected(true)

        seekProgressBar = mActivity.findViewById<SeekBar?>(R.id.playerSeekBar)
        seekProgressBar!!.setMax(100)
        seekProgressBar!!.progress = 0

        barSongTime = mActivity.findViewById<TextView?>(R.id.playerSongProgress)

        musicPlay!!.setOnClickListener(playerClick)
        musicStop.setOnClickListener(playerClick)
        musicNext.setOnClickListener(playerClick)
        musicPrevious.setOnClickListener(playerClick)
        musicList.setOnClickListener(playerClick)
        musicReplay!!.setOnClickListener(playerClick)
        //playerTopLayout.setOnTouchListener(progressBarTouch);
        seekProgressBar!!.setOnSeekBarChangeListener(seekBarTouch)

        myNoisyAudioStreamReceiver = NoisyAudioStreamReceiver()
        intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    }

    fun Play() {
        // Play song
        if (currentIndexSongList < 0) if (currentPath == "") fileChooserDialog!!.chooseDirectory(
            currentPath
        )
        else {
            currentIndexSongList = 0
            buildSongList(currentPath)
            currentFile = songList!!.get(0)
            newSongSelected = true
            Play()
        }
        else {
            try {
                if (newSongSelected) {
                    newSongSelected = false
                    currentFile = songList!!.get(currentIndexSongList)
                    mediaPlayer!!.reset()
                    mediaPlayer!!.setDataSource(currentPath + File.separator + currentFile)
                    mediaPlayer!!.prepareAsync()
                    isStopped = false
                    isPaused = false
                } else if (isPaused) { // differe de STOP
                    mediaPlayer!!.start()
                    mActivity!!.registerReceiver(myNoisyAudioStreamReceiver, intentFilter)
                    musicPlay!!.setImageResource(R.drawable.ic_pause_black_24dp)
                    updateProgressBar()
                    isStopped = false
                    isPaused = false
                }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: SecurityException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    fun Pause() {
        mediaPlayer!!.pause()
        try {
            mActivity!!.unregisterReceiver(myNoisyAudioStreamReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        // Changing Button Image to pause image
        isPaused = true
        musicPlay!!.setImageResource(R.drawable.ic_play_arrow_black_24dp)
    }

    fun Stop() {
        mediaPlayer!!.stop()
        try {
            mActivity!!.unregisterReceiver(myNoisyAudioStreamReceiver)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }
        isStopped = true
        isPaused = false
        barSongTitle!!.setText("")
        seekProgressBar!!.setProgress(0)
        barSongTime!!.setText("")
        currentIndexSongList = -1
        // Changing Button Image to play image
        musicPlay!!.setImageResource(R.drawable.ic_play_arrow_black_24dp)
    }

    fun Next() {
        /* load the new source */
        if (currentIndexSongList >= 0) {
            if (currentIndexSongList + 1 < songList!!.size) {
                currentIndexSongList = currentIndexSongList + 1
                newSongSelected = true
                Play()
            }
        }
    }

    fun Previous() {
        /* load the new source */
        if (currentIndexSongList > 0) {
            currentIndexSongList = currentIndexSongList - 1
            newSongSelected = true
            Play()
        }
    }

    private fun buildSongList(path: String?) {
        songList = fileChooserDialog!!.getFiles(currentPath) as MutableList<String>?
    }

    /**
     * Update timer on seekbar
     */
    fun updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 200)
    }

    private fun loadPreferences() {
        // Restore preferences
        val settings = mActivity.getSharedPreferences(PREFS_NAME, 0)
        currentPath = settings.getString("currentPath", "")!!
    }

    private fun savePreferences() {
        // Restore preferences
        val settings = mActivity.getSharedPreferences(PREFS_NAME, 0)
        val editor = settings.edit()
        editor.putString("currentPath", currentPath)
        val x = editor.commit()
    }

    fun releaseMediaPlayer() {
        mediaPlayer!!.release()
        mediaPlayer = null
    }

    private fun showMP3Player(showit: Boolean) {
        if (showit == true) {
            //this.ba.showMP3Player();
        } else {
        }
    }

    inner class NoisyAudioStreamReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY == intent.getAction()) {
                Pause()
                Log.d("Message", "HeadPhone Unplugged")
            }
        }
    }

    companion object {
        const val MUSICCONTROLLER: Int = 1563540
        val MUSICCONTROLLER_PLAY_CLICK: Int = MUSICCONTROLLER
        var PREFS_NAME: String = "music_prefsfile"
        fun getParentDirPath(fileOrDirPath: String): String {
            val endsWithSlash = fileOrDirPath.endsWith(File.separator)
            return fileOrDirPath.substring(
                0, fileOrDirPath.lastIndexOf(
                    File.separatorChar,
                    if (endsWithSlash) fileOrDirPath.length - 2 else fileOrDirPath.length - 1
                )
            )
        }

        fun getFileName(fileOrDirPath: String): String {
            return fileOrDirPath.substring(fileOrDirPath.lastIndexOf(File.separatorChar) + 1)
        }
    }
}
