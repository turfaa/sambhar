package com.sambhar.sambharappreport.page.register;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.entity.GroupEntity;
import java.util.ArrayList;

public class RegisterDetailAdapter extends Adapter<ViewHolder> {
    private Context context;
    ItemGroupClick listener;
    private ArrayList<GroupEntity> mList;

    interface ItemGroupClick {
        void itemClick(GroupEntity groupEntity);
    }

    public RegisterDetailAdapter(Context context, ArrayList<GroupEntity> arrayList, ItemGroupClick itemGroupClick) {
        this.mList = arrayList;
        this.context = context;
        this.listener = itemGroupClick;
    }

    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new RegisterDetailViewHolder(LayoutInflater.from(this.context).inflate(R.layout.item_register, viewGroup, false));
    }

    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        if (viewHolder instanceof RegisterDetailViewHolder) {
            RegisterDetailViewHolder registerDetailViewHolder = (RegisterDetailViewHolder) viewHolder;
            registerDetailViewHolder.bind((GroupEntity) this.mList.get(i));
            registerDetailViewHolder.llRoot.setOnClickListener(new -$$Lambda$RegisterDetailAdapter$uDVU70P19aK9X8e-EoVVma473CA(this, i));
        }
    }

    public int getItemCount() {
        return this.mList.size();
    }
}
