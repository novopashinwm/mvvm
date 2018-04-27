package com.elegion.test.behancer.ui.projects;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.v4.widget.SwipeRefreshLayout;

import com.elegion.test.behancer.BuildConfig;
import com.elegion.test.behancer.data.Storage;
import com.elegion.test.behancer.data.model.project.Project;
import com.elegion.test.behancer.utils.ApiUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author Azret Magometov
 */
public class ProjectsViewModel extends ViewModel {

    private Disposable mDisposable;
    private Storage mStorage;

    private ProjectsAdapter.OnItemClickListener mOnItemClickListener;
    private MutableLiveData<Boolean> mIsLoading = new MutableLiveData<>();
    private MutableLiveData<Boolean> mIsErrorVisible = new MutableLiveData<>();
    private MutableLiveData<List<Project>> mProjects = new MutableLiveData<>();
    private SwipeRefreshLayout.OnRefreshListener mOnRefreshListener = this::loadProjects;

    public ProjectsViewModel(Storage storage, ProjectsAdapter.OnItemClickListener onItemClickListener) {
        mStorage = storage;
        mOnItemClickListener = onItemClickListener;
        mProjects.setValue(new ArrayList<>());
        loadProjects();
    }

    public void loadProjects() {
        mDisposable = ApiUtils.getApiService().getProjects(BuildConfig.API_QUERY)
                .doOnSuccess(response -> mStorage.insertProjects(response))
                .onErrorReturn(throwable ->
                        ApiUtils.NETWORK_EXCEPTIONS.contains(throwable.getClass()) ? mStorage.getProjects() : null)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(disposable -> mIsLoading.postValue(true))
                .doFinally(() -> mIsLoading.postValue(false))
                .subscribe(
                        response -> {
                            mIsErrorVisible.postValue(false);
                            mProjects.postValue(response.getProjects());
                        },
                        throwable -> mIsErrorVisible.postValue(true));
    }

    @Override
    public void onCleared() {
        mStorage = null;
        if (mDisposable != null) {
            mDisposable.dispose();
        }
    }

    public ProjectsAdapter.OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return mIsLoading;
    }

    public MutableLiveData<Boolean> getIsErrorVisible() {
        return mIsErrorVisible;
    }

    public MutableLiveData<List<Project>> getProjects() {
        return mProjects;
    }

    public SwipeRefreshLayout.OnRefreshListener getOnRefreshListener() {
        return mOnRefreshListener;
    }
}
