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


package com.aidn5.hypeapp.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.aidn5.hypeapp.ServicesProvider;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements ServiceConnection {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		startService(new Intent(getApplicationContext(), ServicesProvider.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
	}

	protected final void bindToLocalService() {
		Intent intent = new Intent(this, ServicesProvider.class);
		bindService(intent, this, Context.BIND_IMPORTANT);
	}

	@Override
	public void onServiceConnected(ComponentName className, IBinder service) {
		ServicesProvider.LocalBinder binder = (ServicesProvider.LocalBinder) service;
		onServiceConnected_(binder.getService());
	}

	@Override
	public void onServiceDisconnected(ComponentName componentName) {
		// No need to inform about disconnect
		// since it will never happen and we only need one-way method #onServiceConnected_
	}

	protected void onServiceConnected_(ServicesProvider servicesProvider) {
		//Override this to get the instance of the ServicesProvider
	}
}
