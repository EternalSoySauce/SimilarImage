package com.ess.similarimage;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

public class SimilarUtils {

    public static int compare(Bitmap original1, Bitmap original2) {
        Bitmap bmp1 = convertGreyImg(narrowBitmap(original1));
        Bitmap bmp2 = convertGreyImg(narrowBitmap(original2));
        String s1 = getBinary(bmp1, getAvg(bmp1));
        String s2 = getBinary(bmp2, getAvg(bmp2));

        char[] s1s = s1.toCharArray();
        char[] s2s = s2.toCharArray();
        int diffNum = 0;
        for (int i = 0; i < s1s.length; i++) {
            if (s1s[i] != s2s[i]) {
                diffNum++;
            }
        }
        return diffNum;
    }

    private static Bitmap narrowBitmap(Bitmap original) {
        Bitmap bitmap8 = ThumbnailUtils.extractThumbnail(original, 8, 8);
        original.recycle();
        return bitmap8;
    }

    private static Bitmap convertGreyImg(Bitmap bmp) {
        int width = bmp.getWidth();         //获取位图的宽
        int height = bmp.getHeight();       //获取位图的高

        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组

        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int alpha = 0xFF << 24;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int original = pixels[width * i + j];
                int red = ((original & 0x00FF0000) >> 16);
                int green = ((original & 0x0000FF00) >> 8);
                int blue = (original & 0x000000FF);

                int grey = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
                grey = alpha | (grey << 16) | (grey << 8) | grey;
                pixels[width * i + j] = grey;
            }
        }
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
    }

    private static int getAvg(Bitmap bmp) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);

        int avgPixel = 0;
        for (int pixel : pixels) {
            avgPixel += pixel;
        }
        return avgPixel / pixels.length;
    }

    private static String getBinary(Bitmap bmp, int average) {
        StringBuilder sb = new StringBuilder();

        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];

        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        bmp.recycle();
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int original = pixels[width * i + j];
                if (original >= average) {
                    pixels[width * i + j] = 1;
                } else {
                    pixels[width * i + j] = 0;
                }
                sb.append(pixels[width * i + j]);
            }
        }
        return sb.toString();
    }

    /************************************************************************************/
    public static List<Photo> findSimilarPhotos(Context context, Bitmap bmpOri) {
        long oriFinger = calculateFingerPrint(bmpOri);

        List<Photo> dbList = Database.queryPhotos(context);
        List<Photo> photoList = Photo.getPhotoList(context);
        Iterator<Photo> iterator = photoList.iterator();
        while (iterator.hasNext()) {
            Photo photo = iterator.next();
            long finger;
            int index = dbList.indexOf(photo);
            if (index == -1) {
//            Bitmap bmp = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), photo.id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                Bitmap bmp = BitmapFactory.decodeFile(photo.path);
                finger = calculateFingerPrint(bmp);
                photo.finger = finger;
            } else {
                Photo data = dbList.get(index);
                photo.finger = data.finger;
                finger = photo.finger;
            }
            int dist = hamDist(oriFinger, finger);
            Log.i("rrr", "fi " + dist);
            photo.dist = dist;
//            if (dist > 5) {
//                iterator.remove();
//            }
        }
        Database.updatePhotos(context, photoList);
        return photoList;
    }

    private static long calculateFingerPrint(Bitmap bitmap) {
        Bitmap bitmap8 = ThumbnailUtils.extractThumbnail(bitmap, 8, 8);
        long print = getFingerPrint(bitmap8);
        bitmap.recycle();
        bitmap8.recycle();
        return print;
    }

    private static long getFingerPrint(Bitmap bitmap) {
        double[][] grayPixels = getGrayPixels(bitmap);
        double grayAvg = getGrayAvg(grayPixels);
        return getFingerPrint(grayPixels, grayAvg);
    }

    private static long getFingerPrint(double[][] pixels, double avg) {
        int width = pixels[0].length;
        int height = pixels.length;

        byte[] bytes = new byte[height * width];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (pixels[i][j] >= avg) {
                    bytes[i * height + j] = 1;
                } else {
                    bytes[i * height + j] = 0;
                }
            }
        }

        long fingerprint1 = 0;
        long fingerprint2 = 0;
        for (int i = 0; i < 64; i++) {
            if (i < 32) {
                fingerprint1 += (bytes[63 - i] << i);
            } else {
                fingerprint2 += (bytes[63 - i] << (i - 31));
            }
        }

        return (fingerprint2 << 32) + fingerprint1;
    }

    private static double getGrayAvg(double[][] pixels) {
        int width = pixels[0].length;
        int height = pixels.length;
        int count = 0;
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                count += pixels[i][j];
            }
        }
        return count / (width * height);
    }


    private static double[][] getGrayPixels(Bitmap bitmap) {
        int width = 8;
        int height = 8;
        double[][] pixels = new double[height][width];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixels[i][j] = computeGrayValue(bitmap.getPixel(i, j));
            }
        }
        return pixels;
    }

    private static double computeGrayValue(int pixel) {
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = (pixel) & 255;
        return 0.3 * red + 0.59 * green + 0.11 * blue;
    }

    private static int hamDist(long finger1, long finger2) {
        int dist = 0;
        long result = finger1 ^ finger2;
        while (result != 0) {
            ++dist;
            result &= result - 1;
        }
        return dist;
    }
}
