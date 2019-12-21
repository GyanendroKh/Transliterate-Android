package com.meiteimayek.transliterate.tf;

import android.app.Application;
import android.content.res.AssetFileDescriptor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Utils {
  
  public static MappedByteBuffer loadModelFile(Application activity, String name) throws IOException {
    AssetFileDescriptor descriptor = activity.getAssets().openFd(name);
    FileInputStream is = new FileInputStream(descriptor.getFileDescriptor());
    
    return is.getChannel().map(FileChannel.MapMode.READ_ONLY,
      descriptor.getStartOffset(), descriptor.getDeclaredLength());
  }
  
  public static int argmax(float[] probabilities) {
    int maxIdx = -1;
    float maxProb = 0.0f;
    
    for(int i = 0; i < probabilities.length; i++) {
      if(probabilities[i] > maxProb) {
        maxProb = probabilities[i];
        maxIdx = i;
      }
    }
    
    return maxIdx;
  }
  
}
