package com.tps.orientnews.ui;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Annotation;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.SpannedString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
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
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.InternetObservingSettings;
import com.github.pwittchen.reactivenetwork.library.rx2.internet.observing.strategy.SocketInternetObservingStrategy;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tps.orientnews.DataLoadingSubject;
import com.tps.orientnews.DataManager;
import com.tps.orientnews.R;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.models.Category;
import com.tps.orientnews.models.OrientPost;
import com.tps.orientnews.ui.adapters.FeedAdapter;
import com.tps.orientnews.ui.adapters.FilterAdapter;
import com.tps.orientnews.ui.views.GridItemDividerDecoration;
import com.tps.orientnews.ui.views.HomeGridItemAnimator;
import com.tps.orientnews.utils.NetworkUtils;


import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
@PerActivity
public class MainActivity extends BaseActivity {

    @Inject
    protected FilterAdapter filterAdapter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.filters)
    RecyclerView filtersList;
    @BindView(android.R.id.empty)
    ProgressBar loading;
    @BindView(R.id.grid) RecyclerView grid;
    @Nullable
    @BindView(R.id.no_connection)
    ImageView noConnection;
    private TextView noFiltersEmptyText;
    private boolean connected = false;
    private boolean monitoringConnectivity = false;
    @Inject
    FeedAdapter adapter;
    @Inject
    RecyclerViewPreloader<OrientPost> shotPreloader;
    @Inject
    DataManager dataManager;
    private Disposable internetDisposable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.OrientTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //todo check this on fullscreen
        drawer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        filtersList.setHasFixedSize(true);
        filtersList.setAdapter(filterAdapter);
        filtersList.setItemAnimator(new FilterAdapter.FilterAnimator());
        filterAdapter.registerFilterChangedCallback(filtersChangedCallbacks);
        dataManager.registerDataCallback(loadingCallbacks);
        dataManager.loadCategories();
        grid.setAdapter(adapter);
        grid.addOnScrollListener(new InfiniteScrolListener(
                (LinearLayoutManager) grid.getLayoutManager(),adapter.dataLoading) {
            @Override
            void onLoadMore() {
                dataManager.loadAllDataSources();
            }
        });
        grid.setHasFixedSize(true);
        grid.addItemDecoration(new GridItemDividerDecoration(this, R.dimen.divider_height, R.color.divider));
        filtersList.addItemDecoration(new GridItemDividerDecoration(this, R.dimen.divider_height, R.color.divider));
        grid.addOnScrollListener(shotPreloader);
        grid.setItemAnimator(new HomeGridItemAnimator());

        NetworkUtils.checkGooglePlayServicesAvailable(MainActivity.this);
    }

    DataLoadingSubject.DataLoadingCallbacks loadingCallbacks = new DataLoadingSubject.DataLoadingCallbacks() {
        @Override
        public void onCategoriesLoaded(List<Category> categories)
        {
            filterAdapter.setItems(categories);
            dataManager.setupPageIndexes();
            dataManager.loadAllDataSources();
            checkEmptyState();
        }

        @Override
        public void onPostsLoaded(List<OrientPost> posts) {
            adapter.addAndResort(posts);
            checkEmptyState();
        }
    };

    FilterAdapter.FiltersChangedCallbacks filtersChangedCallbacks = new FilterAdapter.FiltersChangedCallbacks() {
        @Override
        public void onFiltersChanged(Category changedFilter) {
            dataManager.updateCategory(changedFilter);

            if (!changedFilter.active) {
                adapter.removeDataSource(changedFilter);
            }
            dataManager.onFilterChanged(changedFilter);
            checkEmptyState();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        checkConnectivity();
        NetworkUtils.checkGooglePlayServicesAvailable(MainActivity.this);
    }
    @Override
    protected void onDestroy() {
        dataManager.cancelLoading();
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(monitoringConnectivity)
        {
            internetDisposable.dispose();
            monitoringConnectivity = false;
        }
    }

    private void setNoFiltersEmptyTextVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            if (noFiltersEmptyText == null) {
                // create the no filters empty text
                ViewStub stub = findViewById(R.id.stub_no_filters);
                noFiltersEmptyText = (TextView) stub.inflate();
                SpannedString emptyText = (SpannedString) getText(R.string.no_filters_selected);
                SpannableStringBuilder ssb = new SpannableStringBuilder(emptyText);
                final Annotation[] annotations =
                        emptyText.getSpans(0, emptyText.length(), Annotation.class);
                if (annotations != null && annotations.length > 0) {
                    for (int i = 0; i < annotations.length; i++) {
                        final Annotation annotation = annotations[i];
                        if (annotation.getKey().equals("src")) {
                            // image span markup
                            String name = annotation.getValue();
                            int id = getResources().getIdentifier(name, null, getPackageName());
                            if (id == 0) continue;
                            ssb.setSpan(new ImageSpan(this, id,
                                            ImageSpan.ALIGN_BASELINE),
                                    emptyText.getSpanStart(annotation),
                                    emptyText.getSpanEnd(annotation),
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        } else if (annotation.getKey().equals("foregroundColor")) {
                            // foreground color span markup
                            String name = annotation.getValue();
                            int id = getResources().getIdentifier(name, null, getPackageName());
                            if (id == 0) continue;
                            ssb.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, id)),
                                    emptyText.getSpanStart(annotation),
                                    emptyText.getSpanEnd(annotation),
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                }
                noFiltersEmptyText.setText(ssb);
                noFiltersEmptyText.setOnClickListener(v -> drawer.openDrawer(GravityCompat.END));
            }
            noFiltersEmptyText.setVisibility(visibility);
        } else if (noFiltersEmptyText != null) {
            noFiltersEmptyText.setVisibility(visibility);
        }

    }
    /*check if device connected to internet*/
    private void checkConnectivity(){

        connected = NetworkUtils.checkConnection(this);
        if(!connected && (filterAdapter.getItemCount()==0 || adapter.getDataItemCount()==0)){
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

            /*Reaktive network internede barlar*/
            InternetObservingSettings settings = InternetObservingSettings
                    .host("www.orient.tm")
                    .strategy(new SocketInternetObservingStrategy())
                    .build();
            internetDisposable = ReactiveNetwork.observeInternetConnectivity(settings)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isConnected -> {
                        if(isConnected)
                            connectionEstablished();
                        else
                            connected = false;
                    });
            monitoringConnectivity=true;
         }

    }

    private void connectionEstablished(){
        connected = true;
        if (adapter.getItemCount() != 0) return;
        runOnUiThread(() -> {

                TransitionManager.beginDelayedTransition(drawer);
                noConnection.setVisibility(View.GONE);
                loading.setVisibility(View.VISIBLE);
                if(filterAdapter.getItemCount()>0)
                    dataManager.loadAllDataSources();
                else
                    dataManager.loadCategories();
        });
    }

    void checkEmptyState() {
        if (adapter.getDataItemCount() == 0) {
            // if grid is empty check whether we're loading or if no filters are selected
            if (dataManager.getEnabledSourcesCount() > 0) {
                if (connected) {
                    loading.setVisibility(View.VISIBLE);
                    setNoFiltersEmptyTextVisibility(View.GONE);
                }
            } else {
                loading.setVisibility(View.GONE);
                setNoFiltersEmptyTextVisibility(View.VISIBLE);
            }
//            toolbar.setTranslationZ(0f);
        } else {
            loading.setVisibility(View.GONE);
            setNoFiltersEmptyTextVisibility(View.GONE);
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
                && adapter.getDataItemCount() > 0                           // grid populated
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
