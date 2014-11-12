package com.example.android.camera2video.record;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.camera2video.R;

import co.touchlab.android.threading.eventbus.EventBusExt;

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

    private RecordService mService;
    private ServiceConnection mConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service)
        {
            RecordService.LocalBinder binder = (RecordService.LocalBinder) service;
            mService = binder.getService();
            toggleButton.setChecked(mService.isRecordingVideo());
            toggleButton.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0)
        {
            mService = null;
        }
    };
    private ToggleButton toggleButton;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_test);

        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        toggleButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onToggleScreenShare();
            }
        });
        mProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);

        bindService(new Intent(this, RecordService.class), mConnection, Context.BIND_AUTO_CREATE);

        EventBusExt.getDefault().register(this);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        unbindService(mConnection);

        EventBusExt.getDefault().unregister(this);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void onEventMainThread(RecordService.RecordedVideo video)
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM, video.uri);
        startActivity(Intent.createChooser(intent, "Share using"));
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

        //Make it sticky
        startService(new Intent(this, RecordService.class));

        mService.initProjection(resultCode, data);
        mService.startRecording(this);
    }

    public void onToggleScreenShare()
    {
        if (toggleButton.isChecked())
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
        if (mService.isProjectionReady())
        {
            mService.startRecording(this);
        }
        else
        {
            startActivityForResult(mProjectionManager.createScreenCaptureIntent(), PERMISSION_CODE);
        }
    }

    private void stopScreenSharing()
    {
        mService.stopRecording();
    }
}