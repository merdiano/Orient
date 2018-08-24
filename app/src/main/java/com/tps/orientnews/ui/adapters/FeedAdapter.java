package com.tps.orientnews.ui.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
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
import android.util.Log;
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

import com.tps.orientnews.R;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.room.Assets;
import com.tps.orientnews.room.Category;
import com.tps.orientnews.room.Post;
import com.tps.orientnews.ui.DetailActivity;
import com.tps.orientnews.ui.PostActivity;
import com.tps.orientnews.ui.views.Divided;
import com.tps.orientnews.utils.ObservableColorMatrix;
import com.tps.orientnews.utils.PostComperator;
import com.tps.orientnews.utils.glide.GlideApp;


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
        implements ListPreloader.PreloadModelProvider<Post>{
    private List<Post> items;
    private static final int TYPE_ORIENT_NEWS_POST = 0;
    private static final int TYPE_LOADING_MORE = -1;
    public static final int REQUEST_CODE_VIEW_POST = 5407;
    private boolean showLoadingMore = false;
    private final ColorDrawable[] shotLoadingPlaceholders;
    final Activity host;

    private final ViewPreloadSizeProvider<Post> postPreloadSizeProvider;
    private final PostComperator.OrientItemComparator comparator;
    @Inject
    public FeedAdapter(Activity activity,
                       ViewPreloadSizeProvider<Post> viewPreloadSizeProvider){

        items = new ArrayList<>();
        comparator = new PostComperator.OrientItemComparator();
        this.host = activity;

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

            if (getItem(position) instanceof Post){
                return TYPE_ORIENT_NEWS_POST;
            }
        }
        return TYPE_LOADING_MORE;
    }

    private void bindLoadingViewHolder(LoadingMoreHolder holder, int position) {
        // only show the infinite load progress spinner if there are already items in the
        // grid i.e. it's not the first item & data is being loaded
        holder.progress.setVisibility((position > 0)
                ? View.VISIBLE : View.INVISIBLE);
    }

    private Post getItem(int position){
        if (position < 0 || position >= items.size()) return null;
        return items.get(position);
    }

    public void removeDataSource(Category category) {
        for (int i = items.size() - 1; i >= 0; i--) {
            Post item = items.get(i);
            if(item.categryId == category.id)
            {
                items.remove(i);
            }
        }
        sort();
        notifyDataSetChanged();
    }

    private void  bindPost(final Post post,final OrientNewsHolder holder, final int position){
        holder.title.setText(post.title);
        try {
            Assets assets  = post.thumbnail_images;
            String img_url = assets.thumbnailUrl;
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

        // need both placeholder & background to prevent seeing through shot as it fades in
        holder.image.setBackground(
                shotLoadingPlaceholders[position % shotLoadingPlaceholders.length]);
        //holder.image.setDrawBadge(false);
        // need a unique transition name per shot, let's use it's url
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            holder.image.setTransitionName(post.url);
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
    public void addAndResort(List<Post> newItems) {
        //weighItems(newItems);
        deduplicateAndAdd(newItems);
        sort();
        notifyDataSetChanged();
    }

    private void deduplicateAndAdd(List<Post> newItems) {
        final int count = getDataItemCount();
        for (Post newItem : newItems) {
            boolean add = true;
            for (int i = 0; i < count; i++) {
                Post existingItem = getItem(i);
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
        return getItem(position).id;
    }

    @NonNull
    private OrientNewsHolder createOrientNewsHolder(ViewGroup parent) {
        final OrientNewsHolder holder = new OrientNewsHolder(
                LayoutInflater.from(host)
                        .inflate(R.layout.post_item, parent, false));
        holder.itemView.setOnClickListener(view -> {
//            Bundle b = new Bundle();
//            b.putInt(DetailActivity.EXTRA_POST,getItem(holder.getAdapterPosition()).id);
            Intent intent = new Intent(host,DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_POST,getItem(holder.getAdapterPosition()).id);
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
            if (getItem(position).id == itemId) return position;
        }
        return RecyclerView.NO_POSITION;
    }
    @NonNull
    @Override
    public List<Post> getPreloadItems(int position) {
        Post item = getItem(position);
        return Collections.singletonList(item);
    }

    @Nullable
    @Override
    public RequestBuilder<?> getPreloadRequestBuilder(@NonNull Post item) {
//        return null;

        Assets assets  = item.thumbnail_images;
        String img_url = assets.thumbnailUrl;
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
