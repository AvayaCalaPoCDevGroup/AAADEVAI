package com.avaya.AAADEVAI.AAAPlayAnnouncementTTS;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;

import com.avaya.AAADEVAI.BuscarYRemplazarAcentos;
import com.avaya.AAADEVAI.ServiceAttributeManager;
import com.avaya.AAADEVAI.AAAPlayAnnouncementTTS.Https.Google;
import com.avaya.AAADEVAI.AAAPlayAnnouncementTTS.Https.IBM;
import com.avaya.AAADEVAI.Security.AES;
import com.avaya.AAADEVAI.Util.CommTaskUtil;
import com.avaya.AAADEVAI.Util.Constants;
import com.avaya.AAADEVAI.Util.WFMediaListener;
import com.avaya.AAADEVAI.Util.WFMediaUtil;
import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.CallFactory;
import com.avaya.collaboration.call.Participant;
import com.avaya.collaboration.call.media.MediaFactory;
import com.avaya.collaboration.call.media.MediaService;
import com.avaya.collaboration.eventing.EventingFactory;
import com.avaya.workflow.logger.Logger;
import com.avaya.workflow.logger.LoggerFactory;
import com.roobroo.bpm.model.BpmNode;

public class TTSANDPLAYExecution extends NodeInstance {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = LoggerFactory
			.getLogger(TTSANDPLAYExecution.class);

	private String ucid;
	private volatile boolean isInstanceResumed;
	private WFMediaListener mediaListener;
	/*
	 * TextToSpeech
	 */
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

		
		
		String mediaFileURI = (String) get("mediauri");
		if ((mediaFileURI == null) || (mediaFileURI.isEmpty())) {
			mediaFileURI = model.getMediaUri();
		}
		boolean retval;
		boolean retva2;
		retval = mediaFileURI.contains("http://");
		retva2 = mediaFileURI.contains("https://");

		if (retval == true || retva2 == true) {

		} else {
			
			/*
			 * Text To Speech
			 */
			String voice = (String) get("language");
			if ((voice == null) || (voice.isEmpty())) {
				voice = model.getLanguage();
				if(voice == null || voice.isEmpty()){
					voice = "es";
				}
			}

			String cloudProvider = (String)get("cloudProvider");
			if(cloudProvider == null || cloudProvider.isEmpty()){
				cloudProvider = model.getCloudProvider();
				if(cloudProvider == null || cloudProvider.isEmpty()){
					cloudProvider = "Google";
				}
			}
			AES aes = new AES();
			if(cloudProvider.equals("Google")){
				String voiceGoogle = null;
				String voiceNameGoogle = null;
				if (voice.equals("es-ES")){
					voiceGoogle = "es-ES";
					voiceNameGoogle = "es-ES-Standard-A";
				}

				if (voice.equals("pt-BR")) {
					voiceGoogle = "pt-BR";
					voiceNameGoogle = "pt-BR-Standard-A";
				}
				if(voice.equals("en-US")){
					voiceGoogle = "en-US";
					voiceNameGoogle = "en-US-Wavenet-C";
				}
				
				Google request = new Google();
				JSONObject jsonGoogle = new JSONObject();
				jsonGoogle = request.googleTTS(aes.encrypt(mediaFileURI), aes.encrypt(voiceGoogle), aes.encrypt(voiceNameGoogle));
				if(jsonGoogle.has("status") && (jsonGoogle.getString("status").equals("ok"))){
					mediaFileURI = "http://"+Constants.getFQDN()+"/services/AAADEVCloudServices/EngagementDesignerGoogleCoudTTS.wav";
				}
				if(jsonGoogle.has("error") && (jsonGoogle.getString("error").equals("sin Audio Content"))){
					throw new Exception("Error al crear archivo de audio.");
				}
			}
			if(cloudProvider.equals("IBM")){
				String voiceNameIBM = null;
				if (voice.equals("es-ES")){
					BuscarYRemplazarAcentos español = new BuscarYRemplazarAcentos();
					mediaFileURI = español.Español(mediaFileURI);
					voiceNameIBM = "es-ES_LauraVoice";
				}

				if (voice.equals("pt-BR")) {
					BuscarYRemplazarAcentos portugues = new BuscarYRemplazarAcentos();
					mediaFileURI = portugues.Portugues(mediaFileURI);
					voiceNameIBM = "pt-BR_IsabelaVoice";
				}
				if(voice.equals("en-US")){
					voiceNameIBM = "en-US_AllisonVoice";
				}
				IBM request = new IBM();
				JSONObject jsonIbm = new JSONObject();
				jsonIbm = request.ibmTTS(aes.encrypt(mediaFileURI), aes.encrypt(voiceNameIBM));
				if(jsonIbm.has("status") && (jsonIbm.getString("status").equals("ok"))){
					mediaFileURI = "http://"+Constants.getFQDN()+"/services/AAADEVCloudServices/EngagementDesignerIBMCloudTTS.wav";
				}
				if(jsonIbm.has("status") && (jsonIbm.getString("status").equals("error"))){
					throw new Exception("Error al crear archivo de audio.");
				}
			}
			
			
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
	 * End TextToSpeech Methods
	 */
}
