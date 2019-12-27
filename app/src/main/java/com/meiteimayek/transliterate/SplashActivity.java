package com.meiteimayek.transliterate;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.meiteimayek.transliterate.repository.HomeRepository;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

import static com.meiteimayek.transliterate.Utils.TAG;
import static com.meiteimayek.transliterate.Utils.checkVersion;
import static com.meiteimayek.transliterate.Utils.openStorePage;

public class SplashActivity extends AppCompatActivity {
  
  private CompositeDisposable mDisposable;
  private FirebaseFirestore mFirestore;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    
    mDisposable = new CompositeDisposable();
    mFirestore = FirebaseFirestore.getInstance();
  }
  
  private void openAndFinish() {
    startActivity(new Intent(this, TutorialActivity.class));
    finish();
  }
  
  @Override
  protected void onStart() {
    super.onStart();
    mDisposable.add(checkVersion(mFirestore)
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        b -> {
          Log.d(TAG, "onStart: Update - " + b);
          if(!b) {
            HomeRepository.intialize(getApplication());
            openAndFinish();
          }else {
            new AlertDialog.Builder(this)
              .setTitle(getString(R.string.update_title))
              .setMessage(getString(R.string.update_text))
              .setCancelable(false)
              .setPositiveButton(getString(R.string.update), (d, w) -> openStorePage(this))
              .setNegativeButton(getString(R.string.later), (d, w) -> openAndFinish())
              .create()
              .show();
          }
        },
        e -> {
          e.printStackTrace();
          new AlertDialog.Builder(this)
            .setTitle(getString(R.string.error_title))
            .setMessage(getString(R.string.error_message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.exit), (d, w) -> finish())
            .create()
            .show();
        }
      ));
  }
  
  @Override
  protected void onStop() {
    super.onStop();
    mDisposable.clear();
  }
}
