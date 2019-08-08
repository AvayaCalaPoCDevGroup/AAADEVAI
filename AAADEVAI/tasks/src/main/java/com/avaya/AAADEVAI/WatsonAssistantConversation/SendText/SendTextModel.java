package com.avaya.AAADEVAI.WatsonAssistantConversation.SendText;

import com.roobroo.bpm.model.BpmNode;

public class SendTextModel extends BpmNode{

	public SendTextModel(String name, String id) {
		super(name, id);
	}

	private static final long serialVersionUID = 1L;
	private String userNameSendText;
	private String passwordSendText;
	private String assistantIdSendText;
	private String versionSendText;
	private String sessionSendText;
	private String textSendText;
	
	public String getUserNameSendText() {
		return userNameSendText;
	}
	public void setUserNameSendText(String userNameSendText) {
		this.userNameSendText = userNameSendText;
	}
	public String getPasswordSendText() {
		return passwordSendText;
	}
	public void setPasswordSendText(String passwordSendText) {
		this.passwordSendText = passwordSendText;
	}
	public String getAssistantIdSendText() {
		return assistantIdSendText;
	}
	public void setAssistantIdSendText(String assistantIdSendText) {
		this.assistantIdSendText = assistantIdSendText;
	}
	public String getVersionSendText() {
		return versionSendText;
	}
	public void setVersionSendText(String versionSendText) {
		this.versionSendText = versionSendText;
	}
	public String getSessionSendText() {
		return sessionSendText;
	}
	public void setSessionSendText(String sessionSendText) {
		this.sessionSendText = sessionSendText;
	}
	public String getTextSendText() {
		return textSendText;
	}
	public void setTextSendText(String textSendText) {
		this.textSendText = textSendText;
	}
}
