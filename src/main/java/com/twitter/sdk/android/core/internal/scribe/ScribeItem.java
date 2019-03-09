package com.twitter.sdk.android.core.internal.scribe;

import com.google.gson.annotations.SerializedName;
import com.twitter.sdk.android.core.internal.VineCardUtils;
import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;
import java.io.Serializable;

public class ScribeItem implements Serializable {
    public static final int TYPE_MESSAGE = 6;
    public static final int TYPE_TWEET = 0;
    public static final int TYPE_USER = 3;
    @SerializedName("card_event")
    public final CardEvent cardEvent;
    @SerializedName("description")
    public final String description;
    @SerializedName("id")
    public final Long id;
    @SerializedName("item_type")
    public final Integer itemType;
    @SerializedName("media_details")
    public final MediaDetails mediaDetails;

    public static class Builder {
        private CardEvent cardEvent;
        private String description;
        private Long id;
        private Integer itemType;
        private MediaDetails mediaDetails;

        public Builder setItemType(int i) {
            this.itemType = Integer.valueOf(i);
            return this;
        }

        public Builder setId(long j) {
            this.id = Long.valueOf(j);
            return this;
        }

        public Builder setDescription(String str) {
            this.description = str;
            return this;
        }

        public Builder setCardEvent(CardEvent cardEvent) {
            this.cardEvent = cardEvent;
            return this;
        }

        public Builder setMediaDetails(MediaDetails mediaDetails) {
            this.mediaDetails = mediaDetails;
            return this;
        }

        public ScribeItem build() {
            return new ScribeItem(this.itemType, this.id, this.description, this.cardEvent, this.mediaDetails);
        }
    }

    public static class CardEvent implements Serializable {
        @SerializedName("promotion_card_type")
        final int promotionCardType;

        public CardEvent(int i) {
            this.promotionCardType = i;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            if (this.promotionCardType != ((CardEvent) obj).promotionCardType) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return this.promotionCardType;
        }
    }

    public static class MediaDetails implements Serializable {
        public static final String GIF_TYPE = "animated_gif";
        public static final int TYPE_AMPLIFY = 2;
        public static final int TYPE_ANIMATED_GIF = 3;
        public static final int TYPE_CONSUMER = 1;
        public static final int TYPE_VINE = 4;
        @SerializedName("content_id")
        public final long contentId;
        @SerializedName("media_type")
        public final int mediaType;
        @SerializedName("publisher_id")
        public final long publisherId;

        public MediaDetails(long j, int i, long j2) {
            this.contentId = j;
            this.mediaType = i;
            this.publisherId = j2;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            MediaDetails mediaDetails = (MediaDetails) obj;
            if (this.contentId != mediaDetails.contentId || this.mediaType != mediaDetails.mediaType) {
                return false;
            }
            if (this.publisherId != mediaDetails.publisherId) {
                z = false;
            }
            return z;
        }

        public int hashCode() {
            return (((((int) (this.contentId ^ (this.contentId >>> 32))) * 31) + this.mediaType) * 31) + ((int) (this.publisherId ^ (this.publisherId >>> 32)));
        }
    }

    private ScribeItem(Integer num, Long l, String str, CardEvent cardEvent, MediaDetails mediaDetails) {
        this.itemType = num;
        this.id = l;
        this.description = str;
        this.cardEvent = cardEvent;
        this.mediaDetails = mediaDetails;
    }

    public static ScribeItem fromTweet(Tweet tweet) {
        return new Builder().setItemType(0).setId(tweet.id).build();
    }

    public static ScribeItem fromUser(User user) {
        return new Builder().setItemType(3).setId(user.id).build();
    }

    public static ScribeItem fromMessage(String str) {
        return new Builder().setItemType(6).setDescription(str).build();
    }

    public static ScribeItem fromTweetCard(long j, Card card) {
        return new Builder().setItemType(0).setId(j).setMediaDetails(createCardDetails(j, card)).build();
    }

    public static ScribeItem fromMediaEntity(long j, MediaEntity mediaEntity) {
        return new Builder().setItemType(0).setId(j).setMediaDetails(createMediaDetails(j, mediaEntity)).build();
    }

    static MediaDetails createMediaDetails(long j, MediaEntity mediaEntity) {
        return new MediaDetails(j, getMediaType(mediaEntity), mediaEntity.id);
    }

    static MediaDetails createCardDetails(long j, Card card) {
        return new MediaDetails(j, 4, Long.valueOf(VineCardUtils.getPublisherId(card)).longValue());
    }

    static int getMediaType(MediaEntity mediaEntity) {
        return "animated_gif".equals(mediaEntity.type) ? 3 : 1;
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ScribeItem scribeItem = (ScribeItem) obj;
        if (!this.itemType == null ? this.itemType.equals(scribeItem.itemType) : scribeItem.itemType == null) {
            return false;
        }
        if (!this.id == null ? this.id.equals(scribeItem.id) : scribeItem.id == null) {
            return false;
        }
        if (!this.description == null ? this.description.equals(scribeItem.description) : scribeItem.description == null) {
            return false;
        }
        if (!this.cardEvent == null ? this.cardEvent.equals(scribeItem.cardEvent) : scribeItem.cardEvent == null) {
            return false;
        }
        if (this.mediaDetails == null ? scribeItem.mediaDetails != null : !this.mediaDetails.equals(scribeItem.mediaDetails)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = (((((((this.itemType != null ? this.itemType.hashCode() : 0) * 31) + (this.id != null ? this.id.hashCode() : 0)) * 31) + (this.description != null ? this.description.hashCode() : 0)) * 31) + (this.cardEvent != null ? this.cardEvent.hashCode() : 0)) * 31;
        if (this.mediaDetails != null) {
            i = this.mediaDetails.hashCode();
        }
        return hashCode + i;
    }
}
