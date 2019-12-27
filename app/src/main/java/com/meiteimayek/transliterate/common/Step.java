package com.meiteimayek.transliterate.common;

import androidx.annotation.DrawableRes;

public class Step {
  
  private final int mImg;
  private final String mTitle;
  private final String mDesc;
  
  public Step(@DrawableRes int image, String title, String desc) {
    mImg = image;
    mTitle = title;
    mDesc = desc;
  }
  
  public int getImage() {
    return mImg;
  }
  
  public String getTitle() {
    return mTitle;
  }
  
  public String getDesc() {
    return mDesc;
  }
}
