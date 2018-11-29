package com.avaya.AAADEVAI;


import java.util.List;

import com.roobroo.bpm.model.BpmNode;
import com.roobroo.bpm.util.WFUtil;
@SuppressWarnings("serial")
public class EmocionesModel extends BpmNode {


	public EmocionesModel(String name, String id) {
		super(name, id);
		// TODO Auto-generated constructor stub
	}
	
	private String textEmociones;
	private String userNameEmociones;
	private String passwordEmociones;
    
	
	public String getTextEmociones() {
		return textEmociones;
	}
	public void setTextEmociones(String textEmociones) {
		this.textEmociones = textEmociones;
	}
	public String getUserNameEmociones() {
		return userNameEmociones;
	}
	public void setUserNameEmociones(String userNameEmociones) {
		this.userNameEmociones = userNameEmociones;
	}
	public String getPasswordEmociones() {
		return passwordEmociones;
	}
	public void setPasswordEmociones(String passwordEmociones) {
		this.passwordEmociones = passwordEmociones;
	}

	@Override
    public boolean validateProperties(List<String> w, List<String> e) {
        boolean isValid = true;
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "textEmociones"))
                && (!WFUtil.validateEmptyProperty(textEmociones, "textEmociones", e))) {
            isValid = false;
         
        }
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "userNameEmociones"))
                && (!WFUtil.validateEmptyProperty(userNameEmociones, "userNameEmociones", e))) {
            isValid = false;
         
        }
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "passwordEmociones"))
                && (!WFUtil.validateEmptyProperty(passwordEmociones, "passwordEmociones", e))) {
            isValid = false;
         
        }
        
        return super.validateProperties(w, e) && isValid;
        
	}
	

}
