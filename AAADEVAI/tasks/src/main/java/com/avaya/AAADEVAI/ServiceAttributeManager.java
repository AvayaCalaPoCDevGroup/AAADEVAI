package com.avaya.AAADEVAI;

import com.avaya.workflow.env.CommonFactory;
import com.avaya.workflow.env.Environment;

public class ServiceAttributeManager
{
  public static String getServiceAttribute(String serviceAttribute)
  {
    return CommonFactory.getEnvironment().getServiceAttribute(serviceAttribute, false);
  }
}