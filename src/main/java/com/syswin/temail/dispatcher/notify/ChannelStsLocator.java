package com.syswin.temail.dispatcher.notify;

import com.syswin.temail.dispatcher.notify.entity.TemailAccountLocation;
import java.util.List;

public interface ChannelStsLocator {


  public List<TemailAccountLocation> locate(String temail) ;

}
