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
import com.avaya.workflow.logger.Logger;
import com.avaya.workflow.logger.LoggerFactory;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

public class AnnounceAndBlockMediaListener
  implements MediaListener
{
  public AnnounceAndBlockMediaListener()
  {
    this.uuidMap = new ConcurrentHashMap<UUID, Participant>();
  }
  
  private final Logger log = LoggerFactory.getLogger(AnnounceAndBlockMediaListener.class);
  private Call call;
  private Map<UUID, Participant> uuidMap;
  private NodeInstance nodeInstance;
  
  public Map<UUID, Participant> getUuidMap()
  {
    return this.uuidMap;
  }
  
  public void addUUIDToMap(UUID uuid, Participant participant)
  {
    this.uuidMap.put(uuid, participant);
  }
  
  public void removeUUIDFromMap(UUID uuid)
  {
    this.uuidMap.remove(uuid);
  }
  
  public Call getCall()
  {
    return this.call;
  }
  
  public void setCall(Call call)
  {
    this.call = call;
  }
  
  public NodeInstance getNodeInstance()
  {
    return this.nodeInstance;
  }
  
  public void setNodeInstance(NodeInstance nodeInstance)
  {
    this.nodeInstance = nodeInstance;
  }
  
  public void digitsCollected(UUID requestId, String digits, DigitCollectorOperationCause cause) {}
  
  public void playCompleted(UUID requestId, PlayOperationCause cause)
  {
    if (this.log.isFineEnabled())
    {
      this.log.fine("Play completed callback invoked for uuid " + requestId + " call Id " + this.call.getUCID() + " and appId " + this.call
        .getId() + "for Announce and block");
      this.log.fine("printing uuid map values ");
      for (UUID uuid : this.uuidMap.keySet())
      {
        Participant Participant = (Participant)this.uuidMap.get(uuid);
        this.log.fine("UUID :: " + uuid + " Participant is " + Participant.getHandle());
      }
    }
    if (this.uuidMap.containsKey(requestId))
    {
      removeUUIDFromMap(requestId);
      if (this.uuidMap.isEmpty())
      {
        if (this.log.isFineEnabled()) {
          this.log.fine("All media notifications received for callID against participants with callId:" + this.call
            .getUCID() + " Now producing media family event so that the flow could resume...");
        }
        EventMetaData meta = EventingFactory.createEventMetaData();
        
        meta.addValue("ucid", this.call.getUCID());
        meta.addValue("uuid", requestId.toString());
        
        JSONObject output = new JSONObject();
        try
        {
          output.put("ucid", this.call.getUCID());
          output.put("status", cause);
        }
        catch (Exception e)
        {
          this.log.error("Json error in PA against particpants => ", e);
        }
        EventProducer producer = EventingFactory.createEventProducer("Media", "MEDIA_PROCESSED", meta, output
          .toString(), "");
        producer.publish();
        
        this.call.drop();
      }
    }
    else
    {
      if (this.log.isFinestEnabled()) {
        this.log.finest(requestId + " message dropped because it was not for me. cause:" + cause);
      }
      return;
    }
  }
  
  public void sendDigitsCompleted(UUID arg0, SendDigitsOperationCause arg1) {}
  
  public void recordCompleted(UUID requestId, RecordOperationCause cause) {}
}
