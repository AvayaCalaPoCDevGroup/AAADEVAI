package com.avaya.AAADEVAI.WatsonAssistantConversation.SendText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityException;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;
import com.roobroo.bpm.model.BpmNode;


public class SendTextExecution extends NodeInstance{

	private static final long serialVersionUID = 1L;

	public SendTextExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}
	
	public Object execute() throws Exception {
		SendTextModel sendTextModel = (SendTextModel)getNode();
		JSONObject json = new JSONObject();
		
		String userNameSendText = (String)get("userNameSendText");
		if(userNameSendText == null || userNameSendText.isEmpty()){
			userNameSendText = sendTextModel.getUserNameSendText();
			if(userNameSendText == null || userNameSendText.isEmpty()){
				throw new IllegalArgumentException("User Name Cannot Be Empty...");
			}
		}
		String passwordSendText = (String)get("passwordSendText");
		if(passwordSendText == null || passwordSendText.isEmpty()){
			passwordSendText = sendTextModel.getPasswordSendText();
			if(passwordSendText == null || passwordSendText.isEmpty()){
				throw new IllegalArgumentException("Password Cannot Be Empty...");
			}
		}
		String assistantIdSendText = (String)get("assistantIdSendText");
		if(assistantIdSendText == null || assistantIdSendText.isEmpty()){
			assistantIdSendText = sendTextModel.getAssistantIdSendText();
			if(assistantIdSendText == null || assistantIdSendText.isEmpty()){
				throw new IllegalArgumentException("Assistant ID Cannot Be Empty...");
			}
		}
		String versionSendText = (String)get("versionSendText");
		if(versionSendText == null || versionSendText.isEmpty()){
			versionSendText = sendTextModel.getVersionSendText();
			if(versionSendText == null || versionSendText.isEmpty()){
				throw new IllegalArgumentException("Version Cannot Be Empty...");
			}
		}
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
			json = postSendText(userNameSendText, passwordSendText, assistantIdSendText, versionSendText, sessionSendText, textSendText);
			return json;
		}catch(Exception e){
			json.put("Error", e.toString());
			return json;
		}
	}
	
	public JSONObject postSendText(String userNameSendText, String passwordSendText, String assistantIdSendText, String versionSendText, String sessionSendText, String textSendText) throws SSLUtilityException, ClientProtocolException, IOException, JSONException{
		
			final SSLProtocolType protocolTypeAssistant = SSLProtocolType.TLSv1_2;
			final SSLContext sslContextAssistant = SSLUtilityFactory
					.createSSLContext(protocolTypeAssistant);
			final CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(userNameSendText,
							passwordSendText));

			final String URI = "https://gateway.watsonplatform.net/assistant/api/v2/assistants/"+assistantIdSendText+"/sessions/"+sessionSendText+"/message?version="+versionSendText;

			final HttpClient clientAssistant = HttpClients.custom()
					.setSslcontext(sslContextAssistant)
					.setHostnameVerifier(new AllowAllHostnameVerifier())
					.build();
			final HttpPost postMethodAssistant = new HttpPost(URI);
			postMethodAssistant.addHeader("Accept", "application/json");
			postMethodAssistant.addHeader("Content-Type", "application/json");

			final String authStringAssistant = userNameSendText + ":"
					+ passwordSendText;
			final String authEncBytesAssistant = DatatypeConverter
					.printBase64Binary(authStringAssistant.getBytes());
			postMethodAssistant.addHeader("Authorization", "Basic "
					+ authEncBytesAssistant);

			final String messageBodyAssistant = "{\"input\": {\"text\": \""+ textSendText.trim() + "\"}}";
			final StringEntity conversationEntityAssistant = new StringEntity(
					messageBodyAssistant, ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8));
			postMethodAssistant.setEntity(conversationEntityAssistant);

			final HttpResponse responseAssistant = clientAssistant
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

	 return json;
	}

}
