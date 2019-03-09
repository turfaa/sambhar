package com.facebook.share.internal;

import com.facebook.internal.Validate;
import com.facebook.share.model.GameRequestContent;
import com.facebook.share.model.GameRequestContent.ActionType;

public class GameRequestValidation {
    public static void validate(GameRequestContent gameRequestContent) {
        Validate.notNull(gameRequestContent.getMessage(), "message");
        int i = 0;
        int i2 = gameRequestContent.getObjectId() != null ? 1 : 0;
        int i3 = (gameRequestContent.getActionType() == ActionType.ASKFOR || gameRequestContent.getActionType() == ActionType.SEND) ? 1 : 0;
        if ((i2 ^ i3) == 0) {
            if (gameRequestContent.getRecipients() != null) {
                i = 1;
            }
            if (gameRequestContent.getSuggestions() != null) {
                i++;
            }
            if (gameRequestContent.getFilters() != null) {
                i++;
            }
            if (i > 1) {
                throw new IllegalArgumentException("Parameters to, filters and suggestions are mutually exclusive");
            }
            return;
        }
        throw new IllegalArgumentException("Object id should be provided if and only if action type is send or askfor");
    }
}
