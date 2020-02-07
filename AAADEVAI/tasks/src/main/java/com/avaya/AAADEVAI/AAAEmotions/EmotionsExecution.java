package com.avaya.AAADEVAI.AAAEmotions;

import java.text.DecimalFormat;

import org.json.JSONObject;

import com.avaya.AAADEVAI.BuscarYRemplazarAcentos;
import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.roobroo.bpm.model.BpmNode;

public class EmotionsExecution extends NodeInstance{

	private static final long serialVersionUID = 1L;
	public EmotionsExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	public Object execute() throws Exception {
		
		final EmotionsModel model = (EmotionsModel)getNode();
		//Se valida que se haya insertado texto para Analizar.
		String textToAnalize = (String)get("textEmotions");
		if(textToAnalize == null || textToAnalize.isEmpty()){
			textToAnalize = model.getTextEmotions();
			if(textToAnalize == null || textToAnalize.isEmpty()){
				throw new IllegalArgumentException(
						"Invalid Argument! Text to Analize cannot be empty...");
			}
		}
		//Se valida que se haya seleccionado el lenguaje.
		String languageSelected = (String)get("languageEmotions");
		if(languageSelected == null || languageSelected.isEmpty()){
			languageSelected = model.getLanguageEmotions();
			if(languageSelected == null || languageSelected.isEmpty()){
				languageSelected = "en";
			}
		}
		
		//Se formatea el texto para cambiar caracteres especiales a codigo HTML
		if(languageSelected.equals("es")){
			BuscarYRemplazarAcentos español = new BuscarYRemplazarAcentos();
			textToAnalize = español.Español(textToAnalize);
		}
		//Se formatea el texto para cambiar caracteres especiales a codigo HTML
		if(languageSelected.equals("pt")){
			BuscarYRemplazarAcentos portugues = new BuscarYRemplazarAcentos();
			textToAnalize = portugues.Portugues(textToAnalize);
		}
		
		try{
			JSONObject jsonResultEmotions = new EmotionsHttps(textToAnalize, languageSelected).analize();
			if(jsonResultEmotions.has("status") && jsonResultEmotions.getString("status").equals("ok")){
				if(jsonResultEmotions.has("error") && jsonResultEmotions.has("code")){
					//Cloud Services Hizo bien la petición HTTPS sin embargo hubo un error con IBM Cloud.
					return new JSONObject().put("status", "ERROR").put("error", jsonResultEmotions.getString("error")).put("code", Integer.toString(jsonResultEmotions.getInt("code")));
					
				}else{
					//Respuesta de IBM Cloud desde Cloud Services.
					if(jsonResultEmotions.has("emotion")){
						double sadness = jsonResultEmotions.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getDouble("sadness");
						double joy = jsonResultEmotions.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getDouble("joy");
						double fear = jsonResultEmotions.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getDouble("fear");
						double disgust = jsonResultEmotions.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getDouble("disgust");
						double anger = jsonResultEmotions.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getDouble("anger");
						
						return new JSONObject().put("status", "SUCCESS").put("code", "200").put("error", "nome")
												.put("sadness", (sadness == 0.0)?"0":new DecimalFormat("#.00").format((sadness * 100)))
												.put("joy", (sadness == 0.0)?"0":new DecimalFormat("#.00").format((joy * 100)))
												.put("fear", (sadness == 0.0)?"0":new DecimalFormat("#.00").format((fear * 100)))
												.put("disgust", (sadness == 0.0)?"0":new DecimalFormat("#.00").format((disgust * 100)))
												.put("anger", (sadness == 0.0)?"0":new DecimalFormat("#.00").format((anger * 100)));
					}else{
						return new JSONObject().put("status", "ERROR").put("error", "No Emotions found").put("code", "400");
					}
				}
			}else{
				//Cloud Services falló en realizar la petición HTTPS.
				return new JSONObject().put("status", "ERROR").put("error", jsonResultEmotions.getString("message")).put("code", "400");
			}
		}catch(Exception e){
			return new JSONObject().put("status", "ERROR").put("error", e.toString()).put("code", "400");
		}
	}

}
