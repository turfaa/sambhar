package com.twitter.sdk.android.core.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class Place {
    @SerializedName("attributes")
    public final Map<String, String> attributes;
    @SerializedName("bounding_box")
    public final BoundingBox boundingBox;
    @SerializedName("country")
    public final String country;
    @SerializedName("country_code")
    public final String countryCode;
    @SerializedName("full_name")
    public final String fullName;
    @SerializedName("id")
    public final String id;
    @SerializedName("name")
    public final String name;
    @SerializedName("place_type")
    public final String placeType;
    @SerializedName("url")
    public final String url;

    public static class BoundingBox {
        @SerializedName("coordinates")
        public final List<List<List<Double>>> coordinates;
        @SerializedName("type")
        public final String type;

        private BoundingBox() {
            this(null, null);
        }

        public BoundingBox(List<List<List<Double>>> list, String str) {
            this.coordinates = ModelUtils.getSafeList(list);
            this.type = str;
        }
    }

    public Place(Map<String, String> map, BoundingBox boundingBox, String str, String str2, String str3, String str4, String str5, String str6, String str7) {
        this.attributes = ModelUtils.getSafeMap(map);
        this.boundingBox = boundingBox;
        this.country = str;
        this.countryCode = str2;
        this.fullName = str3;
        this.id = str4;
        this.name = str5;
        this.placeType = str6;
        this.url = str7;
    }
}
