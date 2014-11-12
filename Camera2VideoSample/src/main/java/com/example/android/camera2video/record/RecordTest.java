package com.example.android.camera2video.record;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.camera2video.R;

import java.io.File;
import java.io.IOException;

/**
 * Created by kgalligan on 11/12/14.
 */
public class RecordTest extends Activity
{
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static final String TAG = "MediaProjectionDemo";
    private static final int PERMISSION_CODE = 1;
    private MediaProjectionManager mProjectionManager;
    private MediaProjection mMediaProjection;

    private RecordService mService;
    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            RecordService.LocalBinder binder = (RecordService.LocalBinder) service;
            mService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            mService = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_test);

        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        bindService(new Intent(this, RecordService.class), mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mMediaProjection != null)
        {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode != PERMISSION_CODE)
        {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }

        if (resultCode != RESULT_OK)
        {
            Toast.makeText(this, "User denied screen sharing permission", Toast.LENGTH_SHORT).show();
            return;
        }

        mService.startRecording(this, resultCode, data);
    }

    public void onToggleScreenShare(View view)
    {
        if (((ToggleButton) view).isChecked())
        {
            shareScreen();
        }
        else
        {
            stopScreenSharing();
        }
    }

    private void shareScreen()
    {
        //TODO: Need to be able to restart
        /*if (mMediaProjection == null)
        {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
            return;
        }*/

//        mVirtualDisplay = createVirtualDisplay();

        startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
    }

    private void stopScreenSharing()
    {
        mService.stopRecording();
    }

    /*private void resizeVirtualDisplay()
    {
        if (mVirtualDisplay == null)
        {
            return;
        }
        mVirtualDisplay.resize(mDisplayWidth, mDisplayHeight, mScreenDensity);
    }*/
}