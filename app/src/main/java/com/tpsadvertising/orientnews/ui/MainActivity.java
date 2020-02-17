package com.tpsadvertising.orientnews.ui;


import android.annotation.SuppressLint;
import android.app.ActivityOptions;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;

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
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.mindorks.placeholderview.PlaceHolderView;

import com.tpsadvertising.orientnews.AppRater;
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
import java.util.List;

import javax.inject.Inject;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

@PerActivity
public class MainActivity extends BaseActivity<MainActivityViewModel>
        implements DrawyerItemCallback{

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.OrientTheme);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setupDrawyer();

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

        viewModel.getCategories().observe(this,this::fillDrawyer);

        viewModel.postListOffline.observe(this, posts -> adapter.submitPosts(posts));

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

        swipeRefreshLayout.setProgressViewOffset(false, 0,200);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            new Handler().postDelayed(() -> viewModel.postList.observe(MainActivity.this, posts -> {
                adapter.submitPosts(posts);

                if(posts.size() != 0)
                    swipeRefreshLayout.setRefreshing(false);
                else if(posts.size() == 0 && viewModel.currentSource()!=0 && adapter.getItemCount() ==0)
                    swipeRefreshLayout.setRefreshing(true);
            }), 2000);
            grid.scrollToPosition(0);
        });


        AppRater.app_launched(this);

    }

    private void setupDrawyer(){
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
        toggle.syncState();
    }

    private void fillDrawyer(List<Category> categories){
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

//        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
//        boolean prefModeOn = preferences.getBoolean(KEY_NIGHT_MODE,false);
        //boolean appModeOn = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
//        boolean nightModeFlags = getResources().getConfiguration().uiMode == Configuration.UI_MODE_NIGHT_YES;
        if(requestCode == SettingsActivity.SETTINGS_REQUEST_CODE){
            Intent intent = getIntent();
            finish();
            startActivity(intent);
//            if(resultCode == Activity.RESULT_OK){
//                Intent intent = getIntent();
//                finish();
//                startActivity(intent);
//            }
//            else {
//                Handler handler = new Handler();
//                handler.postDelayed(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        recreate();//wait for activity get resumed then recreate
//                    }
//                }, 0);
//            }

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
}
