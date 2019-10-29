package com.avaya.AAADEVAI;

import com.avaya.workflow.env.CommonFactory;

public class ServiceAttributeManager
{
  public static String getServiceAttribute(String serviceAttribute)
  {
    return CommonFactory.getEnvironment().getServiceAttribute(serviceAttribute, false);
  }
}