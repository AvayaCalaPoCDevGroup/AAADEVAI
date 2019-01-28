package com.avaya.AAADEVAI;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import com.avaya.app.entity.NodeInstance;
import com.avaya.collaboration.call.Call;
import com.avaya.collaboration.call.CallFactory;
import com.avaya.collaboration.call.Participant;
import com.avaya.collaboration.call.media.MediaFactory;
import com.avaya.collaboration.call.media.MediaService;
import com.avaya.collaboration.call.media.PlayItem;
import com.avaya.workflow.logger.Logger;
import com.avaya.workflow.logger.LoggerFactory;
import com.roobroo.bpm.im.InstanceManager;

public class WFMediaUtil
{
  private static final Logger log = LoggerFactory.getLogger(WFMediaUtil.class);
  public static final int DEFAULT__DURATION = 30000;
  public static final boolean DEFAULT_INTERRUPTIBILITY = true;
  public static final int ITERATE_COUNT = 1;
  public static final String CALLING_LEG = "calling";
  public static final String CALLED_LEG = "called";
  private static final String DELIMETER = ":";
  private static final String DOMAIN_DELIMETER = "@";
  
  public void playAnnouncement(String callID, String fileURI, boolean interruptibility, int duration, int iterateCount, boolean forEver, WFMediaListener mediaListener, boolean isReconstruction, String handle, String participant)
    throws Exception
  {
    Call call = getCall(callID);
    if (call == null)
    {
      log.error("call object not found for callID: " + callID);
      throw new RuntimeException("Invalid callID: " + callID);
    }
    mediaListener.setCall(call);
    try
    {
      MediaService mediaService = MediaFactory.createMediaService();
      
      PlayItem playItem = MediaFactory.createPlayItem().setSource(new String[] { fileURI }).setInterruptible(interruptibility);
      if (duration > 0) {
        playItem.setDuration(duration);
      }
      if (forEver) {
        playItem.setIterateCount(0);
      } else {
        playItem.setIterateCount(iterateCount);
      }
      if (isReconstruction) {
        mediaListener.getUuidMap().clear();
      }
      Participant callingParty = call.getCallingParty();
      Participant answeringParty = call.getAnsweringParty();
      String participantHandle;
      if (!StringUtils.isBlank(handle))
      {
        if (handle.contains(":"))
        {
          String[] arr = handle.split(":");
          handle = arr[1];
        }
        participantHandle = null;
        if (handle.contains("@"))
        {
          String[] arr = handle.split("@");
          participantHandle = arr[0];
        }
        else
        {
          participantHandle = handle;
        }
        List<Participant> activeParties = call.getActiveParties();
        for (Participant activeParticipant : activeParties) {
          if (participantHandle.equals(activeParticipant.getHandle()))
          {
            UUID uuID = mediaService.play(activeParticipant, playItem, mediaListener);
            if (uuID == null) {
              break;
            }
            mediaListener.addUUIDToMap(uuID, activeParticipant);
            if (!log.isFineEnabled()) {
              break;
            }
            log.fine("PlayAnnc : invoked on handle --> active party: " + activeParticipant + " succeeded. ucid: " + call.getUCID() + " and the uuid generated is: " + uuID);
            
            log.fine("Adding uuid: " + uuID + " and participant: " + activeParticipant); break;
          }
        }
      }
      else if (participant.equalsIgnoreCase("called"))
      {
        log.fine("its a one party outbound call, playing on called leg...");
        if (call.getAnsweringParty() != null)
        {
          if (log.isFineEnabled()) {
            log.fine("make-1 party outbound call - playAnnouncement for answering party:" + call.getAnsweringParty().getAddress());
          }
          UUID uuID = mediaService.play(answeringParty, playItem, mediaListener);
          if (uuID != null)
          {
            mediaListener.addUUIDToMap(uuID, answeringParty);
            if (log.isFineEnabled()) {
              log.fine("Adding uuid: " + uuID + " and answering party: " + answeringParty);
            }
          }
        }
      }
      else if (participant.equalsIgnoreCase("calling"))
      {
        log.fine("Its a call intercept, playing on calling leg");
        if (call.getCallingParty() != null)
        {
          if (log.isFineEnabled()) {
            log.fine("playAnnouncement for callingparty:" + call.getCallingParty().getAddress());
          }
          UUID uuID = mediaService.play(callingParty, playItem, mediaListener);
          if (uuID != null)
          {
            mediaListener.addUUIDToMap(uuID, callingParty);
            if (log.isFineEnabled()) {
              log.fine("Adding uuid: " + uuID + " and calling party: " + callingParty);
            }
          }
        }
      }
      else
      {
        boolean isPlayed = false;
        log.fine("Its a two party call, playing on call id");
        
        UUID uuID = mediaService.play(call, playItem, mediaListener);
        isPlayed = true;
        if (uuID != null)
        {
          mediaListener.addUUIDToCallIdMap(uuID, callID);
          if (log.isFineEnabled()) {
            log.fine("Adding uuid: " + uuID + " and callId: " + callID);
          }
        }
        if (!isPlayed)
        {
          log.error("Error while Playing Announcement on callid");
          throw new RuntimeException("Error while Playing Announcement on callid");
        }
      }
    }
    catch (Exception e)
    {
      log.error("Error while Playing Announcement." + e);
      throw e;
    }
  }
  
  private Call getCall(String callID)
  {
    try
    {
      return CallFactory.getCall(callID);
    }
    catch (Exception e)
    {
      log.error("Error in getting call object. Invalid callID:" + callID, e);
    }
    return null;
  }
  
  public static void resumeWFInstance(NodeInstance nodeInstance, String cause)
  {
    if (nodeInstance == null)
    {
      log.fine("nodeInstance is null. cannot resume the task...");
      return;
    }
    try
    {
      InstanceManager instanceManager = nodeInstance.instance().getInstanceManager();
      if (instanceManager == null)
      {
        log.error("InstanceManager is null. Cannot resume the workflow task:" + nodeInstance.getNode().getName());
        
        return;
      }
      if (log.isFineEnabled()) {
        log.fine("resuming nodeInstance:" + nodeInstance.getNode().getName());
      }
      JSONObject output = new JSONObject();
      output.put("cause", cause);
      output.put("status", "FAILED");
      instanceManager.resumeInstance(String.valueOf(nodeInstance.instance().instanceId()), nodeInstance.getNode().getName(), output.toString());
      
      log.fine("NodeInstance resumed...");
    }
    catch (Exception e)
    {
      log.error("Failed to resume the nodeInstance:" + nodeInstance.getNode().getName(), e);
    }
  }
  
  public static String getMediaCStoreURI(String nameSpace, String contentGroup, String contentId, String locale)
  {
    String cStoreURI = null;
    if ((locale == null) || (locale.isEmpty())) {
      cStoreURI = "cstore://" + contentId + "?ns=" + nameSpace + "&cg=" + contentGroup;
    } else {
      cStoreURI = "cstore://" + contentId + "?ns=" + nameSpace + "&cg=" + contentGroup + "/" + locale + "";
    }
    return cStoreURI;
  }
  
  public static String buildURIWithLocale(String mediaUri, String locale)
  {
    if ((mediaUri == null) || (mediaUri.isEmpty())) {
      return null;
    }
    if ((locale == null) || (locale.isEmpty())) {
      return mediaUri;
    }
    if (mediaUri.contains("cstore"))
    {
      String[] parts = mediaUri.split("cg=");
      if (parts.length != 2) {
        return mediaUri;
      }
      String beforeCg = parts[0];
      String afterCg = parts[1];
      if (afterCg.contains("/"))
      {
        int index = afterCg.indexOf("/");
        mediaUri = beforeCg + "cg=" + afterCg.substring(0, index) + "/" + locale;
      }
      else
      {
        mediaUri = mediaUri + "/" + locale;
      }
      return mediaUri;
    }
    return mediaUri;
  }
  
  private String retreiveHandle(String phoneNumber)
  {
    String[] handleArr = phoneNumber.split("\\@");
    return handleArr[0];
  }
}