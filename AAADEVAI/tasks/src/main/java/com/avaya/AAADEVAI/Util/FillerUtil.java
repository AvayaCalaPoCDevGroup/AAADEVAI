package com.avaya.AAADEVAI.Util;

import java.util.ArrayList;
import java.util.List;

public class FillerUtil {
	
	private static FillerUtil instance = null;
	
	public static FillerUtil getInstance()
	  {
	    if (instance == null) {
	      synchronized (FillerUtil.class)
	      {
	        if (instance == null) {
	          instance = new FillerUtil();
	        }
	      }
	    }
	    return instance;
	  }
	
	public List<String> comboCloudProvider(){
		List<String> contentTypeList = new ArrayList<String>();
		contentTypeList.add("Google");
		contentTypeList.add("IBM");
		return contentTypeList;
	}
	
	public List<String> comboVoiceGeneric(){
		List<String> contentTypeList = new ArrayList<String>();
		contentTypeList.add("es-ES");
		contentTypeList.add("pt-BR");
		contentTypeList.add("en-US");
		return contentTypeList;
	}
	
	public List<String> comboVoice() {
		List<String> contentTypeList = new ArrayList<String>();
		contentTypeList.add("pt-BR_IsabelaVoice");
		contentTypeList.add("en-US_AllisonVoice");
		contentTypeList.add("en-US_MichaelVoice");
		contentTypeList.add("en-US_LisaVoice");
		contentTypeList.add("es-ES_EnriqueVoice");
		contentTypeList.add("es-LA_SofiaVoice");
		contentTypeList.add("es-ES_LauraVoice");
		contentTypeList.add("es-US_SofiaVoice");
		return contentTypeList;
	}
	
	public List<String> comboLanguage()
	  {
	    List<String> contentTypeLanguage = new ArrayList<String>();
	    contentTypeLanguage.add("en-US");
	    contentTypeLanguage.add("es-MX");
	    contentTypeLanguage.add("pt-BR");
	    return contentTypeLanguage;
	  }
	
	public List<String> booleanType() {
		List<String> booleanTypes = new ArrayList<String>();
		booleanTypes.add("True");
		booleanTypes.add("False");
		return booleanTypes;
	}
	
	public List<String> participantTypeForPA() {
		List<String> participantTypes = new ArrayList<String>();
		participantTypes.add("CallId");
		participantTypes.add("Calling");
		participantTypes.add("Called");
		return participantTypes;
	}
	
	public List<String> participantType() {
		List<String> participantTypes = new ArrayList<String>();
		participantTypes.add("Calling");
		participantTypes.add("Called");
		return participantTypes;
	}


}
