package co.touchlab.record;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Surface;

import java.io.File;
import java.io.IOException;

public class RecordService extends Service
{
    private MediaRecorder mMediaRecorder;
    private File currentVideoFile;


    public RecordService()
    {
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }



    public boolean toggleVideo()
    {
        if (isRecording()) {
            stopRecordingVideo();
        } else {
            startRecordingVideo();
        }

        return isRecording();
    }

    private boolean isRecording()
    {
        return currentVideoFile != null;
    }

    private void startRecordingVideo() {
        mMediaRecorder = new MediaRecorder();
        currentVideoFile = createVideoFile();
        try {

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(currentVideoFile.getAbsolutePath());
            mMediaRecorder.setVideoEncodingBitRate(10000000);
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(1280, 720);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
//            int orientation = ORIENTATIONS.get(rotation);
//            mMediaRecorder.setOrientationHint(orientation);
            mMediaRecorder.prepare();
            Surface surface = mMediaRecorder.getSurface();

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int width=dm.widthPixels;
            int height=dm.heightPixels;
            int dens=dm.densityDpi;

            DisplayManager displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
            displayManager.createVirtualDisplay("mirror", width, height, dens, surface, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, new VirtualDisplay.Callback()
            {
                @Override
                public void onPaused()
                {
                    super.onPaused();
                }

                @Override
                public void onResumed()
                {
                    super.onResumed();
                }

                @Override
                public void onStopped()
                {
                    super.onStopped();
                }
            }, null);

            new Handler().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    mMediaRecorder.start();
                }
            }, 2000);

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createVideoFile()
    {
        return new File(getExternalFilesDir(null), "videoscreen_"+ System.currentTimeMillis() +".mp4");
    }

    private void stopRecordingVideo() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
        currentVideoFile = null;
    }
}
