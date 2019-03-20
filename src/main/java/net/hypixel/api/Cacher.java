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

package net.hypixel.api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.aidn5.hypeapp.services.AbstractedCacher;

public class Cacher extends AbstractedCacher {

	public Cacher(@NonNull Context context) {
		super(context);
	}

	public CachedData get(Class clazz, Object... values) {
		return getByKeyFromDB(generateKey(clazz, values));
	}

	public void save(Class clazz, String data, Object... values) {
		insertIntoDB(generateKey(clazz, values), data);
	}

	private String generateKey(Class clazz, Object... values) {
		StringBuilder data = new StringBuilder(clazz.getSimpleName());

		for (int i = 0; i < values.length - 1; i += 2) {
			data.append("_").append(values[i]).append("_").append(values[i + 1]);
		}

		return data.toString();
	}
}