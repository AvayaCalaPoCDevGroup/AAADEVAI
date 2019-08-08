package com.avaya.AAADEVAI.AAAPlayAnnouncementTTS.Https;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

import com.avaya.AAADEVAI.Util.Constants;
import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityException;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;

public class IBM {
	public JSONObject ibmTTS(String text, String voice) throws SSLUtilityException, ClientProtocolException, IOException, JSONException{
		final SSLProtocolType protocolTypeAssistant = SSLProtocolType.TLSv1_2;
		final SSLContext sslContext = SSLUtilityFactory
				.createSSLContext(protocolTypeAssistant);
		final HttpClient client = HttpClients.custom()
				.setSSLContext(sslContext)
				.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
		final HttpPost postMethod = new HttpPost(Constants.IBM_BREEZE_END_POINT);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();  
		
		StringBody textBody = new StringBody(text, ContentType.TEXT_PLAIN);
		StringBody voiceBody = new StringBody(voice.trim(), ContentType.TEXT_PLAIN);
		
		builder.addPart("text", textBody);
		builder.addPart("voice", voiceBody);
		
		HttpEntity entity = builder.build();
		postMethod.setEntity(entity);
    	
    	final HttpResponse response = client.execute(postMethod);

		final BufferedReader inputStream = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent()));

		String line = "";
		final StringBuilder result = new StringBuilder();
		while ((line = inputStream.readLine()) != null) {
			result.append(line);
		}
		return new JSONObject(result.toString());
	}
}
