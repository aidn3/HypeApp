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

package com.aidn5.hypeapp.activities.main_activity;

public enum ChatColors {
	BLACK(0xFF000000, 0xFFFFFFFF),
	DARK_BLUE(0xFF0000AA, 0xFFFFFFFF),
	DARK_GREEN(0xFF00AA00, 0xFFFFFFFF),
	DARK_AQUA(0xFF00AAAA, 0xFFFFFFFF),
	DARK_RED(0xFFAA0000, 0xFFFFFFFF),
	DARK_PURPLE(0xFFAA00AA, 0xFFFFFFFF),
	GOLD(0xFFFFAA00, 0xFFFFFFFF),
	GRAY(0xFFAAAAAA, 0xFFFFFFFF),
	DARK_GRAY(0xFF555555, 0xFFFFFFFF),
	BLUE(0xFF5555FF, 0xFFFFFFFF),
	GREEN(0xFF55FF55, 0xFFFFFFFF),
	AQUA(0xFF55FFFF, 0xFFFFFFFF),
	RED(0xFFFF5555, 0xFFFFFFFF),
	LIGHT_PURPLE(0xFFFF55FF, 0xFFFFFFFF),
	YELLOW(0xFFFFFF55, 0xFFFFFFFF),
	WHITE(0xFFFFFFFF, 0xFF000000),
	RESET(0xFF000000, 0xFFFFFFFF),
	NONE(0xFF000000, 0xFFFFFFFF);

	private final int primaryColor;
	private final int secondaryColor;

	ChatColors(int primaryColor, int secondaryColor) {
		this.primaryColor = primaryColor;
		this.secondaryColor = secondaryColor;
	}

	public int getPrimaryColor() {
		return primaryColor;
	}

	public int getSecondaryColor() {
		return secondaryColor;
	}
}
