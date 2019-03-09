package com.twitter.sdk.android.tweetui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import com.twitter.sdk.android.core.internal.scribe.ScribeItem;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.tweetui.internal.SwipeToDismissTouchListener.Callback;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class GalleryActivity extends Activity {
    public static final String GALLERY_ITEM = "GALLERY_ITEM";
    static final String MEDIA_ENTITY = "MEDIA_ENTITY";
    GalleryItem galleryItem;
    final GalleryScribeClient galleryScribeClient = new GalleryScribeClientImpl(TweetUi.getInstance());

    public static class GalleryItem implements Serializable {
        public final List<MediaEntity> mediaEntities;
        public final int mediaEntityIndex;
        public final long tweetId;

        public GalleryItem(int i, List<MediaEntity> list) {
            this(0, i, list);
        }

        public GalleryItem(long j, int i, List<MediaEntity> list) {
            this.tweetId = j;
            this.mediaEntityIndex = i;
            this.mediaEntities = list;
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.tw__gallery_activity);
        this.galleryItem = getGalleryItem();
        if (bundle == null) {
            scribeShowEvent();
        }
        GalleryAdapter galleryAdapter = new GalleryAdapter(this, getSwipeToDismissCallback());
        galleryAdapter.addAll(this.galleryItem.mediaEntities);
        ViewPager viewPager = (ViewPager) findViewById(R.id.tw__view_pager);
        viewPager.setPageMargin(getResources().getDimensionPixelSize(R.dimen.tw__gallery_page_margin));
        viewPager.addOnPageChangeListener(getOnPageChangeListener());
        viewPager.setAdapter(galleryAdapter);
        viewPager.setCurrentItem(this.galleryItem.mediaEntityIndex);
    }

    /* Access modifiers changed, original: 0000 */
    public OnPageChangeListener getOnPageChangeListener() {
        return new OnPageChangeListener() {
            int galleryPosition = -1;

            public void onPageScrollStateChanged(int i) {
            }

            public void onPageScrolled(int i, float f, int i2) {
                if (this.galleryPosition == -1 && i == 0 && ((double) f) == 0.0d) {
                    GalleryActivity.this.scribeImpressionEvent(i);
                    this.galleryPosition++;
                }
            }

            public void onPageSelected(int i) {
                if (this.galleryPosition >= 0) {
                    GalleryActivity.this.scribeNavigateEvent();
                }
                this.galleryPosition++;
                GalleryActivity.this.scribeImpressionEvent(i);
            }
        };
    }

    /* Access modifiers changed, original: 0000 */
    public Callback getSwipeToDismissCallback() {
        return new Callback() {
            public void onMove(float f) {
            }

            public void onDismiss() {
                GalleryActivity.this.scribeDismissEvent();
                GalleryActivity.this.finish();
                GalleryActivity.this.overridePendingTransition(0, R.anim.tw__slide_out);
            }
        };
    }

    /* Access modifiers changed, original: 0000 */
    public GalleryItem getGalleryItem() {
        MediaEntity mediaEntity = (MediaEntity) getIntent().getSerializableExtra(MEDIA_ENTITY);
        if (mediaEntity != null) {
            return new GalleryItem(0, Collections.singletonList(mediaEntity));
        }
        return (GalleryItem) getIntent().getSerializableExtra(GALLERY_ITEM);
    }

    public void onBackPressed() {
        scribeDismissEvent();
        super.onBackPressed();
        overridePendingTransition(0, R.anim.tw__slide_out);
    }

    /* Access modifiers changed, original: 0000 */
    public void scribeShowEvent() {
        this.galleryScribeClient.show();
    }

    /* Access modifiers changed, original: 0000 */
    public void scribeDismissEvent() {
        this.galleryScribeClient.dismiss();
    }

    /* Access modifiers changed, original: 0000 */
    public void scribeImpressionEvent(int i) {
        this.galleryScribeClient.impression(ScribeItem.fromMediaEntity(this.galleryItem.tweetId, (MediaEntity) this.galleryItem.mediaEntities.get(i)));
    }

    /* Access modifiers changed, original: 0000 */
    public void scribeNavigateEvent() {
        this.galleryScribeClient.navigate();
    }
}
