package tk.gianxddddd.audiodev.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import tk.gianxddddd.audiodev.R;

import java.io.File;

public class ImageUtil {

    public static void cropImage(Activity activity, String path, int result) {
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            File file = new File(path);
            Uri contentUri = Uri.fromFile(file);

            cropIntent.setDataAndType(contentUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 280);
            cropIntent.putExtra("outputY", 280);
            cropIntent.putExtra("return-data", false);

            activity.startActivityForResult(cropIntent, result);

        } catch (ActivityNotFoundException anfe) {
            ApplicationUtil.toast("Device not supported.", Toast.LENGTH_LONG, activity);
        }
    }

    public static Bitmap getAlbumArt(String path, Resources resources, Resources.Theme theme) {
        Bitmap bitmapArt;
        MediaMetadataRetriever artRetriever = new MediaMetadataRetriever();

        if (path.startsWith("file://") || path.startsWith("/")) {
            artRetriever.setDataSource(path);
        } else if (path.startsWith("content://")) {
            throw new IllegalArgumentException("Content URIs cannot be set as path.");
        }

        byte[] album_art = artRetriever.getEmbeddedPicture();

        if (album_art != null) {
            bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length);
        } else {
            bitmapArt = ((BitmapDrawable) ResourcesCompat.getDrawable(resources, R.drawable.ic_media_album_art, theme)).getBitmap();
        }

        artRetriever.close();

        return bitmapArt;
    }

    public static Bitmap getAlbumArt(Uri contentUri, Activity activity) {
        Bitmap bitmapArt;
        MediaMetadataRetriever artRetriever = new MediaMetadataRetriever();

        if (contentUri.toString().startsWith("content://")) {
            artRetriever.setDataSource(activity, contentUri);
        } else if (contentUri.toString().startsWith("file://") || contentUri.toString().startsWith("/")) {
            throw new IllegalArgumentException("Filepaths cannot be set as URIs.");
        }

        byte[] album_art = artRetriever.getEmbeddedPicture();

        if (album_art != null) {
            bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length);
        } else {
            bitmapArt = ((BitmapDrawable) ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_media_album_art, activity.getTheme())).getBitmap();
        }

        return bitmapArt;
    }
}
