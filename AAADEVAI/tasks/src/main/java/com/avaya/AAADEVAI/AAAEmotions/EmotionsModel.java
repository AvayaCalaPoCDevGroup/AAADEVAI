package com.avaya.AAADEVAI.AAAEmotions;

import java.util.List;

import com.roobroo.bpm.model.BpmNode;
import com.roobroo.bpm.util.WFUtil;

public class EmotionsModel extends BpmNode  {

	private static final long serialVersionUID = 1L;
	private String textEmotions;
	private String languageEmotions;
	
	public EmotionsModel(String name, String id) {
		super(name, id);
	}

	public String getTextEmotions() {
		return textEmotions;
	}

	public void setTextEmotions(String textEmotions) {
		this.textEmotions = textEmotions;
	}

	public String getLanguageEmotions() {
		return languageEmotions;
	}

	public void setLanguageEmotions(String languageEmotions) {
		this.languageEmotions = languageEmotions;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

    public boolean validateProperties(List<String> w, List<String> e) {
        boolean isValid = true;
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "textEmotions"))
                && (!WFUtil.validateEmptyProperty(textEmotions, "textEmotions", e))) {
            isValid = false;
         
        }
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "languageEmotions"))
                && (!WFUtil.validateEmptyProperty(languageEmotions, "languageEmotions", e))) {
            isValid = false;
         
        }        
        return super.validateProperties(w, e) && isValid;
        
	}
	
	
}
