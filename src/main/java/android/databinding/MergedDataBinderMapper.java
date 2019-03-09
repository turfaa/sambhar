package android.databinding;

import android.util.Log;
import android.view.View;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class MergedDataBinderMapper extends DataBinderMapper {
    private static final String TAG = "MergedDataBinderMapper";
    private Set<Class<? extends DataBinderMapper>> mExistingMappers = new HashSet();
    private List<String> mFeatureBindingMappers = new CopyOnWriteArrayList();
    private List<DataBinderMapper> mMappers = new CopyOnWriteArrayList();

    public void addMapper(DataBinderMapper dataBinderMapper) {
        if (this.mExistingMappers.add(dataBinderMapper.getClass())) {
            this.mMappers.add(dataBinderMapper);
            for (DataBinderMapper addMapper : dataBinderMapper.collectDependencies()) {
                addMapper(addMapper);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public void addMapper(String str) {
        List list = this.mFeatureBindingMappers;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(str);
        stringBuilder.append(".DataBinderMapperImpl");
        list.add(stringBuilder.toString());
    }

    public ViewDataBinding getDataBinder(DataBindingComponent dataBindingComponent, View view, int i) {
        for (DataBinderMapper dataBinder : this.mMappers) {
            ViewDataBinding dataBinder2 = dataBinder.getDataBinder(dataBindingComponent, view, i);
            if (dataBinder2 != null) {
                return dataBinder2;
            }
        }
        return loadFeatures() ? getDataBinder(dataBindingComponent, view, i) : null;
    }

    public ViewDataBinding getDataBinder(DataBindingComponent dataBindingComponent, View[] viewArr, int i) {
        for (DataBinderMapper dataBinder : this.mMappers) {
            ViewDataBinding dataBinder2 = dataBinder.getDataBinder(dataBindingComponent, viewArr, i);
            if (dataBinder2 != null) {
                return dataBinder2;
            }
        }
        return loadFeatures() ? getDataBinder(dataBindingComponent, viewArr, i) : null;
    }

    public int getLayoutId(String str) {
        for (DataBinderMapper layoutId : this.mMappers) {
            int layoutId2 = layoutId.getLayoutId(str);
            if (layoutId2 != 0) {
                return layoutId2;
            }
        }
        return loadFeatures() ? getLayoutId(str) : 0;
    }

    public String convertBrIdToString(int i) {
        for (DataBinderMapper convertBrIdToString : this.mMappers) {
            String convertBrIdToString2 = convertBrIdToString.convertBrIdToString(i);
            if (convertBrIdToString2 != null) {
                return convertBrIdToString2;
            }
        }
        return loadFeatures() ? convertBrIdToString(i) : null;
    }

    private boolean loadFeatures() {
        String str;
        StringBuilder stringBuilder;
        boolean z = false;
        for (String str2 : this.mFeatureBindingMappers) {
            try {
                Class cls = Class.forName(str2);
                if (DataBinderMapper.class.isAssignableFrom(cls)) {
                    addMapper((DataBinderMapper) cls.newInstance());
                    this.mFeatureBindingMappers.remove(str2);
                    z = true;
                }
            } catch (ClassNotFoundException unused) {
            } catch (IllegalAccessException e) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("unable to add feature mapper for ");
                stringBuilder.append(str2);
                Log.e(str, stringBuilder.toString(), e);
            } catch (InstantiationException e2) {
                str = TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("unable to add feature mapper for ");
                stringBuilder.append(str2);
                Log.e(str, stringBuilder.toString(), e2);
            }
        }
        return z;
    }
}
