package com.avaya.AAADEVAI.WatsonAssistantConversation.DeleteSession;

import java.util.List;

import com.roobroo.bpm.model.BpmNode;
import com.roobroo.bpm.util.WFUtil;

public class DeleteSessionModel extends BpmNode{

	public DeleteSessionModel(String name, String id) {
		super(name, id);
	}

	private static final long serialVersionUID = 1L;
	private String sessionDeleteSession;
	
	public String getSessionDeleteSession() {
		return sessionDeleteSession;
	}
	public void setSessionDeleteSession(String sessionDeleteSession) {
		this.sessionDeleteSession = sessionDeleteSession;
	}
	
	@Override
    public boolean validateProperties(List<String> w, List<String> e) {
        boolean isValid = true;
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "sessionDeleteSession"))
                && (!WFUtil.validateEmptyProperty(sessionDeleteSession, "sessionDeleteSession", e))) {
            isValid = false;
         
        }
        return super.validateProperties(w, e) && isValid;
	}
}
