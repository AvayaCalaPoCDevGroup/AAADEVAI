package com.avaya.AAADEVAI.WatsonAssistantConversation.DeleteSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityException;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;
import com.roobroo.bpm.model.BpmNode;

public class DeleteSessionExecution extends NodeInstance{

	private static final long serialVersionUID = 1L;

	public DeleteSessionExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}
	
	public Object execute() throws Exception {
		DeleteSessionModel deleteSessionModel = (DeleteSessionModel)getNode();
		JSONObject json = new JSONObject();
		
		String userNameDeleteSession =  (String)get("userNameDeleteSession");
		if(userNameDeleteSession == null || userNameDeleteSession.isEmpty()){
			userNameDeleteSession = deleteSessionModel.getUserNameDeleteSession();
			if(userNameDeleteSession == null || userNameDeleteSession.isEmpty()){
				throw new IllegalArgumentException("User Name Cannot Be Empty...");
			}
		}
		String passwordDeleteSession = (String)get("passwordDeleteSession");
		if(passwordDeleteSession == null || passwordDeleteSession.isEmpty()){
			passwordDeleteSession = deleteSessionModel.getPasswordDeleteSession();
			if(passwordDeleteSession == null || passwordDeleteSession.isEmpty()){
				throw new IllegalArgumentException("Password Cannot Be Empty...");
			}
		}
		String assistantIdDeleteSession = (String)get("assistantIdDeleteSession");
		if(assistantIdDeleteSession == null || assistantIdDeleteSession.isEmpty()){
			assistantIdDeleteSession = deleteSessionModel.getAssistantIdDeleteSession();
			if(assistantIdDeleteSession == null || assistantIdDeleteSession.isEmpty()){
				throw new IllegalArgumentException("Assistant ID Cannot Be Empty...");
			}
		}
		String versionDeleteSession = (String)get("versionDeleteSession");
		if(versionDeleteSession == null || versionDeleteSession.isEmpty()){
			versionDeleteSession = deleteSessionModel.getVersionDeleteSession();
			if(versionDeleteSession == null || versionDeleteSession.isEmpty()){
				throw new IllegalArgumentException("Versión Cannot Be Empty...");
			}
		}
		String sessionDeleteSession = (String)get("sessionDeleteSession");
		if(sessionDeleteSession == null || sessionDeleteSession.isEmpty()){
			sessionDeleteSession = deleteSessionModel.getSessionDeleteSession();
			if(sessionDeleteSession == null || sessionDeleteSession.isEmpty()){
				throw new IllegalArgumentException("Seesion ID Cannot Be Empty...");
			}
		}
		
		try{
			json = deleteSession(userNameDeleteSession, passwordDeleteSession, assistantIdDeleteSession, versionDeleteSession, sessionDeleteSession);
			json.put("status", "SUCCESS");
			return json;
		}catch(Exception e){
			json.put("status", "FAILURE " + e.toString());
			return json;
		}
	}
	
	public JSONObject deleteSession(String userNameDeleteSession, String passwordDeleteSession, String assistantIdDeleteSession, String versionDeleteSession, String sessionDeleteSession) throws ClientProtocolException, IOException, SSLUtilityException, JSONException{
		final SSLProtocolType protocolTypeAssistant = SSLProtocolType.TLSv1_2;
		final SSLContext sslContextAssistant = SSLUtilityFactory
				.createSSLContext(protocolTypeAssistant);
		final CredentialsProvider provider = new BasicCredentialsProvider();
		provider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(userNameDeleteSession,passwordDeleteSession));

		final String URI = "https://gateway.watsonplatform.net/assistant/api/v2/assistants/"+assistantIdDeleteSession+"/sessions/"+sessionDeleteSession+"?version="+versionDeleteSession;

		final HttpClient clientAssistant = HttpClients.custom()
				.setSslcontext(sslContextAssistant)
				.setHostnameVerifier(new AllowAllHostnameVerifier())
				.build();
		final HttpDelete deleteMethodAssistant = new HttpDelete(URI);

		final String authStringAssistant = userNameDeleteSession + ":" + passwordDeleteSession;
		final String authEncBytesAssistant = DatatypeConverter
				.printBase64Binary(authStringAssistant.getBytes());
		deleteMethodAssistant.addHeader("Authorization", "Basic "
				+ authEncBytesAssistant);

		final HttpResponse responseAssistant = clientAssistant.execute(deleteMethodAssistant);

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
