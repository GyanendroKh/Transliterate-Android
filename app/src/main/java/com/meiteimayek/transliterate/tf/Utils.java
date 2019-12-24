package com.meiteimayek.transliterate.tf;

import android.app.Application;
import android.content.res.AssetFileDescriptor;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import static com.meiteimayek.transliterate.tf.Config.DATA;
import static com.meiteimayek.transliterate.tf.Config.DECODER;
import static com.meiteimayek.transliterate.tf.Config.ENCODER;

public class Utils {
  
  public static MappedByteBuffer loadModel(AssetFileDescriptor file) throws IOException {
    return new FileInputStream(file.getFileDescriptor())
      .getChannel()
      .map(FileChannel.MapMode.READ_ONLY, file.getStartOffset(), file.getDeclaredLength());
  }
  
  public static Config getConfig(Application app) throws IOException {
    AssetFileDescriptor encoder = app.getAssets().openFd(ENCODER);
    AssetFileDescriptor decoder = app.getAssets().openFd(DECODER);
    DataInputStream dataStream = new DataInputStream(app.getAssets().open(DATA));
    
    MappedByteBuffer encoderBuffer = loadModel(encoder);
    MappedByteBuffer decoderBuffer = loadModel(decoder);
    
    byte[] dataBytes = new byte[dataStream.available()];
    int read = 0;
    while(true) {
      byte[] buffer = new byte[1024 * 2];
      
      int r = dataStream.read(buffer);
      
      if(r == -1) {
        break;
      }
      
      for(int i = 0; i < r; i++) {
        dataBytes[read++] = buffer[i];
      }
    }
    
    String[] data = new String(dataBytes).trim().split("\n");
    
    String[] indexWord = data[2].trim().split(",");
    HashMap<String, Integer> wordIndex = new HashMap<>();
    
    for(int i = 0; i < indexWord.length; i++) {
      wordIndex.put(indexWord[i], i);
    }
    
    return new Config() {
      @Override
      public MappedByteBuffer getEncoder() {
        return encoderBuffer;
      }
      
      @Override
      public MappedByteBuffer getDecoder() {
        return decoderBuffer;
      }
      
      @Override
      public int getUnits() {
        return Integer.parseInt(data[0].trim());
      }
      
      @Override
      public int getMaxLength() {
        return Integer.parseInt(data[1].trim());
      }
      
      @Override
      public int getVocabSize() {
        return indexWord.length;
      }
      
      @Override
      public HashMap<String, Integer> getWordIndex() {
        return wordIndex;
      }
      
      @Override
      public String[] getIndexWord() {
        return indexWord;
      }
    };
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
