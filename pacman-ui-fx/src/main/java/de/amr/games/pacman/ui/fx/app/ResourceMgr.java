/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

package de.amr.games.pacman.ui.fx.app;

import java.net.URL;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.ui.fx.util.Picker;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class ResourceMgr {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final String ROOT = "/assets/";

	/**
	 * @param relativePath relative path (without leading slash) starting from resource root directory
	 * @return full path to resource including path to resource root directory
	 */
	private static String toFullPath(String relativePath) {
		return ROOT + relativePath;
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @return URL of resource addressed by this path. Never returns <code>null</code>!
	 */
	public static URL urlFromRelPath(String relPath) {
		return url(toFullPath(relPath));
	}

	/**
	 * @param fullPath full path to resource including path to resource root directory
	 * @return URL of resource addressed by this path. Never returns <code>null</code>!
	 */
	public static URL url(String fullPath) {
		Objects.requireNonNull(fullPath);
		var url = ResourceMgr.class.getResource(fullPath);
		if (url == null) {
			throw new MissingResourceException("Missing resource, path=" + fullPath, "", fullPath);
		}
		return url;
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @return audio clip from resource addressed by this path
	 */
	public static AudioClip audioClip(String relPath) {
		var url = urlFromRelPath(relPath);
		return new AudioClip(url.toExternalForm());
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @param size    font size (must be a positive number)
	 * @return font loaded from resource addressed by this path. If no such font can be loaded, a default font is returned
	 */
	public static Font font(String relPath, double size) {
		if (size <= 0) {
			throw new IllegalArgumentException("Font size must be positive but is %.2f".formatted(size));
		}
		var url = urlFromRelPath(relPath);
		var font = Font.loadFont(url.toExternalForm(), size);
		if (font == null) {
			LOG.error(() -> "Font with URL '%s' could not be loaded".formatted(url));
			return Font.font(Font.getDefault().getFamily(), size);
		}
		return font;
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @return image loaded from resource addressed by this path. If no such font can be loaded, a default font is
	 *         returned
	 */
	public static Image image(String relPath) {
		var url = urlFromRelPath(relPath);
		return new Image(url.toExternalForm());
	}

	public static Background colorBackground(Color color) {
		Objects.requireNonNull(color);
		return new Background(new BackgroundFill(color, null, null));
	}

	public static Background imageBackground(String relPath) {
		Objects.requireNonNull(relPath);
		return new Background(new BackgroundImage(image(relPath), null, null, null, null));
	}

	public static PhongMaterial coloredMaterial(Color color) {
		var material = new PhongMaterial(color);
		material.setSpecularColor(color.brighter());
		return material;
	}

	public static Color color(Color color, double opacity) {
		return Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
	}

	public static Picker<String> createPicker(ResourceBundle bundle, String prefix) {
		return new Picker<>(bundle.keySet().stream()//
				.filter(key -> key.startsWith(prefix))//
				.sorted()//
				.map(bundle::getString)//
				.toArray(String[]::new));
	}
}