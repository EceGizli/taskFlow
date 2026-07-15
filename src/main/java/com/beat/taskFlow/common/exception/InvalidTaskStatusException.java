package com.beat.taskFlow.common.exception;

public class InvalidTaskStatusException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidTaskStatusException(String message) {
        super(message);
    }
}