package com.ess.similarimage;

import android.content.Context;

import com.snappydb.DB;
import com.snappydb.DBFactory;
import com.snappydb.SnappydbException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Database {

    private final static String TABLE_PHOTO = "PHOTO";

    public static List<Photo> queryPhotos(Context context) {
        List<Photo> photoList = new ArrayList<>();
        try {
            DB snappydb = DBFactory.open(context);
            photoList.addAll(Arrays.asList(snappydb.getObjectArray(TABLE_PHOTO, Photo.class)));
            snappydb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
        return photoList;
    }

    public static void updatePhotos(Context context, List<Photo> photoList) {
        try {
            DB snappydb = DBFactory.open(context);
            snappydb.put(TABLE_PHOTO, photoList.toArray(new Photo[0]));
            snappydb.close();
        } catch (SnappydbException e) {
            e.printStackTrace();
        }
    }
}
