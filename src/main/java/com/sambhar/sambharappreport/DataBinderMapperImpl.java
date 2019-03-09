package com.sambhar.sambharappreport;

import android.databinding.DataBinderMapper;
import android.databinding.DataBindingComponent;
import android.databinding.ViewDataBinding;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import com.sambhar.sambharappreport.databinding.ActivityChangePasswordBindingImpl;
import com.sambhar.sambharappreport.databinding.ActivityEditProfileBindingImpl;
import com.sambhar.sambharappreport.databinding.ActivityHomeBindingImpl;
import com.sambhar.sambharappreport.databinding.ActivityLoginBindingImpl;
import com.sambhar.sambharappreport.databinding.ActivityMainBindingImpl;
import com.sambhar.sambharappreport.databinding.ActivityRegisterBindingImpl;
import com.sambhar.sambharappreport.databinding.ActivityRegisterDetailDataBindingImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataBinderMapperImpl extends DataBinderMapper {
    private static final SparseIntArray INTERNAL_LAYOUT_ID_LOOKUP = new SparseIntArray(7);
    private static final int LAYOUT_ACTIVITYCHANGEPASSWORD = 1;
    private static final int LAYOUT_ACTIVITYEDITPROFILE = 2;
    private static final int LAYOUT_ACTIVITYHOME = 3;
    private static final int LAYOUT_ACTIVITYLOGIN = 4;
    private static final int LAYOUT_ACTIVITYMAIN = 5;
    private static final int LAYOUT_ACTIVITYREGISTER = 6;
    private static final int LAYOUT_ACTIVITYREGISTERDETAILDATA = 7;

    private static class InnerBrLookup {
        static final SparseArray<String> sKeys = new SparseArray(2);

        private InnerBrLookup() {
        }

        static {
            sKeys.put(0, "_all");
        }
    }

    private static class InnerLayoutIdLookup {
        static final HashMap<String, Integer> sKeys = new HashMap(7);

        private InnerLayoutIdLookup() {
        }

        static {
            sKeys.put("layout/activity_change_password_0", Integer.valueOf(R.layout.activity_change_password));
            sKeys.put("layout/activity_edit_profile_0", Integer.valueOf(R.layout.activity_edit_profile));
            sKeys.put("layout/activity_home_0", Integer.valueOf(R.layout.activity_home));
            sKeys.put("layout/activity_login_0", Integer.valueOf(R.layout.activity_login));
            sKeys.put("layout/activity_main_0", Integer.valueOf(R.layout.activity_main));
            sKeys.put("layout/activity_register_0", Integer.valueOf(R.layout.activity_register));
            sKeys.put("layout/activity_register_detail_data_0", Integer.valueOf(R.layout.activity_register_detail_data));
        }
    }

    static {
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.activity_change_password, 1);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.activity_edit_profile, 2);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.activity_home, 3);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.activity_login, 4);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.activity_main, 5);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.activity_register, 6);
        INTERNAL_LAYOUT_ID_LOOKUP.put(R.layout.activity_register_detail_data, 7);
    }

    public ViewDataBinding getDataBinder(DataBindingComponent dataBindingComponent, View view, int i) {
        i = INTERNAL_LAYOUT_ID_LOOKUP.get(i);
        if (i > 0) {
            Object tag = view.getTag();
            if (tag != null) {
                StringBuilder stringBuilder;
                switch (i) {
                    case 1:
                        if ("layout/activity_change_password_0".equals(tag)) {
                            return new ActivityChangePasswordBindingImpl(dataBindingComponent, view);
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("The tag for activity_change_password is invalid. Received: ");
                        stringBuilder.append(tag);
                        throw new IllegalArgumentException(stringBuilder.toString());
                    case 2:
                        if ("layout/activity_edit_profile_0".equals(tag)) {
                            return new ActivityEditProfileBindingImpl(dataBindingComponent, view);
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("The tag for activity_edit_profile is invalid. Received: ");
                        stringBuilder.append(tag);
                        throw new IllegalArgumentException(stringBuilder.toString());
                    case 3:
                        if ("layout/activity_home_0".equals(tag)) {
                            return new ActivityHomeBindingImpl(dataBindingComponent, view);
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("The tag for activity_home is invalid. Received: ");
                        stringBuilder.append(tag);
                        throw new IllegalArgumentException(stringBuilder.toString());
                    case 4:
                        if ("layout/activity_login_0".equals(tag)) {
                            return new ActivityLoginBindingImpl(dataBindingComponent, view);
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("The tag for activity_login is invalid. Received: ");
                        stringBuilder.append(tag);
                        throw new IllegalArgumentException(stringBuilder.toString());
                    case 5:
                        if ("layout/activity_main_0".equals(tag)) {
                            return new ActivityMainBindingImpl(dataBindingComponent, view);
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("The tag for activity_main is invalid. Received: ");
                        stringBuilder.append(tag);
                        throw new IllegalArgumentException(stringBuilder.toString());
                    case 6:
                        if ("layout/activity_register_0".equals(tag)) {
                            return new ActivityRegisterBindingImpl(dataBindingComponent, view);
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("The tag for activity_register is invalid. Received: ");
                        stringBuilder.append(tag);
                        throw new IllegalArgumentException(stringBuilder.toString());
                    case 7:
                        if ("layout/activity_register_detail_data_0".equals(tag)) {
                            return new ActivityRegisterDetailDataBindingImpl(dataBindingComponent, view);
                        }
                        stringBuilder = new StringBuilder();
                        stringBuilder.append("The tag for activity_register_detail_data is invalid. Received: ");
                        stringBuilder.append(tag);
                        throw new IllegalArgumentException(stringBuilder.toString());
                }
            }
            throw new RuntimeException("view must have a tag");
        }
        return null;
    }

    /* JADX WARNING: Missing block: B:11:0x0022, code skipped:
            return null;
     */
    public android.databinding.ViewDataBinding getDataBinder(android.databinding.DataBindingComponent r2, android.view.View[] r3, int r4) {
        /*
        r1 = this;
        r2 = 0;
        if (r3 == 0) goto L_0x0022;
    L_0x0003:
        r0 = r3.length;
        if (r0 != 0) goto L_0x0007;
    L_0x0006:
        goto L_0x0022;
    L_0x0007:
        r0 = INTERNAL_LAYOUT_ID_LOOKUP;
        r4 = r0.get(r4);
        if (r4 <= 0) goto L_0x0021;
    L_0x000f:
        r4 = 0;
        r3 = r3[r4];
        r3 = r3.getTag();
        if (r3 == 0) goto L_0x0019;
    L_0x0018:
        goto L_0x0021;
    L_0x0019:
        r2 = new java.lang.RuntimeException;
        r3 = "view must have a tag";
        r2.<init>(r3);
        throw r2;
    L_0x0021:
        return r2;
    L_0x0022:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.sambhar.sambharappreport.DataBinderMapperImpl.getDataBinder(android.databinding.DataBindingComponent, android.view.View[], int):android.databinding.ViewDataBinding");
    }

    public int getLayoutId(String str) {
        int i = 0;
        if (str == null) {
            return 0;
        }
        Integer num = (Integer) InnerLayoutIdLookup.sKeys.get(str);
        if (num != null) {
            i = num.intValue();
        }
        return i;
    }

    public String convertBrIdToString(int i) {
        return (String) InnerBrLookup.sKeys.get(i);
    }

    public List<DataBinderMapper> collectDependencies() {
        ArrayList arrayList = new ArrayList(1);
        arrayList.add(new com.android.databinding.library.baseAdapters.DataBinderMapperImpl());
        return arrayList;
    }
}
