package com.ess.similarimage;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

public class RecyclerPhotoAdapter extends BaseQuickAdapter<Photo, BaseViewHolder> {

    public RecyclerPhotoAdapter(@Nullable List<Photo> photoList) {
        super(R.layout.recycler_item_photo, photoList);
    }

    @Override
    protected void convert(BaseViewHolder holder, Photo photo) {
        GlideApp.with(mContext)
                .load(photo.path)
                .into((ImageView) holder.getView(R.id.iv_photo));

        holder.setText(R.id.tv_dist, String.valueOf(photo.dist));
    }
}
