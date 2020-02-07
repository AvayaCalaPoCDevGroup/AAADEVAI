package com.avaya.AAADEVAI.AAAEmotions;

import java.util.ArrayList;
import java.util.List;

import com.avaya.AAADEVAI.Util.FillerUtil;

public class EmotionsPropertyHandler {
	private static EmotionsPropertyHandler instance = null;
	
	public static EmotionsPropertyHandler getInstance()
	  {
	    if (instance == null) {
	      synchronized (FillerUtil.class)
	      {
	        if (instance == null) {
	          instance = new EmotionsPropertyHandler();
	        }
	      }
	    }
	    return instance;
	  }
	
	  public List<String> getLanguageEmotions(){
		  List<String> languages = new ArrayList<String>();
		  languages.add("en");
		  languages.add("es");
		  languages.add("pt");
		  return languages;
	  }
}
