package com.meiteimayek.transliterate;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.meiteimayek.transliterate.repository.HomeRepository;

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
      .delay(300, TimeUnit.MILLISECONDS)
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        b -> HomeRepository.intialize(getApplication()),
        e -> {
          e.printStackTrace();
          new AlertDialog.Builder(this)
            .setTitle(getString(R.string.error_title))
            .setMessage(getString(R.string.error_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.exit), (d, w) -> finish())
            .create()
            .show();
        },
        () -> {
          startActivity(new Intent(this, MainActivity.class));
          finish();
        }
      ));
  }
  
  @Override
  protected void onStop() {
    super.onStop();
    mDisposable.clear();
  }
}
