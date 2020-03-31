package com.tpsadvertising.orientnews.ui;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.core.app.ShareCompat;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Observer;
import androidx.palette.graphics.Palette;
import androidx.appcompat.widget.Toolbar;

import android.text.Spannable;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.r0adkll.slidr.Slidr;
import com.tpsadvertising.orientnews.R;
import com.tpsadvertising.orientnews.injectors.PerActivity;
import com.tpsadvertising.orientnews.room.Post;
import com.tpsadvertising.orientnews.room.User;
import com.tpsadvertising.orientnews.utils.ColorUtils;
import com.tpsadvertising.orientnews.utils.ViewUtils;
import com.tpsadvertising.orientnews.utils.glide.GlideApp;
import com.tpsadvertising.orientnews.viewmodels.DetailActivityViewModel;

import java.text.NumberFormat;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.plaidapp.util.glide.GlideUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.tpsadvertising.orientnews.ui.SettingsFragment.KEY_NIGHT_MODE;
import static com.tpsadvertising.orientnews.utils.AnimUtils.getFastOutSlowInInterpolator;

/**
 * Created by merdan on 8/22/18.
 */
@PerActivity
public class DetailActivity extends BaseActivity<DetailActivityViewModel> {
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
    @BindView(R.id.fab)
    FloatingActionButton fab;


    float x1, x2, y1, y2;

    int postId;

    //    private Post post;
    @SuppressLint("ClickableViewAccessibility")
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

        loading.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.colorAccent),
                        PorterDuff.Mode.SRC_IN);
        loading.setVisibility(View.VISIBLE);

        viewModel.post.observe(this,this::bindPost);

        viewModel.prevPost.observe(this,it -> {
//            MenuItem menuItem = toolbar.getMenu().getItem(0);
//            if(it != null)
//                menuItem.setEnabled(true);
//            else
//                menuItem.setEnabled(false);
        });
        viewModel.nextPost.observe(this,it -> {
//            MenuItem menuItem = toolbar.getMenu().getItem(1);
//            if(it != null){
//                menuItem.setEnabled(true);
//            }
//            else
//                menuItem.setEnabled(false);

        });

        postId = getIntent().getIntExtra(EXTRA_POST,0);

        viewModel.checkPostFavorite(postId).observe(this, new Observer<Post>() {
            @Override
            public void onChanged(Post post) {
                boolean fav = post.isFavorite;

                if (fav){
                    fab.setImageDrawable(getDrawable(R.drawable.ic_bookmark_black_24dp));
                }else if (!fav){
                    fab.setImageDrawable(getDrawable(R.drawable.ic_bookmark_border_black_24dp));
                }else {
                    fab.setImageDrawable(getDrawable(R.drawable.ic_bookmark_border_black_24dp));
                }
            }
        });

        Slidr.attach(this);

    }


    public boolean onTouchEvent(MotionEvent touchEvent){
        switch(touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1 = touchEvent.getX();
                y1 = touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2 = touchEvent.getX();
                y2 = touchEvent.getY();
                if(x1 < x2){
                    Log.d("TAG", "onTouchEvent: swipe left");
            }else if(x1 >  x2) {
                    Log.d("TAG", "onTouchEvent: swipe right");
                }
            break;
        }
        return false;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
//        int postId = intent.getIntExtra(EXTRA_POST,0);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        postId = getIntent().getIntExtra(EXTRA_POST,0);
        viewModel.loadPost(postId);
    }

    @OnClick(R.id.fab)
    void fabClicked(View view){

//        if(post == null) return;

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
//    @OnClick(R.id.shot_share_action)
//    void shareClicked(View v){
////        if(post == null) return;
//        ((AnimatedVectorDrawable) share.getCompoundDrawables()[1]).start();
//        //new ShareOrientImageTask(PostActivity.this, post).execute();
//        Intent i = ShareCompat.IntentBuilder.from(DetailActivity.this)
//                .setType("text/plain")
//                .setChooserTitle(title.getText())
//                .setText(postContent.getText())
//                .createChooserIntent();
//
//        startActivityForResult(i,SHARE_REQUEST_CODE);
//    }

    private void bindPost(Post post){
        if(post == null) return;
//        this.post = post;
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

        if(post.thumbnail_images != null && post.thumbnail_images.mediumUrl != null
                 && !post.thumbnail_images.mediumUrl.isEmpty()){
            GlideApp.with(this)
                    .load(post.thumbnail_images.mediumUrl)
                    .listener(shotLoadListener)
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .priority(Priority.IMMEDIATE)
                    .override(post.thumbnail_images.mediumWidh, post.thumbnail_images.mediumHeght)
                    .transition(withCrossFade())
                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlertDialogWithImage(post.thumbnail_images.mediumUrl);
                }
            });
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
        share.setOnClickListener(v -> {
            ((AnimatedVectorDrawable) share.getCompoundDrawables()[1]).start();
            //new ShareOrientImageTask(PostActivity.this, post).execute();
            Intent i = ShareCompat.IntentBuilder.from(DetailActivity.this)
                    .setType("text/plain")
                    .setChooserTitle(post.title)
                    .setText(post.url)
                    .createChooserIntent();

            startActivityForResult(i,SHARE_REQUEST_CODE);
        });
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
    public boolean onPrepareOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_menu, menu);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
            case R.id.navigateUp:
                viewModel.loadBefore();
                return true;
            case R.id.navigateDown:
                viewModel.loadNext();
                return true;
//            case R.id.day_night:
//                switchViewmode();
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void switchViewmode(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        boolean nMode = preferences.getBoolean(KEY_NIGHT_MODE,false);
        editor.putBoolean(KEY_NIGHT_MODE,!nMode);
        editor.apply();

        AppCompatDelegate.setDefaultNightMode(!nMode?
                AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
        recreate();

//            if(preferences.getBoolean(KEY_NIGHT_MODE,false))
//            {
//
//            }
//            else
//            {
//
//            }
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
                                menuIconColor(menuItem,Color.BLACK);
                                MenuItem menuItem2 = toolbar.getMenu().getItem(1);
                                menuIconColor(menuItem2,Color.BLACK);
//                                menuItem.setIcon(R.drawable.ic_font_solid);
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
    public void menuIconColor(MenuItem menuItem, int color) {
        Drawable drawable = menuItem.getIcon();
        if (drawable != null) {
            drawable.mutate();
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }


}
