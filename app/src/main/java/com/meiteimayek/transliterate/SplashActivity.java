package com.meiteimayek.transliterate;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends AppCompatActivity {
  
  private CompositeDisposable mDisposable;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    
    mDisposable = new CompositeDisposable();
  }
  
  @Override
  protected void onStart() {
    super.onStart();
    mDisposable.add(Observable.just(true)
      .subscribeOn(Schedulers.newThread())
      .delay(800, TimeUnit.MILLISECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .doOnComplete(() -> {
        startActivity(new Intent(SplashActivity.this, MainActivity.class));
        finish();
      }).subscribe());
  }
  
  @Override
  protected void onStop() {
    super.onStop();
    mDisposable.clear();
  }
}
