package com.avaya.AAADEVAI;

import java.util.List;

import com.roobroo.bpm.model.BpmNode;
import com.roobroo.bpm.util.WFUtil;

public class TraductorModel extends BpmNode {

	private static final long serialVersionUID = 1L;

	public TraductorModel(String name, String id) {
		super(name, id);
		// TODO Auto-generated constructor stub
	}
	
	private String texto;
	private String modelId;
	private String apiKey;
		
	/**
     * @return the texto
     */
    public String getTexto() {
        return texto;
    }

    /**
     * @param texto the texto to set
     */
    public void setTexto(String texto) {
        this.texto = texto;
    }

    /**
     * @return the modelId
     */
    public String getModelId() {
        return modelId;
    }

    /**
     * @param modelId the modelId to set
     */
    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    /**
     * @return the apiKey
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * @param apiKey the apiKey to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    @Override
    public boolean validateProperties(List<String> w, List<String> e) {
        boolean isValid = true;
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "texto"))
                && (!WFUtil.validateEmptyProperty(texto, "texto", e))) {
            isValid = false;
         
        }
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "modelId"))
                && (!WFUtil.validateEmptyProperty(modelId, "modelId", e))) {
            isValid = false;
         
        }
        if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(), "apiKey"))
                && (!WFUtil.validateEmptyProperty(apiKey, "apiKey", e))) {
            isValid = false;
         
        }
        
        return super.validateProperties(w, e) && isValid;
        
	}
}
