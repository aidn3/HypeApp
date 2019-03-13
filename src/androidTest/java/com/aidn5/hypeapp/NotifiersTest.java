package com.aidn5.hypeapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.aidn5.hypeapp.notifiers.ForumsEventsNotifier;
import com.aidn5.hypeapp.notifiers.GuildEventsNotifier;
import com.aidn5.hypeapp.notifiers.NotifierFactory;
import com.aidn5.hypeapp.notifiers.app.AppAnnouncementsEvent;
import com.aidn5.hypeapp.notifiers.friends.FriendIgnChangeEvent;
import com.aidn5.hypeapp.notifiers.friends.FriendRemovalEvent;
import com.aidn5.hypeapp.services.DataManager;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;
import com.aidn5.hypeapp.services.NotificationFactory;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


@RunWith(AndroidJUnit4.class)
public class NotifiersTest {
	private final static String[] SAMPLE_UUID_1 = new String[]{"c65261c70d5a46f7ab786e7d189ec722", "f7bd2af3c71b426b88a12851e217256c", "fbf2582cb5904bd9a935ec3688c28ffc"};

	private Context context;
	private SharedPreferences preferences;
	private DataManager dataManager;
	private EventsSaver eventsSaver;

	private IgnProvider ignProvider;

	private Field notificationFactoryField;

	@Before
	public void setUp() throws Exception {
		// get Context
		context = InstrumentationRegistry.getTargetContext();
		Assert.assertNotNull(context);

		// Create data managers
		preferences = PreferenceManager.getDefaultSharedPreferences(context);

		dataManager = new DataManager(context);
		dataManager.removeAll();

		eventsSaver = new EventsSaver(context);
		eventsSaver.clearAllRows();

		ignProvider = new IgnProvider(context);
		ignProvider.clearDB();


		// Get private methods/variables and set their accessible to true
		notificationFactoryField = NotifierFactory.class.getDeclaredField("notificationFactory");
		notificationFactoryField.setAccessible(true);
	}

	@Test
	public void testNotificationsAbility() {
		NotificationFactory notificationFactory = new NotificationFactory(context, "test", "test");
		notificationFactory.notify("test", "message");
		Assert.assertEquals(notificationFactory.showAll(), 1);
	}

	@Test
	public void testAppAnnouncementEvent() throws Exception {
		AppAnnouncementsEvent appAnnouncementsEvent = new AppAnnouncementsEvent(context);

		Method doAnnouncements = appAnnouncementsEvent.getClass().getDeclaredMethod("doAnnouncements", EventsSaver.class, boolean.class, String.class);
		doAnnouncements.setAccessible(true);

		NotificationFactory notificationFactory = (NotificationFactory) notificationFactoryField.get(appAnnouncementsEvent);


		// run the event as it intended to be
		appAnnouncementsEvent.doLoop(dataManager, eventsSaver, ignProvider, preferences);

		////////// //////////  provide data to test the inner method and its handle ////////// //////////
		//empty array -> should just return with no error
		doAnnouncements.invoke(appAnnouncementsEvent, eventsSaver, true, "[]");

		// show one notification and return with no error
		doAnnouncements.invoke(appAnnouncementsEvent, eventsSaver, true, "[{\"message\":\"hellO World\",\"isImportant\":false}]");
		Assert.assertEquals(notificationFactory.showAll(), 1);

		try {
			// providing an JSONObject instead of JSONArray as string -> must throws JSONException on parse
			doAnnouncements.invoke(appAnnouncementsEvent, eventsSaver, true, "{}");
			Assert.fail();
		} catch (Exception ignored) {
		}
	}

	@Test
	public void testFriendIgnChangeEvent() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		FriendIgnChangeEvent friendIgnChangeEvent = new FriendIgnChangeEvent(context);

		////////// Run the event from it main method first. to check for Exception  //////////
		friendIgnChangeEvent.doLoop(dataManager, eventsSaver, ignProvider, preferences);


		////////// Test the array-splitting //////////
		Method splitArray = FriendIgnChangeEvent.class.getDeclaredMethod("splitArray", String[].class, int.class);
		splitArray.setAccessible(true);
		String[][] chunckedUuid = (String[][]) splitArray.invoke(friendIgnChangeEvent, SAMPLE_UUID_1, 2);
		Assert.assertEquals(chunckedUuid.length, 2);


		////////// Test the getter from hypixel network //////////
		Method getPlayersUUID = FriendIgnChangeEvent.class.getDeclaredMethod("getPlayersUUID", SharedPreferences.class);
		getPlayersUUID.setAccessible(true);
		String[] playersUUID = (String[]) getPlayersUUID.invoke(friendIgnChangeEvent, preferences);
		Assert.assertNotNull(playersUUID);


		//////////  look-up these SAMPLE_UUID_1, change one of them from the saved database,  //////////
		//////////                Look-up again and expect a notification                     //////////
		Method ignProviderPutUserIntoDB = IgnProvider.class.getDeclaredMethod("putUserIntoDB", String.class, String.class);
		ignProviderPutUserIntoDB.setAccessible(true);

		Method lookUpUUIDs = FriendIgnChangeEvent.class.getDeclaredMethod("lookUpUUIDs", EventsSaver.class, IgnProvider.class, String[].class);
		lookUpUUIDs.setAccessible(true);

		lookUpUUIDs.invoke(friendIgnChangeEvent, eventsSaver, ignProvider, SAMPLE_UUID_1);
		ignProviderPutUserIntoDB.invoke(ignProvider, SAMPLE_UUID_1[0], ".");
		ignProviderPutUserIntoDB.invoke(ignProvider, SAMPLE_UUID_1[1], ".");
		lookUpUUIDs.invoke(friendIgnChangeEvent, eventsSaver, ignProvider, SAMPLE_UUID_1);

		NotificationFactory notificationFactory = (NotificationFactory) notificationFactoryField.get(friendIgnChangeEvent);
		Assert.assertEquals(notificationFactory.showAll(), 2);
	}

	@Test
	public void testFriendRemovalEvent() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		FriendRemovalEvent friendRemovalEvent = new FriendRemovalEvent(context);
		NotificationFactory notificationFactory = (NotificationFactory) notificationFactoryField.get(friendRemovalEvent);

		Method compareFriendsListsAndSendNotification = FriendRemovalEvent.class.getDeclaredMethod("compareFriendsListsAndSendNotification", EventsSaver.class, IgnProvider.class, String[].class, String[].class);
		compareFriendsListsAndSendNotification.setAccessible(true);

		////////// Run the event from it main method first. to check for Exception  //////////
		friendRemovalEvent.doLoop(dataManager, eventsSaver, ignProvider, preferences);

		////////// Change one name to trigger the event as the old one isn't there anymore  //////////
		String[] sample_2 = SAMPLE_UUID_1.clone();
		sample_2[0] = sample_2[0] + "foo";
		compareFriendsListsAndSendNotification.invoke(friendRemovalEvent, eventsSaver, ignProvider, SAMPLE_UUID_1, sample_2);
		Assert.assertEquals(1, notificationFactory.showAll());


		////////// //////////  Check save/cache String[] ////////// //////////
		Method cacheFriendsUUIDs = FriendRemovalEvent.class.getDeclaredMethod("cacheFriendsUUIDs", DataManager.class, String[].class);
		cacheFriendsUUIDs.setAccessible(true);
		cacheFriendsUUIDs.invoke(friendRemovalEvent, dataManager, SAMPLE_UUID_1); //save them

		Method getFriendsUUIDsFromCache = FriendRemovalEvent.class.getDeclaredMethod("getFriendsUUIDsFromCache", DataManager.class);
		getFriendsUUIDsFromCache.setAccessible(true);
		String[] uuids = (String[]) getFriendsUUIDsFromCache.invoke(friendRemovalEvent, dataManager);
		Assert.assertArrayEquals(SAMPLE_UUID_1, uuids); //retrieve them and check them
	}

	@Test
	public void testForumsEventsNotifier() {
		ForumsEventsNotifier forumsEventsNotifier = new ForumsEventsNotifier(context);
		Result result = JUnitCore.runClasses(NotifiersTest.class);

	}

	@Test
	public void testGuildEventsNotifier() {
		GuildEventsNotifier guildEventsNotifier = new GuildEventsNotifier(context);
	}

	@After
	public void finish() {
		eventsSaver.clearAllRows();
		ignProvider.clearDB();
		dataManager.removeAll();
	}
}
