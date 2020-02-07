package com.avaya.AAADEVAI.SpeechToText;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.avaya.AAADEVAI.Security.AES;
import com.avaya.AAADEVAI.SpeechToText.STT.Https.Google;
import com.avaya.AAADEVAI.SpeechToText.STT.Https.IBM;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.Participant;
import com.avaya.collaboration.call.media.DigitCollectorOperationCause;
import com.avaya.collaboration.call.media.MediaListener;
import com.avaya.collaboration.call.media.PlayOperationCause;
import com.avaya.collaboration.call.media.RecordOperationCause;
import com.avaya.collaboration.call.media.SendDigitsOperationCause;
import com.avaya.collaboration.eventing.EventMetaData;
import com.avaya.collaboration.eventing.EventProducer;
import com.avaya.collaboration.eventing.EventingFactory;
import com.avaya.collaboration.ssl.util.SSLUtilityException;
import com.avaya.workflow.logger.Logger;
import com.avaya.workflow.logger.LoggerFactory;

public class WFMediaListener implements MediaListener {
	private final Logger log = LoggerFactory.getLogger(WFMediaListener.class);
	private Map<UUID, Participant> uuidMap;
	private Map<UUID, String> uuidCallIdMap;
	private Call call;
	private NodeInstance nodeInstance;

	public WFMediaListener() {
		this.uuidMap = new ConcurrentHashMap();
		this.uuidCallIdMap = new ConcurrentHashMap();
	}

	public void addUUIDToMap(UUID uuid, Participant participant) {
		this.uuidMap.put(uuid, participant);
	}

	public void addUUIDToCallIdMap(UUID uuid, String callId) {
		this.uuidCallIdMap.put(uuid, callId);
	}

	public void removeUUIDFromMap(UUID uuid) {
		this.uuidMap.remove(uuid);
	}

	public void removeUUIDFromCallIdMap(UUID uuid) {
		this.uuidCallIdMap.remove(uuid);
	}

	public void setCall(Call call) {
		this.call = call;
	}

	public Call getCall() {
		return this.call;
	}

	public void setNodeInstance(NodeInstance nodeInstance) {
		this.nodeInstance = nodeInstance;
	}

	public NodeInstance getNodeInstance() {
		return this.nodeInstance;
	}

	public Map<UUID, Participant> getUuidMap() {
		return this.uuidMap;
	}

	public void digitsCollected(UUID requestId, String digits,
			DigitCollectorOperationCause cause) {
	}

	public void playCompleted(UUID requestId, PlayOperationCause cause) {
	}

	public void sendDigitsCompleted(UUID arg0, SendDigitsOperationCause arg1) {
	}

	public void recordCompleted(UUID requestId, RecordOperationCause cause) {
		log.info("SpeechToText recordCompleted");
		if (this.log.isFinestEnabled()) {
			this.log.finest("Record operation completed for UUID = "
					+ requestId + "; Cause: " + cause);
		}
		SpeechToTextExecution thisNode = (SpeechToTextExecution) getNodeInstance();
		if (thisNode.Async()) {
			if (this.log.isFinestEnabled()) {
				this.log.finest("recordCompleted: worked as Async task, publish an event.");
			}
			//Petición STT
			EventMetaData meta = EventingFactory.createEventMetaData();
			meta.addValue("recordingUUID", requestId.toString());
			String cloudService = thisNode.getCloudService();
			String language = new AES().encrypt(thisNode.getLanguageForSTT());
			String fileName = new AES().encrypt(thisNode.getFileNameForSTT());
			log.info("SpeechToText recordCompleted cloudService " +  cloudService);
			log.info("SpeechToText recordCompleted language " +  language);
			log.info("SpeechToText recordCompleted fileName " +  fileName);
			JSONObject jsonResponse = null;
			JSONObject output = new JSONObject();
			if(cloudService.equals("IBM")){
				try {
					jsonResponse = new IBM(language, fileName).makePostSTT();
				} catch (SSLUtilityException | IOException | JSONException e) {
					try {
						jsonResponse = new JSONObject().put("status", "error").put("message", e.toString());
					} catch (JSONException e1) {
						log.error(e1.toString());
					}
				}
			}else{
				try {
					jsonResponse = new Google(language, fileName).makePostSTT();
				} catch (SSLUtilityException | IOException | JSONException e) {
					try {
						jsonResponse = new JSONObject().put("status", "error").put("message", e.toString());
					} catch (JSONException e1) {
						log.error(e1.toString());
					}
				}
			}	
			try {
				if(cloudService.equals("IBM")){
					if(jsonResponse.has("status") && jsonResponse.getString("status").equals("error")){
						output.put("transcriptionError", jsonResponse.getString("message"));
					}
					if(jsonResponse.has("error") && jsonResponse.has("status") && jsonResponse.getString("status").equals("ok") && jsonResponse.getInt("code") != 200){
						output.put("transcriptionError", jsonResponse.getString("error"));
					}
					if(jsonResponse.has("results") && jsonResponse.has("status") && jsonResponse.getString("status").equals("ok") && jsonResponse.getInt("code") == 200){
						if(jsonResponse.getJSONArray("results").getJSONObject(0).getJSONArray("alternatives").length() == 1 && jsonResponse.getJSONArray("results").getJSONObject(0).getJSONArray("alternatives").length() != 0 ){
							double confidence = jsonResponse.getJSONArray("results").getJSONObject(0).getJSONArray("alternatives").getJSONObject(0).getDouble("confidence");
							String transcript = jsonResponse.getJSONArray("results").getJSONObject(0).getJSONArray("alternatives").getJSONObject(0).getString("transcript");
							output.put("transcription", transcript);
							output.put("transcriptionConfidence", (confidence == 0.0)?"0":new DecimalFormat("#.00").format((confidence * 100)));
						}else{
							double confianzaTotal = 0;
							double [] confianzas = new double[jsonResponse.getJSONArray("results").length()];
							StringBuilder sb = new StringBuilder();
							for (int i = 0; i < jsonResponse.getJSONArray("results").length(); i++) {
								sb.append(jsonResponse.getJSONArray("results").getJSONObject(i).getJSONArray("alternatives").getJSONObject(0).getString("transcript"));
								confianzas[i] = jsonResponse.getJSONArray("results").getJSONObject(i).getJSONArray("alternatives").getJSONObject(0).getDouble("confidence");
							}
							for (int j = 0; j < confianzas.length; j++) {
								confianzaTotal = confianzaTotal + confianzas[j];
							}
							output.put("transcription", sb.toString());
							output.put("transcriptionConfidence", ((confianzaTotal / confianzas.length) == 0.0)?"0":new DecimalFormat("#.00").format(((confianzaTotal / confianzas.length) * 100)));
							
						}
						

					}
				}else{
					if(jsonResponse.has("status") && jsonResponse.getString("status").equals("error")){
						output.put("transcriptionError", jsonResponse.getString("message"));
					}
					if(jsonResponse.has("error") && jsonResponse.has("status") && jsonResponse.getString("status").equals("ok") && jsonResponse.getInt("code") != 200){
						output.put("transcriptionError", jsonResponse.getJSONObject("message") + " " + jsonResponse.getJSONObject("status"));
					}
					if(jsonResponse.has("results") && jsonResponse.has("status") && jsonResponse.getString("status").equals("ok") && jsonResponse.getInt("code") == 200){
						int confidence = jsonResponse.getJSONArray("results").getJSONObject(0).getJSONArray("alternatives").getJSONObject(0).getInt("confidence");
						String transcript = jsonResponse.getJSONArray("results").getJSONObject(0).getJSONArray("alternatives").getJSONObject(0).getString("transcript");
						output.put("transcription", transcript);
						output.put("transcriptionConfidence", Integer.toString(confidence));
					}
				}
				output.put("cause", cause);
				output.put("retrievalUrl", thisNode.getRetrievalUrl());
				if (RecordOperationCause.FAILED.equals(cause)) {
					output.put("status", "FAILED");
				} else {
					output.put("status", "SUCCESS");
				}
			} catch (Exception e) {
				this.log.error("recordCompleted: Json error: ", e);
			}
			EventProducer producer = EventingFactory.createEventProducer(
					"Media", "MEDIA_PROCESSED", meta, output.toString(), "");
			producer.publish();
		}
	}

	public Map<UUID, String> getUuidCallMap() {
		return this.uuidCallIdMap;
	}

	public void setUuidCallMap(Map<UUID, String> uuidCallMap) {
		this.uuidCallIdMap = uuidCallMap;
	}
}
