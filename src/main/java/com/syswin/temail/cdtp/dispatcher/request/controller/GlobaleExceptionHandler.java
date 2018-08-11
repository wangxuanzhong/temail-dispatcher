package com.syswin.temail.cdtp.dispatcher.request.controller;

import com.syswin.temail.cdtp.dispatcher.receive.application.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@ControllerAdvice
public class GlobaleExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public Response<String> handleResourceOccupiedException(Exception ex) {
        log.error("服务器请求异常", ex);
        return Response.failed(INTERNAL_SERVER_ERROR, ex.getMessage());
    }

}
