package com.avaya.AAADEVAI.WatsonAssistant;


import java.util.List;

import com.roobroo.bpm.model.BpmNode;
import com.roobroo.bpm.util.WFUtil;

@SuppressWarnings("serial")
public class AssistantModel extends BpmNode {

	public AssistantModel(String name, String id) {
		super(name, id);
	}
	
	private String textAssistant;
	
	public String getTextAssistant() {
		return textAssistant;
	}
	public void setTextAssistant(String textAssistant) {
		this.textAssistant = textAssistant;
	}

	@Override
    public boolean validateProperties(List<String> w, List<String> e) {
        boolean isValid = true;
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "textAssistant"))
                && (!WFUtil.validateEmptyProperty(textAssistant, "textAssistant", e))) {
            isValid = false;
         
        }
        return super.validateProperties(w, e) && isValid;
	}

	
}
