/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
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
		namedArrays.computeIfAbsent(arrayName, name -> new ArrayList<>()).add(color);
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