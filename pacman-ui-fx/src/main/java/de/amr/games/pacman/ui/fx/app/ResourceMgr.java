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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class ResourceMgr {

	private static final Logger LOG = LogManager.getFormatterLogger();
	private static final String ROOT = "/assets/";
	private static final ResourceBundle MESSAGES_BUNDLE = ResourceBundle.getBundle("assets.texts.messages");

	private static final Picker<String> PICKER_MSG_CHEATING = createPicker(MESSAGES_BUNDLE, "cheating");
	private static final Picker<String> PICKER_MSG_LEVEL_COMPLETE = createPicker(MESSAGES_BUNDLE, "level.complete");
	private static final Picker<String> PICKER_MSG_GAME_OVER = createPicker(MESSAGES_BUNDLE, "game.over");

	public static final String VOICE_HELP = "sound/common/press-key.mp3";
	public static final String VOICE_AUTOPILOT_OFF = "sound/common/autopilot-off.mp3";
	public static final String VOICE_AUTOPILOT_ON = "sound/common/autopilot-on.mp3";
	public static final String VOICE_IMMUNITY_OFF = "sound/common/immunity-off.mp3";
	public static final String VOICE_IMMUNITY_ON = "sound/common/immunity-on.mp3";

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

	/**
	 * Builds a resource key from the given key pattern and arguments and reads the corresponding message from the
	 * messages resource bundle.
	 * 
	 * @param keyPattern message key pattern
	 * @param args       arguments merged into key pattern
	 * @return message text for composed key or string indicating missing text
	 */
	public static String message(String keyPattern, Object... args) {
		try {
			return MESSAGES_BUNDLE.getString(keyPattern).formatted(args);
		} catch (Exception x) {
			LOG.error("No text resource found for key '%s'", keyPattern);
			return "missing{%s}".formatted(keyPattern);
		}
	}

	private static Picker<String> createPicker(ResourceBundle bundle, String prefix) {
		return new Picker<>(bundle.keySet().stream()//
				.filter(key -> key.startsWith(prefix))//
				.sorted()//
				.map(bundle::getString)//
				.toArray(String[]::new));
	}

	public static String getCheatingMessage() {
		return PICKER_MSG_CHEATING.next();
	}

	public static String getGameOverMessage() {
		return PICKER_MSG_GAME_OVER.next();
	}

	public static String getLevelCompleteMessage() {
		return PICKER_MSG_LEVEL_COMPLETE.next();
	}
}