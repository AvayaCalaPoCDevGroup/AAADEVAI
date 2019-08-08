package com.avaya.AAADEVAI.WatsonAssistantConversation.DeleteSession;

import com.roobroo.bpm.model.BpmNode;

public class DeleteSessionModel extends BpmNode{

	public DeleteSessionModel(String name, String id) {
		super(name, id);
	}

	private static final long serialVersionUID = 1L;
	private String userNameDeleteSession;
	private String passwordDeleteSession;
	private String assistantIdDeleteSession;
	private String versionDeleteSession;
	private String sessionDeleteSession;
	
	public String getSessionDeleteSession() {
		return sessionDeleteSession;
	}
	public void setSessionDeleteSession(String sessionDeleteSession) {
		this.sessionDeleteSession = sessionDeleteSession;
	}
	public String getUserNameDeleteSession() {
		return userNameDeleteSession;
	}
	public void setUserNameDeleteSession(String userNameDeleteSession) {
		this.userNameDeleteSession = userNameDeleteSession;
	}
	public String getPasswordDeleteSession() {
		return passwordDeleteSession;
	}
	public void setPasswordDeleteSession(String passwordDeleteSession) {
		this.passwordDeleteSession = passwordDeleteSession;
	}
	public String getAssistantIdDeleteSession() {
		return assistantIdDeleteSession;
	}
	public void setAssistantIdDeleteSession(String assistantIdDeleteSession) {
		this.assistantIdDeleteSession = assistantIdDeleteSession;
	}
	public String getVersionDeleteSession() {
		return versionDeleteSession;
	}
	public void setVersionDeleteSession(String versionDeleteSession) {
		this.versionDeleteSession = versionDeleteSession;
	}
	
}
