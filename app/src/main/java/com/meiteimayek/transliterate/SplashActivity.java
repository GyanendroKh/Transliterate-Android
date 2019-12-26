package com.meiteimayek.transliterate;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.meiteimayek.transliterate.repository.HomeRepository;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SplashActivity extends AppCompatActivity {
  
  private static final String TAG = "DebugLog";
  private CompositeDisposable mDisposable;
  private FirebaseFirestore mFirestore;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);
    
    mDisposable = new CompositeDisposable();
    mFirestore = FirebaseFirestore.getInstance();
  }
  
  private Observable<Boolean> checkVersion() {
    return Observable.just(
      mFirestore.collection("android")
        .document("app")
        .get()
    ).flatMap(t -> Observable.create(emitter -> t.addOnCompleteListener(task -> {
      if(task.isSuccessful()) {
        DocumentSnapshot result = task.getResult();
        if(result != null) {
          long version = (long) result.get("version");
          Log.d(TAG, "checkVersion: Local Version - " + BuildConfig.VERSION_CODE);
          Log.d(TAG, "checkVersion: Remote Version - " + version);
          if(((long) BuildConfig.VERSION_CODE) < version) {
            emitter.onNext(true);
            emitter.onComplete();
            return;
          }
        }
      }
      Log.w(TAG, "checkVersion: Error getting documents.", task.getException());
      emitter.onNext(false);
      emitter.onComplete();
    })));
  }
  
  @Override
  protected void onStart() {
    super.onStart();
    mDisposable.add(checkVersion()
      .subscribeOn(Schedulers.newThread())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(
        b -> {
          Log.d(TAG, "onStart: Update - " + b);
          if(b) {
            HomeRepository.intialize(getApplication());
            startActivity(new Intent(this, MainActivity.class));
            finish();
          }else {
            new AlertDialog.Builder(this)
              .setTitle(getString(R.string.update_title))
              .setMessage(getString(R.string.update_text))
              .setCancelable(false)
              .setPositiveButton(getString(R.string.update), (d, w) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                  Uri.parse(getString(R.string.play_store_detail) + BuildConfig.APPLICATION_ID));
                startActivity(Intent.createChooser(intent, "Open with..."));
              })
              .setNegativeButton(getString(R.string.later), (d, w) -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
              }).create()
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
