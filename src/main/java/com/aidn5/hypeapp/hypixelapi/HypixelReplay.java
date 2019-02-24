package com.aidn5.hypeapp.hypixelapi;

import com.aidn5.hypeapp.hypixelapi.exception.HypixelApiException;

public final class HypixelReplay {
	public final boolean isSuccess;

	public final Object value;
	public final String fullResponse;

	public final HypixelApiException exception;
	private final long dataOld;


	HypixelReplay(HypixelApiException e, String fullResponse) {
		exception = e;
		this.fullResponse = fullResponse;

		isSuccess = false;
		value = null;
		dataOld = -1;
	}

	public HypixelReplay(Object value, String fullResponse, long dataOld) {
		this.value = value;
		this.fullResponse = fullResponse;

		isSuccess = true;
		exception = null;
		this.dataOld = dataOld;
	}

	public boolean isDataFromCache() {
		return dataOld > 0;
	}
}
