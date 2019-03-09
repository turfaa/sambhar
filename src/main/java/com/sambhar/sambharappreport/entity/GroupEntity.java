package com.sambhar.sambharappreport.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.SerializedName;

public class GroupEntity implements Parcelable {
    public static final Creator<GroupEntity> CREATOR = new Creator<GroupEntity>() {
        public GroupEntity createFromParcel(Parcel parcel) {
            return new GroupEntity(parcel);
        }

        public GroupEntity[] newArray(int i) {
            return new GroupEntity[i];
        }
    };
    @SerializedName("company_id")
    private int companyId;
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;

    public int describeContents() {
        return 0;
    }

    protected GroupEntity(Parcel parcel) {
        this.id = parcel.readInt();
        this.companyId = parcel.readInt();
        this.name = parcel.readString();
    }

    public int getId() {
        return this.id;
    }

    public void setId(int i) {
        this.id = i;
    }

    public int getCompanyId() {
        return this.companyId;
    }

    public void setCompanyId(int i) {
        this.companyId = i;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.id);
        parcel.writeInt(this.companyId);
        parcel.writeString(this.name);
    }
}
