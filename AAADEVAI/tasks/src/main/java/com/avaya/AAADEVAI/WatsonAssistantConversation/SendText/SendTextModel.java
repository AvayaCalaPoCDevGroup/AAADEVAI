package com.avaya.AAADEVAI.WatsonAssistantConversation.SendText;

import java.util.List;

import com.roobroo.bpm.model.BpmNode;
import com.roobroo.bpm.util.WFUtil;

public class SendTextModel extends BpmNode{

	public SendTextModel(String name, String id) {
		super(name, id);
	}

	private static final long serialVersionUID = 1L;

	private String sessionSendText;
	private String textSendText;
	
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
	
	@Override
    public boolean validateProperties(List<String> w, List<String> e) {
        boolean isValid = true;
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "sessionSendText"))
                && (!WFUtil.validateEmptyProperty(sessionSendText, "sessionSendText", e))) {
            isValid = false;
         
        }
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "textSendText"))
                && (!WFUtil.validateEmptyProperty(textSendText, "textSendText", e))) {
            isValid = false;
         
        }
        return super.validateProperties(w, e) && isValid;
	}
}
