package com.avaya.AAADEVAI;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;
import com.roobroo.bpm.model.BpmNode;

//Detail on NodeInstance is provided in the section NodeInstance API class.
@SuppressWarnings({ "serial", "deprecation" })
public class TraductorExecution extends NodeInstance {
	

	/*
	 * 
	 * Funciona con mockable http y https
	 */
	public TraductorExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	public Object execute() throws Exception {
		final JSONObject obj = new JSONObject();
		TraductorModel traductor = (TraductorModel)getNode();
		
		String content = (String)get("texto");
		if ((content == null) || (content.isEmpty())){
			content = traductor.getTexto();
		}
		
		String password = "kVrAnsbYI64uK5oJvjDCYwftGECOUgLDYTJbPc7sbsJX";
//		password = CryptoUtil.getInstance().decrypt(password);
		
		String user = "apikey";
		String modelId = traductor.getModelId();
		
		final String resultTraductor = callWatsonTraductor(user, password, content, modelId);
		if (resultTraductor == null) {
			obj.put("status", "Failure");
		} else {
			obj.put("status", "Success");
			obj.put("traduccion", resultTraductor);
		}
		return obj;

	}

	private String callWatsonTraductor(String user, String password, String content, String modelId) {

		String exitCode = null;
		try {
		      final SSLProtocolType protocolTypeTraductor = SSLProtocolType.TLSv1_2;
		      final SSLContext sslContextTraductor = SSLUtilityFactory.createSSLContext(protocolTypeTraductor);
			final CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(user, password));

			final String URI = "https://gateway.watsonplatform.net/language-translator/api/v3/translate?version=2018-05-01";

			final HttpClient clientTraductor = HttpClients.custom().setSslcontext(sslContextTraductor).setHostnameVerifier(new AllowAllHostnameVerifier()).build();
			final HttpPost postMethodTraductor = new HttpPost(URI);
			postMethodTraductor.addHeader("Accept", "application/json");
			postMethodTraductor.addHeader("Content-Type", "application/json");

			final String authStringTraductor = user + ":" + password;
			final String authEncBytesTraductor = DatatypeConverter
					.printBase64Binary(authStringTraductor.getBytes());
			postMethodTraductor.addHeader("Authorization", "Basic " + authEncBytesTraductor);

			final String messageBodyTraductor = "{\"text\":[\""+content+"\"],\"model_id\":\""+modelId+"\"}";
			final StringEntity conversationEntityTraductor = new StringEntity(messageBodyTraductor);
			postMethodTraductor.setEntity(conversationEntityTraductor);

			final HttpResponse responseTraductor = clientTraductor.execute(postMethodTraductor);

			final BufferedReader inputStreamTraductor = new BufferedReader(
					new InputStreamReader(responseTraductor.getEntity().getContent()));

			String line = "";
			final StringBuilder result = new StringBuilder();
			while ((line = inputStreamTraductor.readLine()) != null) {
				result.append(line);
			}

			JSONObject json = new JSONObject(result.toString());
			
			String translate = json.getString("translations");
			JSONArray array = new JSONArray(translate);
			for (int i = 0; i < array.length(); i++) {
			    JSONObject object = array.getJSONObject(i);
			    exitCode = object.get("translation").toString();
			}
			
			inputStreamTraductor.close();
			postMethodTraductor.reset();
		} catch (Exception ex) {
			exitCode = null;
		}

		return exitCode;

	}

}
