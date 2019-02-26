package com.syswin.temail.dispatcher.valid;

import com.syswin.temail.dispatcher.DispatcherProperties;
import com.syswin.temail.dispatcher.valid.match.PacketValidMatchType;
import com.syswin.temail.dispatcher.valid.match.PacketValidType;
import com.syswin.temail.dispatcher.valid.match.PacketValidTypeAndMatchPair;
import com.syswin.temail.dispatcher.valid.params.ValidParams;
import com.syswin.temail.dispatcher.valid.params.ValidParamsFactory;
import com.syswin.temail.ps.common.entity.CDTPPacket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class PacketValidJudge {

  private final int commandLength = 4;

  private final Comparator<PacketValidTypeAndMatchPair> matchTypeComparator = new Comparator<PacketValidTypeAndMatchPair>() {
    @Override
    public int compare(PacketValidTypeAndMatchPair o1, PacketValidTypeAndMatchPair o2) {
      if (o1.getPacketValidMatchType().ordinal() < o2.getPacketValidMatchType().ordinal()) {
        return -1;
      } else if (o1.getPacketValidMatchType().ordinal() > o2.getPacketValidMatchType().ordinal()) {
        return 1;
      } else {
        return o1.getPacketValidType().ordinal() <= o2.getPacketValidType().ordinal() ? -1 : 1;
      }
    }
  };

  private SingletonFactoryBuilder singletonFactoryBuilder = new SingletonFactoryBuilder();

  private DispatcherProperties dispatcherProperties;

  public PacketValidJudge(DispatcherProperties dispatcherProperties) {
    this.dispatcherProperties = dispatcherProperties;
  }

  public Optional<ValidParams> buildParams(CDTPPacket cdtpPacket, Function<CDTPPacket, String> signExtract) {
    PacketValidType packetValidType = packetValidType(cdtpPacket.getCommandSpace(), cdtpPacket.getCommand());
    log.info("By packetId: {}, commandSpace-command: {} the valid type is: {} .", cdtpPacket.getHeader().getPacketId(),
        buildToFullCmsCmStr(cdtpPacket.getCommandSpace(), cdtpPacket.getCommand()), packetValidType.toString());
    Optional<ValidParamsFactory> validParamsFactory = singletonFactoryBuilder.build(packetValidType);
    return validParamsFactory.isPresent() ? validParamsFactory.get().buildParams(cdtpPacket, signExtract)
        : Optional.empty();
  }

  public PacketValidType packetValidType(short commandSpace, short command) {
    List<PacketValidTypeAndMatchPair> validTypeAndMatchLevelPairs = matchedPairs(commandSpace, command);
    return bestValidType(validTypeAndMatchLevelPairs);
  }

  public List<PacketValidTypeAndMatchPair> matchedPairs(short commandSpace, short command) {
    List<PacketValidTypeAndMatchPair> result = new ArrayList<>();
    for (PacketValidType packetValidType : PacketValidType.values()) {
      extractPair(result, packetValidType, commandSpace, command);
    }
    if (result.isEmpty()) {
      result.add(PacketValidType.defaultValidType());
    }
    return result;
  }

  private PacketValidType bestValidType(List<PacketValidTypeAndMatchPair> validTypeAndMatchLevelPairs) {
    if (validTypeAndMatchLevelPairs.size() == 1) {
      return validTypeAndMatchLevelPairs.get(0).getPacketValidType();
    } else {
      Collections.sort(validTypeAndMatchLevelPairs, matchTypeComparator);
      return validTypeAndMatchLevelPairs.get(0).getPacketValidType();
    }
  }

  private void extractPair(List<PacketValidTypeAndMatchPair> result, PacketValidType packetValidType,
      short commandSpace, short command) {
    List<String> cmscm = this.dispatcherProperties.getValidStrategy().get(packetValidType.getMapKeycode());
    if (cmscm == null || cmscm.isEmpty()) {
      return;
    }
    if (cmscm.contains(buildToFullCmsCmStr(commandSpace, command).toUpperCase()) || cmscm
        .contains(buildToFullCmsCmStr(commandSpace, command).toLowerCase())) {
      result.add(new PacketValidTypeAndMatchPair(packetValidType, PacketValidMatchType.fullCommandMatch));
    }

    if (cmscm.contains(buildToHalfCmscmStr(commandSpace, command).toUpperCase()) || cmscm
        .contains(buildToHalfCmscmStr(commandSpace, command).toLowerCase())) {
      result.add(new PacketValidTypeAndMatchPair(packetValidType, PacketValidMatchType.halfCommandMatch));
    }

    if (cmscm.contains(buildToFullRexExpStr(commandSpace, command).toUpperCase()) || cmscm
        .contains(buildToFullRexExpStr(commandSpace, command).toLowerCase())) {
      result.add(new PacketValidTypeAndMatchPair(packetValidType, PacketValidMatchType.fullRegExpMatch));
    }
  }

  private String buildToFullCmsCmStr(short commandSpace, short command) {
    return StringUtils.leftPad(Integer.toHexString(commandSpace), commandLength, "0") + "-" +
        StringUtils.leftPad(Integer.toHexString(command), commandLength, "0");
  }

  private String buildToHalfCmscmStr(short commandSpace, short command) {
    return StringUtils.leftPad(Integer.toHexString(commandSpace), commandLength, "0") + "-*";
  }

  private String buildToFullRexExpStr(short commandSpace, short command) {
    return "*-*";
  }

}
