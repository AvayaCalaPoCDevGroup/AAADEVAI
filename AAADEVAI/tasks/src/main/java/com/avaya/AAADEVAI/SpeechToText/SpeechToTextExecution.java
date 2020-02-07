package com.avaya.AAADEVAI.SpeechToText;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.avaya.AAADEVAI.Util.CommTaskUtil;
import com.avaya.app.entity.Instance;
import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.Participant;
import com.avaya.collaboration.call.media.MediaFactory;
import com.avaya.collaboration.call.media.MediaService;
import com.avaya.collaboration.call.media.RecordItem;
import com.avaya.workflow.logger.Logger;
import com.avaya.workflow.logger.LoggerFactory;
import com.roobroo.bpm.model.BpmNode;

@SuppressWarnings({ "serial" })
public class SpeechToTextExecution extends NodeInstance {

	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory
			.getLogger(SpeechToTextExecution.class);
	private String ucid;
	private String retrievalUrl;
	private String cloudService;
	private String languageForSTT;
	private String fileNameForSTT;

	public SpeechToTextExecution(Instance instance, BpmNode node) {
		super(instance, node);
	}

	
	private boolean isAsync = false;
	private WFMediaListener mediaListener;
	
	public String getFileNameForSTT() {
		return fileNameForSTT;
	}

	public void setFileNameForSTT(String fileNameForSTT) {
		this.fileNameForSTT = fileNameForSTT;
	}

	public String getLanguageForSTT() {
		return languageForSTT;
	}

	public void setLanguageForSTT(String languageForSTT) {
		this.languageForSTT = languageForSTT;
	}

	public String getCloudService() {
		return cloudService;
	}

	public void setCloudService(String cloudService) {
		this.cloudService = cloudService;
	}

	public void setUcid(String ucid) {
		this.ucid = ucid;
	}

	public String getUcid() {
		return this.ucid;
	}

	public String getRetrievalUrl() {
		return this.retrievalUrl;
	}

	public boolean Async() {
		return this.isAsync;
	}

	public WFMediaListener getMediaListener() {
		if (this.mediaListener == null) {
			this.mediaListener = new WFMediaListener();
		}
		return this.mediaListener;
	}

	public Object execute() throws Exception {

		SpeechToTextModel model = (SpeechToTextModel) getNode();
		RecordItem recordItem = MediaFactory.createRecordItem();
		MediaService mediaService = MediaFactory.createMediaService();

		String callID = (String) get("ucid");
		if (StringUtils.isBlank(callID)) {
			throw new IllegalArgumentException(
					"Invalid Argument! CallID cannot be empty...");
		}
		setUcid(callID);

		String fileUri = (String) get("fileUri");
		if (StringUtils.isBlank(fileUri)) {
			fileUri = model.getFileUri();
			if (StringUtils.isBlank(fileUri)) {
				throw new IllegalArgumentException(
						"Invalid Argument! fileUri cannot be empty...");
			}
		}
		
		String language = (String)get("languages");
		if(StringUtils.isBlank(language)){
			language = model.getLanguages();
			if(StringUtils.isBlank(language)){
				throw new IllegalArgumentException("Invalid Argument! language cannot be empty...");
			}
		}
		
		this.languageForSTT = language;
		
		String cloudServiceInput = (String)get("cloudServices");
		if(StringUtils.isBlank(cloudServiceInput)){
			cloudServiceInput = model.getCloudServices();
			if(StringUtils.isBlank(cloudServiceInput)){
				cloudServiceInput = "IBM";
			}
		}
		
		this.cloudService = cloudServiceInput;
		
		String recordedParty = (String) get("recordedParty");
		if (StringUtils.isBlank(recordedParty)) {
			recordedParty = model.getRecordedParty();
			if (StringUtils.isBlank(recordedParty)) {
				throw new IllegalArgumentException(
						"Invalid Argument! recordedParty cannot be empty...");
			}
		}

		String maxDuration = (String) get("maxDuration");
		if (StringUtils.isBlank(maxDuration)) {
			maxDuration = model.getMaxDuration();
		}

		if ((maxDuration != null) && (!maxDuration.isEmpty())) {
			try {
				int duration = Integer.parseInt(maxDuration);
				recordItem.setMaxDuration(duration);
			} catch (NumberFormatException nfe) {
				throw new IllegalArgumentException(
						"Invalid Argument! maxDuration is not in the number format...");
			}
		}

		String terminationKey = (String) get("terminationKey");
		if (StringUtils.isBlank(terminationKey)) {
			terminationKey = model.getTerminationKey();
		}
		if (!StringUtils.isBlank(terminationKey)) {
			recordItem.setTerminationKey(terminationKey);
		}
		String fileNamePattern = model.getFileNamePattern();
		if (StringUtils.isBlank(fileNamePattern)) {
			fileNamePattern = "RecordedParty_CallId";
		}
		this.retrievalUrl = ((String) get("retrievalUrl"));
		if (StringUtils.isBlank(this.retrievalUrl)) {
			this.retrievalUrl = model.getRetrievalUrl();
			if (StringUtils.isBlank(this.retrievalUrl)) {
				this.retrievalUrl = fileUri;
			}
		}

		Call call = CommTaskUtil.getCall(callID);
		if (call == null) {
			log.error("RecordParticipantExecution: Error getting call object...");
			throw new IllegalArgumentException("Call object not found...");
		}

		validateRecordedParty(recordedParty);
		Participant participant = getParticipant(recordedParty, call);
		String fileName = generateFileName(fileNamePattern, recordedParty,
				call, callID, participant);
		this.fileNameForSTT = fileName + ".wav";
		fileUri = modifyFileUri(fileUri, fileName);
		this.retrievalUrl = (this.retrievalUrl + fileName + ".wav");
		recordItem.setFileUri(fileUri);
		if (log.isFineEnabled()) {
			log.fine("RecordParticipantExecution: ucid = " + callID
					+ "; fileUri = " + fileUri + "; recordedParty = "
					+ recordedParty + "; maxDuration = " + maxDuration
					+ "; termincationKey = " + terminationKey
					+ "; retrievalUrl = " + this.retrievalUrl);
		}
		UUID uuid = null;
		if (participant != null) {
			if (log.isFinestEnabled()) {
				log.finest("RecordParticipantExecution: Call " + call
						+ ", Participant " + participant + "; Record Item: "
						+ recordItem);
			}
			this.mediaListener = getMediaListener();
			this.mediaListener.setNodeInstance(this);

			uuid = mediaService.record(participant, recordItem,
					this.mediaListener);
			if (model.isAsync().booleanValue()) {
				this.isAsync = true;
				String subscribeID = subscribeByCorrelationKey("Media",
						"MEDIA_PROCESSED", "recordingUUID", uuid.toString());
				if (log.isFinestEnabled()) {
					log.finest("RecordParticipantExecution: work as the async task, subscribeID = "
							+ subscribeID);
				}
			}
		} else {
			log.error("RecordParticipantExecution: participant is null. (UN-SUPPORTED for CALL) Call "
					+ call + "; Record Item: " + recordItem);

			throw new IllegalArgumentException(
					"The participant cannot be identified at the current call state.");
		}
		if (log.isFinestEnabled()) {
			log.finest("RecordParticipantExecution: completed. UUID: " + uuid);
		}
		JSONObject output = new JSONObject();
		output.put("cause",
				"Proceeded without waiting until the recording is completed.");
		output.put("retrievalUrl", this.retrievalUrl);
		output.put("status", NodeInstance.Status.SUCCESS.toString());

		return output;
	}

	private void validateRecordedParty(String recordedParty) {
		if ((!recordedParty.equals("Calling"))
				&& (!recordedParty.equals("Called"))
				&& (!recordedParty.equals("Answering"))) {
			log.error("RecordParticipantExecution: invalid recordedParty: "
					+ recordedParty);
			throw new IllegalArgumentException("Invalid recordedParty: "
					+ recordedParty);
		}
	}

	private Participant getParticipant(String recordedParty, Call call) {
		Participant participant = null;
		if (recordedParty.equals("Calling")) {
			participant = call.getCallingParty();
		} else if (recordedParty.equals("Called")) {
			participant = call.getCalledParty();
		} else if (recordedParty.equals("Answering")) {
			participant = call.getAnsweringParty();
			if (participant == null) {
				log.error("RecordParticipantExecution: The answering party cannot be identified at the current call state.");

				throw new IllegalArgumentException(
						"The answering party cannot be identified at the current call state.");
			}
		}
		return participant;
	}

	private String generateFileName(String fileNamePattern,
			String recordedParty, Call call, String callID,
			Participant participant) {
		String filename = null;
		if (fileNamePattern.equals("CallPartyNumber_CallId")) {
			filename = participant.getHandle() + "-" + callID;
		} else if (fileNamePattern.equals("CallPartyNumber_Timestamp")) {
			String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US)
					.format(new Date());
			filename = participant.getHandle() + "-" + timeStamp;
		} else {
			filename = recordedParty + "-" + callID;
		}
		return filename;
	}

	private String modifyFileUri(String fileUri, String fileName) {
		String modifiedUri = null;
		if (fileUri.startsWith("cstore://?")) {
			modifiedUri = fileUri.replace("cstore://?", "cstore://" + fileName
					+ "?");
		} else {
			modifiedUri = fileUri + fileName + ".wav";
		}
		return modifiedUri;
	}

}
