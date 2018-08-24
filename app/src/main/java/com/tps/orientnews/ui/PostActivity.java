package com.tps.orientnews.ui;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.assist.AssistContent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import com.tps.orientnews.OrientPrefs;
import com.tps.orientnews.R;
import com.tps.orientnews.injectors.PerActivity;

import com.tps.orientnews.room.Post;
import com.tps.orientnews.room.PostDao;
import com.tps.orientnews.room.User;
import com.tps.orientnews.utils.ColorUtils;
import com.tps.orientnews.utils.ViewUtils;
import com.tps.orientnews.utils.glide.GlideApp;
import com.tps.orientnews.viewmodels.DetailActivityViewModel;

import java.text.NumberFormat;

import javax.inject.Inject;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.plaidapp.util.glide.GlideUtils;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.tps.orientnews.utils.AnimUtils.getFastOutSlowInInterpolator;

@PerActivity
public class PostActivity extends BaseActivity<DetailActivityViewModel> {

    private static final int SHARE_REQUEST_CODE = 1233;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.post_image)
    ImageView imageView;
    @BindView(R.id.shot_title)
    TextView title;
    @BindView(R.id.web_description)
    WebView webDesc;

    @Inject
    OrientPrefs orientPrefs;
    @Inject
    PostDao postDao;
    public final static String EXTRA_POST = "EXTRA_POST";
    public final static String RESULT_EXTRA_POST_ID = "RESULT_EXTRA_POST_ID";
    private static final float SCRIM_ADJUSTMENT = 0.075f;
    @BindView(R.id.shot_like_count)
    Button likeCount;
    @BindView(R.id.shot_view_count)
    Button viewCount;
    @BindView(R.id.shot_share_action)
    Button share;
    @BindView(R.id.player_name)
    TextView playerName;
    @BindView(R.id.shot_time_ago)
    TextView shotTimeAgo;
    @BindView(R.id.player_avatar)
    ImageView playerAvatar;
    @BindDimen(R.dimen.large_avatar_size) int largeAvatarSize;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout toolbarLayout;
    @BindView(R.id.post_nested_scroll)
    NestedScrollView scrollView;
    Post post;
    WebSettings settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        final Intent intent = getIntent();
        Bundle b = intent.getBundleExtra("bundle");
        int postId = b.getInt(EXTRA_POST);
        postDao.get(postId).observe(this,this::bindPost);
        initWebView();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(post !=null)
                {
                    post.isFavorite = !post.isFavorite;
                    postDao.update(post);

                    Snackbar.make(view, post.isFavorite?
                            R.string.added_to_favs : R.string.removed_from_favs,
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
                //dataManager.updatePost(post);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);


    }

    private void initWebView(){
        settings = webDesc.getSettings();
        settings.setTextZoom(orientPrefs.getFontSizePref());
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setAppCachePath(getApplicationContext().getFilesDir().getAbsolutePath() + "/cache");
//        settings.setDatabaseEnabled(true);

//        settings.setDatabasePath(getApplicationContext().getFilesDir().getAbsolutePath() + "/databases");
        //settings.setLoadWithOverviewMode(true);
        //settings.setUseWideViewPort(true);
//        webDesc.getSettings().setSupportZoom(true);
//        webDesc.getSettings().setBuiltInZoomControls(true);
//        webDesc.getSettings().setDisplayZoomControls(true);

        webDesc.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                super.onShowCustomView(view, callback);
                if (view instanceof FrameLayout){
                    FrameLayout frame = (FrameLayout) view;
                    if (frame.getFocusedChild() instanceof VideoView){
                        VideoView video = (VideoView) frame.getFocusedChild();
                        frame.removeView(video);
                        PostActivity.this.setContentView(video);
                        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                PostActivity.this.setContentView(R.layout.activity_post);
                                WebView wb = PostActivity.this.findViewById(R.id.web_description);
                                PostActivity.this.initWebView();
                            }
                        });
                        video.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            @Override
                            public boolean onError(MediaPlayer mp, int what, int extra) {
                                return false;
                            }
                        });
                        video.start();
                    }
                }
            }
        });
        webDesc.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView webView, String url) {
                super.onPageFinished(webView, url);

                String jScript = "javascript:(function() { " +
                        "var vids = document.getElementsByClassName('wp-video'); "+
                        "for( var i = 0; i < vids.length; i++ ){" +
//                            "var video = vids[i].firstchild;" +
                            "vids[i].replaceWith(vids[i].firstChild);" +
//                            "vids[i].parentNode.removeChild(vids[i]);" +
                        "}" +
                            //"vids[i].removeAttribute('width');" +
                            //"vids[i].removeAttribute('height');" +
                            //"vids[i].style.width='100%';}" +
                        "return document.body.innerHTML;})()";


                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    webView.evaluateJavascript(jScript, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            value.isEmpty();
                        }
                    });
                } else {
                    webView.loadUrl(jScript);
                }

                webView.getParent().requestLayout();
            }
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Intent intent = new Intent(Intent.ACTION_VIEW, request.getUrl());
                startActivity(intent);
                return true;
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.fontAction){
            showTextAdjustDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void bindPost(Post post) {
        if (post == null)return;
        this.post = post;
        getSupportActionBar().setTitle(post.title);
        title.setText(post.title);
        if (!TextUtils.isEmpty(post.content)) {

//            final String content = "<style type='text/css'>@font-face {font-family: MyFont;src: url(\\\"file:///android_asset/fonts/hermeneus_one_regular.ttf\\\")}body,* {font-family: MyFont; text-align: justify;} img,video {max-width: 100% important;height:initial;}</style>"
//                    +post.getContent();
//
//                webDesc.loadData(content, "text/html; charset=utf-8", "utf-8");

            nightModeSwitcher(orientPrefs.getNightModePref());
        } else {
            webDesc.setVisibility(View.GONE);
        }

        GlideApp.with(this)
                .load(post.thumbnail_images.mediumUrl)
                .listener(shotLoadListener)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .priority(Priority.IMMEDIATE)
                .override(post.thumbnail_images.mediumWidh, post.thumbnail_images.mediumHeght)
                .transition(withCrossFade())
                .into(imageView);
        NumberFormat nf = NumberFormat.getInstance();
        final Resources res = getResources();
        likeCount.setText(res.getQuantityString(R.plurals.likes,0,
                        nf.format(0)));
        viewCount.setText(
                res.getQuantityString(R.plurals.views,post.views,
                        nf.format(post.views)));
        viewCount.setOnClickListener(v -> ((AnimatedVectorDrawable) viewCount.getCompoundDrawables()[1]).start());
        share.setOnClickListener(v -> {
            ((AnimatedVectorDrawable) share.getCompoundDrawables()[1]).start();
            //new ShareOrientImageTask(PostActivity.this, post).execute();
            Intent i = ShareCompat.IntentBuilder.from(PostActivity.this)
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
    void setResultAndFinish() {
        if(post!=null){
            final Intent resultData = new Intent();
            resultData.putExtra(RESULT_EXTRA_POST_ID, post.id);
            setResult(RESULT_OK, resultData);

        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SHARE_REQUEST_CODE){
            Snackbar.make(webDesc,R.string.share_completed,Snackbar.LENGTH_SHORT)
            .show();
        }
    }

    private RequestListener<Drawable> shotLoadListener = new RequestListener<Drawable>() {
        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
                                       DataSource dataSource, boolean isFirstResource) {
            final Bitmap bitmap = GlideUtils.getBitmap(resource);
            if (bitmap == null) return false;
            final int twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    24, PostActivity.this.getResources().getDisplayMetrics());
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
                        if(orientPrefs.getNightModePref())
                            statusBarColor = Color.BLACK;

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
                                        getFastOutSlowInInterpolator(PostActivity.this));
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
    @Override
    public void onBackPressed() {
        setResultAndFinish();
    }

    @Override
    public boolean onNavigateUp() {
        setResultAndFinish();
        return true;
    }

    @Override @TargetApi(Build.VERSION_CODES.M)
    public void onProvideAssistContent(AssistContent outContent) {
        outContent.setWebUri(Uri.parse(post.url));
    }
    SeekBar fontSize,brightness;
    SwitchCompat nightmode;
    private boolean shouldPromptForPermission = true;
    private static final int PERMISSIONS_REQUEST_GET_ACCOUNTS = 0;
    @Override @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_GET_ACCOUNTS) {
            //TransitionManager.beginDelayedTransition(container);
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shouldPromptForPermission = false;
            } else {
                shouldPromptForPermission = true;
            }
        }
    }
    private void showTextAdjustDialog(){

        final Dialog dialog = new Dialog(PostActivity.this);
        dialog.setContentView(R.layout.text_adjust_dialog);
        fontSize = dialog.findViewById(R.id.font_size);
        brightness = dialog.findViewById(R.id.brightness);
        nightmode = dialog.findViewById(R.id.nightmode);
        nightmode.setChecked(orientPrefs.getNightModePref());



        fontSize.setProgress(orientPrefs.getFontSizePref());

        if (ContextCompat.checkSelfPermission(PostActivity.this, Manifest.permission.WRITE_SETTINGS) ==
                PackageManager.PERMISSION_GRANTED) {
            shouldPromptForPermission = false;
        }
        int bright =0;
        try {
            bright = android.provider.Settings.System.getInt(getContentResolver(),
                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }

        brightness.setProgress(bright);
        fontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                settings.setTextZoom(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                orientPrefs.setFontSizePref(seekBar.getProgress());
            }
        });

        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                try {
                    android.provider.Settings.System.putInt(getContentResolver(),
                            android.provider.Settings.System.SCREEN_BRIGHTNESS,
                            progress);
                }
                catch (Exception e) {
                    Log.e("Error", "Cannot access system brightness");
                    e.printStackTrace();
                }
            }

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(shouldPromptForPermission){
                    requestPermissions(new String[]{ Manifest.permission.WRITE_SETTINGS },
                            PERMISSIONS_REQUEST_GET_ACCOUNTS);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        nightmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                nightModeSwitcher(isChecked);
                orientPrefs.setNightModePref(isChecked);

            }
        });

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();

        wlp.gravity = Gravity.TOP | Gravity.RIGHT;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;

        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            int actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
            wlp.y  = wlp.y+actionBarHeight;
        }

        window.setAttributes(wlp);

//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        window.setBackgroundDrawableResource(R.drawable.ic_comment_add);
        dialog.show();

    }
    private void nightModeSwitcher(boolean isCheked){
        String htmlData;// = "<style type='text/css'>@font-face {font-family: MyFont;src: url(\\\"file:///android_asset/fonts/iranian_sans.ttf\\\")}body,* {font-family: MyFont;text-align: justify;} img,video {max-width: 100%;height:initial;}</style>";
        //String htmlData =getResources().getString(R.string.web_view_style).toString();
        if(isCheked) {
            ((ViewGroup)webDesc.getParent()).setBackgroundColor(Color.BLACK);
            webDesc.setBackgroundColor(Color.BLACK);
            scrollView.setBackgroundColor(Color.BLACK);
            htmlData = getHtmlData(post.content,"white");
            //title.setBackgroundColor(Color.BLACK);
            title.setTextColor(Color.WHITE);
//            toolbar.setBackgroundColor(Color.BLACK);
//            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
            toolbarLayout.setContentScrimColor(Color.BLACK);
            ((ViewGroup)likeCount.getParent()).setBackgroundColor(Color.BLACK);
            playerName.setTextColor(Color.WHITE);
            shotTimeAgo.setTextColor(Color.WHITE);
        }
        else
        {
            ((ViewGroup)webDesc.getParent()).setBackgroundColor(getResources().getColor(R.color.light_grey));
            webDesc.setBackgroundColor(getResources().getColor(R.color.light_grey));
            scrollView.setBackgroundColor(getResources().getColor(R.color.light_grey));
            htmlData = getHtmlData(post.content,"black");
            //title.setBackgroundColor(getResources().getColor(R.color.light_grey));
            title.setTextColor(getResources().getColor(R.color.text_primary_dark));
//            toolbar.setBackgroundColor(Color.WHITE);
//            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black);

            ((ViewGroup)likeCount.getParent()).setBackgroundColor(getResources().getColor(R.color.light_grey));
            playerName.setTextColor(getResources().getColor(R.color.inline_action_icon));
            shotTimeAgo.setTextColor(getResources().getColor(R.color.hint_disabled_dark));
            toolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimaryDarkTransparent));
        }
        webDesc.loadData(htmlData, "text/html; charset=utf-8", "utf-8");
        likeCount.setTextColor(getResources().getColor(R.color.inline_action_icon));
        viewCount.setTextColor(getResources().getColor(R.color.inline_action_icon));
        share.setTextColor(getResources().getColor(R.color.inline_action_icon));
    }

    private String getHtmlData(String bodyHTML,String fontColor) {
        String head = "<head><style type='text/css'>@font-face {font-family: MyFont;src: url(\\\"file:///fonts/hermeneus_one_regular.ttf\\\")}body,* {font-family: MyFont;text-align: justify;} img,video {max-width: 100%;height:initial;}</style></head>";
        return "<html>" + head + "<body><font color='"+fontColor+"'>" + bodyHTML + "</font></body></html>";
    }
}
