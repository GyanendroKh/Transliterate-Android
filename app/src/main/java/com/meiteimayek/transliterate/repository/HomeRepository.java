package com.meiteimayek.transliterate.repository;

import android.app.Application;
import android.util.Log;

import com.meiteimayek.transliterate.tf.Config;
import com.meiteimayek.transliterate.tf.Model;
import com.meiteimayek.transliterate.tf.Utils;

import java.io.IOException;

import io.reactivex.Observable;

import static com.meiteimayek.transliterate.Utils.TAG;

public class HomeRepository {
  
  private static HomeRepository mSelf = null;
  private Model mModel;
  
  private HomeRepository(Application app) throws IOException {
    Config config = Utils.getConfig(app);
    
    mModel = Model.getInstance(config);
  }
  
  public static void intialize(Application application) throws IOException {
    if(mSelf == null) {
      Log.d(TAG, "intialize: Creating an instance of Repo.");
      mSelf = new HomeRepository(application);
    }
  }
  
  public static HomeRepository getInstance() {
    return mSelf;
  }
  
  public Observable<String> getTrans(String source, boolean mode) {
    return mModel.trans(source, mode);
  }
  
  public void close() {
    mModel.close();
  }
}
