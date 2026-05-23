package com.example.project_mobile.ui.common;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.project_mobile.R;

public final class EvImageLoader {
    private EvImageLoader() {}

    private static final RequestOptions EV_OPTIONS = new RequestOptions()
            .placeholder(R.drawable.ic_car_charging)
            .error(R.drawable.ic_car_charging)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate();

    private static final RequestOptions REMOTE_OPTIONS = new RequestOptions()
            .placeholder(R.drawable.ic_car_charging)
            .error(R.drawable.ic_car_charging)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .dontAnimate();

    public static void load(ImageView imageView, String imageUrl) {
        if (imageView == null) {
            return;
        }
        String source = assetSourceFor(imageUrl);
        Glide.with(imageView.getContext())
                .load(source != null ? source : imageUrl)
                .apply(EV_OPTIONS)
                .thumbnail(0.25f)
                .into(imageView);
    }

    public static void loadRemote(ImageView imageView, String imageUrl) {
        if (imageView == null) {
            return;
        }
        Glide.with(imageView.getContext())
                .load(imageUrl)
                .apply(REMOTE_OPTIONS)
                .thumbnail(0.25f)
                .into(imageView);
    }

    public static void preload(ImageView imageView, String imageUrl) {
        if (imageView == null || imageUrl == null || imageUrl.trim().isEmpty()) {
            return;
        }
        String source = assetSourceFor(imageUrl);
        Glide.with(imageView.getContext())
                .load(source != null ? source : imageUrl)
                .apply(EV_OPTIONS)
                .preload();
    }

    public static String assetSourceFor(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return null;
        }
        int slash = imageUrl.lastIndexOf('/');
        String fileName = slash >= 0 ? imageUrl.substring(slash + 1) : imageUrl;
        if (fileName.isEmpty() || !fileName.toLowerCase(java.util.Locale.US).endsWith(".png")) {
            return null;
        }
        return "file:///android_asset/ev_images/" + fileName;
    }
}
