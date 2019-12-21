package com.meiteimayek.transliterate.repository;

import android.app.Application;
import android.util.Log;

import com.meiteimayek.transliterate.R;
import com.meiteimayek.transliterate.tf.Config;
import com.meiteimayek.transliterate.tf.Model;

import java.util.HashMap;

import io.reactivex.Observable;

public class HomeRepository {
  
  private static final String TAG = "DebugLog";
  private static HomeRepository mSelf = null;
  private Model mModel;
  
  private HomeRepository(Application app) {
    Config config = new Config() {
      @Override
      public String getEncoder() {
        return "encoder.tflite";
      }
      
      @Override
      public String getDecoder() {
        return "decoder.tflite";
      }
      
      @Override
      public int getUnits() {
        return 1024;
      }
      
      @Override
      public int getMaxLength() {
        return 24;
      }
      
      @Override
      public int getVocabSize() {
        return getIndexWord().length;
      }
      
      @Override
      public HashMap<String, Integer> getWordIndex() {
        String[] words = getIndexWord();
        
        int i = 0;
        HashMap<String, Integer> map = new HashMap<>();
        
        for(String w : words) map.put(w, i++);
        
        return map;
      }
      
      @Override
      public String[] getIndexWord() {
        return app.getString(R.string.mapping).split(",");
      }
    };
    
    try {
      mModel = Model.getInstance(app, config);
    }catch(Exception e) {
      e.printStackTrace();
    }
  }
  
  public static HomeRepository getInstance(Application application) {
    if(mSelf == null) {
      Log.d(TAG, "getInstance: Creating a new instance...");
      mSelf = new HomeRepository(application);
    }
    
    return mSelf;
  }
  
  public Observable<String> getTrans(String source, boolean mode) {
    return mModel.trans(source, mode);
  }
  
  public void close() {
    mModel.close();
  }
  
}
