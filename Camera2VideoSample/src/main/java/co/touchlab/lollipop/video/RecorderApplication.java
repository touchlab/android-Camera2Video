package co.touchlab.lollipop.video;
import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;

/**
 * Created by kgalligan on 1/18/16.
 */
public class RecorderApplication extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        Fabric.with(this, new Crashlytics(), new Answers());
    }
}
