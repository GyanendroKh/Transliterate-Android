package com.meiteimayek.transliterate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import io.reactivex.Observable;

public class Utils {
  
  public static final String TAG = "DebugLog";
  
  public static Observable<Boolean> checkVersion(FirebaseFirestore firestore) {
    return Observable.just(
      firestore.collection("android")
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
  
  public static void openStorePage(Context context) {
    Intent intent = new Intent(Intent.ACTION_VIEW,
      Uri.parse(context.getString(R.string.play_store_detail) + BuildConfig.APPLICATION_ID));
    context.startActivity(Intent.createChooser(intent, "Open with..."));
  }
  
  public static void shareApp(Context context) {
    Intent share = new Intent(android.content.Intent.ACTION_SEND);
    share.setType("text/plain");
    
    share.putExtra(Intent.EXTRA_SUBJECT, "Check this out!");
    share.putExtra(Intent.EXTRA_TEXT,
      context.getString(R.string.play_store_detail) + BuildConfig.APPLICATION_ID);
    
    context.startActivity(Intent.createChooser(share, "Share link!"));
  }
  
}
