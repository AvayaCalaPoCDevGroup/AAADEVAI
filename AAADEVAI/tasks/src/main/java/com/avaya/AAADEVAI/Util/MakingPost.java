package com.avaya.AAADEVAI.Util;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.Participant;
import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityException;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;
import com.avaya.collaboration.util.logger.Logger;

@SuppressWarnings({ "unused", "deprecation" })
public class MakingPost {
	private Call call;
	private transient final Logger logger = Logger.getLogger(MakingPost.class);
	public static String nombreWav = null;
	public static String nombreWavfile = null;
	
	public MakingPost(final Call call, final String domain) {
		this.call = call;
	}
	
	public MakingPost(final String domain) {
	}

	public MakingPost() {

	}

	public String[] makingPOST() throws IOException, SSLUtilityException {
		try {

			String userHomeDir = System.getProperty("user.home");
			final SSLProtocolType protocolType = SSLProtocolType.TLSv1_2;
			final SSLContext sslContextTraductor = SSLUtilityFactory
					.createSSLContext(protocolType);

			// DefaultHttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://"+Constants.getFQDN()+"/services/AAADEVLOGGER/InputLogger/");

			final HttpClient httpclient = HttpClients.custom()
					.setSslcontext(sslContextTraductor)
					.setHostnameVerifier(new AllowAllHostnameVerifier())
					.build();


			/*
			 * Obtener Fecha
			 */
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			Date date = new Date(System.currentTimeMillis());
			String fecha = dateFormat.format(date);
			fecha = fecha.replaceAll("[^\\dA-Za-z]", "");

			/*
			 * Obtener hora
			 */
			DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ssz");
			Date hora = new Date(System.currentTimeMillis());
			String tiempo = dateFormat2.format(hora);
			tiempo = tiempo.replaceAll("[^\\dA-Za-z]", "");

			nombreWav = fecha + "_" + tiempo;

			nombreWavfile = nombreWav + ".wav";
			FileBody bin = new FileBody(new File("" + userHomeDir
					+ "/TextToSpeech.wav"));
			

			StringBody comment = new StringBody(nombreWavfile);
			StringBody comment2 = new StringBody("http://"+Constants.getFQDN()+"/services/AAADEVLOGGER/web/RecordParticipant/");
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("rec_data", bin);
			reqEntity.addPart("recFileName", comment);
			reqEntity.addPart("	", comment2);
			httppost.setEntity(reqEntity);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			String[] status = { "ok", nombreWavfile };
			return status;
		} catch (Exception ex) {
			String[] error = { "Error " + ex.toString(),
					"Error " + ex.toString() };
			return error;
		}
	}

	public String[] makingPostWithCall(Call call) throws IOException,
			SSLUtilityException {
		try {

			String userHomeDir = System.getProperty("user.home");
			final SSLProtocolType protocolType = SSLProtocolType.TLSv1_2;
			final SSLContext sslContextTraductor = SSLUtilityFactory
					.createSSLContext(protocolType);
			HttpPost httppost = new HttpPost(
//					"https://breeze2-132.collaboratory.avaya.com/services/AAADEVURIEL_WAV3/ControladorGrabaciones/");
//			HttpPost httppost = new HttpPost(
					"http://"+Constants.getFQDN()+"/services/AAADEVLOGGER/InputLogger/");

			 /*
			 * extensión a la que se llama
			 */
			 Participant participant1 = call.getCalledParty();
			 String origen = participant1.getHandle();
			 /*
			 * Extensión que llama
			 */
			 Participant participant2 = call.getCallingParty();
			 String destino = participant2.getHandle();

			/*
			 * Obtener Fecha
			 */
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
			Date date = new Date(System.currentTimeMillis());
			String fecha = dateFormat.format(date);
			fecha = fecha.replaceAll("[^\\dA-Za-z]", "");

			/*
			 * Obtener hora
			 */
			DateFormat dateFormat2 = new SimpleDateFormat("HH:mm:ssz");
			Date hora = new Date(System.currentTimeMillis());
			String tiempo = dateFormat2.format(hora);
			tiempo = tiempo.replaceAll("[^\\dA-Za-z]", "");

			 nombreWav = fecha + "_" + tiempo + "_" + origen + "_" + destino;


			nombreWavfile = nombreWav + ".wav";
			FileBody bin = new FileBody(new File("" + userHomeDir
					+ "/TextToSpeech.wav"));
			
			final HttpClient httpclient = HttpClients.custom()
					.setSslcontext(sslContextTraductor)
					.setHostnameVerifier(new AllowAllHostnameVerifier())
					.build();

			StringBody comment = new StringBody(nombreWavfile);
			StringBody comment2 = new StringBody(
					"http://"+Constants.getFQDN()+"/services/AAADEVLOGGER/web/RecordParticipant/");
			MultipartEntity reqEntity = new MultipartEntity();
			reqEntity.addPart("rec_data", bin);
			reqEntity.addPart("recFileName", comment);
			reqEntity.addPart("restRecordURI", comment2);
			httppost.setEntity(reqEntity);
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity resEntity = response.getEntity();
			String[] status = { "ok", nombreWavfile };
			return status;
		} catch (Exception ex) {
			String[] error = { "Error " + ex.toString(),
					"Error " + ex.toString() };
			return error;
		}
	}
	public static void makingDelete() throws IOException,
	SSLUtilityException {
		
		final SSLProtocolType protocolType = SSLProtocolType.TLSv1_2;
		final SSLContext sslContextTraductor = SSLUtilityFactory
				.createSSLContext(protocolType);

		// DefaultHttpClient httpclient = new DefaultHttpClient();

		final CloseableHttpClient  httpclient = HttpClients.custom()
				.setSslcontext(sslContextTraductor)
				.setHostnameVerifier(new AllowAllHostnameVerifier())
				.build();
		HttpDelete httpdelete = new HttpDelete(
				"http://"+Constants.getFQDN()+"/services/AAADEVLOGGER/InputLogger/web/RecordParticipant/"+nombreWavfile);
		
		CloseableHttpResponse response = httpclient.execute(httpdelete);
		
		response.close();
		httpclient.close();
		
	}
}