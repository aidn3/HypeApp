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

package net.hypixel.api.reply;

import java.util.ArrayList;
import java.util.List;

public class FriendsReply extends AbstractReply {
	private List<FriendShip> records;

	public List<FriendShip> getFriendShips() {
		return records;
	}

	public List<String> getFriends(String theOwner) {
		List<String> friends = new ArrayList<>(records.size());

		for (FriendShip friendShip : records) {
			if (friendShip.uuidSender.equals(theOwner)) {
				friends.add(friendShip.uuidReceiver);
			} else {
				friends.add(friendShip.uuidSender);
			}
		}

		return friends;
	}

	@Override
	public String toString() {
		return "FriendsReply{" +
				"records=" + records +
				"} " + super.toString();
	}

	public class FriendShip {

		private String uuidSender, uuidReceiver;
		private long started;

		public String getUuidSender() {
			return uuidSender;
		}

		public String getUuidReceiver() {
			return uuidReceiver;
		}

		public long getStarted() {
			return started;
		}

		@Override
		public String toString() {
			return "FriendShip{" +
					"uuidSender=" + uuidSender +
					", uuidReceiver=" + uuidReceiver +
					", started=" + started +
					'}';
		}
	}
}
