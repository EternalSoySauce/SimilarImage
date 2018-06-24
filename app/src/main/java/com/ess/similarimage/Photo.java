package com.ess.similarimage;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class Photo {

    public long id;

    public String path;

    public long finger;

    public int dist;

    private static final String[] STORE_IMAGES = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA
    };

    public static List<Photo> getPhotoList(Context context) {
        List<Photo> photoList = new ArrayList<>();

        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver contentResolver = context.getContentResolver();
        String sortOrder = MediaStore.Images.Media.DATE_TAKEN + " desc";
        Cursor cursor = contentResolver.query(uri, STORE_IMAGES, null, null, sortOrder);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                Photo photo = new Photo();
                photo.id = cursor.getLong(0);
                photo.path = cursor.getString(1);
                photoList.add(photo);
            }
            cursor.close();
        }
        return photoList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null && obj instanceof Photo) {
            Photo photo = (Photo) obj;
            return !(this.path == null || photo.path == null) && this.path.equals(photo.path);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
