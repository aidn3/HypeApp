package com.aidn5.hypeapp.hypixelapi;

public class HypixelReplay {
    public final boolean isSuccess;

    public final Object value;
    public final String fullResponse;

    public final HypixelApiException exception;


    HypixelReplay(HypixelApiException e, String fullResponse) {
        exception = e;
        this.fullResponse = fullResponse;

        isSuccess = false;
        value = null;

    }

    HypixelReplay(Object value, String fullResponse) {
        this.value = value;
        this.fullResponse = fullResponse;

        isSuccess = true;
        exception = null;
    }
}
