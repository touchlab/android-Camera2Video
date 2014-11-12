package com.example.android.camera2video.record;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Surface;

import java.io.File;
import java.io.IOException;

public class RecordService extends Service
{
    public static final int BIT_RATE = 2000000;
    public static final int FRAME_RATE = 30;

    private int mScreenDensity;
    private int mDisplayWidth;
    private int mDisplayHeight;

    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;
    private MediaRecorder mMediaRecorder;

    private boolean mIsRecordingVideo;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder
    {
        RecordService getService()
        {
            // Return this instance of LocalService so clients can call public methods
            return RecordService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return mBinder;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    public void startRecording(Activity activity, int resultCode, Intent data)
    {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mDisplayWidth = metrics.widthPixels;
        mDisplayHeight = metrics.heightPixels;

        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mVirtualDisplay = createVirtualDisplay();
    }

    public void stopRecording()
    {
        mIsRecordingVideo = false;
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;

        if (mVirtualDisplay == null)
        {
            return;
        }
        mVirtualDisplay.release();
    }

    private VirtualDisplay createVirtualDisplay()
    {
        mMediaRecorder = new MediaRecorder();
        final File file = new File(Environment.getExternalStorageDirectory(), "screenvid_" + System.currentTimeMillis() + ".mp4");

        try
        {
            // UI
            mIsRecordingVideo = true;
            // Configure the MediaRecorder
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(file.getAbsolutePath());
            mMediaRecorder.setVideoEncodingBitRate(BIT_RATE);
            mMediaRecorder.setVideoFrameRate(FRAME_RATE);
            mMediaRecorder.setVideoSize(1280, 720);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//            int rotation = getWindowManager().getDefaultDisplay().getRotation();
//            int orientation = ORIENTATIONS.get(rotation);
//            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
            Surface surface = mMediaRecorder.getSurface();

            VirtualDisplay virtualDisplay = mMediaProjection.createVirtualDisplay("ScreenSharingDemo",
                    mDisplayWidth, mDisplayHeight, mScreenDensity,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    surface, null /*Callbacks*/, null /*Handler*/);

            mMediaRecorder.start();
            return virtualDisplay;
        }
        catch (IllegalStateException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
