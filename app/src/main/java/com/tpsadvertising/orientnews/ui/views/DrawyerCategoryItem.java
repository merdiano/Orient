package com.tpsadvertising.orientnews.ui.views;

import android.content.Context;
import android.support.v7.content.res.AppCompatResources;
import android.widget.ImageView;
import android.widget.TextView;

import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.tpsadvertising.orientnews.R;
import com.tpsadvertising.orientnews.room.Category;

/**
 * Created by merdan on 8/1/18.
 */
@NonReusable

@Layout(R.layout.filter_item)
public class DrawyerCategoryItem {
    Category category;
    private DrawyerItemCallback itemCallback;

    Context context;
    @View(R.id.filter_name)
    protected TextView itemNameTxt;
    @View(R.id.filter_icon)
    protected ImageView itemIcon;
    public DrawyerCategoryItem(Context context, Category category) {
        this.context = context;
        this.category = category;
    }

    @Click(R.id.menuItem)
    void menuClicked() {
        itemCallback.categoryItemClicked(category.id);
    }

    @Resolve
    protected void onResolved() {
        itemIcon.setImageResource(R.drawable.ic_designer_news);
        itemNameTxt.setText(category.title);
    }

    public void setItemCallback(DrawyerItemCallback itemCallback) {
        this.itemCallback = itemCallback;
    }

}
