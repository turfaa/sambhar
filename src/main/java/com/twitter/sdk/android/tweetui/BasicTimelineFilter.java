package com.twitter.sdk.android.tweetui;

import android.text.TextUtils;
import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.MentionEntity;
import com.twitter.sdk.android.core.models.SymbolEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.UrlEntity;
import java.net.IDN;
import java.text.BreakIterator;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import okhttp3.HttpUrl;

public class BasicTimelineFilter implements TimelineFilter {
    private final Set<String> handleConstraints;
    private final Set<String> hashTagConstraints;
    private final Set<String> keywordConstraints;
    private final Set<String> urlConstraints;
    private final BreakIterator wordIterator;

    static class IgnoreCaseComparator implements Comparator<String> {
        private final Collator collator;

        IgnoreCaseComparator(Locale locale) {
            this.collator = Collator.getInstance(locale);
            this.collator.setStrength(0);
        }

        public int compare(String str, String str2) {
            return this.collator.compare(str, str2);
        }
    }

    public BasicTimelineFilter(FilterValues filterValues) {
        this(filterValues, Locale.getDefault());
    }

    public BasicTimelineFilter(FilterValues filterValues, Locale locale) {
        IgnoreCaseComparator ignoreCaseComparator = new IgnoreCaseComparator(locale);
        this.wordIterator = BreakIterator.getWordInstance(locale);
        this.keywordConstraints = new TreeSet(ignoreCaseComparator);
        this.keywordConstraints.addAll(filterValues.keywords);
        this.hashTagConstraints = new TreeSet(ignoreCaseComparator);
        for (String normalizeHashtag : filterValues.hashtags) {
            this.hashTagConstraints.add(normalizeHashtag(normalizeHashtag));
        }
        this.handleConstraints = new HashSet(filterValues.handles.size());
        for (String normalizeHashtag2 : filterValues.handles) {
            this.handleConstraints.add(normalizeHandle(normalizeHashtag2));
        }
        this.urlConstraints = new HashSet(filterValues.urls.size());
        for (String normalizeUrl : filterValues.urls) {
            this.urlConstraints.add(normalizeUrl(normalizeUrl));
        }
    }

    public List<Tweet> filter(List<Tweet> list) {
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            Tweet tweet = (Tweet) list.get(i);
            if (!shouldFilterTweet(tweet)) {
                arrayList.add(tweet);
            }
        }
        return Collections.unmodifiableList(arrayList);
    }

    public int totalFilters() {
        return ((this.keywordConstraints.size() + this.hashTagConstraints.size()) + this.urlConstraints.size()) + this.handleConstraints.size();
    }

    /* Access modifiers changed, original: 0000 */
    public boolean shouldFilterTweet(Tweet tweet) {
        if (tweet.user != null && containsMatchingScreenName(tweet.user.screenName)) {
            return true;
        }
        if (tweet.entities == null || (!containsMatchingHashtag(tweet.entities.hashtags) && !containsMatchingSymbol(tweet.entities.symbols) && !containsMatchingUrl(tweet.entities.urls) && !containsMatchingMention(tweet.entities.userMentions))) {
            return containsMatchingText(tweet);
        }
        return true;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean containsMatchingText(Tweet tweet) {
        this.wordIterator.setText(tweet.text);
        int first = this.wordIterator.first();
        int next = this.wordIterator.next();
        while (true) {
            int i = next;
            next = first;
            first = i;
            if (first == -1) {
                return false;
            }
            if (this.keywordConstraints.contains(tweet.text.substring(next, first))) {
                return true;
            }
            next = this.wordIterator.next();
        }
    }

    /* Access modifiers changed, original: 0000 */
    public boolean containsMatchingHashtag(List<HashtagEntity> list) {
        for (HashtagEntity hashtagEntity : list) {
            if (this.hashTagConstraints.contains(hashtagEntity.text)) {
                return true;
            }
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean containsMatchingSymbol(List<SymbolEntity> list) {
        for (SymbolEntity symbolEntity : list) {
            if (this.hashTagConstraints.contains(symbolEntity.text)) {
                return true;
            }
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean containsMatchingUrl(List<UrlEntity> list) {
        for (UrlEntity urlEntity : list) {
            if (this.urlConstraints.contains(normalizeUrl(urlEntity.expandedUrl))) {
                return true;
            }
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean containsMatchingMention(List<MentionEntity> list) {
        for (MentionEntity mentionEntity : list) {
            if (this.handleConstraints.contains(normalizeHandle(mentionEntity.screenName))) {
                return true;
            }
        }
        return false;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean containsMatchingScreenName(String str) {
        return this.handleConstraints.contains(normalizeHandle(str));
    }

    static String normalizeUrl(String str) {
        try {
            HttpUrl parse = HttpUrl.parse(str);
            if (parse != null) {
                if (parse.host() != null) {
                    return parse.host().toLowerCase(Locale.US);
                }
            }
            return IDN.toASCII(str).toLowerCase(Locale.US);
        } catch (IllegalArgumentException unused) {
            return str;
        }
    }

    static String normalizeHashtag(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char charAt = str.charAt(0);
        if (charAt == '#' || charAt == 65283 || charAt == '$') {
            str = str.substring(1, str.length());
        }
        return str;
    }

    static String normalizeHandle(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char charAt = str.charAt(0);
        if (charAt == '@' || charAt == 65312) {
            str = str.substring(1, str.length());
        }
        return str.toLowerCase(Locale.US);
    }
}
