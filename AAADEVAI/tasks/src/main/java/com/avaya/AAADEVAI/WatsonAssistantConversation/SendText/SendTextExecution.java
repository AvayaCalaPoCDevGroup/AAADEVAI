package com.avaya.AAADEVAI.WatsonAssistantConversation.SendText;

import java.text.DecimalFormat;

import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.roobroo.bpm.model.BpmNode;


public class SendTextExecution extends NodeInstance{

	private static final long serialVersionUID = 1L;

	public SendTextExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}
	
	public Object execute() throws Exception {
		SendTextModel sendTextModel = (SendTextModel)getNode();

		String sessionSendText = (String)get("sessionSendText");
		if(sessionSendText == null || sessionSendText.isEmpty()){
			sessionSendText = sendTextModel.getSessionSendText();
			if(sessionSendText == null || sessionSendText.isEmpty()){
				throw new IllegalArgumentException("Session ID Connot Be Empty...");
			}
		}
		String textSendText = (String)get("textSendText");
		if(textSendText == null || textSendText.isEmpty()){
			textSendText = sendTextModel.getTextSendText();
			if(textSendText == null || textSendText.isEmpty()){
				throw new IllegalArgumentException("Text Cannot Be Empty...");
			}
		}
		
		try{
			JSONObject jsonResultAssistant = new SendTextHttps(sessionSendText, textSendText).sendText();
			if(jsonResultAssistant.has("status") && jsonResultAssistant.getString("status").equals("ok")){
				if(jsonResultAssistant.has("error") && jsonResultAssistant.has("code")){
					//Cloud Services Hizo bien la petición HTTPS sin embargo hubo un error con IBM Cloud.
					return new JSONObject().put("status", "ERROR").put("error", jsonResultAssistant.getString("error")).put("code", Integer.toString(jsonResultAssistant.getInt("code")));
					
				}else{
					//Respuesta de IBM Cloud desde Cloud Services.
					if(jsonResultAssistant.has("output")){
						return new JSONObject().put("status", "SUCCESS")
												.put("code", "200").put("error", "none")
												.put("response", jsonResultAssistant.getJSONObject("output").getJSONArray("generic").getJSONObject(0).getString("text"))
												.put("intent", jsonResultAssistant.getJSONObject("output").getJSONArray("intents").getJSONObject(0).getString("intent"))
												.put("confidence", new DecimalFormat("#.00").format((jsonResultAssistant.getJSONObject("output").getJSONArray("intents").getJSONObject(0).getDouble("confidence") * 100)));
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
