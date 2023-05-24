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
import java.util.stream.Stream;

import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class Theme {

	protected Map<String, Object> namedThings = new HashMap<>();
	protected Map<String, ArrayList<Object>> namedArrays = new HashMap<>();

	public void set(String name, Object thing) {
		namedThings.put(name, thing);
	}

	public void add(String arrayName, Color color) {
		if (!namedArrays.containsKey(arrayName)) {
			namedArrays.put(arrayName, new ArrayList<>());
		}
		namedArrays.get(arrayName).add(color);
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		return (T) namedThings.get(name);
	}

	public Color color(String name, int i) {
		var array = namedArrays.get(name);
		return (Color) array.get(i);
	}

	public Color color(String name) {
		return (Color) namedThings.get(name);
	}

	public Font font(String name) {
		return (Font) namedThings.get(name);
	}

	public Font font(String name, double size) {
		return Font.font(font(name).getFamily(), size);
	}

	public Image image(String name) {
		return (Image) namedThings.get(name);
	}

	public Spritesheet spritesheet(String name) {
		return (Spritesheet) namedThings.get(name);
	}

	public Background background(String name) {
		return (Background) namedThings.get(name);
	}

	public AudioClip audioClip(String name) {
		return (AudioClip) namedThings.get(name);
	}

	public Stream<Map.Entry<String, Object>> audioClipEntries() {
		return namedThings.entrySet().stream().filter(e -> e.getValue() instanceof AudioClip);
	}

	public Stream<AudioClip> audioClips() {
		return namedThings.values().stream().filter(AudioClip.class::isInstance).map(AudioClip.class::cast);
	}
}