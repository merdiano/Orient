package com.tps.orientnews.ui;


import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;

import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mindorks.placeholderview.PlaceHolderView;

import com.tps.orientnews.R;
import com.tps.orientnews.data.NetworkState;
import com.tps.orientnews.data.Status;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.injectors.ViewModelFactory;
import com.tps.orientnews.room.Category;
import com.tps.orientnews.room.Post;

import com.tps.orientnews.ui.adapters.PostListAdapter;
import com.tps.orientnews.ui.views.DrawyerCategoryItem;
import com.tps.orientnews.ui.views.DrawyerItemCallback;
import com.tps.orientnews.ui.views.DrawyerMenuItem;
import com.tps.orientnews.ui.views.GridItemDividerDecoration;
import com.tps.orientnews.ui.views.HomeGridItemAnimator;
import com.tps.orientnews.utils.NetworkUtils;
import com.tps.orientnews.viewmodels.MainActivityViewModel;
import com.tps.orientnews.viewmodels.MainViewModel;


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
    @BindView(android.R.id.empty)
    ProgressBar loading;
    @BindView(R.id.grid) RecyclerView grid;
    @Nullable
    @BindView(R.id.no_connection)
    ImageView noConnection;

    private static final int RC_SEARCH = 0;
    private static final String KEY_SOURCE ="SOURCE_KEY";
    @Inject
    PostListAdapter adapter;
    @Inject
    RecyclerViewPreloader<Post> shotPreloader;

    @Inject
    Observable<Boolean> connectionStatus;
    Disposable internetDisposable;
    private DrawyerMenuItem allPosts;
//    @Inject
//    ViewModelFactory<MainActivityViewModel> viewModelFactory;
//    private MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setTheme(R.style.OrientTheme);
//        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);//for vector drawables
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setupDrawyer();
        //todo check this on fullscreen
        loading.getIndeterminateDrawable()
                .setColorFilter(getResources().getColor(R.color.colorAccent),
                    PorterDuff.Mode.SRC_IN);

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
//
//
//        });



        NetworkUtils.checkGooglePlayServicesAvailable(MainActivity.this);

        viewModel.getCategories().observe(this,this::fillDrawyer);
        viewModel.postList.observe(this, posts -> {
              adapter.submitList(posts);

            if(posts.size() != 0)
                loading.setVisibility(View.GONE);
            else
                loading.setVisibility(View.VISIBLE);
        });

        viewModel.networkState.observe(this, networkState -> {
            adapter.setNetworkState(networkState);
            int postCount = adapter.getItemCount();

            if(networkState.status == Status.FAILED){
                if(internetDisposable == null)
                    internetDisposable = connectionStatus.subscribe(this::connectionChanged);
                if(postCount == 1){
                    loading.setVisibility(View.GONE);

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
            }
        });

        //val subreddit = savedInstanceState?.getString(KEY_SUBREDDIT) ?: DEFAULT_SUBREDDIT
        if(savedInstanceState != null){
            int source = savedInstanceState.getInt(KEY_SOURCE);
            viewModel.loadPosts(source);
        }
        else{
            viewModel.loadPosts(-1);


        }


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
        //mDrawyerView.setItemAnimator(new FilterAdapter.FilterAnimator());
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
                loading.setVisibility(View.VISIBLE);
                //todo fill drawyer if it is empty and fetch posts
                if (mDrawyerView.getViewResolverCount() == 3) {
                    viewModel.loadCategories(); //todo load cats somehow

                }
                viewModel.retry();

            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SettingsActivity.SETTINGS_REQUEST_CODE){
            recreate();
        }
    }

    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        if (data == null || resultCode != RESULT_OK
                || !data.hasExtra(PostActivity.RESULT_EXTRA_POST_ID)) return;

        // When reentering, if the shared element is no longer on screen (e.g. after an
        // orientation change) then scroll it into view.
        final long sharedShotId = data.getLongExtra(PostActivity.RESULT_EXTRA_POST_ID, -1L);
        if (sharedShotId != -1L                                             // returning from a shot
                && adapter.getItemCount() > 0                           // grid populated
                && grid.findViewHolderForItemId(sharedShotId) == null) {    // view not attached
            final int position = adapter.getItemPosition(sharedShotId);
            if (position == RecyclerView.NO_POSITION) return;

            // delay the transition until our shared element is on-screen i.e. has been laid out
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                postponeEnterTransition();
            }
            grid.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int l, int t, int r, int b,
                                           int oL, int oT, int oR, int oB) {
                    grid.removeOnLayoutChangeListener(this);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        startPostponedEnterTransition();
                    }
                }
            });
            grid.scrollToPosition(position);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                toolbar.setTranslationZ(-1f);
            }
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
