package com.twitter.sdk.android.core.models;

import java.util.List;

public class UserBuilder {
    private boolean contributorsEnabled;
    private String createdAt;
    private boolean defaultProfile;
    private boolean defaultProfileImage;
    private String description;
    private String email;
    private UserEntities entities;
    private int favouritesCount;
    private boolean followRequestSent;
    private int followersCount;
    private int friendsCount;
    private boolean geoEnabled;
    private long id = -1;
    private String idStr;
    private boolean isTranslator;
    private String lang;
    private int listedCount;
    private String location;
    private String name;
    private String profileBackgroundColor;
    private String profileBackgroundImageUrl;
    private String profileBackgroundImageUrlHttps;
    private boolean profileBackgroundTile;
    private String profileBannerUrl;
    private String profileImageUrl;
    private String profileImageUrlHttps;
    private String profileLinkColor;
    private String profileSidebarBorderColor;
    private String profileSidebarFillColor;
    private String profileTextColor;
    private boolean profileUseBackgroundImage;
    private boolean protectedUser;
    private String screenName;
    private boolean showAllInlineMedia;
    private Tweet status;
    private int statusesCount;
    private String timeZone;
    private String url;
    private int utcOffset;
    private boolean verified;
    private List<String> withheldInCountries;
    private String withheldScope;

    public UserBuilder setContributorsEnabled(boolean z) {
        this.contributorsEnabled = z;
        return this;
    }

    public UserBuilder setCreatedAt(String str) {
        this.createdAt = str;
        return this;
    }

    public UserBuilder setDefaultProfile(boolean z) {
        this.defaultProfile = z;
        return this;
    }

    public UserBuilder setDefaultProfileImage(boolean z) {
        this.defaultProfileImage = z;
        return this;
    }

    public UserBuilder setDescription(String str) {
        this.description = str;
        return this;
    }

    public UserBuilder setEmail(String str) {
        this.email = str;
        return this;
    }

    public UserBuilder setEntities(UserEntities userEntities) {
        this.entities = userEntities;
        return this;
    }

    public UserBuilder setFavouritesCount(int i) {
        this.favouritesCount = i;
        return this;
    }

    public UserBuilder setFollowRequestSent(boolean z) {
        this.followRequestSent = z;
        return this;
    }

    public UserBuilder setFollowersCount(int i) {
        this.followersCount = i;
        return this;
    }

    public UserBuilder setFriendsCount(int i) {
        this.friendsCount = i;
        return this;
    }

    public UserBuilder setGeoEnabled(boolean z) {
        this.geoEnabled = z;
        return this;
    }

    public UserBuilder setId(long j) {
        this.id = j;
        return this;
    }

    public UserBuilder setIdStr(String str) {
        this.idStr = str;
        return this;
    }

    public UserBuilder setIsTranslator(boolean z) {
        this.isTranslator = z;
        return this;
    }

    public UserBuilder setLang(String str) {
        this.lang = str;
        return this;
    }

    public UserBuilder setListedCount(int i) {
        this.listedCount = i;
        return this;
    }

    public UserBuilder setLocation(String str) {
        this.location = str;
        return this;
    }

    public UserBuilder setName(String str) {
        this.name = str;
        return this;
    }

    public UserBuilder setProfileBackgroundColor(String str) {
        this.profileBackgroundColor = str;
        return this;
    }

    public UserBuilder setProfileBackgroundImageUrl(String str) {
        this.profileBackgroundImageUrl = str;
        return this;
    }

    public UserBuilder setProfileBackgroundImageUrlHttps(String str) {
        this.profileBackgroundImageUrlHttps = str;
        return this;
    }

    public UserBuilder setProfileBackgroundTile(boolean z) {
        this.profileBackgroundTile = z;
        return this;
    }

    public UserBuilder setProfileBannerUrl(String str) {
        this.profileBannerUrl = str;
        return this;
    }

    public UserBuilder setProfileImageUrl(String str) {
        this.profileImageUrl = str;
        return this;
    }

    public UserBuilder setProfileImageUrlHttps(String str) {
        this.profileImageUrlHttps = str;
        return this;
    }

    public UserBuilder setProfileLinkColor(String str) {
        this.profileLinkColor = str;
        return this;
    }

    public UserBuilder setProfileSidebarBorderColor(String str) {
        this.profileSidebarBorderColor = str;
        return this;
    }

    public UserBuilder setProfileSidebarFillColor(String str) {
        this.profileSidebarFillColor = str;
        return this;
    }

    public UserBuilder setProfileTextColor(String str) {
        this.profileTextColor = str;
        return this;
    }

    public UserBuilder setProfileUseBackgroundImage(boolean z) {
        this.profileUseBackgroundImage = z;
        return this;
    }

    public UserBuilder setProtectedUser(boolean z) {
        this.protectedUser = z;
        return this;
    }

    public UserBuilder setScreenName(String str) {
        this.screenName = str;
        return this;
    }

    public UserBuilder setShowAllInlineMedia(boolean z) {
        this.showAllInlineMedia = z;
        return this;
    }

    public UserBuilder setStatus(Tweet tweet) {
        this.status = tweet;
        return this;
    }

    public UserBuilder setStatusesCount(int i) {
        this.statusesCount = i;
        return this;
    }

    public UserBuilder setTimeZone(String str) {
        this.timeZone = str;
        return this;
    }

    public UserBuilder setUrl(String str) {
        this.url = str;
        return this;
    }

    public UserBuilder setUtcOffset(int i) {
        this.utcOffset = i;
        return this;
    }

    public UserBuilder setVerified(boolean z) {
        this.verified = z;
        return this;
    }

    public UserBuilder setWithheldInCountries(List<String> list) {
        this.withheldInCountries = list;
        return this;
    }

    public UserBuilder setWithheldScope(String str) {
        this.withheldScope = str;
        return this;
    }

    public User build() {
        return new User(this.contributorsEnabled, this.createdAt, this.defaultProfile, this.defaultProfileImage, this.description, this.email, this.entities, this.favouritesCount, this.followRequestSent, this.followersCount, this.friendsCount, this.geoEnabled, this.id, this.idStr, this.isTranslator, this.lang, this.listedCount, this.location, this.name, this.profileBackgroundColor, this.profileBackgroundImageUrl, this.profileBackgroundImageUrlHttps, this.profileBackgroundTile, this.profileBannerUrl, this.profileImageUrl, this.profileImageUrlHttps, this.profileLinkColor, this.profileSidebarBorderColor, this.profileSidebarFillColor, this.profileTextColor, this.profileUseBackgroundImage, this.protectedUser, this.screenName, this.showAllInlineMedia, this.status, this.statusesCount, this.timeZone, this.url, this.utcOffset, this.verified, this.withheldInCountries, this.withheldScope);
    }
}
