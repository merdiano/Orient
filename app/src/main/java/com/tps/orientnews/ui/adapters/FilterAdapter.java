package com.tps.orientnews.ui.adapters;

/**
 * Created by merdan on 7/14/18.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.TextView;

import com.tps.orientnews.DataLoadingSubject;
import com.tps.orientnews.DataManager;
import com.tps.orientnews.R;
import com.tps.orientnews.Repository;
import com.tps.orientnews.injectors.PerActivity;
import com.tps.orientnews.models.Category;
import com.tps.orientnews.ui.views.Divided;
import com.tps.orientnews.utils.ColorUtils;
import com.tps.orientnews.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

@PerActivity
public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder>{
    List<Category> categories;
    private @Nullable
    List<FiltersChangedCallbacks> callbacks;
    @Inject
    FilterAdapter()
    {
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final FilterViewHolder holder = new FilterViewHolder(LayoutInflater.from(parent
                .getContext()).inflate(R.layout.filter_item, parent, false));
        holder.itemView.setOnClickListener(v -> {
            final int position = holder.getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) return;
            final Category filter = categories.get(position);
            filter.active = !filter.active;
            holder.filterName.setEnabled(filter.active);
            notifyItemChanged(position, filter.active ? FilterAnimator.FILTER_ENABLED
                    : FilterAnimator.FILTER_DISABLED);
            dispatchFiltersChanged(filter);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final FilterViewHolder holder, int position) {
        final Category filter = categories.get(position);

        holder.filterName.setText(filter.getTitle());
        holder.filterName.setEnabled(filter.active);
    }
    @Override
    public void onBindViewHolder(FilterViewHolder holder,
                                 int position,
                                 List<Object> partialChangePayloads) {
        if (!partialChangePayloads.isEmpty()) {
            // if we're doing a partial re-bind i.e. an item is enabling/disabling or being
            // highlighted then data hasn't changed. Just set state based on the payload
            boolean filterEnabled = partialChangePayloads.contains(FilterAnimator.FILTER_ENABLED);
            boolean filterDisabled = partialChangePayloads.contains(FilterAnimator.FILTER_DISABLED);
            if (filterEnabled || filterDisabled) {
                holder.filterName.setEnabled(filterEnabled);
                // icon is handled by the animator
            }
            // nothing to do for highlight
        } else {
            onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {

        if(categories!=null)
            return categories.size();
        else
            return 0;
    }

    @Override
    public long getItemId(int position) {
        return categories.get(position).getId();
    }

    public void registerFilterChangedCallback(FiltersChangedCallbacks callback) {
        if (callbacks == null) {
            callbacks = new ArrayList<>();
        }
        callbacks.add(callback);
    }

    public void unregisterFilterChangedCallback(FiltersChangedCallbacks callback) {
        if (callbacks != null && !callbacks.isEmpty()) {
            callbacks.remove(callback);
        }
    }

    void dispatchFiltersChanged(Category filter) {
        if (callbacks != null && !callbacks.isEmpty()) {
            for (FiltersChangedCallbacks callback : callbacks) {
                callback.onFiltersChanged(filter);
            }
        }
    }

    public void setItems(List<Category> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public static abstract class FiltersChangedCallbacks {
        public void onFiltersChanged(Category changedFilter) { }

    }

    public static class FilterViewHolder extends RecyclerView.ViewHolder implements Divided {
        @BindView(R.id.filter_name)
        public TextView filterName;
//        public ImageView filterIcon;
//        public boolean isSwipeable;

        public FilterViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            filterName = itemView.findViewById(R.id.filter_name);
//            filterIcon = itemView.findViewById(R.id.filter_icon);
        }
    }
    public static class FilterAnimator extends DefaultItemAnimator {

        public static final int FILTER_ENABLED = 1;
        public static final int FILTER_DISABLED = 2;
        public static final int HIGHLIGHT = 3;

        @Override
        public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder) {
            return true;
        }

        @Override
        public ItemHolderInfo obtainHolderInfo() {
            return new FilterHolderInfo();
        }

        /* package */ static class FilterHolderInfo extends ItemHolderInfo {
            boolean doEnable;
            boolean doDisable;
            boolean doHighlight;
        }

        @NonNull
        @Override
        public ItemHolderInfo recordPreLayoutInformation(@NonNull RecyclerView.State state,
                                                         @NonNull RecyclerView.ViewHolder viewHolder,
                                                         int changeFlags,
                                                         @NonNull List<Object> payloads) {
            FilterHolderInfo info = (FilterHolderInfo)
                    super.recordPreLayoutInformation(state, viewHolder, changeFlags, payloads);
            if (!payloads.isEmpty()) {
                info.doEnable = payloads.contains(FILTER_ENABLED);
                info.doDisable = payloads.contains(FILTER_DISABLED);
                info.doHighlight = payloads.contains(HIGHLIGHT);
            }
            return info;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean animateChange(@NonNull RecyclerView.ViewHolder oldHolder,
                                     @NonNull RecyclerView.ViewHolder newHolder,
                                     @NonNull ItemHolderInfo preInfo,
                                     @NonNull ItemHolderInfo postInfo) {
            if (newHolder instanceof FilterViewHolder && preInfo instanceof FilterHolderInfo) {
                final FilterViewHolder holder = (FilterViewHolder) newHolder;
                final FilterHolderInfo info = (FilterHolderInfo) preInfo;

                if (info.doHighlight) {
                    int highlightColor =
                            ContextCompat.getColor(holder.itemView.getContext(), R.color.colorAccent);
                    int fadeFromTo = ColorUtils.modifyAlpha(highlightColor, 0);
                    ObjectAnimator highlightBackground = ObjectAnimator.ofArgb(
                            holder.itemView,
                            ViewUtils.BACKGROUND_COLOR,
                            fadeFromTo,
                            highlightColor,
                            fadeFromTo);
                    highlightBackground.setDuration(1000L);
                    highlightBackground.setInterpolator(new LinearInterpolator());
                    highlightBackground.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            dispatchChangeStarting(holder, false);
                            holder.itemView.setHasTransientState(true);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            holder.itemView.setBackground(null);
                            holder.itemView.setHasTransientState(false);
                            dispatchChangeFinished(holder, false);
                        }
                    });
                    highlightBackground.start();
                }
            }
            return super.animateChange(oldHolder, newHolder, preInfo, postInfo);
        }
    }
}
