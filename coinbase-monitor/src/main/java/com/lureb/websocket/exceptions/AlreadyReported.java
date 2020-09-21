package com.lureb.websocket.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.ALREADY_REPORTED, reason = "Subscription already reported")
public class AlreadyReported extends RuntimeException{
    public AlreadyReported(String message) {
        super(message);
    }
}
