package com.meiteimayek.transliterate.tf;

import android.app.Application;
import android.util.ArrayMap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

import static com.meiteimayek.transliterate.tf.Utils.argmax;
import static com.meiteimayek.transliterate.tf.Utils.loadModelFile;

@SuppressWarnings("ConstantConditions")
public class Model {
  
  private static final String TAG = "DebugLog";
  
  private static Model mSelf = null;
  
  private int mUnits;
  private int mMaxLen;
  private HashMap<String, Integer> mWordIndex;
  private String[] mIndexWord;
  private int mVocabSize;
  
  private Encoder mEncoder;
  private Decoder mDecoder;
  
  private Model(Application app, Config config) throws IOException {
    mUnits = config.getUnits();
    mMaxLen = config.getMaxLength();
    mWordIndex = config.getWordIndex();
    mIndexWord = config.getIndexWord();
    mVocabSize = config.getVocabSize();
    
    Interpreter.Options options = new Interpreter.Options();
    
    mEncoder = new Encoder(app, config.getEncoder(), options);
    mDecoder = new Decoder(app, config.getDecoder(), options);
  }
  
  public static Model getInstance(Application app, Config config) throws IOException {
    if(mSelf == null) {
      Log.d(TAG, "getInstance: Creating an instance of Model.");
      mSelf = new Model(app, config);
    }
    return mSelf;
  }
  
  private int[] encodeSentence(String word, String to) {
    int[] input = new int[mMaxLen];
    int ri = 0;
    
    input[ri++] = mWordIndex.get(String.format("<2%s>", to));
    for(int i = 0; i < word.length() && i < mMaxLen - 2; i++) {
      int j = ri++;
      try {
        input[j] = mWordIndex.get("" + word.charAt(i));
      }catch(NullPointerException e) {
        input[j] = mWordIndex.get("<unk>");
      }
    }
    
    int p = mWordIndex.get("<pad>");
    while(ri < mMaxLen) input[ri++] = p;
    
    return input;
  }
  
  public Observable<String> trans(String word, boolean mode) {
    return Observable.just(word)
      .flatMapIterable(w -> Arrays.asList(w.split("( )+")))
      .map(w -> encodeSentence(w.toLowerCase(), (mode ? "mm" : "en")))
      .map(input -> {
        Log.d(TAG, "trans: Input : " + Arrays.toString(input));
        long start = System.currentTimeMillis();
        
        Map<Integer, Object> m = mEncoder.infer(input);
        float[][][] encOut = (float[][][]) m.get(0);
        float[][] hidden = (float[][]) m.get(1);
        
        ByteBuffer encOutBuffer = ByteBuffer.allocateDirect(4 * mMaxLen * mUnits);
        encOutBuffer.order(ByteOrder.nativeOrder());
        
        for(int j = 0; j < mMaxLen; j++) {
          for(int k = 0; k < mUnits; k++) {
            encOutBuffer.putFloat(encOut[0][j][k]);
          }
        }
        
        StringBuilder result = new StringBuilder();
        
        int inp = mWordIndex.get("<start>");
        int end = mWordIndex.get("<end>");
        
        for(int i = 0; i < mMaxLen; i++) {
          Map<Integer, Object> res = mDecoder.infer(inp, hidden, encOutBuffer);
          float[][] prediction = (float[][]) res.get(0);
          hidden = (float[][]) res.get(1);
          
          int predictedId = argmax(prediction[0]);
          
          if(predictedId == end) {
            break;
          }
          
          result.append(mIndexWord[predictedId]);
          inp = predictedId;
        }
        
        Log.d(TAG, "trans: Output : " + result.toString());
        Log.d(TAG, String.format("trans: Took : %sms", (System.currentTimeMillis() - start)));
        return result.toString();
      }).reduce("", (seed, w) -> seed + w + " ")
      .map(String::trim)
      .toObservable();
  }
  
  public void close() {
    mEncoder.close();
    mDecoder.close();
  }
  
  private class Encoder {
    private Interpreter mInterpreter;
    private ByteBuffer mInitialHiddenState;
    
    private float[][][] mOutput;
    private float[][] mHidden;
    
    Encoder(Application app, String fileName, Interpreter.Options options) throws IOException {
      mInterpreter = new Interpreter(loadModelFile(app, fileName), options);
      
      mInitialHiddenState = ByteBuffer.allocateDirect(4 * mUnits);
      mInitialHiddenState.order(ByteOrder.nativeOrder());
      
      for(int i = 0; i < mUnits; i++) {
        mInitialHiddenState.putFloat(0.0f);
      }
      
      mOutput = new float[1][mMaxLen][mUnits];
      mHidden = new float[1][mUnits];
    }
    
    Map<Integer, Object> infer(int[] str) {
      ByteBuffer inputBuffer = ByteBuffer.allocateDirect(4 * mMaxLen);
      inputBuffer.order(ByteOrder.nativeOrder());
      
      for(int s : str) {
        inputBuffer.putFloat(s);
      }
      
      Map<Integer, Object> result = new ArrayMap<>();
      result.put(0, mOutput);
      result.put(1, mHidden);
      
      mInterpreter.runForMultipleInputsOutputs(
        new ByteBuffer[]{inputBuffer, mInitialHiddenState}, result);
      
      return result;
    }
    
    void close() {
      mInterpreter.close();
    }
  }
  
  private class Decoder {
    
    private Interpreter mInterpreter;
    private float[][] mResult;
    private float[][] mHiddenState;
    
    Decoder(Application app, String fileName, Interpreter.Options options) throws IOException {
      mInterpreter = new Interpreter(loadModelFile(app, fileName), options);
      
      mResult = new float[1][mVocabSize];
      mHiddenState = new float[1][mUnits];
    }
    
    Map<Integer, Object> infer(int str, float[][] hidden, ByteBuffer encOut) {
      ByteBuffer strBuffer = ByteBuffer.allocateDirect(4);
      ByteBuffer hiddenBuffer = ByteBuffer.allocateDirect(4 * mUnits);
      strBuffer.order(ByteOrder.nativeOrder());
      hiddenBuffer.order(ByteOrder.nativeOrder());
      
      strBuffer.putFloat(str);
      
      for(int j = 0; j < mUnits; j++) {
        hiddenBuffer.putFloat(hidden[0][j]);
      }
      
      Map<Integer, Object> result = new ArrayMap<>();
      result.put(0, mResult);
      result.put(1, mHiddenState);
      
      mInterpreter.runForMultipleInputsOutputs(new Object[]{strBuffer, hiddenBuffer, encOut}, result);
      
      return result;
    }
    
    void close() {
      mInterpreter.close();
    }
    
  }
  
}
