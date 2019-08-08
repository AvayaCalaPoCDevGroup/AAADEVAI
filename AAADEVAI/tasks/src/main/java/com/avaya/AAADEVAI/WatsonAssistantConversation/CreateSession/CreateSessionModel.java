package com.avaya.AAADEVAI.WatsonAssistantConversation.CreateSession;

import com.roobroo.bpm.model.BpmNode;

public class CreateSessionModel extends BpmNode{

	public CreateSessionModel(String name, String id) {
		super(name, id);
	}

	private static final long serialVersionUID = 1L;
	private String userNameCreateSession;
	private String passwordCreateSession;
	private String assistantIdCreateSession;
	private String versionCreateSession;
	
	public String getUserNameCreateSession() {
		return userNameCreateSession;
	}
	public void setUserNameCreateSession(String userNameCreateSession) {
		this.userNameCreateSession = userNameCreateSession;
	}
	public String getPasswordCreateSession() {
		return passwordCreateSession;
	}
	public void setPasswordCreateSession(String passwordCreateSession) {
		this.passwordCreateSession = passwordCreateSession;
	}
	public String getAssistantIdCreateSession() {
		return assistantIdCreateSession;
	}
	public void setAssistantIdCreateSession(String assistantIdCreateSession) {
		this.assistantIdCreateSession = assistantIdCreateSession;
	}
	public String getVersionCreateSession() {
		return versionCreateSession;
	}
	public void setversioncreatesession(String versionCreateSession) {
		this.versionCreateSession = versionCreateSession;
	}
	
}
