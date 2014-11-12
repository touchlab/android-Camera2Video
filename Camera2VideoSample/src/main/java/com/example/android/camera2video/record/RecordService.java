package com.example.android.camera2video.record;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.widget.Toast;

import com.example.android.camera2video.utils.Filez;

import java.io.File;
import java.io.IOException;

import co.touchlab.android.threading.eventbus.EventBusExt;
import co.touchlab.android.threading.utils.UiThreadContext;

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

    private boolean recordingVideo;

    private final IBinder mBinder = new LocalBinder();
    private File videoFile;

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

    public boolean isRecordingVideo()
    {
        return recordingVideo;
    }

    public boolean isProjectionReady()
    {
        return mMediaProjection != null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_STICKY;
    }

    public void initProjection(int resultCode, Intent data)
    {
        UiThreadContext.assertUiThread();

        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
    }

    public void startRecording(Activity activity)
    {
        UiThreadContext.assertUiThread();

        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;
        mDisplayWidth = metrics.widthPixels/2;
        mDisplayHeight = metrics.heightPixels/2;

        mVirtualDisplay = createVirtualDisplay();

    }

    public void stopRecording()
    {
        UiThreadContext.assertUiThread();

        recordingVideo = false;
        // Stop recording
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;

        if (mVirtualDisplay != null)
        {
            mVirtualDisplay.release();
            mVirtualDisplay = null;
        }

        if(videoFile != null && videoFile.length() > 0)
            shareVideo(videoFile);
        else
            Toast.makeText(this, "Looks like you had trouble, kid", Toast.LENGTH_LONG).show();

        videoFile = null;
        stopSelf();
    }

    private void shareVideo(File videoFile)
    {
        Uri uri = Filez.storeFile(this, videoFile);
        EventBusExt.getDefault().post(new RecordedVideo(uri));
    }

    public static class RecordedVideo
    {
        public final Uri uri;

        public RecordedVideo(Uri uri)
        {
            this.uri = uri;
        }
    }

    private VirtualDisplay createVirtualDisplay()
    {
        mMediaRecorder = new MediaRecorder();
        videoFile = new File(Filez.makeBaseDir(), "screenvid_" + System.currentTimeMillis() + ".mp4");

        try
        {
            // UI
            recordingVideo = true;
            // Configure the MediaRecorder
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(videoFile.getAbsolutePath());
            mMediaRecorder.setVideoEncodingBitRate(BIT_RATE);
            mMediaRecorder.setVideoFrameRate(FRAME_RATE);
            mMediaRecorder.setVideoSize(mDisplayWidth, mDisplayHeight);
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
