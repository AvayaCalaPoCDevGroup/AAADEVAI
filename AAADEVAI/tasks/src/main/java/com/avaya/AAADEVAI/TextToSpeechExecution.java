package com.avaya.AAADEVAI;

import java.beans.Transient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.net.ssl.SSLContext;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
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
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.CallFactory;
import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;
import com.roobroo.bpm.model.BpmNode;
import com.avaya.workflow.logger.*;

@SuppressWarnings({ "serial", "deprecation", "unused" })
public class TextToSpeechExecution extends NodeInstance {

	private static Logger logger = LoggerFactory
			.getLogger(TextToSpeechExecution.class);
	private TextToSpeechModel model;
	public static int filesize;

	public TextToSpeechExecution(Instance instance, BpmNode node) {
		super(instance, node);
		if ((node instanceof TextToSpeechModel)) {
			this.model = ((TextToSpeechModel) node);
		} else {
			throw new IllegalStateException(
					"Error: node is not a ReadDBModel node.");
		}
	}

	public Object execute() throws Exception {
		String userHomeDir = System.getProperty("user.home");
		String osName = System.getProperty("os.name");

		String content = (String) get("MessageBody");
		if ((content == null) || (content.isEmpty())) {
			content = getReadDBModel().getMessageBody();
		}

		/*
		 * Buscando y reempazando caracteres especiales ESPAÑOL.
		 */
		
		if(getReadDBModel().getVoice().equals("es-ES_LauraVoice")){
			BuscarYRemplazarAcentos español = new BuscarYRemplazarAcentos();
			content = español.Español(content);
		}
		
		
		if(getReadDBModel().getVoice().equals("pt-BR_IsabelaVoice")){
			BuscarYRemplazarAcentos portugues = new BuscarYRemplazarAcentos();
			content = portugues.Portugues(content);
		}

		/*
		 * Creando el HTTPS POST a Watson
		 */
		try {
			String user = getReadDBModel().getUserName();
			String password = "g7rmue4UsCWP";

			final SSLProtocolType protocolTypeTraductor = SSLProtocolType.TLSv1_2;
			final SSLContext sslContextTraductor = SSLUtilityFactory
					.createSSLContext(protocolTypeTraductor);
			final CredentialsProvider provider = new BasicCredentialsProvider();
			provider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(user, password));

			final String URI = "https://stream.watsonplatform.net/text-to-speech/api/v1/synthesize?voice="
					+ getReadDBModel().getVoice();

			final HttpClient clientTTSpeech = HttpClients.custom()
					.setSslcontext(sslContextTraductor)
					.setHostnameVerifier(new AllowAllHostnameVerifier())
					.build();
			// final HttpClient clientTraductor = new DefaultHttpClient();

			final HttpPost postTTSpeech = new HttpPost(URI);
			postTTSpeech.addHeader("Accept", "audio/l16;rate=8000");
			postTTSpeech.addHeader("Content-Type", "application/json");

			final String authStringTTSpecch = user + ":" + password;
			final String authEncBytesTTSpeech = DatatypeConverter
					.printBase64Binary(authStringTTSpecch.getBytes());
			postTTSpeech.addHeader("Authorization", "Basic "
					+ authEncBytesTTSpeech);

			final String messageBodyTTSpeech = "{\"text\":\"" + content
					+ "\"}";
			final StringEntity conversationEntityTTSpeech = new StringEntity(
					messageBodyTTSpeech);
			postTTSpeech.setEntity(conversationEntityTTSpeech);

			final HttpResponse responseTTSpeech = clientTTSpeech
					.execute(postTTSpeech);

			InputStream in = reWriteWaveHeader(responseTTSpeech.getEntity()
					.getContent());
			OutputStream out = new FileOutputStream("" + userHomeDir
					+ "/TextToSpeech.wav");

			byte[] buffer = new byte[filesize + 8];
			int length;
			while ((length = in.read(buffer)) > 0) {

				InputStream byteAudioStream = new ByteArrayInputStream(buffer);
				AudioFormat audioFormat = new AudioFormat(8000.0f, 16, 1,
						false, false);
				AudioInputStream audioInputStream = new AudioInputStream(
						byteAudioStream, audioFormat, buffer.length);
				if (AudioSystem.isFileTypeSupported(AudioFileFormat.Type.WAVE,
						audioInputStream)) {
					AudioSystem.write(audioInputStream,
							AudioFileFormat.Type.WAVE, out);
				}

			}

			out.close();
			in.close();

			// Dependiendo si el ucid es null o no, se envía el post con el
			// nombre

			String ucid = (String)get("ucid");
			String status[] = {null,null};
//			if (ucid != null) {
//				Call call = CallFactory.getCall(ucid);
//				MakingPost post = new MakingPost();
//				status = post.makingPostWithCall(call);
//			}else{
//				MakingPost post = new MakingPost();
//				status = post.makingPOST();
//			}
			

			if (status[0].equals("ok")) {
				JSONObject json = new JSONObject();
				json.put("resutl", responseTTSpeech.toString());
				json.put(
						"url",
						"http://breeze2-213.collaboratory.avaya.com/services/AAADEVWAV/ControladorGrabaciones/web/Grabaciones/"
								+ status[1]);
				return json;
			} else {
				JSONObject json = new JSONObject();
				json.put("result", status);
				return json;
			}

		} catch (Exception e) {
			JSONObject json = new JSONObject();
			json.put("result", "Error: " + e.toString());
			return json;
		}

	}

	@Transient
	public TextToSpeechModel getReadDBModel() {
		if (this.model == null) {
			this.model = ((TextToSpeechModel) getNode());
		}
		return this.model;
	}

	public static byte[] toByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		int nRead;
		byte[] data = new byte[16384]; // 4 kb

		while ((nRead = is.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		buffer.flush();
		return buffer.toByteArray();
	}

	private static void writeInt(int value, byte[] array, int offset) {
		for (int i = 0; i < 4; i++) {
			array[offset + i] = (byte) (value >>> (8 * i));
		}
	}

	public static InputStream reWriteWaveHeader(InputStream is)
			throws IOException {
		byte[] audioBytes = toByteArray(is);
		filesize = audioBytes.length - 8;

		writeInt(filesize, audioBytes, 4);
		writeInt(filesize - 8, audioBytes, 74);

		return new ByteArrayInputStream(audioBytes);
	}

	public static int InputStreamsize(InputStream in) throws IOException {
		byte[] audioBytes = toByteArray(in);
		int filesize = audioBytes.length - 8;
		return filesize;
	}
}
