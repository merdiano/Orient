package com.tpsadvertising.orientnews.ui;


import android.annotation.SuppressLint;
import android.app.ActivityOptions;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;

import androidx.customview.widget.ViewDragHelper;
import androidx.lifecycle.Observer;
import androidx.paging.PagedList;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.transition.TransitionManager;

import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.mindorks.placeholderview.PlaceHolderView;

import com.tpsadvertising.orientnews.AppRater;
import com.tpsadvertising.orientnews.JobService;
import com.tpsadvertising.orientnews.MyAlarm;
import com.tpsadvertising.orientnews.R;
import com.tpsadvertising.orientnews.data.NetworkState;
import com.tpsadvertising.orientnews.data.Status;
import com.tpsadvertising.orientnews.injectors.PerActivity;
import com.tpsadvertising.orientnews.room.Category;
import com.tpsadvertising.orientnews.room.Post;

import com.tpsadvertising.orientnews.ui.adapters.PostListAdapter;
import com.tpsadvertising.orientnews.ui.views.DrawyerCategoryItem;
import com.tpsadvertising.orientnews.ui.views.DrawyerItemCallback;
import com.tpsadvertising.orientnews.ui.views.DrawyerMenuItem;
import com.tpsadvertising.orientnews.ui.views.GridItemDividerDecoration;
import com.tpsadvertising.orientnews.ui.views.HomeGridItemAnimator;
import com.tpsadvertising.orientnews.utils.NetworkUtils;
import com.tpsadvertising.orientnews.viewmodels.MainActivityViewModel;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

@PerActivity
public class MainActivity extends BaseActivity<MainActivityViewModel>
        implements DrawyerItemCallback{

    private static final String TAG = "JobService";
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.drawerView)
    PlaceHolderView mDrawyerView;
//    @BindView(android.R.id.empty)
//    ProgressBar loading;
    @BindView(R.id.grid) RecyclerView grid;
    @BindView(R.id.swipeRefresh)
    SwipeRefreshLayout swipeRefreshLayout;
    @Nullable
    @BindView(R.id.no_connection)
    ImageView noConnection;
    @BindView(R.id.favoritesText)
    TextView noFavs;
    private static final int RC_SEARCH = 0;
    private static final String KEY_SOURCE ="SOURCE_KEY";
    @Inject
    PostListAdapter adapter;
    @Inject
    RecyclerViewPreloader<Post> shotPreloader;

    @Inject
    Observable<Boolean> connectionStatus;
    private Disposable internetDisposable;
    private DrawyerMenuItem allPosts;


    public static SharedPreferences pref;
    SharedPreferences.Editor editor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setTheme(R.style.OrientTheme);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        pref = PreferenceManager.getDefaultSharedPreferences(this);



        try {
            setupDrawyer();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        editor = pref.edit();

//        loading.getIndeterminateDrawable()
//                .setColorFilter(getResources().getColor(R.color.colorAccent),
//                    PorterDuff.Mode.SRC_IN);
        adapter.setHasStableIds(true);
        grid.setAdapter(adapter);
//        grid.setHasFixedSize(true);
        grid.addItemDecoration(new GridItemDividerDecoration(this, R.dimen.divider_height, R.color.divider));
        grid.addOnScrollListener(shotPreloader);
        grid.setItemAnimator(new HomeGridItemAnimator());

//        swipeContainer.setOnRefreshListener(new SwipyRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh(SwipyRefreshLayoutDirection direction) {
//                viewModel.loadMore();
//            }
//        });
        NetworkUtils.checkGooglePlayServicesAvailable(MainActivity.this);

        viewModel.getCategories().observe(this, new Observer<List<Category>>() {
            @Override
            public void onChanged(List<Category> categories) {
                try {
                    fillDrawyer(categories);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });

        viewModel.postListOffline.observe(this, posts -> {
            adapter.submitPosts(posts);
            getLastJobId();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    grid.scrollToPosition(0);
                }
            }, 1000);

        });

        viewModel.networkState.observe(this, networkState -> {
            adapter.setNetworkState(networkState);
            int postCount = adapter.getItemCount();
            if(networkState.status == Status.FAILED){

                Snackbar.make(grid,networkState.msg,Snackbar.LENGTH_LONG).show();

                if(internetDisposable == null)
                    internetDisposable = connectionStatus.subscribe(this::connectionChanged);

                if(postCount == 1){
//                    loading.setVisibility(View.GONE);
                    if (noConnection == null) {
                        final ViewStub stub = findViewById(R.id.stub_no_connection);
                        noConnection = (ImageView) stub.inflate();
                    }
                    final AnimatedVectorDrawable avd;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        avd = (AnimatedVectorDrawable) getDrawable(R.drawable.avd_no_connection);
                        if (noConnection != null && avd != null) {
                            noConnection.setImageDrawable(avd);
                            avd.start();
                        }
                    }
                }
                else if(postCount>1){}
//                    loading.setVisibility(View.GONE);
            }
            else if(networkState == NetworkState.FAVORITE){
//                loading.setVisibility(View.GONE);
                if(postCount == 1) {

                    noFavs.setVisibility(View.VISIBLE);
                }
            }

        });

        viewModel.adverts.observe(this, ads->{
            if(ads!= null && !ads.isEmpty())
                adapter.submitAds(ads);
        });

        if(savedInstanceState != null){
            int source = savedInstanceState.getInt(KEY_SOURCE);
            viewModel.loadPosts(source);
        }
        else{
            viewModel.loadPosts(-1);
        }
        viewModel.loadAdverts();

        swipeRefreshLayout.setProgressViewOffset(false, 0,250);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> viewModel.postList.observe(MainActivity.this, posts -> {
                adapter.submitPosts(posts);

                if(posts.size() != 0)
                    swipeRefreshLayout.setRefreshing(false);
                else if(posts.size() == 0 && viewModel.currentSource()!=0 && adapter.getItemCount() ==0)
                    swipeRefreshLayout.setRefreshing(true);
                getLastJobId();

            }), 2000);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    grid.scrollToPosition(0);
                }
            }, 1000);
        });


        AppRater.app_launched(this);




        if (pref.getBoolean("firstrun", true)) {
            startBackgroundService(this);
        }

//        setAlarm();



    }


    private void setupDrawyer() throws NoSuchFieldException, IllegalAccessException {
        allPosts = new DrawyerMenuItem(getApplicationContext(),R.string.all_categories);
        allPosts.setItemCallback(MainActivity.this);
        DrawyerMenuItem favorites = new DrawyerMenuItem(getApplicationContext(),R.string.favorite_posts);
        DrawyerMenuItem settings = new DrawyerMenuItem(getApplicationContext(),R.string.action_settings);
        favorites.setItemCallback(MainActivity.this);
        settings.setItemCallback(MainActivity.this);
        mDrawyerView.addView(allPosts);
        mDrawyerView.addView(favorites);
        mDrawyerView.addView(settings);
        mDrawyerView.setHasFixedSize(true);
        mDrawyerView.addItemDecoration(new GridItemDividerDecoration(this, R.dimen.divider_height, R.color.divider));
        drawer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);




        drawer.addDrawerListener(toggle);

        Field mDragger = drawer.getClass().getDeclaredField(
                "mLeftDragger");//mRightDragger for right obviously
        mDragger.setAccessible(true);
        ViewDragHelper draggerObj = (ViewDragHelper) mDragger
                .get(drawer);

        Field mEdgeSize = draggerObj.getClass().getDeclaredField(
                "mEdgeSize");
        mEdgeSize.setAccessible(true);
        int edge = mEdgeSize.getInt(draggerObj)/2;

        mEdgeSize.setInt(draggerObj, edge * 5);
        toggle.syncState();
    }

    private void fillDrawyer(List<Category> categories) throws NoSuchFieldException, IllegalAccessException {
        if(categories == null || categories.isEmpty())return;
        mDrawyerView.removeAllViews();
        setupDrawyer();
        for(Category cat : categories){
            DrawyerCategoryItem categoryItem = new DrawyerCategoryItem(getApplicationContext(),cat);
            categoryItem.setItemCallback(MainActivity.this);
            mDrawyerView.addViewAfter(allPosts,categoryItem);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SOURCE, viewModel.currentSource());
    }

    @Override
    protected void onResume() {
        super.onResume();
        NetworkUtils.checkGooglePlayServicesAvailable(MainActivity.this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(internetDisposable != null)
            internetDisposable.dispose();
    }
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawer.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_search:
                View searchMenuView = toolbar.findViewById(R.id.menu_search);
                Bundle options = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    options = ActivityOptions.makeSceneTransitionAnimation(this, searchMenuView,
                            getString(R.string.transition_search_back)).toBundle();
                }
                startActivityForResult(new Intent(this, SearchActivity.class), RC_SEARCH, options);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void connectionChanged(boolean isConnected){
        if(isConnected) {
            runOnUiThread(() -> {
                TransitionManager.beginDelayedTransition(drawer);
                if (noConnection != null)
                    noConnection.setVisibility(View.GONE);
//                loading.setVisibility(View.VISIBLE);
                //fill drawyer if it is empty and fetch posts
                if (mDrawyerView.getViewResolverCount() == 3) {
                    viewModel.loadCategories(); //load cats somehow
                }
                viewModel.retry();
                viewModel.loadAdverts();
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SettingsActivity.SETTINGS_REQUEST_CODE){
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }


    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        try {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        catch (Exception ex){
            Log.e("Create options menu",ex.getLocalizedMessage());
        }

        ImageView refreshBtn = (ImageView) menu.findItem(R.id.menu_refresh).getActionView();
        if (refreshBtn != null) {
            refreshBtn.setImageResource(R.drawable.ic_refresh_black_24dp);

            Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotation);
            rotation.setRepeatCount(Animation.INFINITE);

            refreshBtn.setOnClickListener(view -> {
                view.startAnimation(rotation);

                new Handler().postDelayed(() -> viewModel.postList.observe(MainActivity.this, posts -> {
                    adapter.submitPosts(posts);

                    if(posts.size() != 0)
                        view.clearAnimation();
                    else if(posts.size() == 0 && viewModel.currentSource()!=0 && adapter.getItemCount() ==0)
                        view.startAnimation(rotation);
                    getLastJobId();

                }), 2000);

            });
        }
        return true;
    }


    @Override
    public void menuItemClicked(int item) {
        switch (item){
            case R.string.all_categories:
                categoryItemClicked(-1);
                break;
            case R.string.favorite_posts:
                categoryItemClicked(0);
                //viewModel.loadFavorites();
                break;
            case  R.string.action_settings:
                startActivityForResult(new Intent(MainActivity.this,SettingsActivity.class),
                        SettingsActivity.SETTINGS_REQUEST_CODE);
                break;
        }
    }

    @Override
    public void categoryItemClicked(int categoryId) {

        if(viewModel.loadPosts(categoryId)){
            grid.scrollToPosition(0);
            drawer.closeDrawers();
        }
    }

    private void setAlarm() {

        Calendar calendar = Calendar.getInstance();

        calendar.setTimeInMillis(System.currentTimeMillis());

        // if it's after or equal 6 am schedule for next day
        if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 9) {
            Log.e(TAG, "Alarm will schedule for next day!");
            calendar.add(Calendar.DAY_OF_YEAR, 1); // add, not set!
        }
        else{
            Log.e(TAG, "Alarm will schedule for today!");
        }
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        //getting the alarm manager
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        //creating a new intent specifying the broadcast receiver
        Intent i = new Intent(this, MyAlarm.class);
        i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        //creating a pending intent using the intent
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        if (calendar.before(Calendar.getInstance())){
            calendar.add(Calendar.DATE, 1);
        }

        //setting the repeating alarm that will be fired every day
        am.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);

    }

    private void startBackgroundService(Context context){

        ComponentName componentName = new ComponentName(context, JobService.class);
//        JobInfo jobInfo = new JobInfo.Builder(123, componentName)
//                .setRequiresCharging(false)
//                .setPersisted(true)
//                .setPeriodic(20 * 60 * 1000)
//                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
//                .build();
        JobInfo jobInfo;
        int delay = 720 * 60* 1000;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            jobInfo = new JobInfo.Builder(123, componentName)
                    .setMinimumLatency(delay)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build();
        } else {
            jobInfo = new JobInfo.Builder(123, componentName)
                    .setPeriodic(delay)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(true)
                    .build();
        }

        JobScheduler jobScheduler = (JobScheduler)context.getApplicationContext().getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = jobScheduler.schedule(jobInfo);

        if (resultCode == JobScheduler.RESULT_SUCCESS){
            Log.d(TAG, "Job Scheduled");
            changeFirstTime();
        }
        else {
            Log.d(TAG, "Job Scheduling failed");
        }
    }

    private void getLastJobId(){

        new Thread(new Runnable() {
            @Override
            public void run() {
                Post post = new Post();

                post = viewModel.getLastPost();

                editor.putInt("postid", post.id);
                editor.apply();
            }
        });

    }

    private void setLocale(String lang) {

        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
        editor.putString("lang", lang);
        editor.apply();

    }
    private void loadLocale() {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String lang = sharedPreferences.getString("lang", "");
        String lang = Locale.getDefault().getLanguage();
        setLocale(lang);
    }

    private void changeFirstTime(){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putBoolean("firstrun", false).apply();
    }

}
