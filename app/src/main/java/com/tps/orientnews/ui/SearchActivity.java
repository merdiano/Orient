package com.tps.orientnews.ui;

import android.app.SearchManager;
import android.app.SharedElementCallback;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.annotation.TransitionRes;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.tps.orientnews.R;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.ui.adapters.FeedAdapter;
import com.tps.orientnews.ui.transitions.CircularReveal;
import com.tps.orientnews.ui.views.GridItemDividerDecoration;
import com.tps.orientnews.ui.views.SlideInItemAnimator;
import com.tps.orientnews.utils.ImeUtils;
import com.tps.orientnews.utils.TransitionUtils;
import com.tps.orientnews.viewmodels.SearchViewModel;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

@PerActivity
public class SearchActivity extends BaseActivity<SearchViewModel> {

    @Inject FeedAdapter feedAdapter;

    @BindView(R.id.searchback)
    ImageButton searchBack;
    @BindView(R.id.searchback_container)
    ViewGroup searchBackContainer;
    @BindView(R.id.search_view)
    SearchView searchView;
    @BindView(R.id.search_background)
    View searchBackground;
    @BindView(android.R.id.empty)
    ProgressBar progress;
    @BindView(R.id.search_results)
    RecyclerView results;
    @BindView(R.id.container) ViewGroup container;
    @BindView(R.id.search_toolbar) ViewGroup searchToolbar;
    @BindView(R.id.results_container) ViewGroup resultsContainer;
    @BindView(R.id.fab) ImageButton fab;

    @BindView(R.id.scrim) View scrim;
    @BindView(R.id.results_scrim) View resultsScrim;
//    @BindInt(R.integer.num_columns) int columns;
    @BindDimen(R.dimen.z_app_bar) float appBarElevation;
    private boolean focusQuery = true;
    private TextView noResults;
    private SparseArray<Transition> transitions = new SparseArray<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(SearchActivity.this,viewModelFactory)
                .get(SearchViewModel.class);
        setupSearchView();
//        //setExitSharedElementCallback(FeedAdapter.createSharedElementReenterCallback(this));
        results.setAdapter(feedAdapter);
        results.setItemAnimator(new SlideInItemAnimator());
        viewModel.postList.observe(this,data -> {
            //todo submit list


            if (data != null && data.size() > 0) {
                if (results.getVisibility() != View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(container,
                            getTransition(R.transition.search_show_results));
                    progress.setVisibility(View.GONE);
                    results.setVisibility(View.VISIBLE);
//                fab.setVisibility(View.VISIBLE);
                }
            feedAdapter.addAndResort(data);//todo
            } else {
                TransitionManager.beginDelayedTransition(
                        container, getTransition(R.transition.auto));
                progress.setVisibility(View.GONE);
                setNoResultsVisibility(View.VISIBLE);
            }
        });

        results.setHasFixedSize(true);

        results.addItemDecoration(new GridItemDividerDecoration(this, R.dimen.divider_height, R.color.divider));
        onNewIntent(getIntent());
        ShortcutHelper.reportSearchUsed(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(SearchManager.QUERY)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(query)) {
                searchView.setQuery(query, false);
                searchFor(query);
            }
        }
    }

    @Override
    protected void onPause() {
        overridePendingTransition(0,0);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onEnterAnimationComplete() {
        if (focusQuery) {
            // focus the search view once the enter transition finishes
            searchView.requestFocus();
            ImeUtils.showIme(searchView);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FeedAdapter.REQUEST_CODE_VIEW_POST:
                // by default we focus the search filed when entering this screen. Don't do that
                // when returning from viewing a search result.
                focusQuery = false;
                break;
        }
    }

    @OnClick({ R.id.scrim, R.id.searchback })
    protected void dismiss() {
        // clear the background else the touch ripple moves with the translation which looks bad
        searchBack.setBackground(null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAfterTransition();
        }
        else
            finish();
    }

    @OnClick(R.id.fab)
    protected void save() {
        // show the save confirmation bubble
        //todo
    }

    @OnClick(R.id.results_scrim)
    protected void hideSaveConfirmation() {
//        if (confirmSaveContainer.getVisibility() == View.VISIBLE) {
//            TransitionManager.beginDelayedTransition(
//                    resultsContainer, getTransition(R.transition.search_hide_confirm));
//            confirmSaveContainer.setVisibility(View.GONE);
//            resultsScrim.setVisibility(View.GONE);
//            fab.setVisibility(results.getVisibility());
//        }
        //todo
    }
    void clearResults() {
        TransitionManager.beginDelayedTransition(container, getTransition(R.transition.auto));
        feedAdapter.clear();
//        dataManager.clear();
        results.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        fab.setVisibility(View.GONE);
//        confirmSaveContainer.setVisibility(View.GONE);
        resultsScrim.setVisibility(View.GONE);
        setNoResultsVisibility(View.GONE);
        //todo
    }
    void setNoResultsVisibility(int visibility) {//todo
        if (visibility == View.VISIBLE) {
            if (noResults == null) {
                noResults = (TextView) ((ViewStub)
                        findViewById(R.id.stub_no_search_results)).inflate();
                noResults.setOnClickListener(v -> {
                    searchView.setQuery("", false);
                    searchView.requestFocus();
                    ImeUtils.showIme(searchView);
                });
            }
            String message = String.format(
                    getString(R.string.no_search_results), searchView.getQuery().toString());
            SpannableStringBuilder ssb = new SpannableStringBuilder(message);
            ssb.setSpan(new StyleSpan(Typeface.ITALIC),
                    message.indexOf('“') + 1,
                    message.length() - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            noResults.setText(ssb);
        }
        if (noResults != null) {
            noResults.setVisibility(visibility);
        }
    }

    void searchFor(String key) {
        clearResults();
        progress.setVisibility(View.VISIBLE);
        ImeUtils.hideIme(searchView);
        searchView.clearFocus();
        viewModel.shearch(key);

    }

    Transition getTransition(@TransitionRes int transitionId) {
        Transition transition = transitions.get(transitionId);
        if (transition == null) {
            transition = TransitionInflater.from(this).inflateTransition(transitionId);
            transitions.put(transitionId, transition);
        }
        return transition;
    }

    private void setupSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        // hint, inputType & ime options seem to be ignored from XML! Set in code
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setInputType(InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        searchView.setImeOptions(searchView.getImeOptions() | EditorInfo.IME_ACTION_SEARCH |
                EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_FLAG_NO_FULLSCREEN);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFor(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (TextUtils.isEmpty(query)) {
                    clearResults();
                }
                return true;
            }
        });
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){// && confirmSaveContainer.getVisibility() == View.VISIBLE) {
                hideSaveConfirmation();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setupTransitions() {
        // grab the position that the search icon transitions in *from*
        // & use it to configure the return transition
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(
                    List<String> sharedElementNames,
                    List<View> sharedElements,
                    List<View> sharedElementSnapshots) {
                if (sharedElements != null && !sharedElements.isEmpty()) {
                    View searchIcon = sharedElements.get(0);
                    if (searchIcon.getId() != R.id.searchback) return;
                    int centerX = (searchIcon.getLeft() + searchIcon.getRight()) / 2;
                    CircularReveal hideResults = (CircularReveal) TransitionUtils.findTransition(
                            (TransitionSet) getWindow().getReturnTransition(),
                            CircularReveal.class, R.id.results_container);
                    if (hideResults != null) {
                        hideResults.setCenter(new Point(centerX, 0));
                    }
                }
            }
        });
    }
}
