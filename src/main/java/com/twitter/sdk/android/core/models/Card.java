package com.twitter.sdk.android.core.models;

import com.google.gson.annotations.SerializedName;

public class Card {
    @SerializedName("binding_values")
    public final BindingValues bindingValues;
    @SerializedName("name")
    public final String name;

    public Card(BindingValues bindingValues, String str) {
        this.bindingValues = bindingValues;
        this.name = str;
    }
}
