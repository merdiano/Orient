package com.tps.orientnews.ui.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.ListPreloader;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.tps.orientnews.DataLoadingSubject;
import com.tps.orientnews.DataManager;
import com.tps.orientnews.R;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.models.Assets;
import com.tps.orientnews.models.Category;
import com.tps.orientnews.models.OrientPost;
import com.tps.orientnews.ui.PostActivity;
import com.tps.orientnews.ui.views.Divided;
import com.tps.orientnews.utils.ObservableColorMatrix;
import com.tps.orientnews.utils.PostComperator;
import com.tps.orientnews.utils.TransitionUtils;
import com.tps.orientnews.utils.ViewUtils;
import com.tps.orientnews.utils.glide.GlideApp;

import org.greenrobot.greendao.DaoException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.tps.orientnews.utils.AnimUtils.getFastOutSlowInInterpolator;

/**
 * Created by merdan on 7/12/18.
 */
@PerActivity
public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ListPreloader.PreloadModelProvider<OrientPost>{
    private List<OrientPost> items;
    private static final int TYPE_ORIENT_NEWS_POST = 0;
    private static final int TYPE_LOADING_MORE = -1;
    static final int REQUEST_CODE_VIEW_POST = 5407;
    private boolean showLoadingMore = false;
    private final ColorDrawable[] shotLoadingPlaceholders;
    final Activity host;
//    final LayoutInflater layoutInflater;
    public final DataLoadingSubject dataLoading;
    private final ViewPreloadSizeProvider<OrientPost> postPreloadSizeProvider;
    private final PostComperator.OrientItemComparator comparator;
    @Inject
    public FeedAdapter(Activity activity, DataManager dbManager,
                       ViewPreloadSizeProvider<OrientPost> viewPreloadSizeProvider){

        items = new ArrayList<>();
        comparator = new PostComperator.OrientItemComparator();
        this.host = activity;
        this.dataLoading = dbManager;
        this.dataLoading.registerPostsCallback(postLoadingCallbacks);
        //this.dataLoading.loadPosts();
        this.postPreloadSizeProvider = viewPreloadSizeProvider;
        final TypedArray a = host.obtainStyledAttributes(R.styleable.OrientFeed);
        final int loadingColorArrayId =
                a.getResourceId(R.styleable.OrientFeed_shotLoadingPlaceholderColors, 0);
        if (loadingColorArrayId != 0) {
            int[] placeholderColors = host.getResources().getIntArray(loadingColorArrayId);
            shotLoadingPlaceholders = new ColorDrawable[placeholderColors.length];
            for (int i = 0; i < placeholderColors.length; i++) {
                shotLoadingPlaceholders[i] = new ColorDrawable(placeholderColors[i]);
            }
        } else {
            shotLoadingPlaceholders = new ColorDrawable[] { new ColorDrawable(Color.DKGRAY) };
        }

        items = new ArrayList<>();
        setHasStableIds(true);
    }

    DataLoadingSubject.PostLoadingCallbacks postLoadingCallbacks =
            new DataLoadingSubject.PostLoadingCallbacks() {
                @Override
                public void dataStartedLoading() {
                    if (showLoadingMore) return;
                    showLoadingMore = true;
                    notifyItemInserted(getLoadingMoreItemPosition());
                }

                @Override
                public void dataFinishedLoading() {
                    if (!showLoadingMore) return;
                    final int loadingPos = getLoadingMoreItemPosition();
                    showLoadingMore = false;
                    notifyItemRemoved(loadingPos);
                }

            };

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {

            case TYPE_ORIENT_NEWS_POST:
                return createOrientNewsHolder(parent);
            case TYPE_LOADING_MORE:
                return new LoadingMoreHolder(LayoutInflater
                        .from(host)
                        .inflate(R.layout.infinite_loading, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {

            case TYPE_ORIENT_NEWS_POST:
                bindPost(getItem(position),(OrientNewsHolder)holder,position);
                break;
            case TYPE_LOADING_MORE:
                bindLoadingViewHolder((LoadingMoreHolder) holder, position);
                break;
        }
    }
    @Override
    public int getItemViewType(int position) {
        if (position < getDataItemCount()
                && getDataItemCount() > 0) {

            if (getItem(position) instanceof OrientPost){
                return TYPE_ORIENT_NEWS_POST;
            }
        }
        return TYPE_LOADING_MORE;
    }

    private void bindLoadingViewHolder(LoadingMoreHolder holder, int position) {
        // only show the infinite load progress spinner if there are already items in the
        // grid i.e. it's not the first item & data is being loaded
        holder.progress.setVisibility((position > 0
                && dataLoading != null
                && dataLoading.isDataLoading())
                ? View.VISIBLE : View.INVISIBLE);
    }

    private OrientPost getItem(int position){
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    public void removeDataSource(Category category) {
        for (int i = items.size() - 1; i >= 0; i--) {
            OrientPost item = items.get(i);
            if(item.getCategoryId() == category.getId())
            {
                items.remove(i);
            }
        }
        sort();
        notifyDataSetChanged();
    }

    private void  bindPost(final OrientPost post,final OrientNewsHolder holder, final int position){
        holder.title.setText(post.getTitle());
        try {
            Assets assets  = post.getThumbnail_images();
            String img_url = assets.getThumbnailId();
            GlideApp.with(host)
                    .load(img_url)
                    .listener(new RequestListener<Drawable>() {

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model,
                                                       Target<Drawable> target, DataSource dataSource,
                                                       boolean isFirstResource) {
                            if (!post.hasFadedIn) {
                                holder.image.setHasTransientState(true);
                                final ObservableColorMatrix cm = new ObservableColorMatrix();
                                final ObjectAnimator saturation = ObjectAnimator.ofFloat(
                                        cm, ObservableColorMatrix.SATURATION, 0f, 1f);
                                saturation.addUpdateListener(valueAnimator -> {
                                    // just animating the color matrix does not invalidate the
                                    // drawable so need this update listener.  Also have to create a
                                    // new CMCF as the matrix is immutable :(
                                    holder.image.setColorFilter(new ColorMatrixColorFilter(cm));
                                });
                                saturation.setDuration(2000L);
                                saturation.setInterpolator(getFastOutSlowInInterpolator(host));
                                saturation.addListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        holder.image.clearColorFilter();
                                        holder.image.setHasTransientState(false);
                                    }
                                });
                                saturation.start();
                                post.hasFadedIn = true;
                            }
                            return false;
                        }

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                    Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .placeholder(shotLoadingPlaceholders[position % shotLoadingPlaceholders.length])
                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                    .fitCenter()
                    .transition(withCrossFade())
                    .override(150, 150)//todo image sizes
                    .into(holder.image);


        }catch (NullPointerException ex){
            Log.d("binding post","image may be null");
        }
        catch (DaoException ex){
            Log.d("post :"+post.getId(), ex.getLocalizedMessage());
        }
        // need both placeholder & background to prevent seeing through shot as it fades in
        holder.image.setBackground(
                shotLoadingPlaceholders[position % shotLoadingPlaceholders.length]);
        //holder.image.setDrawBadge(false);
        // need a unique transition name per shot, let's use it's url
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.image.setTransitionName(post.getUrl());
        }
        postPreloadSizeProvider.setView(holder.image);
    }
    @Override
    public int getItemCount() {
        return getDataItemCount() + (showLoadingMore ? 1 : 0);
    }
    private int getLoadingMoreItemPosition() {
        return showLoadingMore ? getItemCount() - 1 : RecyclerView.NO_POSITION;
    }
    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }
    /**
     * Main entry point for adding items to this adapter. Takes care of de-duplicating items and
     * sorting them (depending on the data source). Will also expand some items to span multiple
     * grid columns.
     */
    public void addAndResort(List<OrientPost> newItems) {
        //weighItems(newItems);
        deduplicateAndAdd(newItems);
        sort();
        notifyDataSetChanged();
    }

    private void deduplicateAndAdd(List<OrientPost> newItems) {
        final int count = getDataItemCount();
        for (OrientPost newItem : newItems) {
            boolean add = true;
            for (int i = 0; i < count; i++) {
                OrientPost existingItem = getItem(i);
                if (existingItem.equals(newItem)) {
                    add = false;
                    break;
                }
            }
            if (add) {
                items.add(newItem);
            }
        }
    }
    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == TYPE_LOADING_MORE) {
            return -1L;
        }
        return getItem(position).getId();
    }

    @NonNull
    private OrientNewsHolder createOrientNewsHolder(ViewGroup parent) {
        final OrientNewsHolder holder = new OrientNewsHolder(
                LayoutInflater.from(host)
                        .inflate(R.layout.post_item, parent, false));
        holder.itemView.setOnClickListener(view -> {
            Bundle b = new Bundle();
            b.putLong(PostActivity.EXTRA_POST,getItem(holder.getAdapterPosition()).getId());
            Intent intent = new Intent(host,PostActivity.class);
            //intent.setClass(host, OrientNewsActivity.class);//todo orient
            intent.putExtra("bundle",b);
//            setGridItemContentTransitions(holder.image);

            host.startActivityForResult(intent, REQUEST_CODE_VIEW_POST);

        });
        return holder;
    }
    public int getDataItemCount() {
        return items.size();
    }
    private void sort() {
        Collections.sort(items, comparator); // sort by weight
    }
    public int getItemPosition(final long itemId) {
        for (int position = 0; position < items.size(); position++) {
            if (getItem(position).getId() == itemId) return position;
        }
        return RecyclerView.NO_POSITION;
    }
    @NonNull
    @Override
    public List<OrientPost> getPreloadItems(int position) {
        OrientPost item = getItem(position);
        return Collections.singletonList(item);
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(@NonNull OrientPost item) {
//        return null;

        Assets assets  = item.getThumbnail_images();
        String img_url = assets.getThumbnailId();
        return GlideApp.with(host).load(img_url);
    }

    public static class OrientNewsHolder extends RecyclerView.ViewHolder implements Divided{
        @BindView(R.id.news_spacer)ImageView image;
        @BindView(R.id.news_title)
        public TextView title;
        public OrientNewsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    static class LoadingMoreHolder extends RecyclerView.ViewHolder {

        ProgressBar progress;

        LoadingMoreHolder(View itemView) {
            super(itemView);
            progress = (ProgressBar) itemView;
        }

    }

}
