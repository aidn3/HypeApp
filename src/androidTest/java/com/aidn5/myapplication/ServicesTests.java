package com.aidn5.myapplication;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.aidn5.hypeapp.services.NotificationFactory;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ServicesTests {
	@Test
	public void testNotificationsFactory() {
		NotificationFactory notificationFactory = new NotificationFactory(
				getContext(),
				this.getClass().getSimpleName(),
				this.getClass().getSimpleName());

		notificationFactory.notify("test", "123");
		notificationFactory.showAll();
	}

	private Context getContext() {
		return InstrumentationRegistry.getTargetContext();
	}
}
