package com.ess.similarimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoActivity;
import com.jph.takephoto.model.TImage;
import com.jph.takephoto.model.TResult;

import java.io.File;
import java.util.List;
import java.util.UUID;

public class MainActivity extends TakePhotoActivity implements View.OnClickListener {

    SwipeRefreshLayout swipeRefresh;
    RecyclerView rvPhoto;

    TakePhoto takePhoto;
    Bitmap bitmap;
    File folder = new File(Environment.getExternalStorageDirectory(),"essSimilarTest");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        takePhoto = getTakePhoto();

//        Bitmap original1 = BitmapFactory.decodeResource(getResources(), R.drawable.test);
//        Bitmap original2 = BitmapFactory.decodeResource(getResources(), R.drawable.test3);
//        Log.i("rrr",""+SimilarUtils.compare(original1, original2));

        initSwipeRefresh();
        initRecyclerView();
//        executeTask();

    }

    private void initSwipeRefresh() {
        swipeRefresh = findViewById(R.id.swipe_refresh);
        swipeRefresh.setEnabled(false);
    }

    private void initRecyclerView() {
        rvPhoto = findViewById(R.id.rv_photo);
        rvPhoto.setLayoutManager(new GridLayoutManager(this, 2));
//        rvPhoto.setAdapter(new RecyclerPhotoAdapter(Photo.getPhotoList(this)));
    }


    @Override
    public void takeSuccess(TResult result) {
        super.takeSuccess(result);
        Log.i("rrr", "takeSuccess");
        TImage image = result.getImage();
        String path = image.getOriginalPath();
        File file = new File(path);
//        if (file.getParentFile().equals(getExternalFilesDir(null))) {
        if (file.getParentFile().equals(folder)) {
            BitmapUtils.insertToMediaStore(this, file);
        }
        bitmap = BitmapFactory.decodeFile(path);
        executeTask();
    }

    @Override
    public void takeFail(TResult result, String msg) {
        super.takeFail(result, msg);
        Log.i("rrr", "takeFail");
    }

    @Override
    public void takeCancel() {
        super.takeCancel();
        Log.i("rrr", "takeCancel");
    }

    private void executeTask() {
        new AsyncTask<Void, Void, List<Photo>>() {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                swipeRefresh.setRefreshing(true);
            }

            @Override
            protected List<Photo> doInBackground(Void... voids) {
                long a = System.currentTimeMillis();
//                List<Photo> list = SimilarUtils.findSimilarPhotos(MainActivity.this, BitmapFactory.decodeResource(getResources(), R.drawable.test));
                List<Photo> list = SimilarUtils.findSimilarPhotos(MainActivity.this, bitmap);
                Log.i("rrr", "time " + (System.currentTimeMillis() - a));
                return list;
            }

            @Override
            protected void onPostExecute(List<Photo> photoList) {
                super.onPostExecute(photoList);
                swipeRefresh.setRefreshing(false);
                rvPhoto.setAdapter(new RecyclerPhotoAdapter(photoList));
            }
        }.execute();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_camera:
//                File file = new File(getExternalFilesDir(null), UUID.randomUUID() + ".jpg");
                File file = new File(folder, UUID.randomUUID() + ".jpg");
                takePhoto.onPickFromCapture(Uri.fromFile(file));
                break;

            case R.id.btn_gallery:
                takePhoto.onPickFromGallery();
                break;
        }
    }
}
