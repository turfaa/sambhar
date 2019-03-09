package com.sambhar.sambharappreport.page.register;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import com.sambhar.sambharappreport.R;
import com.sambhar.sambharappreport.base.BaseActivity;
import com.sambhar.sambharappreport.base.BaseViewModel;
import com.sambhar.sambharappreport.data.SambharConstant;
import com.sambhar.sambharappreport.databinding.ActivityRegisterDetailDataBinding;
import com.sambhar.sambharappreport.entity.GroupEntity;
import java.util.ArrayList;

public class RegisterDetailDataActivity extends BaseActivity<BaseViewModel, ActivityRegisterDetailDataBinding> implements ItemGroupClick {
    public static final String LIST_DATA = "list_data";
    public static final String PICK_MODE = "pick_mode";
    private RegisterDetailAdapter mAdapter;
    private ArrayList<GroupEntity> mList;
    private String pickMode;

    public int setLayoutView() {
        return R.layout.activity_register_detail_data;
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.pickMode = getIntent().getStringExtra(PICK_MODE);
        if (this.pickMode.equals(SambharConstant.PICK_MODE_PROVINCE)) {
            setupActionBar("Pilih Provinsi", true);
        } else if (this.pickMode.equals(SambharConstant.PICK_MODE_GROUP)) {
            setupActionBar("Pilih Group", true);
        }
        this.mList = getIntent().getParcelableArrayListExtra(LIST_DATA);
        this.mAdapter = new RegisterDetailAdapter(this, this.mList, this);
        ((ActivityRegisterDetailDataBinding) getDataBinding()).rvRegisterDetail.setLayoutManager(new LinearLayoutManager(this));
        ((ActivityRegisterDetailDataBinding) getDataBinding()).rvRegisterDetail.setAdapter(this.mAdapter);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        finish();
        return true;
    }

    public void itemClick(GroupEntity groupEntity) {
        Intent intent = new Intent();
        if (this.pickMode.equals(SambharConstant.PICK_MODE_PROVINCE)) {
            intent.putExtra(RegisterActivity.PROVINCE_DATA, groupEntity);
        } else if (this.pickMode.equals(SambharConstant.PICK_MODE_GROUP)) {
            intent.putExtra(RegisterActivity.GROUP_DATA, groupEntity);
        }
        setResult(-1, intent);
        finish();
    }
}
