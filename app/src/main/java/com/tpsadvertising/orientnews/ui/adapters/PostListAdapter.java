package com.tpsadvertising.orientnews.ui.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
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
import com.tpsadvertising.orientnews.R;
import com.tpsadvertising.orientnews.data.NetworkState;
import com.tpsadvertising.orientnews.injectors.PerActivity;
import com.tpsadvertising.orientnews.room.Assets;
import com.tpsadvertising.orientnews.room.Post;
import com.tpsadvertising.orientnews.room.Reklama;
import com.tpsadvertising.orientnews.ui.DetailActivity;
import com.tpsadvertising.orientnews.ui.SettingsFragment;
import com.tpsadvertising.orientnews.ui.views.Divided;
import com.tpsadvertising.orientnews.utils.ObservableColorMatrix;
import com.tpsadvertising.orientnews.utils.glide.GlideApp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static com.tpsadvertising.orientnews.utils.AnimUtils.getFastOutSlowInInterpolator;

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
    private List<Reklama> adsList;
    private float textSize = 0;
    public static final int REQUEST_CODE_VIEW_POST = 5407;
    private static final int AD_POSITION = 5;
    private int lastAdindex = 0;
    private HashMap<Integer,Reklama> mappedAds;
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
        return networkState != null
                && networkState != NetworkState.LOADED
                && networkState != NetworkState.FAVORITE;
    }

    @Override
    public int getItemViewType(int position) {
        if(hasExtraRow() && position == getItemCount() -1){
            return R.layout.infinite_loading;
        }


        if(position+1 >= AD_POSITION && adsList!= null && !adsList.isEmpty()){
            if(mappedAds != null && mappedAds.containsKey(position))
                return R.layout.post_item_with_ad;


            boolean divisible = (position+1) % AD_POSITION == 0;
            if(divisible && lastAdindex< adsList.size())
            {
                Log.d("ADAPTER getItemViewType"," position :"+position);
                mappedAds.put(position,adsList.get(lastAdindex++));
                //adsList.remove(0);
                return R.layout.post_item_with_ad;
            }
        }
        return R.layout.post_item;
    }

    public void submitAds(List<Reklama> adverts){
        this.adsList = adverts;
        lastAdindex = 0;
        mappedAds = new HashMap<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {

            case R.layout.post_item:
                return createOrientNewsHolder(parent);

            case R.layout.post_item_with_ad:
                return createNewsWithAdsHolder(parent);
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
            case R.layout.post_item_with_ad:
                bindWithAds(getItem(position),(NewsWithAdsHolder) holder,position);
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
    private void  bindWithAds(final Post post, final NewsWithAdsHolder holder, final int position){
        holder.title.setText(post.title);
        try {
            Assets assets  = post.thumbnail_images;
            String img_url = assets.thumbnailUrl;
            if(assets != null && !assets.thumbnailUrl.isEmpty())
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

//            Log.w("binding post adIndex",lastAdindex+" position :"+position);
                    if(mappedAds != null && mappedAds.containsKey(position)){
                    //positionu tutmak lazim key value maybe ordn kontrol etmek lazim
                        Reklama reklama = mappedAds.get(position);
                        if(reklama.image_url != null && !reklama.image_url.isEmpty()){
                            GlideApp.with(host)
                                    .load(reklama.image_url)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA)
                                    .fitCenter()
                                    .transition(withCrossFade())
                                    .into(holder.banner);

                        }
                        if(reklama.link_to != null && !reklama.image_url.isEmpty()){

                            holder.banner.setOnClickListener(view -> {
                                String url =(!reklama.link_to.startsWith("http://") &&
                                        !reklama.link_to.startsWith("https://"))?
                                        "http://"+reklama.link_to : reklama.link_to;
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(url));
                                try {
                                    host.startActivity(i);
                                }catch(Exception ex){
                                    Log.e("PostlistAdapter","Error in starting activity with browser");
                                }

                            });
                        }
                    }
        }catch (Exception ex){
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
                        .inflate(R.layout.post_item, parent, false),textSize);
        holder.itemView.setOnClickListener(view -> {

            Intent intent = new Intent(host,DetailActivity.class);
            try {
                intent.putExtra(DetailActivity.EXTRA_POST,getItem(holder.getAdapterPosition()).id);
                host.startActivityForResult(intent, REQUEST_CODE_VIEW_POST);

            }catch (Exception e){
                Log.e("PstLstAdptrOnItmVwClick",e.getLocalizedMessage());
            }

//            setGridItemContentTransitions(holder.image);


        });
        return holder;
    }

    @NonNull
    private NewsWithAdsHolder createNewsWithAdsHolder(ViewGroup parent) {
        final NewsWithAdsHolder holder = new NewsWithAdsHolder(
                LayoutInflater.from(host)
                        .inflate(R.layout.post_item_with_ad, parent, false),
                textSize);

        holder.title.setOnClickListener(view -> {

            Intent intent = new Intent(host,DetailActivity.class);
            intent.putExtra(DetailActivity.EXTRA_POST,getItem(holder.getAdapterPosition()).id);
//            setGridItemContentTransitions(holder.image);

            host.startActivityForResult(intent, REQUEST_CODE_VIEW_POST);

        });
        holder.image.setOnClickListener(view -> {

            Intent intent = new Intent(host,DetailActivity.class);
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

    public void submitPosts(PagedList<Post> posts){
        lastAdindex = 0;
        mappedAds = new HashMap<>();
        submitList(posts);
    }

    public void clear(){

        submitList(null);

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

    static class NewsWithAdsHolder extends RecyclerView.ViewHolder implements Divided {
        @BindView(R.id.news_spacer)  ImageView image;
        @BindView(R.id.ad_banner)  ImageView banner;
        @BindView(R.id.news_title)   TextView title;

        //        @BindView(R.id.content_text) TextView content_text;
        public NewsWithAdsHolder(View itemView,float textSize) {
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
                // Post details may have changed if reloaded from the database,
                // but ID is fixed.
                @Override
                public boolean areItemsTheSame(Post oldPost, Post newPost) {
                    return oldPost.id == newPost.id;
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(Post oldPost,
                                                  Post newPost) {
                    return oldPost.equals(newPost);
                }
            };


}
