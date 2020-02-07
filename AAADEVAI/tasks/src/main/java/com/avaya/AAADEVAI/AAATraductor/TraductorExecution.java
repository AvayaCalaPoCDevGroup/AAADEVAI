package com.avaya.AAADEVAI.AAATraductor;

import org.json.JSONObject;

import com.avaya.AAADEVAI.BuscarYRemplazarAcentos;
import com.avaya.AAADEVAI.AAATraductor.TraductorModel;
import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.roobroo.bpm.model.BpmNode;

public class TraductorExecution extends NodeInstance{

	private static final long serialVersionUID = 1L;
	public TraductorExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	public Object execute() throws Exception {
	
		final TraductorModel traductor = (TraductorModel)getNode();
		//Se valida que se haya insertado texto para traducir.
		String textToTranslate = (String)get("textTraductor");
		if (textToTranslate == null || textToTranslate.isEmpty()){
			textToTranslate = traductor.getTextoTraductor();
			if (textToTranslate == null || textToTranslate.isEmpty()){
				throw new IllegalArgumentException(
						"Invalid Argument! Text to Translate cannot be empty...");
			}
		}
		//Se valida que se haya seleccionado el lenguaje.
		String languageSelected = (String)get("languageTraductor");
		if(languageSelected == null || languageSelected.isEmpty()){
			languageSelected = traductor.getLanguajeTraductor();
			if(languageSelected == null || languageSelected.isEmpty()){
				throw new IllegalArgumentException(
						"Invalid Argument! Language cannot be empty...");
			}
		}
		//Se formatea el texto para cambiar caracteres especiales a codigo HTML
		if(languageSelected.equals("es-en")){
			BuscarYRemplazarAcentos español = new BuscarYRemplazarAcentos();
			textToTranslate = español.Español(textToTranslate);
		}
		//Se formatea el texto para cambiar caracteres especiales a codigo HTML
		if(languageSelected.equals("pt-en")){
			BuscarYRemplazarAcentos portugues = new BuscarYRemplazarAcentos();
			textToTranslate = portugues.Portugues(textToTranslate);
		}
		try{
		JSONObject jsonResultTranslator = new TraductorHttps(textToTranslate, languageSelected).translate();
			if(jsonResultTranslator.has("status") && jsonResultTranslator.getString("status").equals("ok")){
				if(jsonResultTranslator.has("error") && jsonResultTranslator.has("code")){
					//Cloud Services Hizo bien la petición HTTPS sin embargo hubo un error con IBM Cloud.
					return new JSONObject().put("status", "ERROR").put("error", jsonResultTranslator.getString("error")).put("code", Integer.toString(jsonResultTranslator.getInt("code")));
					
				}else{
					//Respuesta de IBM Cloud desde Cloud Services.
					if(jsonResultTranslator.has("translations")){
						return new JSONObject().put("translation", jsonResultTranslator.getJSONArray("translations").getJSONObject(0).getString("translation"))
												.put("wordcount", Integer.toString(jsonResultTranslator.getInt("word_count")))
												.put("charactercount", Integer.toString(jsonResultTranslator.getInt("character_count")))
												.put("status", "SUCCESS")
												.put("code", "200")
												.put("error", "none");
					}else{
						return new JSONObject().put("status", "ERROR").put("error", "No transcript found").put("code", "400");
					}
				}
			}else{
				//Cloud Services falló en realizar la petición HTTPS.
				return new JSONObject().put("status", "ERROR").put("error", jsonResultTranslator.getString("message")).put("code", "400");
			}
		}catch(Exception e){
			return new JSONObject().put("status", "ERROR").put("error", e.toString()).put("code", "400");
		}

		
		
		
	}
}
