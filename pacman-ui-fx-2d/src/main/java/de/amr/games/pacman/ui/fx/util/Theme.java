/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class Theme {

	protected Map<String, Object> namedThings = new HashMap<>();
	protected Map<String, ArrayList<Object>> namedArrays = new HashMap<>();

	private long countEntriesOfType(Class<?> clazz) {
		var count = namedThings.values().stream().filter(thing -> thing.getClass().isAssignableFrom(clazz)).count();
		for (var array: namedArrays.values()) {
			if (!array.isEmpty() && array.get(0).getClass().isAssignableFrom(clazz)) {
				count += array.size();
			}
		}
		return count;
	}
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(": ");
		sb.append(countEntriesOfType(Image.class)).append(" images").append(", ");
		sb.append(countEntriesOfType(Font.class)).append(" fonts").append(", ");
		sb.append(countEntriesOfType(Color.class)).append(" colors").append(", ");
		sb.append(countEntriesOfType(AudioClip.class)).append(" audio clips").append(", ");
		return sb.toString();
	}

	public void set(String name, Object thing) {
		namedThings.put(name, thing);
	}

	public void add(String arrayName, Color color) {
		namedArrays.computeIfAbsent(arrayName, name -> new ArrayList<>()).add(color);
	}

	public void addAll(String arrayName, Color... colors) {
		for (var color : colors) {
			add(arrayName, color);
		}
	}

	/**
	 * Generic getter. Example usage:
	 * 
	 * <pre>
	 * AnyType value = theme.get("key.for.value");
	 * </pre>
	 * 
	 * @param <T>  expected return type
	 * @param name name of thing
	 * @return stored value casted to return type
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		return (T) namedThings.get(name);
	}

	public Color color(String name, int i) {
		var array = namedArrays.get(name);
		return (Color) array.get(i);
	}

	public Color color(String name) {
		return get(name);
	}

	public Font font(String name) {
		return get(name);
	}

	public Font font(String name, double size) {
		return Font.font(font(name).getFamily(), size);
	}

	public Image image(String name) {
		return get(name);
	}

	public Background background(String name) {
		return get(name);
	}

	public AudioClip audioClip(String name) {
		return get(name);
	}

	public Stream<Map.Entry<String, Object>> audioClipEntries() {
		return namedThings.entrySet().stream().filter(e -> e.getValue() instanceof AudioClip);
	}

	public Stream<AudioClip> audioClips() {
		return namedThings.values().stream().filter(AudioClip.class::isInstance).map(AudioClip.class::cast);
	}
}