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


package com.aidn5.hypeapp.hypixelapi.models;

import java.util.HashMap;

/**
 * Holder holds the information of a member
 *
 * @author aidn5
 */
public class GuildMember implements Comparable<GuildMember> {

	public String username = null;
	public String uuid = null;

	public String guildName = null;
	public int joinedAt = -1;

	public String rank = "Member";
	public String tag = "";
	public int rankPriority = 0;
	public String tagColor = "DARK_AQUA";

	public int lastTimeOnline = -1;
	public HashMap<Integer, Integer> coins = null;
	@SuppressWarnings("FieldCanBeLocal")
	private double hypixelLevel = -1;
	private int totalGuildExp = -1;

	/**
	 * This method returns the level of a player calculated by the current
	 * experience gathered. The result is a precise level of the player The value is
	 * not zero-indexed and represents the absolute visible level for the player.
	 * <p>
	 * <b>Credits:</b>
	 * https://github.com/HypixelDev/PublicAPI/blob/master/Java/src/main/java/net/hypixel/api/util/ILeveling.java
	 * </p>
	 *
	 * @return player's level
	 */
	double getHypixelLevel() {
		final double REVERSE_PQ_PREFIX = -3.5;
		final double REVERSE_CONST = 12.25;
		final double GROWTH_DIVIDES_2 = 0.0008;

		if (this.hypixelLevel < 0) return 1;
		return Math.floor(1 + REVERSE_PQ_PREFIX + Math.sqrt(REVERSE_CONST + GROWTH_DIVIDES_2 * this.hypixelLevel));
	}

	public int getWeeklyGuildExp() {
		if (this.totalGuildExp > 0) return this.totalGuildExp;

		for (Integer value : this.coins.values()) {
			this.totalGuildExp += value;
		}

		return this.totalGuildExp;
	}

	@Override
	public int compareTo(GuildMember o2) {
		// First in rank priority
		if (this.rankPriority > o2.rankPriority) return -1;
		else if (this.rankPriority < o2.rankPriority) return 1;

		// If the same... who tag has
		if (!this.tag.isEmpty() && o2.tag.isEmpty()) return -1;
		else if (this.tag.isEmpty() && !o2.tag.isEmpty()) return 1;

		// check in name?!
		return this.rank.toLowerCase().compareTo(o2.rank.toLowerCase());
	}
}