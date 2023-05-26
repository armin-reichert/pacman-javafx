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

package de.amr.games.pacman.ui.fx.util;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.tinylog.Logger;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.CornerRadii;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class ResourceManager {

	/**
	 * Builds a resource key from the given key pattern and the arguments and returns the corresponding text from the
	 * resource bundle.
	 * 
	 * @param bundle     resource bundle
	 * @param keyPattern pattern for creating final resource bundle key
	 * @param args       arguments merged into key pattern
	 * @return text for composed key or string indicating missing text
	 */
	public static String fmtMessage(ResourceBundle bundle, String keyPattern, Object... args) {
		try {
			var pattern = bundle.getString(keyPattern);
			return MessageFormat.format(pattern, args);
		} catch (Exception x) {
			Logger.error("No text resource found for key '{}'", keyPattern);
			return String.format("noresfound{%s}", keyPattern);
		}
	}

	public static Background coloredBackground(Color color) {
		checkNotNull(color);
		return new Background(new BackgroundFill(color, CornerRadii.EMPTY, Insets.EMPTY));
	}

	public static Background coloredRoundedBackground(Color color, int radius) {
		checkNotNull(color);
		return new Background(new BackgroundFill(color, new CornerRadii(radius), Insets.EMPTY));
	}

	public static PhongMaterial coloredMaterial(Color color) {
		checkNotNull(color);
		var material = new PhongMaterial(color);
		material.setSpecularColor(color.brighter());
		return material;
	}

	public static Color color(Color color, double opacity) {
		checkNotNull(color);
		return Color.color(color.getRed(), color.getGreen(), color.getBlue(), opacity);
	}

	private final Class<?> callerClass;
	private final String rootDir;

	public ResourceManager(String rootDir, Class<?> callerClass) {
		checkNotNull(rootDir);
		checkNotNull(callerClass);
		this.rootDir = rootDir;
		this.callerClass = callerClass; // TODO what about Reflection.getCallerClass()?
	}

	/**
	 * Creates an URL from a resource path. If the path does not start with a slash, the path to the asset root directory
	 * is prepended.
	 * 
	 * @param path path of resource
	 * @return URL of resource addressed by this path
	 */
	public URL url(String path) {
		checkNotNull(path);
		if (path.startsWith("/")) {
			return callerClass.getResource(path);
		}
		return callerClass.getResource(rootDir + path);
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @return audio clip from resource addressed by this path
	 */
	public AudioClip audioClip(String relPath) {
		return new AudioClip(url(relPath).toExternalForm());
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @param size    font size (must be a positive number)
	 * @return font loaded from resource addressed by this path. If no such font can be loaded, a default font is returned
	 */
	public Font font(String relPath, double size) {
		if (size <= 0) {
			throw new IllegalArgumentException(String.format("Font size must be positive but is %.2f", size));
		}
		var url = url(relPath);
		var font = Font.loadFont(url.toExternalForm(), size);
		if (font == null) {
			Logger.error("Font with URL '{}' could not be loaded", url);
			return Font.font(Font.getDefault().getFamily(), size);
		}
		return font;
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @return image loaded from resource addressed by this path.
	 */
	public Image image(String relPath) {
		var url = url(relPath);
		return new Image(url.toExternalForm());
	}

	/**
	 * @param relPath relative path (without leading slash) starting from resource root directory
	 * @return background displaying image loaded from resource addressed by this path.
	 */
	public Background imageBackground(String relPath) {
		return new Background(new BackgroundImage(image(relPath), null, null, null, null));
	}
}