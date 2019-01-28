package com.avaya.AAADEVAI;

import com.roobroo.bpm.model.BpmNode;

public class TextToSpeechModel extends BpmNode {
	private static final long serialVersionUID = 1L;

	public TextToSpeechModel(String name, String id) {
		super(name, id);
		// TODO Auto-generated constructor stub
	}

	private String messageBody;
	private String userName;
	private String password;
	private String voice;
	
	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVoice() {
		return voice;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}

	
	
}
