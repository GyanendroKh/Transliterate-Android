package com.meiteimayek.transliterate.viewmodel;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.meiteimayek.transliterate.repository.HomeRepository;

import io.reactivex.Observable;

public class HomeViewModel extends ViewModel {
  
  private static final String TAG = "DebugLog";
  
  private boolean mMode;
  private HomeRepository mRepo;
  private String mLastString;
  
  public HomeViewModel() {
    mMode = true;
    mRepo = HomeRepository.getInstance();
    mLastString = "";
    Log.d(TAG, "HomeViewModel: Creating ViewModel");
  }
  
  public Observable<String> getTrans(String source) {
    return mRepo.getTrans(source, mMode);
  }
  
  public boolean getMode() {
    return mMode;
  }
  
  public void setMode(boolean mMode) {
    this.mMode = mMode;
  }
  
  public String getLastString() {
    return mLastString;
  }
  
  public void setLastString(String s) {
    mLastString = s;
  }
  
  @Override
  protected void onCleared() {
    super.onCleared();
    mRepo.close();
  }
}
