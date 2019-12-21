package com.meiteimayek.transliterate.repository;

import android.app.Application;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;

public class HomeRepository {
  
  private static final String TAG = "DebugLog";
  private static HomeRepository mSelf = null;
  
  private HomeRepository(Application application) {
  
  }
  
  public static HomeRepository getInstance(Application application) {
    if(mSelf == null) {
      Log.d(TAG, "getInstance: Creating a new instance...");
      mSelf = new HomeRepository(application);
    }
    
    return mSelf;
  }
  
  public Observable<String> getTrans(String source, boolean mode) {
    return Observable.just(source)
      .delay(800, TimeUnit.MILLISECONDS)
      .map(s -> (!mode ? "en" : "mm") + " " + s)
      .doOnNext(s -> Log.d(TAG, "getTrans: " + s));
  }
  
}
