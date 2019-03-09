package android.databinding;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.view.View;
import java.util.Collections;
import java.util.List;

@RestrictTo({Scope.LIBRARY})
public abstract class DataBinderMapper {
    public abstract String convertBrIdToString(int i);

    public abstract ViewDataBinding getDataBinder(DataBindingComponent dataBindingComponent, View view, int i);

    public abstract ViewDataBinding getDataBinder(DataBindingComponent dataBindingComponent, View[] viewArr, int i);

    public abstract int getLayoutId(String str);

    @NonNull
    public List<DataBinderMapper> collectDependencies() {
        return Collections.emptyList();
    }
}
