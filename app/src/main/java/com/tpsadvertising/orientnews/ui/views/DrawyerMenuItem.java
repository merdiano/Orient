package com.tpsadvertising.orientnews.ui.views;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import android.widget.TextView;

import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.NonReusable;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;
import com.tpsadvertising.orientnews.R;

/**
 * Created by merdan on 8/1/18.
 */
@NonReusable
@Layout(R.layout.filter_item)
public class DrawyerMenuItem implements Divided{
    protected int itemId;
    protected DrawyerItemCallback itemCallback;

    protected Context context;
    @View(R.id.filter_name)
    protected TextView itemNameTxt;
    @View(R.id.filter_icon)
    protected AppCompatImageView itemIcon;

    public DrawyerMenuItem(Context context,int itemId) {
        this.context = context;
        this.itemId = itemId;
    }

    @Resolve
    protected void onResolved(){
        itemIcon.setImageResource(getMenuIcon());
        itemNameTxt.setText(context.getResources().getString(itemId));
    }

    @Click(R.id.menuItem)
    void menuClicked() {
        itemCallback.menuItemClicked(itemId);
    }

    int getMenuIcon() {
        switch (itemId){
            case R.string.all_categories:
                return R.drawable.ms_ic_news;
            case R.string.favorite_posts:
                return R.drawable.ic_favorite;
            case  R.string.action_settings:
                return R.drawable.ic_settings;
        }
        return 0;
    }

    public void setItemCallback(DrawyerItemCallback itemCallback) {
        this.itemCallback = itemCallback;
    }
}
