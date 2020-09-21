package com.lureb.websocket.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Bad request")
public class BadRequest extends RuntimeException {
    public BadRequest(String message) {
        super(message);
    }
}
