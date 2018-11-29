package com.avaya.AAADEVAI;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;
import com.roobroo.bpm.model.BpmNode;

@SuppressWarnings({ "serial", "deprecation" })
public class EmocionesExecution extends NodeInstance {

	public EmocionesExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	public Object execute() throws Exception {
		final JSONObject obj = new JSONObject();
		final JSONObject emotions = new JSONObject();
		EmocionesModel emociones = (EmocionesModel)getNode();
		
		String content = (String)get("textEmociones");
		if ((content == null) || (content.isEmpty())){	
			content = emociones.getTextEmociones();
		}
		
		String user = emociones.getUserNameEmociones();
		String password = "zhpZkWeqIdT6";
		
		

		final String [] resultEmotions = callWatsonEmotions(user, password, content);
		if (resultEmotions == null) {
			obj.put("status", "failure");
		} else {
			obj.put("status", "success");
			emotions.put("sadness", resultEmotions[0]);
			emotions.put("joy", resultEmotions[1]);
			emotions.put("fear", resultEmotions[2]);
			emotions.put("disgust", resultEmotions[3]);
			emotions.put("anger", resultEmotions[4]);
			obj.put("emociones", emotions);
		}
		
		return obj;
	}
	
	private String [] callWatsonEmotions(String user, String password, String content) {

		try {
		      final SSLProtocolType protocolType = SSLProtocolType.TLSv1_2;
		      final SSLContext sslContext = SSLUtilityFactory.createSSLContext(protocolType);
			final CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(user, password));
			String encodedMessage = URLEncoder.encode(content, "UTF-8");
			final String URI = "https://gateway.watsonplatform.net/natural-language-understanding/api/v1/analyze?version=2018-05-01&text="
					+encodedMessage+"&features=emotion&return_analyzed_text=false&clean=true&fallback_to_raw=true&concepts.limit=8&emotion.document=true&entities.limit=50&keywords.limit=50&sentiment.document=true";

		      HttpClient client = HttpClients.custom().setSslcontext(sslContext).setHostnameVerifier(new AllowAllHostnameVerifier()).build();
		      HttpGet getMethod = new HttpGet(URI);
		      getMethod.addHeader("Accept", "application/json");
		      getMethod.addHeader("Content-Type", "application/json");

			final String authString = user + ":" + password;
			final String authEncBytes = DatatypeConverter
					.printBase64Binary(authString.getBytes());
			getMethod.addHeader("Authorization", "Basic " + authEncBytes);


			final HttpResponse response = client.execute(getMethod);

			final BufferedReader inputStream = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			final StringBuilder result = new StringBuilder();
			while ((line = inputStream.readLine()) != null) {
				result.append(line);
			}
			
			JSONObject json = new JSONObject(result.toString());
			
			String sadness = json.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getString("sadness");
			String joy = json.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getString("joy");
			String fear = json.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getString("fear");
			String disgust = json.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getString("disgust");
			String anger = json.getJSONObject("emotion").getJSONObject("document").getJSONObject("emotion").getString("anger");
			
			
			String [] arregloEmociones = {sadness, joy, fear, disgust, anger};
			inputStream.close();
			return arregloEmociones;
		} catch (Exception ex) {
			return null;
		}

	}

}
