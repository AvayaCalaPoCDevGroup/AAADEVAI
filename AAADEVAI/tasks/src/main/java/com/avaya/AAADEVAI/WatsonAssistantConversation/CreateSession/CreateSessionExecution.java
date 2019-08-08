package com.avaya.AAADEVAI.WatsonAssistantConversation.CreateSession;

import java.io.BufferedReader;
import java.io.IOException;
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
import org.json.JSONException;
import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityException;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;
import com.roobroo.bpm.model.BpmNode;

public class CreateSessionExecution extends NodeInstance{
	
	private static final long serialVersionUID = 1L;

	public CreateSessionExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}
	
	public Object execute() throws Exception {
		CreateSessionModel createSessionModel = (CreateSessionModel)getNode();
		JSONObject json = new JSONObject();
		
		String userNameCreateSession = (String)get("userNameCreateSession");
		if(userNameCreateSession == null || userNameCreateSession.isEmpty()){
			userNameCreateSession = createSessionModel.getUserNameCreateSession();
			if(userNameCreateSession == null || userNameCreateSession.isEmpty()){
				throw new IllegalArgumentException("User Name Cannot Be Empty...");
			}
		}
		String passwordCreateSession = (String)get("passwordCreateSession");
		if(passwordCreateSession == null || passwordCreateSession.isEmpty()){
			passwordCreateSession = createSessionModel.getPasswordCreateSession();
			if(passwordCreateSession == null || passwordCreateSession.isEmpty()){
				throw new IllegalArgumentException("Password Cannot Be Empty...");
			}
		}
		
		String assistantIdCreateSession = (String)get("assistantIdCreateSession");
		if(assistantIdCreateSession == null || assistantIdCreateSession.isEmpty()){
			assistantIdCreateSession = createSessionModel.getAssistantIdCreateSession();
			if(assistantIdCreateSession == null || assistantIdCreateSession.isEmpty()){
				throw new IllegalArgumentException("Assistant ID Cannot Be Empty...");
			}
		}
		
		String versionCreateSession = (String)get("versionCreateSession");
		if(versionCreateSession == null || versionCreateSession.isEmpty()){
			versionCreateSession = createSessionModel.getVersionCreateSession();
			if(versionCreateSession == null || versionCreateSession.isEmpty()){
				throw new IllegalArgumentException("Version Cannot Be Empty...");
			}
		}
		
		try{
			json = postCreateSession(userNameCreateSession, passwordCreateSession, assistantIdCreateSession, versionCreateSession);
			
			return json;
		}catch(Exception e){
			json.put("Error", e.toString());
			return json;
		}
	}
	
	public JSONObject postCreateSession(String userName, String password, String assistantID, String version) throws UnsupportedOperationException, IOException, SSLUtilityException, JSONException{
			final SSLProtocolType protocolTypeAssistant = SSLProtocolType.TLSv1_2;
			final SSLContext sslContextAssistant = SSLUtilityFactory
					.createSSLContext(protocolTypeAssistant);
			final CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(userName,password));

			final String URI = "https://gateway.watsonplatform.net/assistant/api/v2/assistants/"+assistantID+"/sessions?version="+version;

			final HttpClient clientAssistant = HttpClients.custom()
					.setSslcontext(sslContextAssistant)
					.setHostnameVerifier(new AllowAllHostnameVerifier())
					.build();
			final HttpPost postMethodAssistant = new HttpPost(URI);
			

			final String authStringAssistant = userName + ":" + password;
			final String authEncBytesAssistant = DatatypeConverter
					.printBase64Binary(authStringAssistant.getBytes());
			postMethodAssistant.addHeader("Authorization", "Basic "
					+ authEncBytesAssistant);

			final HttpResponse responseAssistant = clientAssistant.execute(postMethodAssistant);

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
