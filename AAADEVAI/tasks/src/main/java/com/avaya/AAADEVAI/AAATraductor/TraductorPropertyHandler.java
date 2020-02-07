package com.avaya.AAADEVAI.AAATraductor;

import java.util.ArrayList;
import java.util.List;

import com.avaya.AAADEVAI.Util.FillerUtil;

public class TraductorPropertyHandler {
	private static TraductorPropertyHandler instance = null;
	
	public static TraductorPropertyHandler getInstance()
	  {
	    if (instance == null) {
	      synchronized (FillerUtil.class)
	      {
	        if (instance == null) {
	          instance = new TraductorPropertyHandler();
	        }
	      }
	    }
	    return instance;
	  }
	
	
	  public List<String> getLanguageTranslator(){
		  List<String> languages = new ArrayList<String>();
		  languages.add("es-en");
		  languages.add("pt-en");
		  languages.add("en-pt");
		  languages.add("en-es");
		  return languages;
	  }
	 
}
