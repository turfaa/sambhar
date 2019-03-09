package com.twitter.sdk.android.tweetui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.tweetui.internal.GalleryImageView;
import com.twitter.sdk.android.tweetui.internal.SwipeToDismissTouchListener.Callback;
import java.util.ArrayList;
import java.util.List;

class GalleryAdapter extends PagerAdapter {
    final Callback callback;
    final Context context;
    final List<MediaEntity> items = new ArrayList();

    public boolean isViewFromObject(View view, Object obj) {
        return view == obj;
    }

    GalleryAdapter(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    /* Access modifiers changed, original: 0000 */
    public void addAll(List<MediaEntity> list) {
        this.items.addAll(list);
        notifyDataSetChanged();
    }

    public int getCount() {
        return this.items.size();
    }

    public Object instantiateItem(ViewGroup viewGroup, int i) {
        Target galleryImageView = new GalleryImageView(this.context);
        galleryImageView.setSwipeToDismissCallback(this.callback);
        viewGroup.addView(galleryImageView);
        Picasso.with(this.context).load(((MediaEntity) this.items.get(i)).mediaUrlHttps).into(galleryImageView);
        return galleryImageView;
    }

    public void destroyItem(ViewGroup viewGroup, int i, Object obj) {
        viewGroup.removeView((View) obj);
    }
}
