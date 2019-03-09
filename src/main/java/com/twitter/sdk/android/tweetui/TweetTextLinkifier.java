package com.twitter.sdk.android.tweetui;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import com.twitter.sdk.android.core.models.ModelUtils;
import com.twitter.sdk.android.tweetui.internal.ClickableLinkSpan;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

final class TweetTextLinkifier {
    static final Pattern QUOTED_STATUS_URL = Pattern.compile("^https?://twitter\\.com(/#!)?/\\w+/status/\\d+$");
    static final Pattern VINE_URL = Pattern.compile("^https?://vine\\.co(/#!)?/v/\\w+$");

    private TweetTextLinkifier() {
    }

    static CharSequence linkifyUrls(FormattedTweetText formattedTweetText, LinkClickListener linkClickListener, int i, int i2, boolean z, boolean z2) {
        if (formattedTweetText == null) {
            return null;
        }
        if (TextUtils.isEmpty(formattedTweetText.text)) {
            return formattedTweetText.text;
        }
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(formattedTweetText.text);
        List mergeAndSortEntities = mergeAndSortEntities(ModelUtils.getSafeList(formattedTweetText.urlEntities), ModelUtils.getSafeList(formattedTweetText.mediaEntities), ModelUtils.getSafeList(formattedTweetText.hashtagEntities), ModelUtils.getSafeList(formattedTweetText.mentionEntities), ModelUtils.getSafeList(formattedTweetText.symbolEntities));
        addUrlEntities(spannableStringBuilder, mergeAndSortEntities, getEntityToStrip(formattedTweetText.text, mergeAndSortEntities, z, z2), linkClickListener, i, i2);
        return trimEnd(spannableStringBuilder);
    }

    static CharSequence trimEnd(CharSequence charSequence) {
        int length = charSequence.length();
        while (length > 0 && charSequence.charAt(length - 1) <= ' ') {
            length--;
        }
        return length < charSequence.length() ? charSequence.subSequence(0, length) : charSequence;
    }

    static List<FormattedUrlEntity> mergeAndSortEntities(List<FormattedUrlEntity> list, List<FormattedMediaEntity> list2, List<FormattedUrlEntity> list3, List<FormattedUrlEntity> list4, List<FormattedUrlEntity> list5) {
        ArrayList arrayList = new ArrayList(list);
        arrayList.addAll(list2);
        arrayList.addAll(list3);
        arrayList.addAll(list4);
        arrayList.addAll(list5);
        Collections.sort(arrayList, new Comparator<FormattedUrlEntity>() {
            public int compare(FormattedUrlEntity formattedUrlEntity, FormattedUrlEntity formattedUrlEntity2) {
                if (formattedUrlEntity == null && formattedUrlEntity2 != null) {
                    return -1;
                }
                if (formattedUrlEntity != null && formattedUrlEntity2 == null) {
                    return 1;
                }
                if (formattedUrlEntity == null && formattedUrlEntity2 == null) {
                    return 0;
                }
                if (formattedUrlEntity.start < formattedUrlEntity2.start) {
                    return -1;
                }
                return formattedUrlEntity.start > formattedUrlEntity2.start ? 1 : 0;
            }
        });
        return arrayList;
    }

    private static void addUrlEntities(SpannableStringBuilder spannableStringBuilder, List<FormattedUrlEntity> list, FormattedUrlEntity formattedUrlEntity, LinkClickListener linkClickListener, int i, int i2) {
        if (list != null && !list.isEmpty()) {
            int i3 = 0;
            for (final FormattedUrlEntity formattedUrlEntity2 : list) {
                int i4 = formattedUrlEntity2.start - i3;
                int i5 = formattedUrlEntity2.end - i3;
                if (i4 >= 0 && i5 <= spannableStringBuilder.length()) {
                    if (formattedUrlEntity != null && formattedUrlEntity.start == formattedUrlEntity2.start) {
                        spannableStringBuilder.replace(i4, i5, "");
                        i3 += i5 - i4;
                    } else if (!TextUtils.isEmpty(formattedUrlEntity2.displayUrl)) {
                        spannableStringBuilder.replace(i4, i5, formattedUrlEntity2.displayUrl);
                        int length = i5 - (formattedUrlEntity2.displayUrl.length() + i4);
                        i3 += length;
                        final LinkClickListener linkClickListener2 = linkClickListener;
                        spannableStringBuilder.setSpan(new ClickableLinkSpan(i2, i, false) {
                            public void onClick(View view) {
                                if (linkClickListener2 != null) {
                                    linkClickListener2.onUrlClicked(formattedUrlEntity2.url);
                                }
                            }
                        }, i4, i5 - length, 33);
                    }
                }
            }
        }
    }

    static FormattedUrlEntity getEntityToStrip(String str, List<FormattedUrlEntity> list, boolean z, boolean z2) {
        if (list.isEmpty()) {
            return null;
        }
        FormattedUrlEntity formattedUrlEntity = (FormattedUrlEntity) list.get(list.size() - 1);
        if (stripLtrMarker(str).endsWith(formattedUrlEntity.url) && (isPhotoEntity(formattedUrlEntity) || ((z && isQuotedStatus(formattedUrlEntity)) || (z2 && isVineCard(formattedUrlEntity))))) {
            return formattedUrlEntity;
        }
        return null;
    }

    static String stripLtrMarker(String str) {
        return str.endsWith(Character.toString(8206)) ? str.substring(0, str.length() - 1) : str;
    }

    static boolean isPhotoEntity(FormattedUrlEntity formattedUrlEntity) {
        return (formattedUrlEntity instanceof FormattedMediaEntity) && "photo".equals(((FormattedMediaEntity) formattedUrlEntity).type);
    }

    static boolean isQuotedStatus(FormattedUrlEntity formattedUrlEntity) {
        return QUOTED_STATUS_URL.matcher(formattedUrlEntity.expandedUrl).find();
    }

    static boolean isVineCard(FormattedUrlEntity formattedUrlEntity) {
        return VINE_URL.matcher(formattedUrlEntity.expandedUrl).find();
    }
}
