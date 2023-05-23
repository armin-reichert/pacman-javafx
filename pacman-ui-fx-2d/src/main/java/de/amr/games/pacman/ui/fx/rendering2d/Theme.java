/*
MIT License

Copyright (c) 2022 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class Theme {

	protected Map<String, Object> namedThings = new HashMap<>();
	protected Map<String, ArrayList<Object>> namedArrays = new HashMap<>();

	protected void set(String name, Object thing) {
		namedThings.put(name, thing);
	}

	protected void addToArray(String arrayName, Color color) {
		if (!namedArrays.containsKey(arrayName)) {
			namedArrays.put(arrayName, new ArrayList<>());
		}
		namedArrays.get(arrayName).add(color);
	}

	public Color color(String key, int i) {
		var array = namedArrays.get(key);
		return (Color) array.get(i);
	}

	public Color color(String key) {
		return (Color) namedThings.get(key);
	}

	private Font font(String key) {
		return (Font) namedThings.get(key);
	}

	public Font font(String key, double size) {
		return Font.font(font(key).getFamily(), size);
	}
}