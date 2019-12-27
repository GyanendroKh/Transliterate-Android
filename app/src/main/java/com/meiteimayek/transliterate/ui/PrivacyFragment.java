package com.meiteimayek.transliterate.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.meiteimayek.transliterate.R;

import io.noties.markwon.Markwon;

@SuppressWarnings("ConstantConditions")
public class PrivacyFragment extends Fragment {
  
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_privacy, container, false);
    
    Markwon markwon = Markwon.create(getContext());
    markwon.setMarkdown(view.findViewById(R.id.privacy_text), getString(R.string.privacy_policy_content));
    
    return view;
  }
}
