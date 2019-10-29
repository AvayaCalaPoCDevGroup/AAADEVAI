package com.avaya.AAADEVAI.PlayAndCollect.TTS;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.avaya.AAADEVAI.BuscarYRemplazarAcentos;
import com.avaya.AAADEVAI.ServiceAttributeManager;
import com.avaya.AAADEVAI.AAAPlayAnnouncementTTS.Https.Google;
import com.avaya.AAADEVAI.AAAPlayAnnouncementTTS.Https.IBM;
import com.avaya.AAADEVAI.Security.AES;
import com.avaya.AAADEVAI.Util.CommTaskUtil;
import com.avaya.AAADEVAI.Util.Constants;
import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.Participant;
import com.avaya.collaboration.call.media.DigitCollectorOperationCause;
import com.avaya.collaboration.call.media.DigitOptions;
import com.avaya.collaboration.call.media.MediaFactory;
import com.avaya.collaboration.call.media.MediaService;
import com.avaya.collaboration.call.media.PlayItem;
import com.avaya.workflow.logger.Logger;
import com.avaya.workflow.logger.LoggerFactory;
import com.roobroo.bpm.im.InstanceManager;
import com.roobroo.bpm.model.BpmNode;

public class PlayAndCollectExecution extends NodeInstance {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(PlayAndCollectExecution.class);
	public static final int ITERATE_COUNT = 1;
	public static final int DEFAULT_TIMEOUT = 60000;
	public static final String CALLING_LEG = "calling";
	public static final String CALLED_LEG = "called";

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
}
