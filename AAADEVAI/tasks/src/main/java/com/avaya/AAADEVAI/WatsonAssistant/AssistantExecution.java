package com.avaya.AAADEVAI.WatsonAssistant;

import java.text.DecimalFormat;

import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.roobroo.bpm.model.BpmNode;

public class AssistantExecution extends NodeInstance {

	private static final long serialVersionUID = 1L;

	public AssistantExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	public Object execute() throws Exception {

		AssistantModel assistant = (AssistantModel) getNode();
		String textAssistant = (String) get("textAssistant");
		if ((textAssistant == null) || (textAssistant.isEmpty())) {
			textAssistant = assistant.getTextAssistant();
			if(textAssistant == null || textAssistant.isEmpty()){
				throw new IllegalArgumentException("The Text Cannot Be Empty...");
			}
		}
		try{
			JSONObject jsonResultAssistant = new AssistantHttps(textAssistant).assist();
			if(jsonResultAssistant.has("status") && jsonResultAssistant.getString("status").equals("ok")){
				if(jsonResultAssistant.has("error") && jsonResultAssistant.has("code")){
					//Cloud Services Hizo bien la petición HTTPS sin embargo hubo un error con IBM Cloud.
					return new JSONObject().put("status", "ERROR").put("error", jsonResultAssistant.getString("error")).put("code", Integer.toString(jsonResultAssistant.getInt("code")));
					
				}else{
					//Respuesta de IBM Cloud desde Cloud Services.
					if(jsonResultAssistant.has("intents")){
						return new JSONObject().put("status", "SUCCESS")
												.put("code", "200").put("error", "none")
												.put("intent", jsonResultAssistant.getJSONArray("intents").getJSONObject(0).getString("intent"))
												.put("confidence", new DecimalFormat("#.00").format((jsonResultAssistant.getJSONArray("intents").getJSONObject(0).getDouble("confidence") * 100)));
					}else{
						return new JSONObject().put("status", "ERROR").put("error", "No Emotions found").put("code", "400");
					}
				}
			}else{
				//Cloud Services falló en realizar la petición HTTPS.
				return new JSONObject().put("status", "ERROR").put("error", jsonResultAssistant.getString("message")).put("code", "400");
			}
		}catch(Exception e){
			return new JSONObject().put("status", "ERROR").put("error", e.toString()).put("code", "400");
		}
		

	}

}
