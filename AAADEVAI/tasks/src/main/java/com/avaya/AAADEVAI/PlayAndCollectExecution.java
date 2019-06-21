package com.avaya.AAADEVAI;

import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.roobroo.bpm.model.BpmNode;
import com.avaya.workflow.logger.*;
import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.CallFactory;
import com.avaya.collaboration.call.Participant;
import com.avaya.collaboration.call.media.DigitCollectorOperationCause;
import com.avaya.collaboration.call.media.DigitOptions;
import com.avaya.collaboration.call.media.MediaFactory;
import com.avaya.collaboration.call.media.MediaService;
import com.avaya.collaboration.call.media.PlayItem;
import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;
import com.avaya.workflow.logger.Logger;
import com.avaya.workflow.logger.LoggerFactory;
import com.roobroo.bpm.im.InstanceManager;
import com.roobroo.bpm.model.BpmNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.lang.StringUtils;
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
import org.json.JSONObject;

public class PlayAndCollectExecution extends NodeInstance {
	private static final Logger log = LoggerFactory
			.getLogger(PlayAndCollectExecution.class);
	public static final int ITERATE_COUNT = 1;
	public static final int DEFAULT_TIMEOUT = 60000;
	public static final String CALLING_LEG = "calling";
	public static final String CALLED_LEG = "called";
	private static final String DELIMETER = ":";
	private static final String DOMAIN_DELIMETER = "@";
	/*
	 * TextToSpeech
	 */
	private String userHomeDir = System.getProperty("user.home");
	private String osName = System.getProperty("os.name");
	public static int filesize;
	private Call call;

	public PlayAndCollectExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	public Object execute() throws Exception {
		PlayAndCollectModel model = (PlayAndCollectModel) getNode();

		String callID = (String) get("ucid");

		String leg = (String) get("participant");
		if ((leg == null) || (leg.isEmpty())) {
			leg = model.getParticipant();
		}
		String locale = (String) get("locale");
		if ((locale == null) || (locale.isEmpty())) {
			locale = model.getLocale();
			if ((locale == null) || (locale.isEmpty())) {
				locale = ServiceAttributeManager
						.getServiceAttribute("avayaMediaServerLocale");
			}
		}
		/*
		 * Text To Speech
		 */
		log.info("AAADEVTTSANDPLAY Text To Speech");

		String mediaFileURI = (String) get("mediauri");
		if ((mediaFileURI == null) || (mediaFileURI.isEmpty())) {
			mediaFileURI = model.getMediaUri();
		}
		log.info("AAADEVTTSANDPLAY media File Uri " + mediaFileURI);
		/**/
		String numberOfDigits = (String) get("numberOfDigits");
		if ((numberOfDigits == null) || (numberOfDigits.isEmpty())) {
			numberOfDigits = model.getNumberOfDigits();
		}
		validateParameter(callID, "callID");
		validateParameter(mediaFileURI, "mediaFileURI");
		validateParameter(numberOfDigits, "numberOfDigits");
		if (log.isFineEnabled()) {
			log.fine("Media Prompt to be played is:" + mediaFileURI);
		}
		String terminationKey = (String) get("terminationKey");
		if ((terminationKey == null) || (terminationKey.isEmpty())) {
			terminationKey = model.getTerminationKey();
		}
		String interruptibility = (String) get("interruptibility");
		if ((interruptibility == null) || (interruptibility.isEmpty())) {
			interruptibility = model.getInterruptibility();
		}
		String handle = (String) get("handle");
		if ((handle == null) || (handle.isEmpty())) {
			handle = model.getHandle();
		}
		String timeout = (String) get("timeout");
		if ((timeout == null) || (timeout.isEmpty())) {
			timeout = model.getTimeout();
		}
		Call call = CommTaskUtil.getCall(callID);
		if (call == null) {
			log.error("Error getting call object...");
			throw new IllegalArgumentException("Call object not found...");
		}

		boolean retval;
		boolean retva2;
		retval = mediaFileURI.contains("http://");
		retva2 = mediaFileURI.contains("https://");

		if (retval == true || retva2 == true) {

		} else {

			String voice = (String) get("voice");
			if ((voice == null) || (voice.isEmpty())) {
				voice = model.getVoice();
			}

			if (voice.equals("es-ES_LauraVoice")
					|| voice.equals("es-ES_EnriqueVoice")
					|| voice.equals("es-LA_SofiaVoice")
					|| voice.equals("es-US_SofiaVoice")) {
				BuscarYRemplazarAcentos español = new BuscarYRemplazarAcentos();
				mediaFileURI = español.Español(mediaFileURI);
			}

			if (voice.equals("pt-BR_IsabelaVoice")) {
				BuscarYRemplazarAcentos portugues = new BuscarYRemplazarAcentos();
				mediaFileURI = portugues.Portugues(mediaFileURI);
			}
			log.info("AAADEVTTSANDPLAY media File Uri " + mediaFileURI);
			/*
			 * Creando el HTTPS POST a Watson
			 */
			try {
				log.info("AAADEVTTSANDPLAY Creando HTTP POST");
				String user = "1a750c00-9343-4032-9e4d-dd485052692d";
				String password = "g7rmue4UsCWP";

				final SSLProtocolType protocolTypeTraductor = SSLProtocolType.TLSv1_2;
				final SSLContext sslContextTraductor = SSLUtilityFactory
						.createSSLContext(protocolTypeTraductor);
				final CredentialsProvider provider = new BasicCredentialsProvider();
				provider.setCredentials(AuthScope.ANY,
						new UsernamePasswordCredentials(user, password));

				final String URI = "https://stream.watsonplatform.net/text-to-speech/api/v1/synthesize?voice="
						+ voice;

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

				final String messageBodyTTSpeech = "{\"text\":\""
						+ mediaFileURI + "\"}";
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

					InputStream byteAudioStream = new ByteArrayInputStream(
							buffer);
					AudioFormat audioFormat = new AudioFormat(8000.0f, 16, 1,
							false, false);
					AudioInputStream audioInputStream = new AudioInputStream(
							byteAudioStream, audioFormat, buffer.length);
					if (AudioSystem.isFileTypeSupported(
							AudioFileFormat.Type.WAVE, audioInputStream)) {
						AudioSystem.write(audioInputStream,
								AudioFileFormat.Type.WAVE, out);
					}

				}

				out.close();
				in.close();
				/*
				 * Haciendo POST
				 */

				String status[] = { null, null };
				if (call.getUCID() != null) {
					MakingPost post = new MakingPost(call, "10.0.0.10");
					log.info("AAADEVTTSANDPLAY MakePost With Call");
					status = post.makingPostWithCall(call);
				} else {
					log.info("AAADEVTTSANDPLAY MakePost Without Call");
					MakingPost post = new MakingPost("10.0.0.10");
					status = post.makingPOST();
				}
				log.info("AAADEVTTSANDPLAY Status" + status[0]);
				if (status[0].equals("ok")) {

					mediaFileURI = "http://10.0.0.10/services/AAADEVLOGGER/FileSaveServlet/web/RecordParticipant/"
							+ status[1];
					log.info("AAADEVTTSANDPLAY " + mediaFileURI);
					// mediaFileURI = formUrl() + status[1];

				} else {

					throw new IllegalArgumentException(
							"No se ha realizado el POST al Laboratorio correspondiente");
				}

				/*
				 * CATCH TRY TTS
				 */
			} catch (Exception e) {
				JSONObject json = new JSONObject();
				json.put("status", e.toString());
				return e;
			}
		}

		try {
			PlayCollectMediaListener mediaListener = new PlayCollectMediaListener(
					call);
			mediaListener.setNodeInstance(this);

			MediaService mediaService = MediaFactory.createMediaService();

			PlayItem playItem = MediaFactory.createPlayItem()
					.setSource(new String[] { mediaFileURI })
					.setIterateCount(1);
			if ((interruptibility == null) || (interruptibility.isEmpty())) {
				playItem.setInterruptible(true);
			} else {
				playItem.setInterruptible(Boolean.valueOf(interruptibility)
						.booleanValue());
			}
			DigitOptions digitOptions = MediaFactory.createDigitOptions()
					.setNumberOfDigits(
							Integer.valueOf(numberOfDigits).intValue());
			if ((terminationKey != null) && (!terminationKey.isEmpty())) {
				digitOptions.setTerminationKey(terminationKey);
			}
			if ((terminationKey != null) && (!terminationKey.isEmpty())) {
				digitOptions.setTerminationKey(terminationKey);
			}
			if ((timeout != null) && (!timeout.isEmpty())) {
				int timeoutInMillis = (int) TimeUnit.SECONDS.toMillis(Integer
						.valueOf(timeout).intValue());
				digitOptions.setTimeout(timeoutInMillis);
			} else {
				digitOptions.setTimeout(60000);
			}
			subscribeByCall("Call",
					Arrays.asList(new String[] { "CALL_ENDED" }), callID,
					new String[0]);

			UUID uuID = null;
			String participantHandle;
			if (!StringUtils.isBlank(handle)) {
				if (handle.contains(":")) {
					String[] arr = handle.split(":");
					handle = arr[1];
				}
				participantHandle = null;
				if (handle.contains("@")) {
					String[] arr = handle.split("@");
					participantHandle = arr[0];
				} else {
					participantHandle = handle;
				}
				List<Participant> activeParties = call.getActiveParties();
				for (Participant activeParticipant : activeParties) {
					if (participantHandle.equals(activeParticipant.getHandle())) {
						uuID = mediaService.promptAndCollect(activeParticipant,
								playItem, digitOptions, mediaListener);
						if (!log.isFineEnabled()) {
							break;
						}
						log.fine("PlayNCollect : invoked on handle , "
								+ activeParticipant + " succeeded. ucid: "
								+ call.getUCID()
								+ " and the uuid generated is: " + uuID);
						break;
					}
				}
			} else if (!StringUtils.isBlank(leg)) {
				if (leg.equalsIgnoreCase("called")) {
					uuID = mediaService.promptAndCollect(
							call.getAnsweringParty(), playItem, digitOptions,
							mediaListener);
					if (log.isFineEnabled()) {
						log.fine("PlayNCollect : on participant answering party: "
								+ call.getAnsweringParty()
								+ " succeeded. ucid: "
								+ call.getUCID()
								+ " and the uuid generated is: " + uuID);
					}
				} else {
					uuID = mediaService.promptAndCollect(
							call.getCallingParty(), playItem, digitOptions,
							mediaListener);
					if (log.isFineEnabled()) {
						log.fine("PlayNCollect : on participant calling party: "
								+ call.getCallingParty()
								+ " succeeded. ucid: "
								+ call.getUCID()
								+ " and the uuid generated is: " + uuID);
					}
				}
			} else {
				List<Participant> activeParties = call.getActiveParties();
				if (activeParties.size() > 1) {
					uuID = mediaService.promptAndCollect(
							call.getCallingParty(), playItem, digitOptions,
							mediaListener);
					if (log.isFineEnabled()) {
						log.fine("PlayNCollect : no handle and participant provided, invoked on calling party: "
								+ call.getCallingParty()
								+ " succeeded. ucid: "
								+ call.getUCID()
								+ " and the uuid generated is: " + uuID);
					}
				} else {
					Participant activeParty = (Participant) activeParties
							.get(0);
					uuID = mediaService.promptAndCollect(activeParty, playItem,
							digitOptions, mediaListener);
					if (log.isFineEnabled()) {
						log.fine("PlayNCollect : no handle and participant provided, invoked on single active party: "
								+ activeParty
								+ " succeeded. ucid: "
								+ call.getUCID()
								+ " and the uuid generated is: " + uuID);
					}
				}
			}
			if (uuID != null) {
				if (log.isFineEnabled()) {
					log.fine("UUID generated is: " + uuID + " and ucid: "
							+ call.getUCID());
				}
				mediaListener.setUuid(uuID);
			} else {
				log.error("UUID not generated, something went wrong for ucid: "
						+ call.getUCID());
				resumeTask(call,
						"UUID not generated, something went wrong for ucid: ",
						DigitCollectorOperationCause.FAILED);
			}
		} catch (Exception e) {
			log.error(
					"Error while Playing announcement and collecting digits:",
					e);
			throw e;
		}
		return null;
	}

	private void validateParameter(String param, String name) {
		if ((param == null) || (param.isEmpty())) {
			throw new IllegalArgumentException("Invalid Argument: " + name);
		}
	}

	public void resumeTask(Call call, String digits,
			DigitCollectorOperationCause cause) {
		try {
			InstanceManager instanceManager = instance().getInstanceManager();
			if (instanceManager == null) {
				log.error("InstanceManager is null. Cannot resume the workflow task:"
						+ getNode().getName());
				return;
			}
			if (log.isFineEnabled()) {
				log.fine("resuming nodeInstance:" + getNode().getName());
			}
			JSONObject output = new JSONObject();
			output.put("ucid", call.getUCID());
			output.put("digits", digits);
			output.put("status", cause);
			instanceManager.resumeInstance(String.valueOf(instance()
					.instanceId()), getNode().getName(), output.toString());
			if (log.isFineEnabled()) {
				log.fine("NodeInstance resumed with event body=> "
						+ output.toString());
			}
		} catch (Exception e) {
			log.error("Failed to resume the nodeInstance:"
					+ getNode().getName(), e);
		}
	}

	/*
	 * TextToSpeech Methods
	 */
	public static InputStream reWriteWaveHeader(InputStream is)
			throws IOException {
		byte[] audioBytes = toByteArray(is);
		filesize = audioBytes.length - 8;

		writeInt(filesize, audioBytes, 4);
		writeInt(filesize - 8, audioBytes, 74);

		return new ByteArrayInputStream(audioBytes);
	}

	private static void writeInt(int value, byte[] array, int offset) {
		for (int i = 0; i < 4; i++) {
			array[offset + i] = (byte) (value >>> (8 * i));
		}
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
	/*
	 * End TextToSpeech Methods
	 */

}
