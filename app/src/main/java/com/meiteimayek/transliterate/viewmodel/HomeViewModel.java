package com.meiteimayek.transliterate.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.meiteimayek.transliterate.repository.HomeRepository;

import java.io.IOException;

import io.reactivex.Observable;

public class HomeViewModel extends AndroidViewModel {
  
  private boolean mMode;
  private HomeRepository mRepo;
  
  public HomeViewModel(@NonNull Application app) throws IOException {
    super(app);
    
    mMode = true;
    mRepo = HomeRepository.getInstance(app);
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
  
  @Override
  protected void onCleared() {
    super.onCleared();
    mRepo.close();
  }
}
