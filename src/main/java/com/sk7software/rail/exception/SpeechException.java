package com.sk7software.rail.exception;

public abstract class SpeechException extends Exception {
    private String spokenMessage;

    public SpeechException() {}

    public SpeechException(String message) {
        super();
        this.spokenMessage = message;
    }

    public String getSpokenMessage() {
        return spokenMessage;
    }
}
