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


package com.aidn5.hypeapp;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.aidn5.hypeapp.services.AbstractedCacher;
import com.aidn5.hypeapp.services.DataManager;
import com.aidn5.hypeapp.services.EventsSaver;
import com.aidn5.hypeapp.services.IgnProvider;
import com.snappydb.SnappydbException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;


@RunWith(AndroidJUnit4.class)
public class ServicesTest {
	private static final String[] SAMPLE_UUID_1 = new String[]{"c65261c70d5a46f7ab786e7d189ec722", "f7bd2af3c71b426b88a12851e217256c", "fbf2582cb5904bd9a935ec3688c28ffc"};
	private static final String[] SAMPLE_UUID_2 = new String[]{"fad6048072fd45f096aa5554ed5c2ad1", "1663e41e66d443f8a43dc81cb25f0095", "1415af534a524b44b25a862f07e8a40c"};

	private DataManager dataManager;
	private EventsSaver eventsSaver;
	private IgnProvider ignProvider;

	@After
	public void finish() {
		eventsSaver.clearAllRows();
		ignProvider.clearDB();
		dataManager.removeAll();
	}

	@Before
	public void setUp() {
		// get Context
		Context context = InstrumentationRegistry.getTargetContext();
		Assert.assertNotNull(context);

		dataManager = new DataManager(context);
		dataManager.removeAll();

		eventsSaver = new EventsSaver(context);
		eventsSaver.clearAllRows();

		ignProvider = new IgnProvider(context);
		ignProvider.clearDB();
	}

	@Test
	public void testDataManager_basicFunctions() throws Exception {
		dataManager.remove("test");
		Assert.assertFalse(dataManager.exists("test"));

		dataManager.put("test", 1);
		Assert.assertTrue(dataManager.exists("test"));
		Assert.assertEquals((int) dataManager.get("test", int.class), 1);

		dataManager.remove("test");
		Assert.assertFalse(dataManager.exists("test"));

		dataManager.put("test", 123);
		dataManager.put("test", "a");
		dataManager.put("test", false);
		Assert.assertEquals(dataManager.get("test", boolean.class), false);

		try {
			dataManager.put("test", "a");
			dataManager.removeAll();
			dataManager.get("test", String.class);
			Assert.fail();
		} catch (SnappydbException ignored) {
		}
	}

	@Test
	public void testDataManager_UserData() throws Exception {
		dataManager.setBestFriends(SAMPLE_UUID_1);
		Assert.assertArrayEquals(dataManager.getBestFriends(), SAMPLE_UUID_1);

		dataManager.setBestFriends(SAMPLE_UUID_2);
		Assert.assertArrayEquals(dataManager.getBestFriends(), SAMPLE_UUID_2);


		dataManager.setIsInGuild(true);
		Assert.assertTrue(dataManager.getIsInGuild());

		dataManager.setIsInGuild(false);
		Assert.assertFalse(dataManager.getIsInGuild());


		dataManager.setGuildMembers(SAMPLE_UUID_1);
		Assert.assertArrayEquals(dataManager.getGuildMembers(), SAMPLE_UUID_1);

		dataManager.setGuildMembers(SAMPLE_UUID_2);
		Assert.assertArrayEquals(dataManager.getGuildMembers(), SAMPLE_UUID_2);


		dataManager.setFriends(SAMPLE_UUID_1);
		Assert.assertArrayEquals(dataManager.getFriends(), SAMPLE_UUID_1);

		dataManager.setFriends(SAMPLE_UUID_2);
		Assert.assertArrayEquals(dataManager.getFriends(), SAMPLE_UUID_2);
	}

	@Test
	public void testEventsSaver() {
		final EventsSaver.DataHolder dataHolder = new EventsSaver.DataHolder();
		dataHolder.provider = android.R.string.unknownName;
		dataHolder.title = android.R.string.dialog_alert_title;
		dataHolder.message = android.R.string.ok;


		eventsSaver.register(dataHolder);
		eventsSaver.register(dataHolder);

		List<EventsSaver.DataHolder> events = eventsSaver.fetch(1);

		Assert.assertEquals(events.size(), 2);
		testEventsSaver_CompareDataHolders(events.get(0), dataHolder);
		testEventsSaver_CompareDataHolders(events.get(1), dataHolder);

		eventsSaver.clearAllRows();
		Assert.assertEquals(eventsSaver.fetch(1).size(), 0);


		eventsSaver.clearAllRows();
		dataHolder.args = new String[]{""};
		eventsSaver.register(dataHolder);
		testEventsSaver_CompareDataHolders(eventsSaver.fetch(1).get(0), dataHolder);

		eventsSaver.clearAllRows();
		dataHolder.args = new String[]{"123"};
		eventsSaver.register(dataHolder);
		testEventsSaver_CompareDataHolders(eventsSaver.fetch(1).get(0), dataHolder);

		eventsSaver.clearAllRows();
		dataHolder.args = new String[]{"asd1", "asd2", "asd3"};
		eventsSaver.register(dataHolder);
		testEventsSaver_CompareDataHolders(eventsSaver.fetch(1).get(0), dataHolder);

		eventsSaver.clearAllRows();
		dataHolder.args = new String[]{"asd", "", "asd"};
		eventsSaver.register(dataHolder);
		testEventsSaver_CompareDataHolders(eventsSaver.fetch(1).get(0), dataHolder);

		eventsSaver.clearAllRows();
		dataHolder.args = new String[]{"asd", "", ""};
		eventsSaver.register(dataHolder);
		testEventsSaver_CompareDataHolders(eventsSaver.fetch(1).get(0), dataHolder);

		eventsSaver.clearAllRows();
		dataHolder.args = new String[]{"1"};
		eventsSaver.register(dataHolder);
		testEventsSaver_CompareDataHolders(eventsSaver.fetch(1).get(0), dataHolder);


		Assert.assertEquals(eventsSaver.fetch(3).size(), 0);
	}

	private void testEventsSaver_CompareDataHolders(EventsSaver.DataHolder o1, EventsSaver.DataHolder o2) {
		Assert.assertEquals(o1.provider, o2.provider);
		Assert.assertEquals(o1.title, o2.title);
		Assert.assertEquals(o1.message, o2.message);
		if (o1.args != null && o2.args != null) Assert.assertArrayEquals(o1.args, o2.args);
		else if (o1.args != o2.args) Assert.assertSame(o1.args, o2.args);
		// That there IS time-stamp saved
		Assert.assertTrue(o1.registerTime > ((System.currentTimeMillis() / 1000L) - 5000L));
	}

	@Test
	public void testIgnProvider() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		Method insertIntoDB = AbstractedCacher.class.getDeclaredMethod("insertIntoDB", String.class, String.class);
		insertIntoDB.setAccessible(true);

		Assert.assertNotNull(ignProvider.getUsername(SAMPLE_UUID_1[0], true));

		insertIntoDB.invoke(ignProvider, SAMPLE_UUID_1[0], ".");
		Assert.assertEquals(".", ignProvider.getUsername(SAMPLE_UUID_1[0], false));
		Assert.assertNotEquals(".", ignProvider.getUsername(SAMPLE_UUID_1[0], true));
	}

	@Test
	public void testNotificationFactory() {
		// TODO: [tests] 13.03.2019 find a way to view notifications and test them through NotificationFactory
	}
}
