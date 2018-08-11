package com.syswin.temail.cdtp.dispatcher.request.controller;

import com.syswin.temail.cdtp.dispatcher.request.application.CDTPWrapper;
import com.syswin.temail.cdtp.dispatcher.request.application.PackageDispatcher;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPBody;
import com.syswin.temail.cdtp.dispatcher.request.entity.CDTPPackage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class DispatchController {

    private final CDTPWrapper cdtpWrapper = new CDTPWrapper();
    private final PackageDispatcher packageDispatcher;

    @Autowired
    public DispatchController(PackageDispatcher packageDispatcher) {
        this.packageDispatcher = packageDispatcher;
    }

    @PostMapping(value = "/dispatch")
    public ResponseEntity<CDTPPackage<String>> dispatch(@RequestBody CDTPPackage<CDTPBody> cdtpPackage) {
        ResponseEntity<String> responseEntity = packageDispatcher.dispatch(cdtpPackage);
        return new ResponseEntity<>(cdtpWrapper.adapt(cdtpPackage, responseEntity.getBody()), responseEntity.getStatusCode());
    }
}
