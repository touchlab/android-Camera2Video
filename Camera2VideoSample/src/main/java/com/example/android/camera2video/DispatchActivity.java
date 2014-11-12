package com.example.android.camera2video;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.android.camera2video.R;
import com.example.android.camera2video.record.RecordTest;

public class DispatchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch);

        findViewById(R.id.cameraButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                CameraActivity.callMe(DispatchActivity.this);
            }
        });

        findViewById(R.id.screenButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                RecordTest.callMe(DispatchActivity.this);
            }
        });
    }


}
