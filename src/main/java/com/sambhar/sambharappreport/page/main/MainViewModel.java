package com.sambhar.sambharappreport.page.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import com.sambhar.sambharappreport.base.BaseViewModel;
import com.sambhar.sambharappreport.entity.bodypost.JobCheckBodyPost;
import com.sambhar.sambharappreport.entity.bodypost.NotifyFormBody;
import com.sambhar.sambharappreport.entity.response.JobCheckResponse;
import com.sambhar.sambharappreport.entity.response.JobCreateResponse;
import com.sambhar.sambharappreport.entity.response.NotifyResponse;
import com.sambhar.sambharappreport.rest.AppRepository;
import com.sambhar.sambharappreport.rest.Resource;
import javax.inject.Inject;

public class MainViewModel extends BaseViewModel {
    private final AppRepository mAppRepository;
    private final LiveData<Resource<JobCheckResponse>> mCheckJob;
    private final LiveData<Resource<JobCreateResponse>> mJobData;
    private final LiveData<Resource<NotifyResponse>> mNotifyData;
    private final MutableLiveData<JobCheckBodyPost> mTriggerCheckJob = new MutableLiveData();
    private final MutableLiveData<Boolean> mTriggerCreateJob = new MutableLiveData();
    private final MutableLiveData<NotifyFormBody> mTriggerNotify = new MutableLiveData();

    @Inject
    MainViewModel(AppRepository appRepository) {
        this.mAppRepository = appRepository;
        this.mJobData = Transformations.switchMap(this.mTriggerCreateJob, new -$$Lambda$MainViewModel$_S_ZmD1fDwBned0hw0LMi2ytMHo(this));
        this.mNotifyData = Transformations.switchMap(this.mTriggerNotify, new -$$Lambda$MainViewModel$zKJnuvfuawcs6AbRd-bTZ1g5PvU(this));
        this.mCheckJob = Transformations.switchMap(this.mTriggerCheckJob, new -$$Lambda$MainViewModel$m0xmmXyW9bLjeYnXowXpetECHf0(this));
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<Resource<JobCreateResponse>> jobData() {
        return this.mJobData;
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<Resource<JobCheckResponse>> jobCheckData() {
        return this.mCheckJob;
    }

    /* Access modifiers changed, original: 0000 */
    public LiveData<Resource<NotifyResponse>> notifyData() {
        return this.mNotifyData;
    }

    /* Access modifiers changed, original: protected */
    public void createJob() {
        this.mTriggerCreateJob.setValue(Boolean.valueOf(true));
    }

    /* Access modifiers changed, original: protected */
    public void notifyServer(NotifyFormBody notifyFormBody) {
        this.mTriggerNotify.setValue(notifyFormBody);
    }

    /* Access modifiers changed, original: protected */
    public void checkJob(JobCheckBodyPost jobCheckBodyPost) {
        this.mTriggerCheckJob.setValue(jobCheckBodyPost);
    }
}
