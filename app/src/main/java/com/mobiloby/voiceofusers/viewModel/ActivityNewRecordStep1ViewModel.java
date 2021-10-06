package com.mobiloby.voiceofusers.viewModel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ActivityNewRecordStep1ViewModel extends ViewModel {

    MutableLiveData<Boolean> isLogoClicked = new MutableLiveData<>();
    MutableLiveData<Boolean> isDeleted = new MutableLiveData<>();
    MutableLiveData<Boolean> isLocked = new MutableLiveData<>();

    public MutableLiveData<Boolean> getIsLogoClicked() {
        return isLogoClicked;
    }

    public void setIsLogoClicked(Boolean isLogoClicked) {
        this.isLogoClicked.postValue(isLogoClicked);
    }

    public MutableLiveData<Boolean> getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted.postValue(isDeleted);
    }

    public MutableLiveData<Boolean> getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(Boolean isLocked) {
        this.isLocked.postValue(isLocked);
    }
}
