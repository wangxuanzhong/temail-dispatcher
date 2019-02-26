package com.syswin.temail.dispatcher.valid.match;

import lombok.Getter;

@Getter
public enum PacketValidType {

  crossSingleSignValid("crossSingleSignValid","/cross/verify/single"),
  crossGroupsignValid("crossGroupsignValid",""),
  crossTopicSignValid("crossTopicSignValid","/cross/verify/Topic"),
  commonSignValid("commonSignValid","/verify"),
  skipSignValid("skipSignValid","");

  private String mapKeycode;

  private String authUri;

  private PacketValidType(String mapKeycode, String authUri) {
    this.mapKeycode = mapKeycode;
    this.authUri = authUri;
  }

  public static PacketValidTypeAndMatchPair defaultValidType(){
    return new PacketValidTypeAndMatchPair(commonSignValid, PacketValidMatchType.fullCommandMatch);
  }

}
