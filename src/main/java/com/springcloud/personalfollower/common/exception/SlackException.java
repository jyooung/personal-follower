package com.springcloud.personalfollower.common.exception;

public class SlackException extends RuntimeException {
    public SlackException(String message) {
        super(message);
    }
}
