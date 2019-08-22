package com.avaya.AAADEVAI.WatsonAssistant;


import java.util.List;

import com.roobroo.bpm.model.BpmNode;
import com.roobroo.bpm.util.WFUtil;

@SuppressWarnings("serial")
public class AssistantModel extends BpmNode {

	public AssistantModel(String name, String id) {
		super(name, id);
		// TODO Auto-generated constructor stub
	}
	
	private String textAssistant;
	private String userNameAssistant;
	private String passwordAssistant;
	private String workSpaceIdAssistant;
	
	public String getTextAssistant() {
		return textAssistant;
	}
	public void setTextAssistant(String textAssistant) {
		this.textAssistant = textAssistant;
	}
	public String getUserNameAssistant() {
		return userNameAssistant;
	}
	public void setUserNameAssistant(String userNameAssistant) {
		this.userNameAssistant = userNameAssistant;
	}
	public String getPasswordAssistant() {
		return passwordAssistant;
	}
	public void setPasswordAssistant(String passwordAssistant) {
		this.passwordAssistant = passwordAssistant;
	}
	public String getWorkSpaceIdAssistant() {
		return workSpaceIdAssistant;
	}
	public void setWorkSpaceIdAssistant(String workSpaceIdAssistant) {
		this.workSpaceIdAssistant = workSpaceIdAssistant;
	}

	@Override
    public boolean validateProperties(List<String> w, List<String> e) {
        boolean isValid = true;
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "textAssistant"))
                && (!WFUtil.validateEmptyProperty(textAssistant, "textAssistant", e))) {
            isValid = false;
         
        }
        
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "userNameAssistant"))
                && (!WFUtil.validateEmptyProperty(userNameAssistant, "userNameAssistant", e))) {
            isValid = false;
            
        }

        
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "passwordAssistant"))
                && (!WFUtil.validateEmptyProperty(passwordAssistant, "passwordAssistant", e))) {
            isValid = false;
           
        }

        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "workSpaceIdAssistant"))
                && (!WFUtil.validateEmptyProperty(workSpaceIdAssistant, "workSpaceIdAssistant", e))) {
            isValid = false;
           
        }
        

        
        return super.validateProperties(w, e) && isValid;
	}

	
}
