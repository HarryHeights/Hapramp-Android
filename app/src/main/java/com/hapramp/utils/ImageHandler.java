package com.hapramp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.hapramp.R;


/**
 * Created by Ankit on 12/17/2017.
 */

public class ImageHandler {

  public static void load(Context context, ImageView target, String _uri) {
    Glide.with(context)
      .load(_uri)
      .diskCacheStrategy(DiskCacheStrategy.RESULT)
      .listener(new RequestListener<String, GlideDrawable>() {
        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
          return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
          GlideDrawableImageViewTarget glideTarget = (GlideDrawableImageViewTarget) target;
          ImageView iv = glideTarget.getView();
          int width = iv.getMeasuredWidth();
          int targetHeight = width * resource.getIntrinsicHeight() / resource.getIntrinsicWidth();
          if (iv.getLayoutParams().height != targetHeight) {
            iv.getLayoutParams().height = targetHeight;
            iv.requestLayout();
          }
          return false;
        }
      })
      .skipMemoryCache(true)
      .into(target);
  }

  public static void loadSmaller(Context context, ImageView imageView, String _uri) {
    try {
      Glide.with(context)
        .load(_uri)
        .skipMemoryCache(true)
        .override(PixelUtils.dpToPx(72), PixelUtils.dpToPx(72))
        .centerCrop()
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(imageView);

    }
    catch (IllegalArgumentException e) {
    }
  }

  public static void loadCircularImage(final Context context, final ImageView imageView, String url) {
    try {
      Glide.with(context)
        .load(url)
        .asBitmap()
        .placeholder(R.drawable.profile)
        .centerCrop()
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.RESULT)
        .into(new BitmapImageViewTarget(imageView) {
          @Override
          protected void setResource(Bitmap resource) {
            RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
            circularBitmapDrawable.setCircular(true);
            imageView.setImageDrawable(circularBitmapDrawable);
          }
        });
    }
    catch (IllegalArgumentException e) {
    }
  }
}
