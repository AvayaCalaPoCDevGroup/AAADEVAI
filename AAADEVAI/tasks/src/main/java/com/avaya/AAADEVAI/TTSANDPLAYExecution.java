package com.avaya.AAADEVAI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.CallFactory;
import com.avaya.collaboration.call.Participant;
import com.avaya.collaboration.call.media.MediaFactory;
import com.avaya.collaboration.call.media.MediaService;
import com.avaya.collaboration.eventing.EventingFactory;
import com.avaya.collaboration.ssl.util.SSLProtocolType;
import com.avaya.collaboration.ssl.util.SSLUtilityFactory;
import com.roobroo.bpm.model.BpmNode;
import com.avaya.workflow.logger.*;

public class TTSANDPLAYExecution extends NodeInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory
			.getLogger(TTSANDPLAYExecution.class);

	private static final int DEFAULT_MEDIA_LENGTH = -1;
	private static final int DEFAULT_ITERATE_COUNT = 1;
	private static final String NO_DEFAULT_VALUE = "-1";
	private String ucid;
	private volatile boolean isInstanceResumed;
	private WFMediaListener mediaListener;
	private MediaService mediaService;
	/*
	 * TextToSpeech
	 */
	private String userHomeDir = System.getProperty("user.home");
	private String osName = System.getProperty("os.name");
	public static int filesize;
	private Call call;

	public TTSANDPLAYExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}

	public String getUcid() {
		return this.ucid;
	}

	public boolean isInstanceResumed() {
		return this.isInstanceResumed;
	}

	public void setInstanceResumed(boolean isInstanceResumed) {
		this.isInstanceResumed = isInstanceResumed;
	}

	public WFMediaListener getMediaListener() {
		if (this.mediaListener == null) {
			this.mediaListener = new WFMediaListener();
		}
		return this.mediaListener;
	}

	public Object execute() throws Exception {
		TTSANDPLAYModel model = (TTSANDPLAYModel) getNode();

		String callID = (String) get("ucid");
		if ((callID == null) || (callID.isEmpty())) {
			throw new IllegalArgumentException(
					"Invalid Argument! CallID cannot be empty...");
		}
		setUcid(callID);

		String handle = (String) get("handle");
		if ((handle == null) || (handle.isEmpty())) {
			handle = model.getHandle();
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
		boolean retval;
		boolean retva2;
		retval = mediaFileURI.contains("http://");
		retva2 = mediaFileURI.contains("https://");

		String domain = "10.0.0.10";
		// if ((domain == null) || (domain.isEmpty())) {
		// domain = model.getDomain();
		// }

		log.info("AAADEVTTSANDPLAY media File Uri " + mediaFileURI);

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
				if (ucid != null) {
					call = CallFactory.getCall(ucid);
					MakingPost post = new MakingPost(call, domain);
					log.info("AAADEVTTSANDPLAY MakePost With Call");
					status = post.makingPostWithCall(call);
				} else {
					MakingPost post = new MakingPost(domain);
					log.info("AAADEVTTSANDPLAY MakePost Without Call");
					status = post.makingPOST();
				}
				log.info("AAADEVTTSANDPLAY Status" + status[0]);
				if (status[0].equals("ok")) {

					mediaFileURI = "http://"
							+ domain
							+ "/services/AAADEVLOGGER/FileSaveServlet/web/RecordParticipant/"
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
			/*
			 * Final Text To Speech
			 */
		}
		if (log.isFineEnabled()) {
			log.fine("Media Prompt to be played is:" + mediaFileURI);
		}
		String interruptibility = (String) get("interruptibility");
		if ((interruptibility == null) || (interruptibility.isEmpty())) {
			interruptibility = model.getInterruptibility();
			if ((interruptibility == null) || (interruptibility.isEmpty())) {
				interruptibility = "true";
			}
		}
		String duration = (String) get("duration");
		if ((duration == null) || (duration.isEmpty())) {
			duration = model.getDuration();
			if ((duration == null) || (duration.isEmpty())) {
				duration = "-1";
			}
		}
		int durationInMillis = (int) TimeUnit.SECONDS.toMillis(Integer.valueOf(
				duration).intValue());

		String announcementForever = (String) get("announcementForever");
		if ((announcementForever == null) || (announcementForever.isEmpty())) {
			announcementForever = model.getAnnouncementForever();
			if ((announcementForever == null)
					|| (announcementForever.isEmpty())) {
				announcementForever = "false";
			}
		}
		String participant = (String) get("participant");
		if ((participant == null) || (participant.isEmpty())) {
			participant = model.getParticipant();
		}
		boolean result = false;
		WFMediaUtil mediaUtil = new WFMediaUtil();

		setInstanceResumed(false);

		String subscribeId = null;

		this.mediaListener = getMediaListener();
		this.mediaListener.setNodeInstance(this);
		try {
			if (!isReconstruction()) {
				List<String> eventList = Arrays
						.asList(new String[] { "MEDIA_PROCESSED" });

				subscribeId = subscribeByCall("Media", eventList, callID,
						new String[0]);
				if (log.isFineEnabled()) {
					log.fine("Subscribing media event with subscribeId:"
							+ subscribeId);
				}
			}
			if (log.isFineEnabled()) {
				log.fine("Playing announcement for callID:" + callID);
			}
			if (!isReconstruction()) {
				subscribeByCall("Call",
						Arrays.asList(new String[] { "CALL_ENDED" }), callID,
						new String[0]);
			}
			mediaUtil.playAnnouncement(callID, mediaFileURI,
					Boolean.valueOf(interruptibility).booleanValue(),
					durationInMillis, 1, Boolean.valueOf(announcementForever)
							.booleanValue(), this.mediaListener,
					isReconstruction(), handle, participant);

			result = true;
			if (log.isFineEnabled()) {
				log.fine("play announcement for callID:" + callID
						+ " succeeded...");
			}
		} catch (Exception e) {
			log.error("Failed to play announcement for callID:" + callID, e);
			EventingFactory.createEventingService().unsubscribe(subscribeId);
			throw e;
		}
		JSONObject output = new JSONObject();
		output.put("ucid", callID);
		if (result) {
			output.put("status", NodeInstance.Status.SUCCESS.toString());
		} else {
			output.put("status", NodeInstance.Status.FAILED.toString());
		}
		return output;

	}

	public void cancel() throws Exception {
		log.fine("PlayAnnouncement cancel() callback invoked...");
		if (!CommTaskUtil.isCallValid(getUcid())) {
			log.fine("Call has been disconnected, No explicit cancel required...");
			return;
		}
		Map<UUID, Participant> map = getMediaListener().getUuidMap();
		MediaService mediaService;
		if ((map != null) && (!map.isEmpty())) {
			if (log.isFineEnabled()) {
				log.fine("Stopping announcement on all the participant for callID:"
						+ getMediaListener().getCall().getUCID());
			}
			mediaService = MediaFactory.createMediaService();
			for (Map.Entry<UUID, Participant> e : map.entrySet()) {
				try {
					if (log.isFineEnabled()) {
						log.fine("Media Listener ==>> UUIDs: " + e.getKey()
								+ "====== values: "
								+ ((Participant) e.getValue()).getAddress());
					}
					mediaService.stop((Participant) e.getValue(),
							(UUID) e.getKey());
					map.remove(e.getKey());
				} catch (Exception ex) {
					if (log.isFineEnabled()) {
						log.fine("mediaService.stop() unSuccessful on "
								+ e.getValue()
								+ ", Call appears to have been abandoned.");
					}
				}
			}
		} else {
			log.fine("Map is empty ===================== nothing to cancel");
		}
		Map<UUID, String> callIdMap = getMediaListener().getUuidCallMap();

		if ((callIdMap != null) && (!callIdMap.isEmpty())) {
			if (log.isFineEnabled()) {
				log.fine("Stopping announcement on callID:"
						+ getMediaListener().getCall().getUCID());
			}
			mediaService = MediaFactory.createMediaService();
			for (Map.Entry<UUID, String> e : callIdMap.entrySet()) {
				try {
					if (log.isFineEnabled()) {
						log.fine("Media Listener ==>> UUIDs: " + e.getKey()
								+ "====== callid: " + (String) e.getValue());
					}
					mediaService.stop(getCall((String) e.getValue()),
							(UUID) e.getKey());
					callIdMap.remove(e.getKey());
				} catch (Exception ex) {
					if (log.isFineEnabled()) {
						log.fine("mediaService.stop() unSuccessful on "
								+ (String) e.getValue()
								+ ", Call appears to have been abandoned.");
					}
				}
			}
		} else {
			log.fine("Callid Map is empty ===================== nothing to cancel");
		}
	}

	protected void initFromCustomPropertiesJSON(JSONObject props) {
		try {
			this.isInstanceResumed = getJSONPropertyAsBoolean(props,
					"instanceResumed");
			WFMediaListener listener = getMediaListener();
			Call call = getCall(getJSONPropertyAsString(props, "callUCID"));
			setUcid(getJSONPropertyAsString(props, "callUCID"));
			if (call != null) {
				List<Participant> participants = call.getActiveParties();
				JSONObject parties;
				if ((participants != null) && (!participants.isEmpty())) {
					parties = (JSONObject) getJSONProperty(props,
							"participants");
					if (parties != null) {
						for (Participant party : participants) {
							String uuid = getJSONPropertyAsString(parties,
									party.getAddress());
							if (uuid != null) {
								listener.addUUIDToMap(UUID.fromString(uuid),
										party);
							}
						}
					}
				}
				String uuid = getJSONPropertyAsString(props, "uuid");
				if (uuid != null) {
					listener.addUUIDToCallIdMap(UUID.fromString(uuid),
							getJSONPropertyAsString(props, "callUCID"));
				}
				listener.setCall(call);
			}
		} catch (Exception e) {
			log.warn("Exception occurred while initializing customer properties for node: "
					+ getTitle() + ", Error: " + e.getMessage());
		}
	}

	public JSONObject convertCustomPropertiesToJSON() {
		JSONObject root = null;
		try {
			root = new JSONObject();
			root.put("instanceResumed", String.valueOf(this.isInstanceResumed));

			WFMediaListener mediaListener = getMediaListener();
			if (mediaListener != null) {
				Call call = mediaListener.getCall();
				if (call != null) {
					root.put("callUCID", call.getUCID());
					Map<UUID, Participant> uuidMap = mediaListener.getUuidMap();
					if ((uuidMap != null) && (!uuidMap.isEmpty())) {
						JSONObject json = new JSONObject();
						for (Map.Entry<UUID, Participant> e : uuidMap
								.entrySet()) {
							json.put(((Participant) e.getValue()).getAddress(),
									((UUID) e.getKey()).toString());
						}
						root.put("participants", json);
					}
					if ((mediaListener.getUuidCallMap() != null)
							&& (!mediaListener.getUuidCallMap().isEmpty())) {
						for (Map.Entry<UUID, String> e : mediaListener
								.getUuidCallMap().entrySet()) {
							root.put("uuid", ((UUID) e.getKey()).toString());
						}
					}
				}
			}
		} catch (Exception e) {
			log.warn("Exception occurred while converting customer properties to Json for node: "
					+ getTitle() + ", Error: " + e.getMessage());
		}
		return root;
	}

	private Object getJSONProperty(JSONObject json, String key) {
		try {
			return json.get(key);
		} catch (Exception e) {
		}
		return null;
	}

	private String getJSONPropertyAsString(JSONObject json, String key) {
		Object val = getJSONProperty(json, key);
		return val == null ? null : val.toString();
	}

	private boolean getJSONPropertyAsBoolean(JSONObject json, String key) {
		Object val = getJSONProperty(json, key);
		if (val == null) {
			return false;
		}
		return (Boolean.TRUE.equals(val))
				|| ("true".equalsIgnoreCase(val.toString()));
	}

	private Call getCall(String id) {
		try {
			return id == null ? null : CallFactory.getCall(id);
		} catch (Exception e) {
			log.warn("Issue with call id '" + id
					+ "', probably the call is completed ...");
		}
		return null;
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
