package com.syswin.temail.dispatcher.valid;

import com.syswin.temail.dispatcher.valid.match.PacketValidType;
import com.syswin.temail.dispatcher.valid.params.ValidParamsCommonFactory;
import com.syswin.temail.dispatcher.valid.params.ValidParamsCrossGroupFactory;
import com.syswin.temail.dispatcher.valid.params.ValidParamsCrossSingleFactory;
import com.syswin.temail.dispatcher.valid.params.ValidParamsFactory;
import com.syswin.temail.dispatcher.valid.params.ValidParamsSkipFactory;
import com.syswin.temail.dispatcher.valid.params.ValidParamsTopicFactory;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactoryBuilder {

  private final Map<PacketValidType, ValidParamsFactory> cache = new ConcurrentHashMap<>();

  public Optional<ValidParamsFactory> build(PacketValidType packetValidType){
    switch (packetValidType){
      case crossSingleSignValid:{
        return Optional.ofNullable(
            cache.computeIfAbsent(PacketValidType.crossSingleSignValid, t -> new ValidParamsCrossSingleFactory()));
      }
      case crossGroupsignValid:{
        return Optional.ofNullable(
            cache.computeIfAbsent(PacketValidType.crossGroupsignValid, t -> new ValidParamsCrossGroupFactory()));
      }
      case crossTopicSignValid:{
        return Optional.ofNullable(
            cache.computeIfAbsent(PacketValidType.crossTopicSignValid, t -> new ValidParamsTopicFactory()));
      }
      case skipSignValid:{
        return Optional.ofNullable(
            cache.computeIfAbsent(PacketValidType.skipSignValid, t -> new ValidParamsSkipFactory()));
      }
      case commonSignValid:{
        return Optional.ofNullable(
            cache.computeIfAbsent(PacketValidType.commonSignValid, t -> new ValidParamsCommonFactory()));
      }
      default:{
        return Optional.empty();
      }
    }
  }
}
