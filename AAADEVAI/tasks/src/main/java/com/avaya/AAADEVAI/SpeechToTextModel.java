package com.avaya.AAADEVAI;

import com.roobroo.bpm.model.BpmNode;

@SuppressWarnings("serial")
public class SpeechToTextModel extends BpmNode {

	public SpeechToTextModel(String name, String id) {
		super(name, id);
		
	}
	
	private String url;
	private String Language;
	private String apiKey;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getLanguage() {
		return Language;
	}
	public void setLanguage(String language) {
		Language = language;
	}
	public String getApiKey() {
		return apiKey;
	}
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	
}
