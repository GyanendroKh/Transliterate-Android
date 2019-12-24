package com.meiteimayek.transliterate.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.meiteimayek.transliterate.R;
import com.meiteimayek.transliterate.viewmodel.HomeViewModel;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.ReplaySubject;

@SuppressWarnings("ConstantConditions")
public class HomeFragment extends Fragment {
  
  @BindView(R.id.trans_source)
  MaterialTextView mSource;
  @BindView(R.id.trans_target)
  MaterialTextView mTarget;
  @BindView(R.id.trans_swap_btn)
  AppCompatImageButton mSwapBtn;
  @BindView(R.id.trans_source_title)
  MaterialTextView mSourceTitle;
  @BindView(R.id.trans_clear)
  AppCompatImageButton mClear;
  @BindView(R.id.trans_source_text)
  TextInputEditText mSourceText;
  @BindView(R.id.trans_target_title)
  MaterialTextView mTargetTitle;
  @BindView(R.id.trans_target_text)
  MaterialTextView mTargetText;
  @BindView(R.id.trans_progress)
  ProgressBar mProgress;
  @BindView(R.id.trans_copy)
  AppCompatImageButton mCopy;
  @BindView(R.id.ad_view)
  AdView mAd;
  
  private CompositeDisposable mDisposable;
  private HomeViewModel mViewModel;
  private ClipboardManager mClipboard;
  private ReplaySubject<Boolean> mModeSubject;
  private InterstitialAd mInterAds;
  private Random mRandom;
  private int mTimes = 0;
  private final String TAG = "DebugLog";
  
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
    View root = inflater.inflate(R.layout.fragment_home, container, false);
    ButterKnife.bind(this, root);
    
    mDisposable = new CompositeDisposable();
    mViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
    mClipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
    mModeSubject = ReplaySubject.create();
    mInterAds = new InterstitialAd(getActivity());
    mRandom = new Random(System.currentTimeMillis());
    
    initView();
    
    return root;
  }
  
  private void initView() {
    mClear.setOnClickListener(v -> mSourceText.setText(""));
    mSwapBtn.setOnClickListener(v -> mModeSubject.onNext(!mViewModel.getMode()));
    mCopy.setOnClickListener(v -> {
      String transliterated = mTargetText.getText().toString();
      ClipData data = ClipData.newPlainText("Transliterated Sentence", transliterated);
      mClipboard.setPrimaryClip(data);
      Toast.makeText(HomeFragment.this.getActivity(),
        "Copied: " + transliterated, Toast.LENGTH_SHORT).show();
    });
    mInterAds.setAdUnitId(getString(R.string.ad_inter));
    mInterAds.setAdListener(new AdListener() {
      @Override
      public void onAdClosed() {
        super.onAdClosed();
        mInterAds.loadAd(new AdRequest.Builder().build());
        mTimes = 0;
      }
    });
  }
  
  @Override
  public void onStart() {
    super.onStart();
    mInterAds.loadAd(new AdRequest.Builder().build());
    mAd.loadAd(new AdRequest.Builder().build());
    
    mModeSubject.onNext(mViewModel.getMode());
    
    mDisposable.add(mModeSubject
      .doOnNext(m -> mViewModel.setMode(m))
      .map(mode -> {
        String source = mode ? getString(R.string.lang_eng) : getString(R.string.lang_mm);
        String target = !mode ? getString(R.string.lang_eng) : getString(R.string.lang_mm);
        return new String[]{source, target};
      }).subscribe(lang -> {
        mSource.setText(lang[0]);
        mSourceTitle.setText(lang[0]);
        mTarget.setText(lang[1]);
        mTargetTitle.setText(lang[1]);
        mSourceText.setText("");
      }));
    
    mDisposable.add(Observable.create((ObservableOnSubscribe<String>) emitter -> mSourceText
      .addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        
        }
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          if(s.toString().trim().equals("")) {
            mTargetText.setText(getString(R.string.transliteration));
            return;
          }
          emitter.onNext(s.toString());
        }
        
        @Override
        public void afterTextChanged(Editable s) {
        
        }
      })).subscribeOn(Schedulers.computation())
      .debounce(700, TimeUnit.MILLISECONDS)
      .doOnNext(s -> getActivity().runOnUiThread(() -> mProgress.setVisibility(View.VISIBLE)))
      .flatMap(s -> mViewModel.getTrans(s))
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe(s -> {
        mTimes++;
        mTargetText.setText(s);
        mProgress.setVisibility(View.INVISIBLE);
        if(canShowAds()) {
          Log.d(TAG, "onStart: Showing Inter.");
          new Handler().postDelayed(() -> mInterAds.show(), 2000);
        }
      }, err -> {
        Log.e(TAG, "error: " + err.getLocalizedMessage());
        err.printStackTrace();
        mProgress.setVisibility(View.INVISIBLE);
      }));
  }
  
  private boolean canShowAds() {
    return (mTimes > 2) && (mRandom.nextInt(9) > 5);
  }
  
  @Override
  public void onResume() {
    super.onResume();
    if(canShowAds()) mInterAds.show();
  }
  
  @Override
  public void onStop() {
    super.onStop();
    mDisposable.clear();
  }
}
