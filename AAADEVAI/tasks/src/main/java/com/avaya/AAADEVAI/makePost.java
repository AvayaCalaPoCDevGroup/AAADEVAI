package com.avaya.AAADEVAI;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;

@SuppressWarnings("deprecation")
public class makePost extends Thread{

	private final String url;
	private final String password;
	private final String language;
	private String Base64;
	private String traducción;
	private String confianza;
	private String result;
	
	public makePost(final String url, final String password, final String language) {
		this.url = url;
		this.password = password;
		this.language = language;
	}
	
	
	@Override
	public void run() {
		final String URI = url;

		final HttpClient client = HttpClients.createDefault();
		HttpGet getMethod = new HttpGet(URI);
		getMethod.addHeader("Accept", "application/json");
		getMethod.addHeader("Content-Type", "application/json");

		HttpResponse response = null;
		try {
			response = client.execute(getMethod);
		} catch (IOException e) {
			setResult(e.toString());
		}

		BufferedReader inputStream = null;
		try {
			inputStream = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));
		} catch (IOException e) {
			setResult(e.toString());
		}

		String line = "";
		final StringBuilder result = new StringBuilder();
		try {
			while ((line = inputStream.readLine()) != null) {
				result.append(line);
			}
		} catch (IOException e) {
			setResult(e.toString());
		}
		
		Base64 = result.toString();
		
		final String [] resultSpeech = callWatsonSpeech(Base64.trim(), password, language);
		
		setTraducción(resultSpeech[0]);
		setConfianza(resultSpeech[1]);
		setResult("success");
		notify();
	}
	
	
	public String getTraducción() {
		return traducción;
	}


	public void setTraducción(String traducción) {
		this.traducción = traducción;
	}


	public String getConfianza() {
		return confianza;
	}


	public void setConfianza(String confianza) {
		this.confianza = confianza;
	}


	public String getUrl() {
		return url;
	}


	public String getBase64() {
		return Base64;
	}


	public String getPassword() {
		return password;
	}


	public String getLanguage() {
		return language;
	}
	
	public String getResult() {
		return result;
	}


	public void setResult(String result) {
		this.result = result;
	}
	
	
	private String[] callWatsonSpeech(String Base64, String  password, String language){

		String[] exitCodes = {null, null};
		try {
			final SSLProtocolType protocolTypeSpeech = SSLProtocolType.TLSv1_2;
		    final SSLContext sslContextSpeech = SSLUtilityFactory.createSSLContext(protocolTypeSpeech);
//		    
		    final String URI = "https://speech.googleapis.com/v1/speech:recognize?key="+password;
		    
		    int timeout = 5;
		    RequestConfig config = RequestConfig.custom()
		      .setConnectTimeout(timeout * 10000)
		      .setConnectionRequestTimeout(timeout * 10000)
		      .setSocketTimeout(timeout * 10000).build();
		    
		    final HttpClient clientSpeech = HttpClients.custom().setSslcontext(sslContextSpeech).setHostnameVerifier(new AllowAllHostnameVerifier()).setDefaultRequestConfig(config).build();
//		    final HttpClient clientSpeech = HttpClients.createDefault();
		    
		    final HttpPost postMethodSpeech = new HttpPost(URI);
			
			postMethodSpeech.addHeader("Accept", "application/json");
			postMethodSpeech.addHeader("Content-Type", "application/json");
			
			final String messageBodySpeech = "{ \"audio\": { \"content\": \""+Base64+"\"},\"config\": {\"enableAutomaticPunctuation\": true, \"encoding\": \"LINEAR16\", \"languageCode\": \""+language+"\",\"model\": \"default\"} }";
			final StringEntity conversationEntitySpeech = new StringEntity(messageBodySpeech);
			postMethodSpeech.setEntity(conversationEntitySpeech);
			
			final HttpResponse responseSpeech = clientSpeech.execute(postMethodSpeech);

			final BufferedReader inputStreamSpeech = new BufferedReader(
					new InputStreamReader(responseSpeech.getEntity().getContent()));

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
//			    exitCode = object.get("alternatives").toString();
			    String alternatives = object.getString("alternatives");
			    JSONArray array2 = new JSONArray(alternatives);
			    for (int j = 0; j < array2.length(); j++){
			    	JSONObject object2 = array2.getJSONObject(i);
			    	exitCodes[0] = object2.get("transcript").toString();
			    	exitCodes[1] = object2.get("confidence").toString();
			    }
			
			}
			
			
			inputStreamSpeech.close();
			postMethodSpeech.reset();
			
		}catch(Exception e){
			String [] error = {e.toString(), e.toString()};
			return error;
		}
		
		return exitCodes;
	}



}
