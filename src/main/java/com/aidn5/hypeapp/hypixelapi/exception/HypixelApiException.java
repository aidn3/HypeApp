/*
 * MIT License
 *
 * Copyright (c) 2019 aidn5
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


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
