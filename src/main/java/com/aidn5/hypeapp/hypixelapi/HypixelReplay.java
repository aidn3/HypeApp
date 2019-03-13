/*
 * MIT License
 *
 * Copyright (c) [2019] [aidn5]
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
