package co.touchlab.record;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by kgalligan on 11/12/14.
 */
public class ActivityConfig
{
    private static final String TAG = ActivityConfig.class.getSimpleName();
    private int mScreenDensity;
    private static final int PERMISSION_CODE = 1;
    private MediaProjectionManager mProjectionManager;

    private int mDisplayWidth;
    private int mDisplayHeight;
    private boolean mScreenSharing;

    private MediaProjection mMediaProjection;
    private VirtualDisplay mVirtualDisplay;

    private Activity activity;

    public ActivityConfig(Activity activity)
    {
        this.activity = activity;
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mScreenDensity = metrics.densityDpi;

        mProjectionManager =
                (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);

    }

    public void onDestroy() {
        if (mMediaProjection != null) {
            mMediaProjection.stop();
            mMediaProjection = null;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != PERMISSION_CODE) {
            Log.e(TAG, "Unknown request code: " + requestCode);
            return;
        }
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(activity, "User denied screen sharing permission", Toast.LENGTH_SHORT).show();
            return;
        }
        mMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        mVirtualDisplay = createVirtualDisplay();
    }
}
