package com.aidn5.myapplication;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.aidn5.hypeapp.notifiers.NotifierFactory;
import com.aidn5.hypeapp.notifiers.friends.FriendRemovalEvent;
import com.aidn5.hypeapp.services.NotificationFactory;
import com.snappydb.DBFactory;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public final class FriendRemovalEventTest {
	private final FriendRemovalEvent friendRemovalEvent;
	private Method getFriendsUUIDsFromNet;
	private Method cacheFriendsUUIDs;

	public FriendRemovalEventTest() throws Exception {
		friendRemovalEvent = new FriendRemovalEvent(getContext(), DBFactory.open(getContext()));

		getFriendsUUIDsFromNet = friendRemovalEvent.getClass().getDeclaredMethod("getFriendsUUIDsFromNet");
		getFriendsUUIDsFromNet.setAccessible(true);

		cacheFriendsUUIDs = friendRemovalEvent.getClass().getDeclaredMethod("cacheFriendsUUIDs", String[].class);
		cacheFriendsUUIDs.setAccessible(true);
	}

	/**
	 * Test the notifications system of {@link FriendRemovalEvent}
	 * by adding a random-user to the cache and call the main method, so it can detect the random-user
	 * and consider it and to send Notifications. <b>A Notifications must be send to the device on success!</b>
	 * <p>
	 * - Get the Up-To-Date friends list
	 * - Clone the list, add random friend ans save it
	 * - Run the main method {@link FriendRemovalEvent#doLoop()}
	 *
	 * @throws Exception (Ignore all try/cache blocks since the main purpose is to test and detect errors)
	 */
	@Test
	public void checkFriendsRemoval() throws Exception {
		String[] friendList1 = new String[]{"2f4a83f49a0f4b31b25d4f8ab6488a9a"};

		//Get the Up-To-Date friends list
		String[] upToDateFriends = (String[]) getFriendsUUIDsFromNet.invoke(friendRemovalEvent);
		Log.v(this.getClass().getSimpleName(), Arrays.toString(upToDateFriends));
		Assert.assertNotNull(upToDateFriends);

		// merge both
		String[] toCacheList = concatenate(upToDateFriends.clone(), friendList1);
		Assert.assertEquals(upToDateFriends.length + friendList1.length, toCacheList.length);

		cacheFriendsUUIDs.invoke(friendRemovalEvent, new Object[]{toCacheList});

		friendRemovalEvent.doLoop();
		friendRemovalEvent.showNotifications();
	}

	@Test
	public void testNotifications() throws Exception {
		Field notificationFactoryField = NotifierFactory.class.getDeclaredField("notificationFactory");
		notificationFactoryField.setAccessible(true);
		NotificationFactory notificationFactory = (NotificationFactory) notificationFactoryField.get(friendRemovalEvent);
		notificationFactory.notify("test", "123");
		notificationFactory.showAll();
	}

	private Context getContext() {
		return InstrumentationRegistry.getTargetContext();
	}

	private String[] concatenate(String[] first, String[] second) {
		List<String> both = new ArrayList<>(first.length + second.length);
		Collections.addAll(both, first);
		Collections.addAll(both, second);
		return both.toArray(new String[0]);
	}
}
