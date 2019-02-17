package com.aidn5.hypeapp.hypixelapi.exception;


import android.content.Context;

import com.aidn5.hypeapp.R;

import static com.aidn5.hypeapp.hypixelapi.exception.ExceptionTypes.API_Error;
import static com.aidn5.hypeapp.hypixelapi.exception.ExceptionTypes.Internet;
import static com.aidn5.hypeapp.hypixelapi.exception.ExceptionTypes.NoHypixelApi;
import static com.aidn5.hypeapp.hypixelapi.exception.ExceptionTypes.NoUserUUID;
import static com.aidn5.hypeapp.hypixelapi.exception.ExceptionTypes.Parse;
import static com.aidn5.hypeapp.hypixelapi.exception.ExceptionTypes.Throttle;

public class HypixelApiException extends Throwable {
	private ExceptionTypes errorType = ExceptionTypes.Unknown;
	private String cause = null;

	public HypixelApiException(ExceptionTypes exceptionType) {
		super();
		this.errorType = exceptionType;
	}

	HypixelApiException(String cause) {
		super(cause);
	}

	public HypixelApiException(ExceptionTypes exceptionType, Throwable throwable) {
		super(throwable);
		this.errorType = exceptionType;
	}

	public String getErrorTypeMessage(Context context) {
		if (errorType == NoHypixelApi) return context.getString(R.string.no_api);
		if (errorType == NoUserUUID) return context.getString(R.string.no_user_uuid);
		if (errorType == Throttle) return context.getString(R.string.hypixel_api_throttled);
		if (errorType == API_Error) return context.getString(R.string.hypixel_api_invalid);
		if (errorType == Parse) return context.getString(R.string.hypixel_api_cannot_parse);
		if (errorType == Internet) return context.getString(R.string.error_internet);

		return context.getString(R.string.something_went_wrong);
	}

	@Override
	public String getMessage() {
		return cause != null ? cause : super.getMessage();
	}

	public ExceptionTypes getErrorType() {
		return errorType;
	}
}
