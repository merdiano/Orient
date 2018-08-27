package com.tps.orientnews.ui;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spannable;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.tps.orientnews.R;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.room.Post;
import com.tps.orientnews.room.User;
import com.tps.orientnews.ui.widgets.GlideImageGetter;
import com.tps.orientnews.utils.ColorUtils;
import com.tps.orientnews.utils.ViewUtils;
import com.tps.orientnews.utils.glide.GlideApp;
import com.tps.orientnews.viewmodels.DetailActivityViewModel;

import java.text.NumberFormat;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.plaidapp.util.glide.GlideUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.tps.orientnews.utils.AnimUtils.getFastOutSlowInInterpolator;

/**
 * Created by merdan on 8/22/18.
 */
@PerActivity
public class DetailActivity extends BaseActivity<DetailActivityViewModel>{
    private static final int SHARE_REQUEST_CODE = 1233;
    public final static String EXTRA_POST = "EXTRA_POST";
    public final static String RESULT_EXTRA_POST_ID = "RESULT_EXTRA_POST_ID";
    private static final float SCRIM_ADJUSTMENT = 0.075f;

    @BindView(R.id.shot_title) TextView title;
    @BindView(R.id.post_image) ImageView imageView;
    @BindView(R.id.post_content) TextView postContent;
    @BindView(R.id.shot_view_count) Button viewCount;
    @BindView(R.id.shot_share_action) Button share;
    @BindView(R.id.player_name) TextView playerName;
    @BindView(R.id.shot_time_ago) TextView shotTimeAgo;
    @BindView(R.id.player_avatar) ImageView playerAvatar;
    @BindDimen(R.dimen.large_avatar_size) int largeAvatarSize;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(android.R.id.empty)ProgressBar loading;
    private Post post;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        if(shPrefs.contains(SettingsFragment.KEY_FONT_SIZE)){
            String name = shPrefs.getString(SettingsFragment.KEY_FONT_SIZE,"medium_text");
            int cid = getResources().getIdentifier(name, "dimen", getPackageName());
            int tid = getResources().getIdentifier(name+"_title_content", "dimen", getPackageName());

            postContent.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(cid));
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX,getResources().getDimension(tid));
        }

        int postId = getIntent().getIntExtra(EXTRA_POST,0);
        loading.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.colorAccent),
                        PorterDuff.Mode.SRC_IN);
        loading.setVisibility(View.VISIBLE);
        viewModel.post.observe(this,this::bindPost);
        viewModel.loadPost(postId);

    }

    @OnClick(R.id.fab)
    void fabClicked(View view){

        if(post == null) return;

        Snackbar.make(view, viewModel.addToFavorite()?
                        R.string.added_to_favs : R.string.removed_from_favs,
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @OnClick(R.id.shot_view_count)
    void viewClicked(View view){
        ((AnimatedVectorDrawable) viewCount.getCompoundDrawables()[1])
                .start();
    }
    @OnClick(R.id.shot_share_action)
    void shareClicked(View v){
        if(post == null) return;
        ((AnimatedVectorDrawable) share.getCompoundDrawables()[1]).start();
        //new ShareOrientImageTask(PostActivity.this, post).execute();
        Intent i = ShareCompat.IntentBuilder.from(DetailActivity.this)
                .setType("text/plain")
                .setChooserTitle(post.title)
                .setText(post.url)
                .createChooserIntent();

        startActivityForResult(i,SHARE_REQUEST_CODE);
    }

    private void bindPost(Post post){
        if(post == null) return;
        this.post = post;
        loading.setVisibility(View.GONE);
//        getSupportActionBar().setTitle(post.title);
//        ctLayout.setTitle(post.title);
        title.setText(post.title);

        Spannable html = ViewUtils.getSpannableHtmlWithImageGetter(postContent,post.content);
        ViewUtils.setClickListenerOnHtmlImageGetter(html, new ViewUtils.Callback() {
            @Override
            public void onImageClick(String imageUrl) {
                showAlertDialogWithImage(imageUrl);
            }
        });

        postContent.setText(html);
        postContent.setMovementMethod(LinkMovementMethod.getInstance());

        if(post.thumbnail_images != null){
            GlideApp.with(this)
                    .load(post.thumbnail_images.mediumUrl)
                    .listener(shotLoadListener)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .priority(Priority.IMMEDIATE)
                    .override(post.thumbnail_images.mediumWidh, post.thumbnail_images.mediumHeght)
                    .transition(withCrossFade())
                    .into(imageView);
        }

        NumberFormat nf = NumberFormat.getInstance();
        final Resources res = getResources();
        viewCount.setText(
                res.getQuantityString(
                        R.plurals.views,
                        post.views,
                        nf.format(post.views)));
//        viewCount.setOnClickListener(v ->
//                ((AnimatedVectorDrawable) viewCount.getCompoundDrawables()[1])
//                        .start());
//        share.setOnClickListener(v -> {
//            ((AnimatedVectorDrawable) share.getCompoundDrawables()[1]).start();
//            //new ShareOrientImageTask(PostActivity.this, post).execute();
//            Intent i = ShareCompat.IntentBuilder.from(DetailActivity.this)
//                    .setType("text/plain")
//                    .setChooserTitle(post.title)
//                    .setText(post.url)
//                    .createChooserIntent();
//
//            startActivityForResult(i,SHARE_REQUEST_CODE);
//        });
        User author = post.author;
        if (author!= null) {
            playerName.setText(author.fullName());
            GlideApp.with(this)
                    .load(author.url)
                    .circleCrop()
                    .placeholder(R.drawable.avatar_placeholder)
                    .override(largeAvatarSize, largeAvatarSize)
                    .transition(withCrossFade())
                    .into(playerAvatar);

            if (post.date != null) {
                shotTimeAgo.setText(DateUtils.getRelativeTimeSpanString(post.date.getTime(),
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS).toString().toLowerCase());
            }
        } else {
            playerName.setVisibility(View.GONE);
            playerAvatar.setVisibility(View.GONE);
            shotTimeAgo.setVisibility(View.GONE);
        }
    }

    private void showAlertDialogWithImage(String source){
        final Dialog d = new Dialog(this,android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        d.setContentView(R.layout.fullscreen_image);
        PhotoView img = d.findViewById(R.id.full_image);
        GlideApp.with(this)
                .load(source)
//                .circleCrop()
//                .placeholder(R.drawable.avatar_placeholder)
//                .override(largeAvatarSize, largeAvatarSize)
                .transition(withCrossFade())
                .into(img);
        d.show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SHARE_REQUEST_CODE){
            Snackbar.make(postContent,R.string.share_completed,Snackbar.LENGTH_SHORT)
                    .show();
        }
    }
    @Override
    public void onBackPressed() {
        setResultAndFinish();
    }

    @Override
    public boolean onNavigateUp() {
        setResultAndFinish();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                setResultAndFinish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void setResultAndFinish() {
        if(viewModel.post!=null){
            final Intent resultData = new Intent();
            try{
                resultData.putExtra(RESULT_EXTRA_POST_ID, viewModel.post.getValue().id);
                setResult(RESULT_OK, resultData);

            }
            catch (Exception ex){
                setResult(RESULT_CANCELED);
            }

        }
        finish();
    }

    private RequestListener<Drawable> shotLoadListener = new RequestListener<Drawable>() {
        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                       DataSource dataSource, boolean isFirstResource) {
            final Bitmap bitmap = GlideUtils.getBitmap(resource);
            if (bitmap == null) return false;
            final int twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    24, DetailActivity.this.getResources().getDisplayMetrics());
            Palette.from(bitmap)
                    .maximumColorCount(3)
                    .clearFilters() /* by default palette ignore certain hues
                        (e.g. pure black/white) but we don't want this. */
                    .setRegion(0, 0, bitmap.getWidth() - 1, twentyFourDip) /* - 1 to work around
                        https://code.google.com/p/android/issues/detail?id=191013 */
                    .generate(palette -> {
                        boolean isDark;
                        @ColorUtils.Lightness int lightness = ColorUtils.isDark(palette);
                        if (lightness == ColorUtils.LIGHTNESS_UNKNOWN) {
                            isDark = ColorUtils.isDark(bitmap, bitmap.getWidth() / 2, 0);
                        } else {
                            isDark = lightness == ColorUtils.IS_DARK;
                        }

                        if (!isDark) { // make back icon dark on light thumbnail_images
//                            back.setColorFilter(ContextCompat.getColor(
//                                    OrientNewsActivity.this, R.color.dark_icon));
                            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black);
                            try {
                                MenuItem menuItem = toolbar.getMenu().getItem(0);
                                menuItem.setIcon(R.drawable.ic_font_solid);
                            }
                            catch (Exception ex){}
                        }

                        // color the status bar. Set a complementary dark color on L,
                        // light or dark color on M (with matching status bar icons)
                        int statusBarColor = 0;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            statusBarColor = getWindow().getStatusBarColor();
                        }
                        final Palette.Swatch topColor =
                                ColorUtils.getMostPopulousSwatch(palette);
                        if (topColor != null &&
                                (isDark || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
                            statusBarColor = ColorUtils.scrimify(topColor.getRgb(),
                                    isDark, SCRIM_ADJUSTMENT);
                            // set a light status bar on M+
                            if (!isDark && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                ViewUtils.setLightStatusBar(imageView);
                            }
                        }

//                        if(orientPrefs.getNightModePref()) todo night mode
//                            statusBarColor = Color.BLACK;

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (statusBarColor != getWindow().getStatusBarColor()) {
//                                imageView.setScrimColor(statusBarColor);
//                                toolbarLayout.setContentScrimColor(statusBarColor);
//                                getSupportActionBar().setBackgroundDrawable(new ColorDrawable(statusBarColor));

                                ValueAnimator statusBarColorAnim = ValueAnimator.ofArgb(
                                        getWindow().getStatusBarColor(), statusBarColor);
                                statusBarColorAnim.addUpdateListener(animation -> getWindow().setStatusBarColor(
                                        (int) animation.getAnimatedValue()));
                                statusBarColorAnim.setDuration(1000L);
                                statusBarColorAnim.setInterpolator(
                                        getFastOutSlowInInterpolator(DetailActivity.this));
                                statusBarColorAnim.start();
                            }
                        }
                    });



            // TODO should keep the background if the image contains transparency?!
//            imageView.setBackground(null);
            return false;
        }
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                    Target<Drawable> target, boolean isFirstResource) {
            return false;
        }
    };
}
