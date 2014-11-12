package com.example.android.camera2video;

import android.app.Activity;
import android.os.Bundle;


public class TestMirrorActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_mirror);
        if (null == savedInstanceState) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, Screen2VideoFragment.newInstance())
                    .commit();
        }
    }


}
