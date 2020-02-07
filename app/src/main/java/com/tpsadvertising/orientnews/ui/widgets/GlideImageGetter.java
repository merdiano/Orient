package com.tpsadvertising.orientnews.ui.widgets;

/**
 * Created by merdan on 8/22/18.
 */

import java.util.ArrayList;

import com.bumptech.glide.Glide;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.tpsadvertising.orientnews.utils.glide.GlideApp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Html.ImageGetter;
import android.widget.TextView;

public class GlideImageGetter implements ImageGetter{

    ArrayList<Target> targets;
    final TextView mTextView;

    Context mContext;
    public GlideImageGetter(Context ctx , TextView tv){
        this.mTextView = tv;
        this.mContext = ctx;
        this.targets = new ArrayList<>();
    }

    @Override
    public Drawable getDrawable(String source) {
        final UrlDrawable urlDrawable = new UrlDrawable();
        final Target target = new BitmapTarget(urlDrawable);
        targets.add(target);
        GlideApp.with(mContext)
                .load(source)
                .centerCrop()
                .fitCenter()
                .into(target);
        return urlDrawable;
    }

    private class BitmapTarget extends SimpleTarget<BitmapDrawable> {

//        Drawable drawable;
        private final UrlDrawable urlDrawable;
        public BitmapTarget(UrlDrawable urlDrawable) {
            this.urlDrawable = urlDrawable;
        }

        @Override
        public void onResourceReady(@NonNull BitmapDrawable resource,
                                    @Nullable Transition<? super BitmapDrawable> transition) {
            //drawable = new BitmapDrawable(mContext.getResources(), resource);

            mTextView.post(new Runnable() {
                @Override
                public void run() {
                    int w = mTextView.getWidth();
                    int hh=resource.getIntrinsicHeight();
                    int ww=resource.getIntrinsicWidth() ;
                    int newHeight = hh * ( w  )/ww;
                    Rect rect = new Rect( 0 , 0 , w  ,newHeight);
                    resource.setBounds(rect);
                    urlDrawable.setBounds(rect);
                    urlDrawable.setDrawable(resource);
                    mTextView.setText(mTextView.getText());
                    mTextView.invalidate();
                }
            });
        }
    }

    class UrlDrawable extends BitmapDrawable{
        private Drawable drawable;

        @SuppressWarnings("deprecation")
        public UrlDrawable() {
        }
        @Override
        public void draw(Canvas canvas) {
            if (drawable != null)
                drawable.draw(canvas);
        }
        public Drawable getDrawable() {
            return drawable;
        }
        public void setDrawable(Drawable drawable) {
            this.drawable = drawable;
        }
    }

}
