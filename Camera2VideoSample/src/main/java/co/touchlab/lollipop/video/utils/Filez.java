package co.touchlab.lollipop.video.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by kgalligan on 11/12/14.
 */
public class Filez
{
    public static File makeBaseDir()
    {
        File root = new File(Environment.getExternalStorageDirectory(), "touchlabiscool");
        root.mkdirs();

        return root;
    }

    public static Uri storeFile(Context context, File videoFile)
    {
        ContentValues content = new ContentValues(4);
        content.put(MediaStore.Video.VideoColumns.TITLE, videoFile.getName());
        content.put(MediaStore.Video.VideoColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        content.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        content.put(MediaStore.Video.Media.DATA, videoFile.getPath());
        ContentResolver resolver = context.getContentResolver();
        return resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, content);
    }
}
