package com.twitter.sdk.android.core.internal;

import com.twitter.sdk.android.core.models.Card;
import com.twitter.sdk.android.core.models.ImageValue;
import com.twitter.sdk.android.core.models.UserValue;

public class VineCardUtils {
    public static final String PLAYER_CARD = "player";
    public static final String VINE_CARD = "vine";
    public static final long VINE_USER_ID = 586671909;

    private VineCardUtils() {
    }

    public static boolean isVine(Card card) {
        return (PLAYER_CARD.equals(card.name) || VINE_CARD.equals(card.name)) && isVineUser(card);
    }

    private static boolean isVineUser(Card card) {
        UserValue userValue = (UserValue) card.bindingValues.get("site");
        if (userValue != null) {
            try {
                if (Long.parseLong(userValue.idStr) == VINE_USER_ID) {
                    return true;
                }
            } catch (NumberFormatException unused) {
                return false;
            }
        }
        return false;
    }

    public static String getPublisherId(Card card) {
        return ((UserValue) card.bindingValues.get("site")).idStr;
    }

    public static String getStreamUrl(Card card) {
        return (String) card.bindingValues.get("player_stream_url");
    }

    public static ImageValue getImageValue(Card card) {
        return (ImageValue) card.bindingValues.get("player_image");
    }
}
