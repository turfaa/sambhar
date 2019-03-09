package com.twitter.sdk.android.tweetui;

import android.text.TextUtils;
import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.MentionEntity;
import com.twitter.sdk.android.core.models.SymbolEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.UrlEntity;
import com.twitter.sdk.android.tweetui.internal.util.HtmlEntities;
import com.twitter.sdk.android.tweetui.internal.util.HtmlEntities.Unescaped;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

final class TweetTextUtils {
    private TweetTextUtils() {
    }

    static FormattedTweetText formatTweetText(Tweet tweet) {
        if (tweet == null) {
            return null;
        }
        FormattedTweetText formattedTweetText = new FormattedTweetText();
        convertEntities(formattedTweetText, tweet);
        format(formattedTweetText, tweet);
        return formattedTweetText;
    }

    static void convertEntities(FormattedTweetText formattedTweetText, Tweet tweet) {
        if (tweet.entities != null) {
            List<UrlEntity> list = tweet.entities.urls;
            if (list != null) {
                for (UrlEntity createFormattedUrlEntity : list) {
                    formattedTweetText.urlEntities.add(FormattedUrlEntity.createFormattedUrlEntity(createFormattedUrlEntity));
                }
            }
            List<MediaEntity> list2 = tweet.entities.media;
            if (list2 != null) {
                for (MediaEntity formattedMediaEntity : list2) {
                    formattedTweetText.mediaEntities.add(new FormattedMediaEntity(formattedMediaEntity));
                }
            }
            List<HashtagEntity> list3 = tweet.entities.hashtags;
            if (list3 != null) {
                for (HashtagEntity createFormattedUrlEntity2 : list3) {
                    formattedTweetText.hashtagEntities.add(FormattedUrlEntity.createFormattedUrlEntity(createFormattedUrlEntity2));
                }
            }
            List<MentionEntity> list4 = tweet.entities.userMentions;
            if (list4 != null) {
                for (MentionEntity createFormattedUrlEntity3 : list4) {
                    formattedTweetText.mentionEntities.add(FormattedUrlEntity.createFormattedUrlEntity(createFormattedUrlEntity3));
                }
            }
            List<SymbolEntity> list5 = tweet.entities.symbols;
            if (list5 != null) {
                for (SymbolEntity createFormattedUrlEntity4 : list5) {
                    formattedTweetText.symbolEntities.add(FormattedUrlEntity.createFormattedUrlEntity(createFormattedUrlEntity4));
                }
            }
        }
    }

    static void format(FormattedTweetText formattedTweetText, Tweet tweet) {
        if (!TextUtils.isEmpty(tweet.text)) {
            Unescaped unescape = HtmlEntities.HTML40.unescape(tweet.text);
            StringBuilder stringBuilder = new StringBuilder(unescape.unescaped);
            adjustIndicesForEscapedChars(formattedTweetText.urlEntities, unescape.indices);
            adjustIndicesForEscapedChars(formattedTweetText.mediaEntities, unescape.indices);
            adjustIndicesForEscapedChars(formattedTweetText.hashtagEntities, unescape.indices);
            adjustIndicesForEscapedChars(formattedTweetText.mentionEntities, unescape.indices);
            adjustIndicesForEscapedChars(formattedTweetText.symbolEntities, unescape.indices);
            adjustIndicesForSupplementaryChars(stringBuilder, formattedTweetText);
            formattedTweetText.text = stringBuilder.toString();
        }
    }

    static void adjustIndicesForEscapedChars(List<? extends FormattedUrlEntity> list, List<int[]> list2) {
        if (list != null && list2 != null && !list2.isEmpty()) {
            int size = list2.size();
            int i = 0;
            int i2 = 0;
            for (FormattedUrlEntity formattedUrlEntity : list) {
                int i3 = i;
                int i4 = 0;
                while (i < size) {
                    int[] iArr = (int[]) list2.get(i);
                    int i5 = iArr[0];
                    int i6 = iArr[1];
                    i5 = i6 - i5;
                    if (i6 < formattedUrlEntity.start) {
                        i2 += i5;
                        i3++;
                    } else if (i6 < formattedUrlEntity.end) {
                        i4 += i5;
                    }
                    i++;
                }
                i4 += i2;
                formattedUrlEntity.start -= i4;
                formattedUrlEntity.end -= i4;
                i = i3;
            }
        }
    }

    static void adjustIndicesForSupplementaryChars(StringBuilder stringBuilder, FormattedTweetText formattedTweetText) {
        ArrayList arrayList = new ArrayList();
        int length = stringBuilder.length() - 1;
        int i = 0;
        while (i < length) {
            if (Character.isHighSurrogate(stringBuilder.charAt(i)) && Character.isLowSurrogate(stringBuilder.charAt(i + 1))) {
                arrayList.add(Integer.valueOf(i));
            }
            i++;
        }
        adjustEntitiesWithOffsets(formattedTweetText.urlEntities, arrayList);
        adjustEntitiesWithOffsets(formattedTweetText.mediaEntities, arrayList);
        adjustEntitiesWithOffsets(formattedTweetText.hashtagEntities, arrayList);
        adjustEntitiesWithOffsets(formattedTweetText.mentionEntities, arrayList);
        adjustEntitiesWithOffsets(formattedTweetText.symbolEntities, arrayList);
    }

    static void adjustEntitiesWithOffsets(List<? extends FormattedUrlEntity> list, List<Integer> list2) {
        if (list != null && list2 != null) {
            for (FormattedUrlEntity formattedUrlEntity : list) {
                int i = formattedUrlEntity.start;
                int i2 = 0;
                Iterator it = list2.iterator();
                while (it.hasNext() && ((Integer) it.next()).intValue() - i2 <= i) {
                    i2++;
                }
                formattedUrlEntity.start += i2;
                formattedUrlEntity.end += i2;
            }
        }
    }
}
