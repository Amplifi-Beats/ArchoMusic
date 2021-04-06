package com.gianxd.audiodev.util;

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

    public static Bitmap getAlbumArt(String path, Resources resources) {
        Bitmap bitmapArt;
        MediaMetadataRetriever artRetriever = new MediaMetadataRetriever();
        artRetriever.setDataSource(StringUtil.decodeString(path));
        byte[] album_art = artRetriever.getEmbeddedPicture();
        if( album_art != null ){
            bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length);
        } else {
            bitmapArt = ((BitmapDrawable)resources.getDrawable(R.drawable.ic_media_album_art)).getBitmap();
        }
        return bitmapArt;
    }

}
