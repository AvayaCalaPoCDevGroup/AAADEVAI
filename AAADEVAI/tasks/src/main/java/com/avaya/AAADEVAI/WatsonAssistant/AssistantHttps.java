package com.avaya.AAADEVAI.WatsonAssistant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;

import com.avaya.AAADEVAI.Security.AES;
import com.avaya.AAADEVAI.Util.Constants;
import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityException;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;

public class AssistantHttps {
	protected final String textAssistant;
	
	
	public AssistantHttps(String textAssistant) {
		super();
		this.textAssistant = textAssistant;
	}
	
	public JSONObject assist() throws SSLUtilityException, ClientProtocolException, IOException, JSONException{
		final SSLProtocolType protocolTypeAssistant = SSLProtocolType.TLSv1_2;
		final SSLContext sslContext = SSLUtilityFactory
				.createSSLContext(protocolTypeAssistant);
		final HttpClient client = HttpClients.custom()
				.setSSLContext(sslContext)
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
		final HttpPost postMethod = new HttpPost(Constants.IBM_BREEZE_END_POINT_ASSISTANT);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();  

		StringBody textToAssistPart = new StringBody(new AES().encrypt(textAssistant), ContentType.TEXT_PLAIN);
		builder.addPart("text", textToAssistPart);
		HttpEntity entity = builder.build();
		postMethod.setEntity(entity);
		
    	final HttpResponse response = client.execute(postMethod);

			final BufferedReader inputStream = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent(),
							StandardCharsets.ISO_8859_1));
	
			String line = "";
			final StringBuilder result = new StringBuilder();
			while ((line = inputStream.readLine()) != null) {
				result.append(line);
			}
		return new JSONObject(result.toString());
	}
}
