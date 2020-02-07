package com.avaya.AAADEVAI.WatsonAssistantConversation.DeleteSession;

import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.roobroo.bpm.model.BpmNode;

public class DeleteSessionExecution extends NodeInstance{

	private static final long serialVersionUID = 1L;

	public DeleteSessionExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}
	
	public Object execute() throws Exception {
		DeleteSessionModel deleteSessionModel = (DeleteSessionModel)getNode();


		String sessionDeleteSession = (String)get("sessionDeleteSession");
		if(sessionDeleteSession == null || sessionDeleteSession.isEmpty()){
			sessionDeleteSession = deleteSessionModel.getSessionDeleteSession();
			if(sessionDeleteSession == null || sessionDeleteSession.isEmpty()){
				throw new IllegalArgumentException("Seesion ID Cannot Be Empty...");
			}
		}
		
		try{
			JSONObject jsonResultAssistant = new DeleteSessionHttps(sessionDeleteSession).deleteSession();
			if(jsonResultAssistant.has("status") && jsonResultAssistant.getString("status").equals("ok")){
				if(jsonResultAssistant.has("error") && jsonResultAssistant.has("code")){
					//Cloud Services Hizo bien la petición HTTPS sin embargo hubo un error con IBM Cloud.
					return new JSONObject().put("status", "ERROR").put("error", jsonResultAssistant.getString("error")).put("code", Integer.toString(jsonResultAssistant.getInt("code")));
					
				}else{
					//Respuesta de IBM Cloud desde Cloud Services.
					return new JSONObject().put("status", "SUCCESS")
												.put("code", "200").put("error", "none");
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
