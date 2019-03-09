package com.facebook.appevents.codeless.internal;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.NestedScrollingChild;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import com.facebook.internal.Utility;
import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ViewHierarchy {
    private static final int ADAPTER_VIEW_ITEM_BITMASK = 9;
    private static final int BUTTON_BITMASK = 2;
    private static final int CHECKBOX_BITMASK = 15;
    private static final String CHILDREN_VIEW_KEY = "childviews";
    private static final String CLASS_NAME_KEY = "classname";
    private static final String CLASS_RCTROOTVIEW = "com.facebook.react.ReactRootView";
    private static final String CLASS_RCTTEXTVIEW = "com.facebook.react.views.view.ReactTextView";
    private static final String CLASS_RCTVIEWGROUP = "com.facebook.react.views.view.ReactViewGroup";
    private static final String CLASS_TOUCHTARGETHELPER = "com.facebook.react.uimanager.TouchTargetHelper";
    private static final String CLASS_TYPE_BITMASK_KEY = "classtypebitmask";
    private static final int CLICKABLE_VIEW_BITMASK = 5;
    private static final String DESC_KEY = "description";
    private static final String DIMENSION_HEIGHT_KEY = "height";
    private static final String DIMENSION_KEY = "dimension";
    private static final String DIMENSION_LEFT_KEY = "left";
    private static final String DIMENSION_SCROLL_X_KEY = "scrollx";
    private static final String DIMENSION_SCROLL_Y_KEY = "scrolly";
    private static final String DIMENSION_TOP_KEY = "top";
    private static final String DIMENSION_VISIBILITY_KEY = "visibility";
    private static final String DIMENSION_WIDTH_KEY = "width";
    private static final String GET_ACCESSIBILITY_METHOD = "getAccessibilityDelegate";
    private static final String HINT_KEY = "hint";
    private static final String ICON_BITMAP = "icon_image";
    private static final int ICON_MAX_EDGE_LENGTH = 44;
    private static final String ID_KEY = "id";
    private static final int IMAGEVIEW_BITMASK = 1;
    private static final int INPUT_BITMASK = 11;
    private static final int LABEL_BITMASK = 10;
    private static final String METHOD_FIND_TOUCHTARGET_VIEW = "findTouchTargetView";
    private static final int PICKER_BITMASK = 12;
    private static final int RADIO_GROUP_BITMASK = 14;
    private static final int RATINGBAR_BITMASK = 16;
    private static WeakReference<View> RCTRootViewReference = new WeakReference(null);
    private static final int REACT_NATIVE_BUTTON_BITMASK = 6;
    private static final int SWITCH_BITMASK = 13;
    private static final String TAG = ViewHierarchy.class.getCanonicalName();
    private static final String TAG_KEY = "tag";
    private static final int TEXTVIEW_BITMASK = 0;
    private static final String TEXT_IS_BOLD = "is_bold";
    private static final String TEXT_IS_ITALIC = "is_italic";
    private static final String TEXT_KEY = "text";
    private static final String TEXT_SIZE = "font_size";
    private static final String TEXT_STYLE = "text_style";
    @Nullable
    private static Method methodFindTouchTargetView = null;

    @Nullable
    public static ViewGroup getParentOfView(View view) {
        if (view == null) {
            return null;
        }
        ViewParent parent = view.getParent();
        if (parent == null || !(parent instanceof ViewGroup)) {
            return null;
        }
        return (ViewGroup) parent;
    }

    public static List<View> getChildrenOfView(View view) {
        ArrayList arrayList = new ArrayList();
        if (view != null && (view instanceof ViewGroup)) {
            ViewGroup viewGroup = (ViewGroup) view;
            int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                arrayList.add(viewGroup.getChildAt(i));
            }
        }
        return arrayList;
    }

    public static JSONObject setBasicInfoOfView(View view, JSONObject jSONObject) {
        try {
            String textOfView = getTextOfView(view);
            String hintOfView = getHintOfView(view);
            Object tag = view.getTag();
            CharSequence contentDescription = view.getContentDescription();
            jSONObject.put(CLASS_NAME_KEY, view.getClass().getCanonicalName());
            jSONObject.put(CLASS_TYPE_BITMASK_KEY, getClassTypeBitmask(view));
            jSONObject.put("id", view.getId());
            if (SensitiveUserDataUtils.isSensitiveUserData(view)) {
                jSONObject.put(TEXT_KEY, "");
            } else {
                jSONObject.put(TEXT_KEY, textOfView);
            }
            jSONObject.put(HINT_KEY, hintOfView);
            if (tag != null) {
                jSONObject.put(TAG_KEY, tag.toString());
            }
            if (contentDescription != null) {
                jSONObject.put("description", contentDescription.toString());
            }
            jSONObject.put(DIMENSION_KEY, getDimensionOfView(view));
        } catch (JSONException e) {
            Utility.logd(TAG, e);
        }
        return jSONObject;
    }

    public static JSONObject setAppearanceOfView(View view, JSONObject jSONObject, float f) {
        try {
            JSONObject jSONObject2 = new JSONObject();
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                Typeface typeface = textView.getTypeface();
                if (typeface != null) {
                    jSONObject2.put(TEXT_SIZE, (double) textView.getTextSize());
                    jSONObject2.put(TEXT_IS_BOLD, typeface.isBold());
                    jSONObject2.put(TEXT_IS_ITALIC, typeface.isItalic());
                    jSONObject.put(TEXT_STYLE, jSONObject2);
                }
            }
            if (view instanceof ImageView) {
                Drawable drawable = ((ImageView) view).getDrawable();
                if ((drawable instanceof BitmapDrawable) && ((float) view.getHeight()) / f <= 44.0f && ((float) view.getWidth()) / f <= 44.0f) {
                    Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
                    if (bitmap != null) {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(CompressFormat.PNG, 100, byteArrayOutputStream);
                        jSONObject.put(ICON_BITMAP, Base64.encodeToString(byteArrayOutputStream.toByteArray(), 0));
                    }
                }
            }
        } catch (JSONException e) {
            Utility.logd(TAG, e);
        }
        return jSONObject;
    }

    public static JSONObject getDictionaryOfView(View view) {
        JSONObject basicInfoOfView;
        Throwable e;
        if (view.getClass().getName().equals(CLASS_RCTROOTVIEW)) {
            RCTRootViewReference = new WeakReference(view);
        }
        JSONObject jSONObject = new JSONObject();
        try {
            basicInfoOfView = setBasicInfoOfView(view, jSONObject);
            try {
                JSONArray jSONArray = new JSONArray();
                List childrenOfView = getChildrenOfView(view);
                for (int i = 0; i < childrenOfView.size(); i++) {
                    jSONArray.put(getDictionaryOfView((View) childrenOfView.get(i)));
                }
                basicInfoOfView.put(CHILDREN_VIEW_KEY, jSONArray);
            } catch (JSONException e2) {
                e = e2;
                Log.e(TAG, "Failed to create JSONObject for view.", e);
                return basicInfoOfView;
            }
        } catch (JSONException e3) {
            e = e3;
            basicInfoOfView = jSONObject;
            Log.e(TAG, "Failed to create JSONObject for view.", e);
            return basicInfoOfView;
        }
        return basicInfoOfView;
    }

    private static int getClassTypeBitmask(View view) {
        int i = view instanceof ImageView ? 2 : 0;
        if (isClickableView(view)) {
            i |= 32;
        }
        if (isAdapterViewItem(view)) {
            i |= 512;
        }
        if (view instanceof TextView) {
            i = (i | 1024) | 1;
            if (view instanceof Button) {
                i |= 4;
                if (view instanceof Switch) {
                    i |= 8192;
                } else if (view instanceof CheckBox) {
                    i |= 32768;
                }
            }
            if (view instanceof EditText) {
                return i | 2048;
            }
            return i;
        } else if ((view instanceof Spinner) || (view instanceof DatePicker)) {
            return i | 4096;
        } else {
            if (view instanceof RatingBar) {
                return i | 65536;
            }
            if (view instanceof RadioGroup) {
                return i | 16384;
            }
            return ((view instanceof ViewGroup) && isRCTButton(view, (View) RCTRootViewReference.get())) ? i | 64 : i;
        }
    }

    public static boolean isClickableView(View view) {
        boolean z = false;
        try {
            Field declaredField = Class.forName("android.view.View").getDeclaredField("mListenerInfo");
            if (declaredField != null) {
                declaredField.setAccessible(true);
            }
            Object obj = declaredField.get(view);
            if (obj == null) {
                return false;
            }
            OnClickListener onClickListener = null;
            Field declaredField2 = Class.forName("android.view.View$ListenerInfo").getDeclaredField("mOnClickListener");
            if (declaredField2 != null) {
                onClickListener = (OnClickListener) declaredField2.get(obj);
            }
            if (onClickListener != null) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            Log.e(TAG, "Failed to check if the view is clickable.", e);
            return false;
        }
    }

    private static boolean isAdapterViewItem(View view) {
        ViewParent parent = view.getParent();
        return parent != null && ((parent instanceof AdapterView) || (parent instanceof NestedScrollingChild));
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x00d0  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00cd  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00cd  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00d0  */
    public static java.lang.String getTextOfView(android.view.View r7) {
        /*
        r0 = r7 instanceof android.widget.TextView;
        r1 = 0;
        if (r0 == 0) goto L_0x0020;
    L_0x0005:
        r0 = r7;
        r0 = (android.widget.TextView) r0;
        r1 = r0.getText();
        r0 = r7 instanceof android.widget.Switch;
        if (r0 == 0) goto L_0x00cb;
    L_0x0010:
        r7 = (android.widget.Switch) r7;
        r7 = r7.isChecked();
        if (r7 == 0) goto L_0x001d;
    L_0x0018:
        r7 = "1";
    L_0x001a:
        r1 = r7;
        goto L_0x00cb;
    L_0x001d:
        r7 = "0";
        goto L_0x001a;
    L_0x0020:
        r0 = r7 instanceof android.widget.Spinner;
        if (r0 == 0) goto L_0x0038;
    L_0x0024:
        r7 = (android.widget.Spinner) r7;
        r0 = r7.getCount();
        if (r0 <= 0) goto L_0x00cb;
    L_0x002c:
        r7 = r7.getSelectedItem();
        if (r7 == 0) goto L_0x00cb;
    L_0x0032:
        r1 = r7.toString();
        goto L_0x00cb;
    L_0x0038:
        r0 = r7 instanceof android.widget.DatePicker;
        r2 = 2;
        r3 = 1;
        r4 = 0;
        if (r0 == 0) goto L_0x0069;
    L_0x003f:
        r7 = (android.widget.DatePicker) r7;
        r0 = r7.getYear();
        r1 = r7.getMonth();
        r7 = r7.getDayOfMonth();
        r5 = "%04d-%02d-%02d";
        r6 = 3;
        r6 = new java.lang.Object[r6];
        r0 = java.lang.Integer.valueOf(r0);
        r6[r4] = r0;
        r0 = java.lang.Integer.valueOf(r1);
        r6[r3] = r0;
        r7 = java.lang.Integer.valueOf(r7);
        r6[r2] = r7;
        r1 = java.lang.String.format(r5, r6);
        goto L_0x00cb;
    L_0x0069:
        r0 = r7 instanceof android.widget.TimePicker;
        if (r0 == 0) goto L_0x0094;
    L_0x006d:
        r7 = (android.widget.TimePicker) r7;
        r0 = r7.getCurrentHour();
        r0 = r0.intValue();
        r7 = r7.getCurrentMinute();
        r7 = r7.intValue();
        r1 = "%02d:%02d";
        r2 = new java.lang.Object[r2];
        r0 = java.lang.Integer.valueOf(r0);
        r2[r4] = r0;
        r7 = java.lang.Integer.valueOf(r7);
        r2[r3] = r7;
        r1 = java.lang.String.format(r1, r2);
        goto L_0x00cb;
    L_0x0094:
        r0 = r7 instanceof android.widget.RadioGroup;
        if (r0 == 0) goto L_0x00bd;
    L_0x0098:
        r7 = (android.widget.RadioGroup) r7;
        r0 = r7.getCheckedRadioButtonId();
        r2 = r7.getChildCount();
    L_0x00a2:
        if (r4 >= r2) goto L_0x00cb;
    L_0x00a4:
        r3 = r7.getChildAt(r4);
        r5 = r3.getId();
        if (r5 != r0) goto L_0x00ba;
    L_0x00ae:
        r5 = r3 instanceof android.widget.RadioButton;
        if (r5 == 0) goto L_0x00ba;
    L_0x00b2:
        r3 = (android.widget.RadioButton) r3;
        r7 = r3.getText();
        goto L_0x001a;
    L_0x00ba:
        r4 = r4 + 1;
        goto L_0x00a2;
    L_0x00bd:
        r0 = r7 instanceof android.widget.RatingBar;
        if (r0 == 0) goto L_0x00cb;
    L_0x00c1:
        r7 = (android.widget.RatingBar) r7;
        r7 = r7.getRating();
        r1 = java.lang.String.valueOf(r7);
    L_0x00cb:
        if (r1 != 0) goto L_0x00d0;
    L_0x00cd:
        r7 = "";
        goto L_0x00d4;
    L_0x00d0:
        r7 = r1.toString();
    L_0x00d4:
        return r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.facebook.appevents.codeless.internal.ViewHierarchy.getTextOfView(android.view.View):java.lang.String");
    }

    public static String getHintOfView(View view) {
        Object hint = view instanceof TextView ? ((TextView) view).getHint() : view instanceof EditText ? ((EditText) view).getHint() : null;
        if (hint == null) {
            return "";
        }
        return hint.toString();
    }

    private static JSONObject getDimensionOfView(View view) {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put(DIMENSION_TOP_KEY, view.getTop());
            jSONObject.put(DIMENSION_LEFT_KEY, view.getLeft());
            jSONObject.put(DIMENSION_WIDTH_KEY, view.getWidth());
            jSONObject.put(DIMENSION_HEIGHT_KEY, view.getHeight());
            jSONObject.put(DIMENSION_SCROLL_X_KEY, view.getScrollX());
            jSONObject.put(DIMENSION_SCROLL_Y_KEY, view.getScrollY());
            jSONObject.put(DIMENSION_VISIBILITY_KEY, view.getVisibility());
        } catch (JSONException e) {
            Log.e(TAG, "Failed to create JSONObject for dimension.", e);
        }
        return jSONObject;
    }

    @Nullable
    public static AccessibilityDelegate getExistingDelegate(View view) {
        try {
            return (AccessibilityDelegate) view.getClass().getMethod(GET_ACCESSIBILITY_METHOD, new Class[0]).invoke(view, new Object[0]);
        } catch (NoSuchMethodException unused) {
            return null;
        } catch (NullPointerException unused2) {
            return null;
        } catch (SecurityException unused3) {
            return null;
        } catch (IllegalAccessException unused4) {
            return null;
        } catch (InvocationTargetException unused5) {
            return null;
        }
    }

    @Nullable
    public static OnTouchListener getExistingOnTouchListener(View view) {
        try {
            Field declaredField = Class.forName("android.view.View").getDeclaredField("mListenerInfo");
            if (declaredField != null) {
                declaredField.setAccessible(true);
            }
            Object obj = declaredField.get(view);
            if (obj == null) {
                return null;
            }
            OnTouchListener onTouchListener;
            declaredField = Class.forName("android.view.View$ListenerInfo").getDeclaredField("mOnTouchListener");
            if (declaredField != null) {
                declaredField.setAccessible(true);
                onTouchListener = (OnTouchListener) declaredField.get(obj);
            } else {
                onTouchListener = null;
            }
            return onTouchListener;
        } catch (NoSuchFieldException e) {
            Utility.logd(TAG, e);
            return null;
        } catch (ClassNotFoundException e2) {
            Utility.logd(TAG, e2);
            return null;
        } catch (IllegalAccessException e22) {
            Utility.logd(TAG, e22);
            return null;
        }
    }

    @Nullable
    public static View getTouchReactView(float[] fArr, @Nullable View view) {
        initTouchTargetHelperMethods();
        if (methodFindTouchTargetView == null || view == null) {
            return null;
        }
        try {
            View view2 = (View) methodFindTouchTargetView.invoke(null, new Object[]{fArr, view});
            if (view2 != null && view2.getId() > 0) {
                view2 = (View) view2.getParent();
                if (view2 != null) {
                    return view2;
                }
            }
        } catch (IllegalAccessException e) {
            Utility.logd(TAG, e);
        } catch (InvocationTargetException e2) {
            Utility.logd(TAG, e2);
        }
        return null;
    }

    public static boolean isRCTButton(View view, @Nullable View view2) {
        boolean z = false;
        if (!view.getClass().getName().equals(CLASS_RCTVIEWGROUP)) {
            return false;
        }
        view2 = getTouchReactView(getViewLocationOnScreen(view), view2);
        if (view2 != null && view2.getId() == view.getId()) {
            z = true;
        }
        return z;
    }

    public static boolean isRCTRootView(View view) {
        return view.getClass().getName().equals(CLASS_RCTROOTVIEW);
    }

    public static boolean isRCTTextView(View view) {
        return view.getClass().getName().equals(CLASS_RCTTEXTVIEW);
    }

    public static boolean isRCTViewGroup(View view) {
        return view.getClass().getName().equals(CLASS_RCTVIEWGROUP);
    }

    @Nullable
    public static View findRCTRootView(View view) {
        while (view != null) {
            if (!isRCTRootView(view)) {
                ViewParent parent = view.getParent();
                if (parent == null || !(parent instanceof View)) {
                    break;
                }
                view = (View) parent;
            } else {
                return view;
            }
        }
        return null;
    }

    private static float[] getViewLocationOnScreen(View view) {
        float[] fArr = new float[2];
        r0 = new int[2];
        view.getLocationOnScreen(r0);
        fArr[0] = (float) r0[0];
        fArr[1] = (float) r0[1];
        return fArr;
    }

    private static void initTouchTargetHelperMethods() {
        if (methodFindTouchTargetView == null) {
            try {
                methodFindTouchTargetView = Class.forName(CLASS_TOUCHTARGETHELPER).getDeclaredMethod(METHOD_FIND_TOUCHTARGET_VIEW, new Class[]{float[].class, ViewGroup.class});
                methodFindTouchTargetView.setAccessible(true);
            } catch (ClassNotFoundException e) {
                Utility.logd(TAG, e);
            } catch (NoSuchMethodException e2) {
                Utility.logd(TAG, e2);
            }
        }
    }
}
