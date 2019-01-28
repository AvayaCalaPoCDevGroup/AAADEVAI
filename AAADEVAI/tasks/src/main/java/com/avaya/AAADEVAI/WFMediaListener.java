package com.avaya.AAADEVAI;

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

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

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
		log.info("AAADEVAI PLAY COMPLETED");
		try {
			MakingPost.makingDelete();
		} catch (IOException e1) {
			log.info("AAADEVAI MakeDelete Error: " + e1.toString());
		} catch (SSLUtilityException e1) {
			log.info("AAADEVAI MakeDelete Error: " + e1.toString());
		}

		if (this.uuidMap.containsKey(requestId)) {
			removeUUIDFromMap(requestId);
			if (this.uuidMap.isEmpty()) {
				if (this.log.isFineEnabled()) {
					this.log.fine("All media notifications received for callID against participants:"
							+ this.call.getUCID()
							+ " Now producing media family event so that the flow could resume...");
				}
				EventMetaData meta = EventingFactory.createEventMetaData();

				meta.addValue("ucid", this.call.getUCID());

				JSONObject output = new JSONObject();
				try {
					output.put("ucid", this.call.getUCID());
					output.put("status", cause);
				} catch (Exception e) {
					this.log.error("Json error in PA against particpants => ",
							e);
				}
				EventProducer producer = EventingFactory
						.createEventProducer("Media", "MEDIA_PROCESSED", meta,
								output.toString(), "");

				producer.publish();
			}
		} else if (this.uuidCallIdMap.containsKey(requestId)) {
			removeUUIDFromCallIdMap(requestId);
			removeUUIDFromMap(requestId);
			if (this.uuidCallIdMap.isEmpty()) {
				if (this.log.isFineEnabled()) {
					this.log.fine("All media notifications received for callID:"
							+ this.call.getUCID()
							+ " Now producing media family event so that the flow could resume...");
				}
				EventMetaData meta = EventingFactory.createEventMetaData();

				meta.addValue("ucid", this.call.getUCID());

				JSONObject output = new JSONObject();
				try {
					output.put("ucid", this.call.getUCID());
					output.put("status", cause);
				} catch (Exception e) {
					this.log.error("Json error in PA => ", e);
				}
				EventProducer producer = EventingFactory
						.createEventProducer("Media", "MEDIA_PROCESSED", meta,
								output.toString(), "");

				producer.publish();
			}
		} else {
			if (this.log.isFinestEnabled()) {
				this.log.finest(requestId
						+ " message dropped because it was not for me. cause:"
						+ cause);
			}
			return;
		}

	}

	public void sendDigitsCompleted(UUID requestId,
			SendDigitsOperationCause sendDigitsCause) {

	}

	public void recordCompleted(UUID requestId, RecordOperationCause cause) {

	}

	public Map<UUID, String> getUuidCallMap() {
		return this.uuidCallIdMap;
	}

	public void setUuidCallMap(Map<UUID, String> uuidCallMap) {
		this.uuidCallIdMap = uuidCallMap;
	}
}
