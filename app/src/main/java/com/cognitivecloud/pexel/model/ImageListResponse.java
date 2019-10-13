package com.cognitivecloud.pexel.model;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.library.baseAdapters.BR;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.cognitivecloud.pexel.R;
import com.google.gson.annotations.SerializedName;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.List;

public class ImageListResponse extends BaseObservable {

    public ImageListResponse(){}

    @SerializedName("photos")
    private List<Images> images;

    @Bindable
    public List<Images> getImages() {
        return images;
    }

    public void setImages(List<Images> images) {
        this.images = images;
        notifyPropertyChanged(BR.images);
    }

    public static class Images extends BaseObservable{
        public Images(){}

        @SerializedName("src")
        private OriginalImage imageObject;

        @Bindable
        public OriginalImage getImageObject() {
            return imageObject;
        }


        public void setImageObject(OriginalImage imageObject) {
            this.imageObject = imageObject;
            notifyPropertyChanged(BR.imageObject);
        }

        public static class OriginalImage extends BaseObservable{
            @Bindable
            public String getImage() {
                return image;
            }

            public void setImage(String image) {
                this.image = image;
                notifyPropertyChanged(BR.image);
            }

            @SerializedName("original")
            private String image;


            @BindingAdapter({"bind:url", "bind:spinner"})
            public static void loadImage(ImageView view, String url, AVLoadingIndicatorView spinner)
            {
                spinner.show();
                spinner.setVisibility(View.VISIBLE);
                Glide.with(view.getContext())
                        .load(url)
                        .thumbnail(0.1f)
                        .apply(new RequestOptions().error(R.drawable.no_image))
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                spinner.hide();
                                spinner.setVisibility(View.GONE);
                                view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                spinner.hide();
                                spinner.setVisibility(View.GONE);
                                return false;
                            }
                        })
                        .into(view);
            }
        }
    }
}
