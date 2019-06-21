package com.avaya.AAADEVAI;

import com.roobroo.bpm.model.BpmNode;
import com.roobroo.bpm.util.WFUtil;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class PlayAndCollectModel extends BpmNode {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mediaUri;
	private String interruptibility;
	private String duration;
	private String iterateCount;
	private String timeout;
	private String numberOfDigits;
	private String isFlush;
	private String terminationKey;
	private String participant;
	private String locale;
	private String handle;
	/*
	 * Text To Speech
	 */
	private String messageBody;
	private String userName;
	private String password;
	private String voice;
	private String domain;

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVoice() {
		return voice;
	}

	public void setVoice(String voice) {
		this.voice = voice;
	}

	public String getHandle() {
		return this.handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getLocale() {
		return this.locale;
	}

	public void setIterateCount(String iterateCount) {
		this.iterateCount = iterateCount;
	}

	public String getIterateCount() {
		return this.iterateCount;
	}

	public void setMediaUri(String mediaUri) {
		this.mediaUri = mediaUri;
	}

	public void setInterruptibility(String interruptibility) {
		this.interruptibility = interruptibility;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public void setTimeout(String timeout) {
		this.timeout = timeout;
	}

	public void setNumberOfDigits(String numberOfDigits) {
		this.numberOfDigits = numberOfDigits;
	}

	public void setIsFlush(String isFlush) {
		this.isFlush = isFlush;
	}

	public void setTerminationKey(String terminationKey) {
		this.terminationKey = terminationKey;
	}

	public void setParticipant(String participant) {
		this.participant = participant;
	}

	public PlayAndCollectModel(String name, String id) {
		super(name, id);
	}

	public String getParticipant() {
		return this.participant;
	}

	public String getMediaUri() {
		return this.mediaUri;
	}

	public String getInterruptibility() {
		return this.interruptibility;
	}

	public String getDuration() {
		return this.duration;
	}

	public String getTimeout() {
		return this.timeout;
	}

	public String getNumberOfDigits() {
		return this.numberOfDigits;
	}

	public String getIsFlush() {
		return this.isFlush;
	}

	public String getTerminationKey() {
		return this.terminationKey;
	}

	public boolean validateProperties(List<String> w, List<String> e) {
		boolean isValid = true;
		if (!WFUtil.validateUCIDMapping(w, e, getDataInputAssociations(),
				getName())) {
			isValid = false;
		}
		if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(),
				"mediauri"))
				&& ((this.mediaUri == null) || (this.mediaUri.isEmpty()))) {
			e.add("No value assigned to Media URI/Text/cstoreURI, enter either one to use this task");
			isValid = false;
		}
//		if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(),
//				"domain"))
//				&& ((this.domain == null) || (this.domain.isEmpty()))) {
//			e.add("Favor de anexar el dominio en que se ejecuta la tarea dinámica");
//			isValid = false;
//		}
		if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(),
				"numberOfDigits"))
				&& (!WFUtil.validateEmptyProperty(this.numberOfDigits,
						"numberOfDigits", e))
				&& (!WFUtil.validateNumberProperty(this.numberOfDigits,
						"numberOfDigits", e))) {
			isValid = false;
		}
		if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(),
				"duration"))
				&& (!StringUtils.isBlank(this.duration))
				&& (WFUtil.validateNumberProperty(this.duration, "Duration", e))) {
			isValid = false;
		}
		if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(),
				"iterateCount"))
				&& (!StringUtils.isBlank(this.iterateCount))
				&& (WFUtil.validateNumberProperty(this.iterateCount,
						"iterateCount", e))) {
			isValid = false;
		}
		if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(),
				"timeout"))
				&& (!StringUtils.isBlank(this.timeout))
				&& (WFUtil.validateNumberProperty(this.timeout, "timeout", e))) {
			isValid = false;
		}
		if ((!WFUtil.validateMapping(w, e, getDataInputAssociations(),
				"terminationKey"))
				&& (!StringUtils.isBlank(this.terminationKey))
				&& (!this.terminationKey.matches("[0-9*#]"))) {
			e.add("Value passed to termination key is not a valid key ");
		}
		boolean isHandleOrParticipantValid = true;
		if ((StringUtils.isBlank(this.handle))
				&& (StringUtils.isBlank(this.participant))) {
			if (!WFUtil.validateMapping(w, e, getDataInputAssociations(),
					"handle")) {
				isHandleOrParticipantValid = false;
			}
			if (isHandleOrParticipantValid) {
				isValid = true;
			} else {
				w.add("No value provided for Handle or Participant, enter either one, else by default calling party would be used ");
			}
		}
		return (super.validateProperties(w, e)) && (isValid);
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}
}
