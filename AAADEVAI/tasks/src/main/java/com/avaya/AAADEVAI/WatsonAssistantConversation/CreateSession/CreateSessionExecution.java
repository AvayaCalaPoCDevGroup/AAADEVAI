package com.avaya.AAADEVAI.WatsonAssistantConversation.CreateSession;

import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.roobroo.bpm.model.BpmNode;

public class CreateSessionExecution extends NodeInstance{
	
	private static final long serialVersionUID = 1L;

	public CreateSessionExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}
	
	public Object execute() throws Exception {
		try{
			JSONObject jsonResultAssistant = new CreateSessionHttps().createSessionAssistantConversation();
			if(jsonResultAssistant.has("status") && jsonResultAssistant.getString("status").equals("ok")){
				if(jsonResultAssistant.has("error") && jsonResultAssistant.has("code")){
					//Cloud Services Hizo bien la petición HTTPS sin embargo hubo un error con IBM Cloud.
					return new JSONObject().put("status", "ERROR").put("error", jsonResultAssistant.getString("error")).put("code", Integer.toString(jsonResultAssistant.getInt("code")));
					
				}else{
					//Respuesta de IBM Cloud desde Cloud Services.
					if(jsonResultAssistant.has("session_id")){
						return new JSONObject().put("status", "SUCCESS")
												.put("code", "200").put("error", "none")
												.put("session_id", jsonResultAssistant.getString("session_id"));
					}else{
						return new JSONObject().put("status", "ERROR").put("error", "No Session found").put("code", "400");
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
