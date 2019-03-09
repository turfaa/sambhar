package com.sambhar.sambharappreport.entity;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;

public class RegisterDataEntity implements Parcelable {
    public static final Creator<RegisterDataEntity> CREATOR = new Creator<RegisterDataEntity>() {
        public RegisterDataEntity createFromParcel(Parcel parcel) {
            return new RegisterDataEntity(parcel);
        }

        public RegisterDataEntity[] newArray(int i) {
            return new RegisterDataEntity[i];
        }
    };
    @SerializedName("companies")
    private ArrayList<GroupEntity> companyEntityList;
    @SerializedName("task_groups")
    private ArrayList<GroupEntity> groupEntityList;

    public int describeContents() {
        return 0;
    }

    protected RegisterDataEntity(Parcel parcel) {
        this.companyEntityList = parcel.createTypedArrayList(GroupEntity.CREATOR);
        this.groupEntityList = parcel.createTypedArrayList(GroupEntity.CREATOR);
    }

    public ArrayList<GroupEntity> getCompanyEntityList() {
        return this.companyEntityList;
    }

    public void setCompanyEntityList(ArrayList<GroupEntity> arrayList) {
        this.companyEntityList = arrayList;
    }

    public ArrayList<GroupEntity> getGroupEntityList() {
        return this.groupEntityList;
    }

    public void setGroupEntityList(ArrayList<GroupEntity> arrayList) {
        this.groupEntityList = arrayList;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeTypedList(this.companyEntityList);
        parcel.writeTypedList(this.groupEntityList);
    }
}
