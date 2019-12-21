package com.meiteimayek.transliterate.tf;

import java.util.HashMap;

public interface Config {
  String getEncoder();
  
  String getDecoder();
  
  int getUnits();
  
  int getMaxLength();
  
  int getVocabSize();
  
  HashMap<String, Integer> getWordIndex();
  
  String[] getIndexWord();
}
