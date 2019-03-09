package com.sambhar.sambharappreport.page.register;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.LinearLayout;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.view.ShambarTextView;
import com.sambhar.sambharappreport.entity.GroupEntity;

public class RegisterDetailViewHolder extends ViewHolder {
    public LinearLayout llRoot;
    private ShambarTextView tvName;

    public RegisterDetailViewHolder(@NonNull View view) {
        super(view);
        this.tvName = (ShambarTextView) view.findViewById(R.id.tv_item_register);
        this.llRoot = (LinearLayout) view.findViewById(R.id.ll_register_item);
    }

    public void bind(GroupEntity groupEntity) {
        this.tvName.setText(groupEntity.getName());
    }
}
