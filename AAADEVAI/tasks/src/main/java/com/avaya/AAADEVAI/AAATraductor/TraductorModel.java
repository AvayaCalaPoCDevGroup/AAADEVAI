package com.avaya.AAADEVAI.AAATraductor;

import java.util.List;

import com.roobroo.bpm.model.BpmNode;
import com.roobroo.bpm.util.WFUtil;

public class TraductorModel extends BpmNode {

	private static final long serialVersionUID = 1L;

	public TraductorModel(String name, String id) {
		super(name, id);
	}
	
	private String textTraductor;
	private String languageTraductor;


	public String getTextoTraductor() {
		return textTraductor;
	}

	public void setTextoTraductor(String textoTraductor) {
		this.textTraductor = textoTraductor;
	}

	public String getLanguajeTraductor() {
		return languageTraductor;
	}

	public void setLanguajeTraductor(String languajeTraductor) {
		this.languageTraductor = languajeTraductor;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
    public boolean validateProperties(List<String> w, List<String> e) {
        boolean isValid = true;
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "textTraductor"))
                && (!WFUtil.validateEmptyProperty(textTraductor, "textTraductor", e))) {
            isValid = false;
         
        }
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "languageTraductor"))
                && (!WFUtil.validateEmptyProperty(languageTraductor, "languageTraductor", e))) {
            isValid = false;
         
        }        
        return super.validateProperties(w, e) && isValid;
        
	}
	
}
