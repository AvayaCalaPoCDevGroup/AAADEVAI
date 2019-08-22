package com.avaya.AAADEVAI.WatsonAssistant;

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
import org.apache.http.conn.ssl.NoopHostnameVerifier;
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

public class AssistantExecution extends NodeInstance {

	private static final long serialVersionUID = 1L;

	public AssistantExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	public Object execute() throws Exception {
		final JSONObject obj = new JSONObject();
		
		AssistantModel assistant = (AssistantModel) getNode();
		String textAssistant = (String) get("textAssistant");
		if ((textAssistant == null) || (textAssistant.isEmpty())) {
			textAssistant = assistant.getTextAssistant();
			if(textAssistant == null || textAssistant.isEmpty()){
				throw new IllegalArgumentException("The Text Cannot Be Empty...");
			}
		}
		
		String userNameAssistant = (String)get("userNameAssistant");
		if(userNameAssistant == null || userNameAssistant.isEmpty()){
			userNameAssistant = assistant.getUserNameAssistant();
			if(userNameAssistant == null || userNameAssistant.isEmpty()){
				throw new IllegalArgumentException("User Name Cannot Be Empty...");
			}
		}
		
		String passwordAssistant = (String)get("passwordAssistant");
		if(passwordAssistant == null || passwordAssistant.isEmpty()){
			passwordAssistant = assistant.getPasswordAssistant();
			if(passwordAssistant == null || passwordAssistant.isEmpty()){
				passwordAssistant = "ZIrFXSd1exPG";
			}
		}
		
		
		String workSpaceIdAssistant = (String)get("workSpaceIdAssistant");
		if(workSpaceIdAssistant == null || workSpaceIdAssistant.isEmpty()){
			workSpaceIdAssistant = assistant.getWorkSpaceIdAssistant();
			if(workSpaceIdAssistant == null || workSpaceIdAssistant.isEmpty()){
				throw new IllegalArgumentException("Work Space ID Cannot Be Empty...");
			}
		}

		
		
		final String[] resultAssistant = callWatsonAssitant(textAssistant,
				userNameAssistant, passwordAssistant, workSpaceIdAssistant);
		if (resultAssistant == null) {
			obj.put("status", "failure");
		} else {
			obj.put("intencion", resultAssistant[0]);
			obj.put("confianza", resultAssistant[1]);
			obj.put("status", "success");
		}

		return obj;

	}

	private String[] callWatsonAssitant(String textAssistant,
			String userNameAssistant, String passwordAssistant,
			String workSpaceIdAssistant) {

		try {
			final SSLProtocolType protocolTypeAssistant = SSLProtocolType.TLSv1_2;
			final SSLContext sslContextAssistant = SSLUtilityFactory
					.createSSLContext(protocolTypeAssistant);
			final CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(userNameAssistant,
							passwordAssistant));

			final String URI = "https://gateway.watsonplatform.net/assistant/api/v1/workspaces/"
					+ workSpaceIdAssistant + "/message?version=2018-07-10";

			final HttpClient client = HttpClients.custom()
					.setSSLContext(sslContextAssistant)
					.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
			
			final HttpPost postMethodAssistant = new HttpPost(URI);
			postMethodAssistant.addHeader("Accept", "application/json");
			postMethodAssistant.addHeader("Content-Type", "application/json");

			final String authStringAssistant = userNameAssistant + ":"
					+ passwordAssistant;
			final String authEncBytesAssistant = DatatypeConverter
					.printBase64Binary(authStringAssistant.getBytes());
			postMethodAssistant.addHeader("Authorization", "Basic "
					+ authEncBytesAssistant);

			final String messageBodyAssistant = "{\"input\": {\"text\": \""
					+ textAssistant + "\"}}";
			final StringEntity conversationEntityAssistant = new StringEntity(
					messageBodyAssistant);
			postMethodAssistant.setEntity(conversationEntityAssistant);

			final HttpResponse responseAssistant = client
					.execute(postMethodAssistant);

			final BufferedReader inputStreamAssistant = new BufferedReader(
					new InputStreamReader(responseAssistant.getEntity()
							.getContent()));

			String line = "";
			final StringBuilder result = new StringBuilder();
			while ((line = inputStreamAssistant.readLine()) != null) {
				result.append(line);
			}
			JSONObject json = new JSONObject(result.toString());
			String intentOut = null;
			String confidence = null;

			String intent = json.getString("intents");
			JSONArray array = new JSONArray(intent);
			for (int i = 0; i < array.length(); i++) {
				JSONObject object = array.getJSONObject(i);
				intentOut = object.get("intent").toString();
				confidence = object.get("confidence").toString();

			}

			String[] arregloAssistant = { intentOut, confidence };

			inputStreamAssistant.close();
			postMethodAssistant.reset();

			return arregloAssistant;
		} catch (Exception ex) {
			return null;
		}

	}

}
