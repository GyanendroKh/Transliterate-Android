package com.meiteimayek.transliterate.tf;

import android.util.ArrayMap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;

import static com.meiteimayek.transliterate.tf.Utils.argmax;

@SuppressWarnings("ConstantConditions")
public class Model {
  
  private static final String TAG = "DebugLog";
  
  private static Model mSelf = null;
  
  private final int mUnits;
  private final int mMaxLen;
  private final HashMap<String, Integer> mWordIndex;
  private final String[] mIndexWord;
  private final int mVocabSize;
  private final Encoder mEncoder;
  private final Decoder mDecoder;
  
  private Model(Config config) {
    mUnits = config.getUnits();
    mMaxLen = config.getMaxLength();
    mWordIndex = config.getWordIndex();
    mIndexWord = config.getIndexWord();
    mVocabSize = config.getVocabSize();
    
    Interpreter.Options options = new Interpreter.Options();
    options.addDelegate(new GpuDelegate());
    
    mEncoder = new Encoder(config.getEncoder(), options);
    mDecoder = new Decoder(config.getDecoder(), options);
  }
  
  public static Model getInstance(Config config) {
    if(mSelf == null) {
      Log.d(TAG, "getInstance: Creating an instance of Model.");
      mSelf = new Model(config);
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
    long start = System.currentTimeMillis();
    
    return Observable.just(word)
      .flatMapIterable(w -> Arrays.asList(w.split("( )+")))
      .map(w -> encodeSentence(w.toLowerCase(), (mode ? "mm" : "en")))
      .map(input -> {
        Log.d(TAG, "trans: Input : " + Arrays.toString(input));
        
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
        
        return result.toString();
      }).reduce("", (seed, w) -> seed + w + " ")
      .map(String::trim)
      .doOnEvent((s, t) -> Log.d(TAG,
        String.format("trans: Took : %sms", (System.currentTimeMillis() - start))))
      .toObservable();
  }
  
  public void close() {
    Log.d(TAG, "close: Closing...");
    mEncoder.close();
    mDecoder.close();
  }
  
  private class Encoder {
    private final Interpreter mInterpreter;
    private final ByteBuffer mInitialHiddenState;
    
    private final float[][][] mOutput;
    private final float[][] mHidden;
    
    Encoder(MappedByteBuffer model, Interpreter.Options options) {
      mInterpreter = new Interpreter(model, options);
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
    private final Interpreter mInterpreter;
    private final float[][] mResult;
    private final float[][] mHiddenState;
    
    Decoder(MappedByteBuffer model, Interpreter.Options options) {
      mInterpreter = new Interpreter(model, options);
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
