package com.gianxd.audiodev.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;

import com.gianxd.audiodev.R;

public class ImageUtil {

    public static Bitmap getAlbumArt(String path, Resources resources) {
        String decodedData = "";
        if (!path.startsWith("/")) {
            try {
                decodedData = new String(android.util.Base64.decode(path, android.util.Base64.DEFAULT), "UTF-8");
            } catch (Exception e) {
                // DO NOTHING
            }
        } else {
            // I'm lazy to add a string variable so I set decodedData instead :sus:
            decodedData = path;
        }
        Bitmap bitmapArt;
        MediaMetadataRetriever artRetriever = new MediaMetadataRetriever();
        artRetriever.setDataSource(decodedData);
        byte[] album_art = artRetriever.getEmbeddedPicture();
        if( album_art != null ){
            bitmapArt = BitmapFactory.decodeByteArray(album_art, 0, album_art.length);
        } else {
            bitmapArt = ((BitmapDrawable)resources.getDrawable(R.drawable.ic_media_album_art)).getBitmap();
        }
        return bitmapArt;
    }

}
