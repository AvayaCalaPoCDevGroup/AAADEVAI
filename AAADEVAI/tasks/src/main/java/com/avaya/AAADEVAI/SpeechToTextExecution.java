package com.avaya.AAADEVAI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.roobroo.bpm.model.BpmNode;

@SuppressWarnings({ "serial" })
public class SpeechToTextExecution extends NodeInstance {

	private String[] resultSpeechs;

	public SpeechToTextExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	public Object execute() throws Exception {
		final JSONObject obj = new JSONObject();
		SpeechToTextModel speech = (SpeechToTextModel) getNode();

		String url = (String) get("url");
		if ((url == null) || (url.isEmpty())) {
			url = speech.getUrl();
		}
		
		

		
		String[] transcriptNameArray = url.split("\\/");
		String recFileName = transcriptNameArray[transcriptNameArray.length - 1];
		
		String domain = transcriptNameArray[2];

		String language = speech.getLanguage();

		String password = "";

		resultSpeechs = callVPS(recFileName, password, language, domain);

		if (resultSpeechs == null) {
			obj.put("status", "failure");
		} else {
			String transcripcion = null;
			obj.put("status", "success");
			if(language.equals("es-MX")){
				BuscarYRemplazarAcentos español = new BuscarYRemplazarAcentos();
				transcripcion = español.Español(resultSpeechs[0]);
				
			}
			if(language.equals("pt-BR")){
				BuscarYRemplazarAcentos portugues = new BuscarYRemplazarAcentos();
				transcripcion = portugues.Portugues(resultSpeechs[0]);
			}
			if(language.equals("en-US")){
				transcripcion = resultSpeechs[0];
			}
			obj.put("transcripcion", transcripcion);
			obj.put("confianza", resultSpeechs[1]);
		}

		return obj;
	}

	private String[] callVPS(String recFileName, String password,
			String language, String domain) {

		String[] exitCodes = { null, null };
		try {

			final String URI = "http://devavaya.ddns.net:8080/AAADEVURIEL_PRUEBAS_WATSON-war-1.0.0.0.0/TranscriptDesdeEngagement?apiKey="
					+ password
					+ "&idioma="
					+ language
					+ "&nomreArchivo="
					+ recFileName	
					+ "&dominio="
					+ domain;
				

			final HttpClient clientSpeech = HttpClients.createDefault();

			final HttpPost postMethodSpeech = new HttpPost(URI);

			postMethodSpeech.addHeader("Accept", "application/json; charset=UTF-8");
			postMethodSpeech.addHeader("Content-Type", "application/json; charset=UTF-8");

			final String messageBodySpeech = "";
			final StringEntity conversationEntitySpeech = new StringEntity(
					messageBodySpeech);
			postMethodSpeech.setEntity(conversationEntitySpeech);

			final HttpResponse responseSpeech = clientSpeech
					.execute(postMethodSpeech);

			final BufferedReader inputStreamSpeech = new BufferedReader(
					new InputStreamReader(responseSpeech.getEntity()
			.getContent(), StandardCharsets.ISO_8859_1));

			String line = "";
			final StringBuilder result = new StringBuilder();
			while ((line = inputStreamSpeech.readLine()) != null) {
				result.append(line);
			}

			JSONObject json = new JSONObject(result.toString());

			String transcript = json.getString("results");
			JSONArray array = new JSONArray(transcript);
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				// exitCode = object.get("alternatives").toString();
				String alternatives = object.getString("alternatives");
				JSONArray array2 = new JSONArray(alternatives);
				for (int j = 0; j < array2.length(); j++) {
					JSONObject object2 = array2.getJSONObject(i);
					exitCodes[0] = object2.get("transcript").toString();
					exitCodes[1] = object2.get("confidence").toString();
				}

			}

			inputStreamSpeech.close();
			postMethodSpeech.reset();

		} catch (Exception e) {
			String[] error = { e.toString(), e.toString() };
			return error;
		}
		
		
		return exitCodes;
	}

}
