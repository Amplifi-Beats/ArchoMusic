package com.gianxd.audiodev.util;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.widget.Toast;

import com.gianxd.audiodev.AudioDev;
import com.gianxd.audiodev.R;

import java.io.File;

public class ImageUtil {

    public static void cropImage(Activity activity, String path, int result){
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
            ApplicationUtil.toast(activity, "Device not supported.", Toast.LENGTH_LONG);
        }
    }

    public static Bitmap getAlbumArt(String path) {
        Bitmap bitmapArt;
        MediaMetadataRetriever artRetriever = new MediaMetadataRetriever();
        if (path.startsWith("file://") || path.startsWith("/")) {
            artRetriever.setDataSource(path);
        } else if (path.startsWith("content://")) {
            throw new IllegalArgumentException("Content URIs cannot be set as path.");
        }
        byte[] album_art = artRetriever.getEmbeddedPicture();
        if( album_art != null ){
            bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length);
        } else {
            bitmapArt = ((BitmapDrawable)AudioDev.applicationResources.getDrawable(R.drawable.ic_media_album_art)).getBitmap();
        }
        artRetriever.close();
        return bitmapArt;
    }

    public static Bitmap getAlbumArt(Context context, Uri contentUri, Resources resources) {
        Bitmap bitmapArt;
        MediaMetadataRetriever artRetriever = new MediaMetadataRetriever();
        if (contentUri.toString().startsWith("content://")) {
            artRetriever.setDataSource(context, contentUri);
        } else if (contentUri.toString().startsWith("file://") || contentUri.toString().startsWith("/")) {
            throw new IllegalArgumentException("Filepaths cannot be set as URIs.");
        }
        byte[] album_art = artRetriever.getEmbeddedPicture();
        if( album_art != null ){
            bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length);
        } else {
            bitmapArt = ((BitmapDrawable)AudioDev.applicationResources.getDrawable(R.drawable.ic_media_album_art)).getBitmap();
        }
        return bitmapArt;
    }

}
