package com.tps.orientnews.ui.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.arch.paging.PagedListAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
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
import com.tps.orientnews.data.NetworkState;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.room.Assets;
import com.tps.orientnews.room.Post;
import com.tps.orientnews.ui.DetailActivity;
import com.tps.orientnews.ui.PostActivity;
import com.tps.orientnews.ui.SettingsFragment;
import com.tps.orientnews.ui.views.Divided;
import com.tps.orientnews.utils.ObservableColorMatrix;
import com.tps.orientnews.utils.glide.GlideApp;


import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.tps.orientnews.ui.adapters.FeedAdapter.REQUEST_CODE_VIEW_POST;
import static com.tps.orientnews.utils.AnimUtils.getFastOutSlowInInterpolator;

/**
 * Created by merdan on 8/9/18.
 */
@PerActivity
public class PostListAdapter extends PagedListAdapter<Post,RecyclerView.ViewHolder>
        implements ListPreloader.PreloadModelProvider<Post>{
    private final Activity host;
    private final ColorDrawable[] shotLoadingPlaceholders;
    private final ViewPreloadSizeProvider<Post> postPreloadSizeProvider;
    private NetworkState networkState = null;
    private float textSize = 0;
    @Inject
    protected PostListAdapter(Activity host, ViewPreloadSizeProvider<Post> viewPreloadSizeProvider,
        @Named("defaultPrefs") SharedPreferences shPrefs) {
        super(DIFF_CALLBACK);
        this.host = host;
        this.postPreloadSizeProvider = viewPreloadSizeProvider;

        if(shPrefs.contains(SettingsFragment.KEY_FONT_SIZE)){
            String name = shPrefs.getString(SettingsFragment.KEY_FONT_SIZE,"medium_text")+"_title";
            int id = host.getResources()
                    .getIdentifier(name, "dimen",
                            host.getPackageName());

            //textView.setTextSize(getResources().getDimension(R.dimen.textsize));todo for various ekranlar
            textSize = host.getResources().getDimension(id);

        }

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
    }

    private boolean hasExtraRow(){
        return networkState != null && networkState != NetworkState.LOADED;
    }

    @Override
    public int getItemViewType(int position) {
        if(hasExtraRow() && position == getItemCount() -1){
            return R.layout.infinite_loading;
        }

        return R.layout.post_item;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {

            case R.layout.post_item:
                return createOrientNewsHolder(parent);
            case R.layout.infinite_loading:
                return new LoadingMoreHolder(LayoutInflater
                        .from(host)
                        .inflate(R.layout.infinite_loading, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {

            case R.layout.post_item:
                bindPost(getItem(position),(PostListAdapter.OrientNewsHolder)holder,position);
                break;
            case R.layout.infinite_loading:
                bindLoadingViewHolder((LoadingMoreHolder) holder, position);
                break;
        }
    }

    private void bindLoadingViewHolder(LoadingMoreHolder holder, int position) {
        // only show the infinite load progress spinner if there are already items in the
        // grid i.e. it's not the first item & data is being loaded
        holder.progress.setVisibility((position > 0)
                ? View.VISIBLE : View.INVISIBLE);
    }

    private void  bindPost(final Post post, final PostListAdapter.OrientNewsHolder holder, final int position){
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
                    .override(assets.thumbnailWidh, assets.thumbnailHeght)
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

    @NonNull
    private PostListAdapter.OrientNewsHolder createOrientNewsHolder(ViewGroup parent) {
        final PostListAdapter.OrientNewsHolder holder = new PostListAdapter.OrientNewsHolder(
                LayoutInflater.from(host)
                        .inflate(R.layout.post_item, parent, false),
                textSize);
        holder.itemView.setOnClickListener(view -> {
            //Bundle b = new Bundle();
            //b.putInt(PostActivity.EXTRA_POST,getItem(holder.getAdapterPosition()).id);
            Intent intent = new Intent(host,DetailActivity.class);
            //intent.setClass(host, OrientNewsActivity.class);//todo orient
            intent.putExtra(DetailActivity.EXTRA_POST,getItem(holder.getAdapterPosition()).id);
//            setGridItemContentTransitions(holder.image);

            host.startActivityForResult(intent, REQUEST_CODE_VIEW_POST);

        });
        return holder;
    }

    public void setNetworkState(NetworkState networkState) {
        NetworkState previosState = this.networkState;
        boolean hadExtraRow = hasExtraRow();
        this.networkState = networkState;
        boolean hasExtraRow = hasExtraRow();
        if(hadExtraRow != hasExtraRow){
            if(hadExtraRow){
                notifyItemRemoved(super.getItemCount());
            }
            else
                notifyItemInserted(super.getItemCount());
        }
        else if(hasExtraRow && previosState != networkState){
            notifyItemChanged(getItemCount()-1);
        }
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + (hasExtraRow() ? 1 : 0);
    }

    public int getItemPosition(final long itemId) {
        for (int position = 0; position < getCurrentList().size(); position++) {
            if (getItem(position).id == itemId) return position;
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public long getItemId(int position) {
        if (getItemViewType(position) == R.layout.infinite_loading || getItem(position )== null) {
            return -1L;
        }
        return getItem(position).id;
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

        Assets assets  = item.thumbnail_images;
        String img_url = assets.thumbnailUrl;
        return GlideApp.with(host).load(img_url);
    }

    static class OrientNewsHolder extends RecyclerView.ViewHolder implements Divided {
        @BindView(R.id.news_spacer)  ImageView image;
        @BindView(R.id.news_title)   TextView title;
//        @BindView(R.id.content_text) TextView content_text;
        public OrientNewsHolder(View itemView,float textSize) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            title.setTextSize(TypedValue.COMPLEX_UNIT_PX,textSize);
        }
    }

    static class LoadingMoreHolder extends RecyclerView.ViewHolder {

        ProgressBar progress;

        LoadingMoreHolder(View itemView) {
            super(itemView);
            progress = (ProgressBar) itemView;
        }

    }
    private static DiffUtil.ItemCallback<Post> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Post>() {
                // Concert details may have changed if reloaded from the database,
                // but ID is fixed.
                @Override
                public boolean areItemsTheSame(Post oldConcert, Post newConcert) {
                    return oldConcert.id == newConcert.id;
                }

                @Override
                public boolean areContentsTheSame(Post oldConcert,
                                                  Post newConcert) {
                    return oldConcert.equals(newConcert);
                }
            };
}
