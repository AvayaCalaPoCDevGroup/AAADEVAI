package com.avaya.AAADEVAI;

import java.util.ArrayList;
import java.util.List;

import com.roobroo.bpm.model.BpmNode;
import com.roobroo.bpm.util.WFUtil;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

public class TTSANDPLAYModel extends BpmNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TTSANDPLAYModel(String name, String id) {
		super(name, id);
		// TODO Auto-generated constructor stub
	}
	/*
	 * Play anoouncement
	 */
	private String mediaUri;
	private String mediaUriAdditional;
	private String interruptibility;
	private String duration;
	private String announcementForever;
	private String iterateCount;
	private String nameSpace;
	private String groupName;
	private String fileURI;
	private String useLocale;
	private String locale;
	private String handle;
	private String participant;
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

	public String getParticipant() {
		return this.participant;
	}

	public void setParticipant(String participant) {
		this.participant = participant;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getLocale() {
		return this.locale;
	}

	public String getUseLocale() {
		return this.useLocale;
	}

	public void setUseLocale(String useLocale) {
		this.useLocale = useLocale;
	}

	public String getNameSpace() {
		return this.nameSpace;
	}

	public void setNameSpace(String nameSpace) {
		this.nameSpace = nameSpace;
	}

	public String getGroupName() {
		return this.groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getFileURI() {
		return this.fileURI;
	}

	public void setFileURI(String fileURI) {
		this.fileURI = fileURI;
	}

	public void setIterateCount(String iterateCount) {
		this.iterateCount = iterateCount;
	}

	public String getIterateCount() {
		return this.iterateCount;
	}

	public void setAnnouncementForever(String announcementForever) {
		this.announcementForever = announcementForever;
	}

	public String getAnnouncementForever() {
		return this.announcementForever;
	}

	public void setMediaUri(String mediaUri) {
		this.mediaUri = mediaUri;
	}

	public void setMediaUriAdditional(String mediaUriAdditional) {
		this.mediaUriAdditional = mediaUriAdditional;
	}

	public void setInterruptibility(String interruptibility) {
		this.interruptibility = interruptibility;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getMediaUri() {
		return this.mediaUri;
	}

	public String getMediaUriAdditional() {
		return this.mediaUriAdditional;
	}

	public String getInterruptibility() {
		return this.interruptibility;
	}

	public String getDuration() {
		return this.duration;
	}

	public String getHandle() {
		return this.handle;
	}

	public void setHandle(String handle) {
		this.handle = handle;
	}

	

	public ArrayList<String> createMediaURIsArray(String mediaURIAdditional)
			throws Exception {
		JSONArray mediaURIArray = getMediaURIsAsJsonArray(mediaURIAdditional);
		ArrayList<String> mediaURIs = new ArrayList();
		if (mediaURIArray == null) {
			return mediaURIs;
		}
		for (int i = 0; i < mediaURIArray.length(); i++) {
			JSONObject item = mediaURIArray.getJSONObject(i);

			String mediaURI = checkNotNullOrMissing(item
					.getString("MediaURIAdditional"));

			mediaURIs.add(mediaURI);
		}
		return mediaURIs;
	}

	private JSONArray getMediaURIsAsJsonArray(String mediaURIAdditional)
			throws Exception {
		if ((mediaURIAdditional == null) || (mediaURIAdditional.isEmpty())) {
			return null;
		}
		return new JSONArray(mediaURIAdditional);
	}

	private String checkNotNullOrMissing(String value) {
		if ((value == null) || ((value = value.trim()).length() == 0)) {
			throw new NullPointerException(
					"One of Additional Media URI is missing");
		}
		return value;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

}
