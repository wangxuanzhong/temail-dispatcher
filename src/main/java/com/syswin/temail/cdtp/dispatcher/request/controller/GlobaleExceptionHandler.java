package com.syswin.temail.cdtp.dispatcher.request.controller;

import com.syswin.temail.cdtp.dispatcher.exceptions.TeMailUnsupportedCommandException;
import com.syswin.temail.cdtp.dispatcher.request.application.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@ControllerAdvice
public class GlobaleExceptionHandler {
    @ExceptionHandler(TeMailUnsupportedCommandException.class)
    @ResponseStatus(BAD_REQUEST)
    public Response<String> handleBadRequest(TeMailUnsupportedCommandException ex) {
        log.error("无效的请求", ex);
        return Response.failed(BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    public Response<String> handleResourceOccupiedException(Exception ex) {
        log.error("服务器请求异常", ex);
        return Response.failed(INTERNAL_SERVER_ERROR, ex.getMessage());
    }

}
