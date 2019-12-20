package com.meiteimayek.transliterate.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.meiteimayek.transliterate.R;

public class HomeFragment extends Fragment {
  
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
    View root = inflater.inflate(R.layout.fragment_home, container, false);
    final TextView textView = root.findViewById(R.id.text_home);
    textView.setText("Home");
    return root;
  }
}