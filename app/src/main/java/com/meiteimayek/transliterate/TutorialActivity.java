package com.meiteimayek.transliterate;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.meiteimayek.transliterate.adapter.PagerAdapter;
import com.meiteimayek.transliterate.common.Step;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TutorialActivity extends AppCompatActivity {
  
  @BindView(R.id.tut_pager)
  ViewPager2 mPager;
  @BindView(R.id.tut_const)
  ConstraintLayout mConst;
  @BindView(R.id.tut_indicator)
  LinearLayoutCompat mIndicator;
  @BindView(R.id.tut_btn_prev)
  AppCompatButton mPrevBtn;
  @BindView(R.id.tut_btn_next)
  AppCompatButton mNextBtn;
  
  private List<Step> mList;
  private int mCurrentPos = 0;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_tutorial);
    
    ButterKnife.bind(this);
    
    mList = new ArrayList<>();
    mList.add(new Step(R.drawable.tut_1, getString(R.string.tut_1_title), getString(R.string.tut_1)));
    mList.add(new Step(R.drawable.tut_2, getString(R.string.tut_2_title), getString(R.string.tut_2)));
    mList.add(new Step(R.drawable.tut_3, getString(R.string.tut_3_title), getString(R.string.tut_3)));
    
    PagerAdapter adapter = new PagerAdapter(mList);
    mPager.setAdapter(adapter);
    
    mPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
      @Override
      public void onPageSelected(int position) {
        mCurrentPos = position;
        controlPos();
      }
    });
    
    mPrevBtn.setOnClickListener(v -> mPager.setCurrentItem(mCurrentPos - 1, true));
    
    mNextBtn.setOnClickListener(v -> {
      if(mCurrentPos == mList.size() - 1) {
        startActivity(new Intent(TutorialActivity.this, MainActivity.class));
        finish();
      }else {
        mPager.setCurrentItem(mCurrentPos + 1, true);
      }
    });
  }
  
  private void controlPos() {
    if(mCurrentPos == 0) {
      mPrevBtn.setVisibility(View.GONE);
    }else {
      mPrevBtn.setVisibility(View.VISIBLE);
    }
    
    if(mCurrentPos == mList.size() - 1) {
      mNextBtn.setText(R.string.finish);
    }else {
      mNextBtn.setText(R.string.next);
    }
    
    // TODO: Fix Indicator
    for(int i = 0; i < mList.size(); i++) {
      ImageView imageView = new ImageView(TutorialActivity.this);
      imageView.setPadding(8, 8, 8, 8);
      int drawable = R.drawable.circle_black;
      if(i == mCurrentPos) drawable = R.drawable.circle_white;
      imageView.setImageResource(drawable);
      
      int finalI = i;
      imageView.setOnClickListener(v -> mPager.setCurrentItem(finalI, true));
      
      if(mIndicator.getChildCount() > i) mIndicator.removeViewAt(i);
      mIndicator.addView(imageView, i);
    }
  }
}
