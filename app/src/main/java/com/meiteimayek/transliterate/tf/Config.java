package com.meiteimayek.transliterate.tf;

import java.nio.MappedByteBuffer;
import java.util.HashMap;

public interface Config {
  String ENCODER = "encoder.tflite";
  String DECODER = "decoder.tflite";
  String DATA = "data.txt";
  
  MappedByteBuffer getEncoder();
  
  MappedByteBuffer getDecoder();
  
  int getUnits();
  
  int getMaxLength();
  
  int getVocabSize();
  
  HashMap<String, Integer> getWordIndex();
  
  String[] getIndexWord();
}
