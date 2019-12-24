package com.meiteimayek.transliterate.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.textview.MaterialTextView;
import com.meiteimayek.transliterate.BuildConfig;
import com.meiteimayek.transliterate.R;

import io.noties.markwon.Markwon;

@SuppressWarnings("ConstantConditions")
public class AboutAppFragment extends Fragment {
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.fragment_about_app, container, false);
    
    ((MaterialTextView) root.findViewById(R.id.about_app_version)).setText(BuildConfig.VERSION_NAME);
    ((AdView) root.findViewById(R.id.ad_view)).loadAd(new AdRequest.Builder().build());
    
    Markwon markwon = Markwon.create(getContext());
    markwon.setMarkdown(root.findViewById(R.id.text_view), getString(R.string.about_app));
    
    return root;
  }
}
