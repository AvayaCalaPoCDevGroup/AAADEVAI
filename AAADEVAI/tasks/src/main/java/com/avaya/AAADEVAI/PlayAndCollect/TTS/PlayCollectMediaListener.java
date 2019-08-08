package com.avaya.AAADEVAI.PlayAndCollect.TTS;

import com.avaya.AAADEVAI.Util.MakingPost;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.media.DigitCollectorOperationCause;
import com.avaya.collaboration.call.media.MediaListener;
import com.avaya.collaboration.call.media.PlayOperationCause;
import com.avaya.collaboration.call.media.RecordOperationCause;
import com.avaya.collaboration.call.media.SendDigitsOperationCause;
import com.avaya.collaboration.ssl.util.SSLUtilityException;
import com.avaya.workflow.logger.Logger;
import com.avaya.workflow.logger.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

public class PlayCollectMediaListener implements MediaListener {
	private final Logger log = LoggerFactory
			.getLogger(PlayCollectMediaListener.class);
	private Call call;
	private UUID uuid;
	private PlayAndCollectExecution nodeInstance;

	public PlayCollectMediaListener(Call call) {
		this.call = call;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public void setNodeInstance(PlayAndCollectExecution nodeInstance) {
		this.nodeInstance = nodeInstance;
	}

	public void digitsCollected(UUID requestId, String digits,
			DigitCollectorOperationCause cause) {
		log.info("AAADEVAI digitsCollected PlayCollectMediaListener");
		try {
			MakingPost.makingDelete();
		} catch (IOException e1) {
			log.info("AAADEVAI MakeDelete Error: " + e1.toString());
		} catch (SSLUtilityException e1) {
			log.info("AAADEVAI MakeDelete Error: " + e1.toString());
		}
		if (!this.uuid.equals(requestId)) {
			if (this.log.isFineEnabled()) {
				this.log.fine(requestId
						+ " message dropped because it was not " + this.uuid);
			}
			return;
		}
		if (this.log.isFineEnabled()) {
			this.log.fine("Notification received for UUID: " + requestId
					+ " ucid: " + this.call.getUCID() + " received digits "
					+ digits + " cause: " + cause);
		}
		if ((digits == null) || (digits.length() == 0)) {
			if (this.log.isFineEnabled()) {
				this.log.fine(this.call.getId() + " No selection was made. ");
			}
		}
		this.nodeInstance.resumeTask(this.call, digits, cause);
	}

	public void playCompleted(UUID requestId, PlayOperationCause cause) {
		log.info("AAADEVAI playCompleted PlayCollectMediaListener");
		try {
			MakingPost.makingDelete();
		} catch (IOException e1) {
			log.info("AAADEVAI MakeDelete Error: " + e1.toString());
		} catch (SSLUtilityException e1) {
			log.info("AAADEVAI MakeDelete Error: " + e1.toString());
		}
	}

	public void sendDigitsCompleted(UUID arg0, SendDigitsOperationCause arg1) {
	}

	public void recordCompleted(UUID requestId, RecordOperationCause cause) {
	}
}
