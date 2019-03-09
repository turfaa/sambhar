package android.support.v4.graphics.drawable;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Preconditions;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import androidx.versionedparcelable.CustomVersionedParcelable;
import java.io.ByteArrayOutputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;

public class IconCompat extends CustomVersionedParcelable {
    private static final float ADAPTIVE_ICON_INSET_FACTOR = 0.25f;
    private static final int AMBIENT_SHADOW_ALPHA = 30;
    private static final float BLUR_FACTOR = 0.010416667f;
    static final Mode DEFAULT_TINT_MODE = Mode.SRC_IN;
    private static final float DEFAULT_VIEW_PORT_SCALE = 0.6666667f;
    private static final String EXTRA_INT1 = "int1";
    private static final String EXTRA_INT2 = "int2";
    private static final String EXTRA_OBJ = "obj";
    private static final String EXTRA_TINT_LIST = "tint_list";
    private static final String EXTRA_TINT_MODE = "tint_mode";
    private static final String EXTRA_TYPE = "type";
    private static final float ICON_DIAMETER_FACTOR = 0.9166667f;
    private static final int KEY_SHADOW_ALPHA = 61;
    private static final float KEY_SHADOW_OFFSET_FACTOR = 0.020833334f;
    private static final String TAG = "IconCompat";
    public static final int TYPE_UNKNOWN = -1;
    @RestrictTo({Scope.LIBRARY})
    public byte[] mData;
    @RestrictTo({Scope.LIBRARY})
    public int mInt1;
    @RestrictTo({Scope.LIBRARY})
    public int mInt2;
    Object mObj1;
    @RestrictTo({Scope.LIBRARY})
    public Parcelable mParcelable;
    @RestrictTo({Scope.LIBRARY})
    public ColorStateList mTintList = null;
    Mode mTintMode = DEFAULT_TINT_MODE;
    @RestrictTo({Scope.LIBRARY})
    public String mTintModeStr;
    @RestrictTo({Scope.LIBRARY})
    public int mType;

    @RestrictTo({Scope.LIBRARY})
    @Retention(RetentionPolicy.SOURCE)
    public @interface IconType {
    }

    private static String typeToString(int i) {
        switch (i) {
            case 1:
                return "BITMAP";
            case 2:
                return "RESOURCE";
            case 3:
                return "DATA";
            case 4:
                return "URI";
            case 5:
                return "BITMAP_MASKABLE";
            default:
                return "UNKNOWN";
        }
    }

    public static IconCompat createWithResource(Context context, @DrawableRes int i) {
        if (context != null) {
            return createWithResource(context.getResources(), context.getPackageName(), i);
        }
        throw new IllegalArgumentException("Context must not be null.");
    }

    @RestrictTo({Scope.LIBRARY})
    public static IconCompat createWithResource(Resources resources, String str, @DrawableRes int i) {
        if (str == null) {
            throw new IllegalArgumentException("Package must not be null.");
        } else if (i != 0) {
            IconCompat iconCompat = new IconCompat(2);
            iconCompat.mInt1 = i;
            if (resources != null) {
                try {
                    iconCompat.mObj1 = resources.getResourceName(i);
                } catch (NotFoundException unused) {
                    throw new IllegalArgumentException("Icon resource cannot be found");
                }
            }
            iconCompat.mObj1 = str;
            return iconCompat;
        } else {
            throw new IllegalArgumentException("Drawable resource ID must not be 0");
        }
    }

    public static IconCompat createWithBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            IconCompat iconCompat = new IconCompat(1);
            iconCompat.mObj1 = bitmap;
            return iconCompat;
        }
        throw new IllegalArgumentException("Bitmap must not be null.");
    }

    public static IconCompat createWithAdaptiveBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            IconCompat iconCompat = new IconCompat(5);
            iconCompat.mObj1 = bitmap;
            return iconCompat;
        }
        throw new IllegalArgumentException("Bitmap must not be null.");
    }

    public static IconCompat createWithData(byte[] bArr, int i, int i2) {
        if (bArr != null) {
            IconCompat iconCompat = new IconCompat(3);
            iconCompat.mObj1 = bArr;
            iconCompat.mInt1 = i;
            iconCompat.mInt2 = i2;
            return iconCompat;
        }
        throw new IllegalArgumentException("Data must not be null.");
    }

    public static IconCompat createWithContentUri(String str) {
        if (str != null) {
            IconCompat iconCompat = new IconCompat(4);
            iconCompat.mObj1 = str;
            return iconCompat;
        }
        throw new IllegalArgumentException("Uri must not be null.");
    }

    public static IconCompat createWithContentUri(Uri uri) {
        if (uri != null) {
            return createWithContentUri(uri.toString());
        }
        throw new IllegalArgumentException("Uri must not be null.");
    }

    private IconCompat(int i) {
        this.mType = i;
    }

    public int getType() {
        if (this.mType != -1 || VERSION.SDK_INT < 23) {
            return this.mType;
        }
        return getType((Icon) this.mObj1);
    }

    @NonNull
    public String getResPackage() {
        if (this.mType == -1 && VERSION.SDK_INT >= 23) {
            return getResPackage((Icon) this.mObj1);
        }
        if (this.mType == 2) {
            return ((String) this.mObj1).split(":", -1)[0];
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("called getResPackage() on ");
        stringBuilder.append(this);
        throw new IllegalStateException(stringBuilder.toString());
    }

    @IdRes
    public int getResId() {
        if (this.mType == -1 && VERSION.SDK_INT >= 23) {
            return getResId((Icon) this.mObj1);
        }
        if (this.mType == 2) {
            return this.mInt1;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("called getResId() on ");
        stringBuilder.append(this);
        throw new IllegalStateException(stringBuilder.toString());
    }

    @NonNull
    public Uri getUri() {
        if (this.mType != -1 || VERSION.SDK_INT < 23) {
            return Uri.parse((String) this.mObj1);
        }
        return getUri((Icon) this.mObj1);
    }

    public IconCompat setTint(@ColorInt int i) {
        return setTintList(ColorStateList.valueOf(i));
    }

    public IconCompat setTintList(ColorStateList colorStateList) {
        this.mTintList = colorStateList;
        return this;
    }

    public IconCompat setTintMode(Mode mode) {
        this.mTintMode = mode;
        return this;
    }

    @RequiresApi(23)
    public Icon toIcon() {
        int i = this.mType;
        if (i == -1) {
            return (Icon) this.mObj1;
        }
        Icon createWithBitmap;
        switch (i) {
            case 1:
                createWithBitmap = Icon.createWithBitmap((Bitmap) this.mObj1);
                break;
            case 2:
                createWithBitmap = Icon.createWithResource(getResPackage(), this.mInt1);
                break;
            case 3:
                createWithBitmap = Icon.createWithData((byte[]) this.mObj1, this.mInt1, this.mInt2);
                break;
            case 4:
                createWithBitmap = Icon.createWithContentUri((String) this.mObj1);
                break;
            case 5:
                if (VERSION.SDK_INT < 26) {
                    createWithBitmap = Icon.createWithBitmap(createLegacyIconFromAdaptiveIcon((Bitmap) this.mObj1, false));
                    break;
                }
                createWithBitmap = Icon.createWithAdaptiveBitmap((Bitmap) this.mObj1);
                break;
            default:
                throw new IllegalArgumentException("Unknown type");
        }
        if (this.mTintList != null) {
            createWithBitmap.setTintList(this.mTintList);
        }
        if (this.mTintMode != DEFAULT_TINT_MODE) {
            createWithBitmap.setTintMode(this.mTintMode);
        }
        return createWithBitmap;
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public void checkResource(Context context) {
        if (this.mType == 2) {
            String str = (String) this.mObj1;
            if (str.contains(":")) {
                String str2 = str.split(":", -1)[1];
                String str3 = str2.split("/", -1)[0];
                str2 = str2.split("/", -1)[1];
                str = str.split(":", -1)[0];
                int identifier = getResources(context, str).getIdentifier(str2, str3, str);
                if (this.mInt1 != identifier) {
                    String str4 = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Id has changed for ");
                    stringBuilder.append(str);
                    stringBuilder.append("/");
                    stringBuilder.append(str2);
                    Log.i(str4, stringBuilder.toString());
                    this.mInt1 = identifier;
                }
            }
        }
    }

    public Drawable loadDrawable(Context context) {
        checkResource(context);
        if (VERSION.SDK_INT >= 23) {
            return toIcon().loadDrawable(context);
        }
        Drawable loadDrawableInner = loadDrawableInner(context);
        if (!(loadDrawableInner == null || (this.mTintList == null && this.mTintMode == DEFAULT_TINT_MODE))) {
            loadDrawableInner.mutate();
            DrawableCompat.setTintList(loadDrawableInner, this.mTintList);
            DrawableCompat.setTintMode(loadDrawableInner, this.mTintMode);
        }
        return loadDrawableInner;
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0082  */
    private android.graphics.drawable.Drawable loadDrawableInner(android.content.Context r7) {
        /*
        r6 = this;
        r0 = r6.mType;
        r1 = 0;
        r2 = 0;
        switch(r0) {
            case 1: goto L_0x00e0;
            case 2: goto L_0x00a6;
            case 3: goto L_0x0090;
            case 4: goto L_0x001b;
            case 5: goto L_0x0009;
            default: goto L_0x0007;
        };
    L_0x0007:
        goto L_0x00ee;
    L_0x0009:
        r0 = new android.graphics.drawable.BitmapDrawable;
        r7 = r7.getResources();
        r2 = r6.mObj1;
        r2 = (android.graphics.Bitmap) r2;
        r1 = createLegacyIconFromAdaptiveIcon(r2, r1);
        r0.<init>(r7, r1);
        return r0;
    L_0x001b:
        r0 = r6.mObj1;
        r0 = (java.lang.String) r0;
        r0 = android.net.Uri.parse(r0);
        r1 = r0.getScheme();
        r3 = "content";
        r3 = r3.equals(r1);
        if (r3 != 0) goto L_0x005f;
    L_0x002f:
        r3 = "file";
        r1 = r3.equals(r1);
        if (r1 == 0) goto L_0x0038;
    L_0x0037:
        goto L_0x005f;
    L_0x0038:
        r1 = new java.io.FileInputStream;	 Catch:{ FileNotFoundException -> 0x0047 }
        r3 = new java.io.File;	 Catch:{ FileNotFoundException -> 0x0047 }
        r4 = r6.mObj1;	 Catch:{ FileNotFoundException -> 0x0047 }
        r4 = (java.lang.String) r4;	 Catch:{ FileNotFoundException -> 0x0047 }
        r3.<init>(r4);	 Catch:{ FileNotFoundException -> 0x0047 }
        r1.<init>(r3);	 Catch:{ FileNotFoundException -> 0x0047 }
        goto L_0x0080;
    L_0x0047:
        r1 = move-exception;
        r3 = "IconCompat";
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "Unable to load image from path: ";
        r4.append(r5);
        r4.append(r0);
        r0 = r4.toString();
        android.util.Log.w(r3, r0, r1);
        goto L_0x007f;
    L_0x005f:
        r1 = r7.getContentResolver();	 Catch:{ Exception -> 0x0068 }
        r1 = r1.openInputStream(r0);	 Catch:{ Exception -> 0x0068 }
        goto L_0x0080;
    L_0x0068:
        r1 = move-exception;
        r3 = "IconCompat";
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = "Unable to load image from URI: ";
        r4.append(r5);
        r4.append(r0);
        r0 = r4.toString();
        android.util.Log.w(r3, r0, r1);
    L_0x007f:
        r1 = r2;
    L_0x0080:
        if (r1 == 0) goto L_0x00ee;
    L_0x0082:
        r0 = new android.graphics.drawable.BitmapDrawable;
        r7 = r7.getResources();
        r1 = android.graphics.BitmapFactory.decodeStream(r1);
        r0.<init>(r7, r1);
        return r0;
    L_0x0090:
        r0 = new android.graphics.drawable.BitmapDrawable;
        r7 = r7.getResources();
        r1 = r6.mObj1;
        r1 = (byte[]) r1;
        r2 = r6.mInt1;
        r3 = r6.mInt2;
        r1 = android.graphics.BitmapFactory.decodeByteArray(r1, r2, r3);
        r0.<init>(r7, r1);
        return r0;
    L_0x00a6:
        r0 = r6.getResPackage();
        r3 = android.text.TextUtils.isEmpty(r0);
        if (r3 == 0) goto L_0x00b4;
    L_0x00b0:
        r0 = r7.getPackageName();
    L_0x00b4:
        r0 = getResources(r7, r0);
        r3 = r6.mInt1;	 Catch:{ RuntimeException -> 0x00c3 }
        r7 = r7.getTheme();	 Catch:{ RuntimeException -> 0x00c3 }
        r7 = android.support.v4.content.res.ResourcesCompat.getDrawable(r0, r3, r7);	 Catch:{ RuntimeException -> 0x00c3 }
        return r7;
    L_0x00c3:
        r7 = move-exception;
        r0 = "IconCompat";
        r3 = "Unable to load resource 0x%08x from pkg=%s";
        r4 = 2;
        r4 = new java.lang.Object[r4];
        r5 = r6.mInt1;
        r5 = java.lang.Integer.valueOf(r5);
        r4[r1] = r5;
        r1 = 1;
        r5 = r6.mObj1;
        r4[r1] = r5;
        r1 = java.lang.String.format(r3, r4);
        android.util.Log.e(r0, r1, r7);
        goto L_0x00ee;
    L_0x00e0:
        r0 = new android.graphics.drawable.BitmapDrawable;
        r7 = r7.getResources();
        r1 = r6.mObj1;
        r1 = (android.graphics.Bitmap) r1;
        r0.<init>(r7, r1);
        return r0;
    L_0x00ee:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.graphics.drawable.IconCompat.loadDrawableInner(android.content.Context):android.graphics.drawable.Drawable");
    }

    private static Resources getResources(Context context, String str) {
        if ("android".equals(str)) {
            return Resources.getSystem();
        }
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(str, 8192);
            if (applicationInfo != null) {
                return packageManager.getResourcesForApplication(applicationInfo);
            }
            return null;
        } catch (NameNotFoundException e) {
            Log.e(TAG, String.format("Unable to find pkg=%s for icon", new Object[]{str}), e);
            return null;
        }
    }

    @RestrictTo({Scope.LIBRARY_GROUP})
    public void addToShortcutIntent(@NonNull Intent intent, @Nullable Drawable drawable, @NonNull Context context) {
        Bitmap bitmap;
        checkResource(context);
        int i = this.mType;
        if (i != 5) {
            switch (i) {
                case 1:
                    bitmap = (Bitmap) this.mObj1;
                    if (drawable != null) {
                        bitmap = bitmap.copy(bitmap.getConfig(), true);
                        break;
                    }
                    break;
                case 2:
                    try {
                        context = context.createPackageContext(getResPackage(), 0);
                        if (drawable == null) {
                            intent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", ShortcutIconResource.fromContext(context, this.mInt1));
                            return;
                        }
                        Drawable drawable2 = ContextCompat.getDrawable(context, this.mInt1);
                        if (drawable2.getIntrinsicWidth() > 0) {
                            if (drawable2.getIntrinsicHeight() > 0) {
                                bitmap = Bitmap.createBitmap(drawable2.getIntrinsicWidth(), drawable2.getIntrinsicHeight(), Config.ARGB_8888);
                                drawable2.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                                drawable2.draw(new Canvas(bitmap));
                                break;
                            }
                        }
                        int launcherLargeIconSize = ((ActivityManager) context.getSystemService("activity")).getLauncherLargeIconSize();
                        bitmap = Bitmap.createBitmap(launcherLargeIconSize, launcherLargeIconSize, Config.ARGB_8888);
                        drawable2.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
                        drawable2.draw(new Canvas(bitmap));
                    } catch (NameNotFoundException e) {
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Can't find package ");
                        stringBuilder.append(this.mObj1);
                        throw new IllegalArgumentException(stringBuilder.toString(), e);
                    }
                default:
                    throw new IllegalArgumentException("Icon type not supported for intent shortcuts");
            }
        }
        bitmap = createLegacyIconFromAdaptiveIcon((Bitmap) this.mObj1, true);
        if (drawable != null) {
            i = bitmap.getWidth();
            int height = bitmap.getHeight();
            drawable.setBounds(i / 2, height / 2, i, height);
            drawable.draw(new Canvas(bitmap));
        }
        intent.putExtra("android.intent.extra.shortcut.ICON", bitmap);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        int i = this.mType;
        if (i != -1) {
            switch (i) {
                case 1:
                case 5:
                    bundle.putParcelable(EXTRA_OBJ, (Bitmap) this.mObj1);
                    break;
                case 2:
                case 4:
                    bundle.putString(EXTRA_OBJ, (String) this.mObj1);
                    break;
                case 3:
                    bundle.putByteArray(EXTRA_OBJ, (byte[]) this.mObj1);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid icon");
            }
        }
        bundle.putParcelable(EXTRA_OBJ, (Parcelable) this.mObj1);
        bundle.putInt("type", this.mType);
        bundle.putInt(EXTRA_INT1, this.mInt1);
        bundle.putInt(EXTRA_INT2, this.mInt2);
        if (this.mTintList != null) {
            bundle.putParcelable(EXTRA_TINT_LIST, this.mTintList);
        }
        if (this.mTintMode != DEFAULT_TINT_MODE) {
            bundle.putString(EXTRA_TINT_MODE, this.mTintMode.name());
        }
        return bundle;
    }

    public String toString() {
        if (this.mType == -1) {
            return String.valueOf(this.mObj1);
        }
        StringBuilder stringBuilder = new StringBuilder("Icon(typ=");
        stringBuilder.append(typeToString(this.mType));
        switch (this.mType) {
            case 1:
            case 5:
                stringBuilder.append(" size=");
                stringBuilder.append(((Bitmap) this.mObj1).getWidth());
                stringBuilder.append("x");
                stringBuilder.append(((Bitmap) this.mObj1).getHeight());
                break;
            case 2:
                stringBuilder.append(" pkg=");
                stringBuilder.append(getResPackage());
                stringBuilder.append(" id=");
                stringBuilder.append(String.format("0x%08x", new Object[]{Integer.valueOf(getResId())}));
                break;
            case 3:
                stringBuilder.append(" len=");
                stringBuilder.append(this.mInt1);
                if (this.mInt2 != 0) {
                    stringBuilder.append(" off=");
                    stringBuilder.append(this.mInt2);
                    break;
                }
                break;
            case 4:
                stringBuilder.append(" uri=");
                stringBuilder.append(this.mObj1);
                break;
        }
        if (this.mTintList != null) {
            stringBuilder.append(" tint=");
            stringBuilder.append(this.mTintList);
        }
        if (this.mTintMode != DEFAULT_TINT_MODE) {
            stringBuilder.append(" mode=");
            stringBuilder.append(this.mTintMode);
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public void onPreParceling(boolean z) {
        this.mTintModeStr = this.mTintMode.name();
        int i = this.mType;
        if (i != -1) {
            switch (i) {
                case 1:
                case 5:
                    if (z) {
                        Bitmap bitmap = (Bitmap) this.mObj1;
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(CompressFormat.PNG, 90, byteArrayOutputStream);
                        this.mData = byteArrayOutputStream.toByteArray();
                        return;
                    }
                    this.mParcelable = (Parcelable) this.mObj1;
                    return;
                case 2:
                    this.mData = ((String) this.mObj1).getBytes(Charset.forName("UTF-16"));
                    return;
                case 3:
                    this.mData = (byte[]) this.mObj1;
                    return;
                case 4:
                    this.mData = this.mObj1.toString().getBytes(Charset.forName("UTF-16"));
                    return;
                default:
                    return;
            }
        } else if (z) {
            throw new IllegalArgumentException("Can't serialize Icon created with IconCompat#createFromIcon");
        } else {
            this.mParcelable = (Parcelable) this.mObj1;
        }
    }

    public void onPostParceling() {
        this.mTintMode = Mode.valueOf(this.mTintModeStr);
        int i = this.mType;
        if (i != -1) {
            switch (i) {
                case 1:
                case 5:
                    if (this.mParcelable != null) {
                        this.mObj1 = this.mParcelable;
                        return;
                    }
                    this.mObj1 = this.mData;
                    this.mType = 3;
                    this.mInt1 = 0;
                    this.mInt2 = this.mData.length;
                    return;
                case 2:
                case 4:
                    this.mObj1 = new String(this.mData, Charset.forName("UTF-16"));
                    return;
                case 3:
                    this.mObj1 = this.mData;
                    return;
                default:
                    return;
            }
        } else if (this.mParcelable != null) {
            this.mObj1 = this.mParcelable;
        } else {
            throw new IllegalArgumentException("Invalid icon");
        }
    }

    @Nullable
    public static IconCompat createFromBundle(@NonNull Bundle bundle) {
        int i = bundle.getInt("type");
        IconCompat iconCompat = new IconCompat(i);
        iconCompat.mInt1 = bundle.getInt(EXTRA_INT1);
        iconCompat.mInt2 = bundle.getInt(EXTRA_INT2);
        if (bundle.containsKey(EXTRA_TINT_LIST)) {
            iconCompat.mTintList = (ColorStateList) bundle.getParcelable(EXTRA_TINT_LIST);
        }
        if (bundle.containsKey(EXTRA_TINT_MODE)) {
            iconCompat.mTintMode = Mode.valueOf(bundle.getString(EXTRA_TINT_MODE));
        }
        if (i != -1) {
            switch (i) {
                case 1:
                case 5:
                    break;
                case 2:
                case 4:
                    iconCompat.mObj1 = bundle.getString(EXTRA_OBJ);
                    break;
                case 3:
                    iconCompat.mObj1 = bundle.getByteArray(EXTRA_OBJ);
                    break;
                default:
                    String str = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Unknown type ");
                    stringBuilder.append(i);
                    Log.w(str, stringBuilder.toString());
                    return null;
            }
        }
        iconCompat.mObj1 = bundle.getParcelable(EXTRA_OBJ);
        return iconCompat;
    }

    @Nullable
    @RequiresApi(23)
    public static IconCompat createFromIcon(@NonNull Context context, @NonNull Icon icon) {
        Preconditions.checkNotNull(icon);
        int type = getType(icon);
        if (type == 2) {
            String resPackage = getResPackage(icon);
            try {
                return createWithResource(getResources(context, resPackage), resPackage, getResId(icon));
            } catch (NotFoundException unused) {
                throw new IllegalArgumentException("Icon resource cannot be found");
            }
        } else if (type == 4) {
            return createWithContentUri(getUri(icon));
        } else {
            IconCompat iconCompat = new IconCompat(-1);
            iconCompat.mObj1 = icon;
            return iconCompat;
        }
    }

    @Nullable
    @RequiresApi(23)
    @RestrictTo({Scope.LIBRARY_GROUP})
    public static IconCompat createFromIcon(@NonNull Icon icon) {
        Preconditions.checkNotNull(icon);
        int type = getType(icon);
        if (type == 2) {
            return createWithResource(null, getResPackage(icon), getResId(icon));
        }
        if (type == 4) {
            return createWithContentUri(getUri(icon));
        }
        IconCompat iconCompat = new IconCompat(-1);
        iconCompat.mObj1 = icon;
        return iconCompat;
    }

    @RequiresApi(23)
    private static int getType(@NonNull Icon icon) {
        String str;
        StringBuilder stringBuilder;
        if (VERSION.SDK_INT >= 28) {
            return icon.getType();
        }
        try {
            return ((Integer) icon.getClass().getMethod("getType", new Class[0]).invoke(icon, new Object[0])).intValue();
        } catch (IllegalAccessException e) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to get icon type ");
            stringBuilder.append(icon);
            Log.e(str, stringBuilder.toString(), e);
            return -1;
        } catch (InvocationTargetException e2) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to get icon type ");
            stringBuilder.append(icon);
            Log.e(str, stringBuilder.toString(), e2);
            return -1;
        } catch (NoSuchMethodException e3) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Unable to get icon type ");
            stringBuilder.append(icon);
            Log.e(str, stringBuilder.toString(), e3);
            return -1;
        }
    }

    @Nullable
    @RequiresApi(23)
    private static String getResPackage(@NonNull Icon icon) {
        if (VERSION.SDK_INT >= 28) {
            return icon.getResPackage();
        }
        try {
            return (String) icon.getClass().getMethod("getResPackage", new Class[0]).invoke(icon, new Object[0]);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unable to get icon package", e);
            return null;
        } catch (InvocationTargetException e2) {
            Log.e(TAG, "Unable to get icon package", e2);
            return null;
        } catch (NoSuchMethodException e3) {
            Log.e(TAG, "Unable to get icon package", e3);
            return null;
        }
    }

    @RequiresApi(23)
    @DrawableRes
    @IdRes
    private static int getResId(@NonNull Icon icon) {
        if (VERSION.SDK_INT >= 28) {
            return icon.getResId();
        }
        try {
            return ((Integer) icon.getClass().getMethod("getResId", new Class[0]).invoke(icon, new Object[0])).intValue();
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unable to get icon resource", e);
            return 0;
        } catch (InvocationTargetException e2) {
            Log.e(TAG, "Unable to get icon resource", e2);
            return 0;
        } catch (NoSuchMethodException e3) {
            Log.e(TAG, "Unable to get icon resource", e3);
            return 0;
        }
    }

    @Nullable
    @RequiresApi(23)
    private static Uri getUri(@NonNull Icon icon) {
        if (VERSION.SDK_INT >= 28) {
            return icon.getUri();
        }
        try {
            return (Uri) icon.getClass().getMethod("getUri", new Class[0]).invoke(icon, new Object[0]);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Unable to get icon uri", e);
            return null;
        } catch (InvocationTargetException e2) {
            Log.e(TAG, "Unable to get icon uri", e2);
            return null;
        } catch (NoSuchMethodException e3) {
            Log.e(TAG, "Unable to get icon uri", e3);
            return null;
        }
    }

    @VisibleForTesting
    static Bitmap createLegacyIconFromAdaptiveIcon(Bitmap bitmap, boolean z) {
        int min = (int) (((float) Math.min(bitmap.getWidth(), bitmap.getHeight())) * DEFAULT_VIEW_PORT_SCALE);
        Bitmap createBitmap = Bitmap.createBitmap(min, min, Config.ARGB_8888);
        Canvas canvas = new Canvas(createBitmap);
        Paint paint = new Paint(3);
        float f = (float) min;
        float f2 = 0.5f * f;
        float f3 = ICON_DIAMETER_FACTOR * f2;
        if (z) {
            float f4 = BLUR_FACTOR * f;
            paint.setColor(0);
            paint.setShadowLayer(f4, 0.0f, f * KEY_SHADOW_OFFSET_FACTOR, 1023410176);
            canvas.drawCircle(f2, f2, f3, paint);
            paint.setShadowLayer(f4, 0.0f, 0.0f, 503316480);
            canvas.drawCircle(f2, f2, f3, paint);
            paint.clearShadowLayer();
        }
        paint.setColor(ViewCompat.MEASURED_STATE_MASK);
        BitmapShader bitmapShader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
        Matrix matrix = new Matrix();
        matrix.setTranslate((float) ((-(bitmap.getWidth() - min)) / 2), (float) ((-(bitmap.getHeight() - min)) / 2));
        bitmapShader.setLocalMatrix(matrix);
        paint.setShader(bitmapShader);
        canvas.drawCircle(f2, f2, f3, paint);
        canvas.setBitmap(null);
        return createBitmap;
    }
}
