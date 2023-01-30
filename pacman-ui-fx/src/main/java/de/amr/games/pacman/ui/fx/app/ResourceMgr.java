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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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

	private static final String RESOURCE_ROOT_DIR = "/de/amr/games/pacman/ui/fx/";

	public static final String VOICE_HELP = "sound/common/press-key.mp3";
	public static final String VOICE_AUTOPILOT_OFF = "sound/common/autopilot-off.mp3";
	public static final String VOICE_AUTOPILOT_ON = "sound/common/autopilot-on.mp3";
	public static final String VOICE_IMMUNITY_OFF = "sound/common/immunity-off.mp3";
	public static final String VOICE_IMMUNITY_ON = "sound/common/immunity-on.mp3";

	/**
	 * @param relativePath relative path starting from resource root directory
	 * @return full path to resource including path to resource root directory
	 */
	public static String toFullPath(String relativePath) {
		return RESOURCE_ROOT_DIR + relativePath;
	}

	/**
	 * @param relativePath relative path starting from resource root directory
	 * @return URL of resource addressed by this path
	 */
	public static URL urlFromRelPath(String relativePath) {
		return url(toFullPath(relativePath));
	}

	/**
	 * @param fullPath full path to resource including path to resource root directory
	 * @return URL of resource addressed by this path
	 */
	public static URL url(String fullPath) {
		var url = ResourceMgr.class.getResource(fullPath);
		if (url == null) {
			throw new MissingResourceException("Missing resource, path=" + fullPath, "", fullPath);
		}
		return url;
	}

	public static Font font(String relPath, double size) {
		Objects.requireNonNull(relPath, "Font path cannot be NULL");
		var url = ResourceMgr.urlFromRelPath(relPath);
		if (url == null) {
			LOG.error(() -> "Font at '%s' not found".formatted(ResourceMgr.toFullPath(relPath)));
			return Font.font(Font.getDefault().getFamily(), size);
		}
		var font = Font.loadFont(url.toExternalForm(), size);
		if (font == null) {
			LOG.error(() -> "Font at '%s' not loaded".formatted(ResourceMgr.toFullPath(relPath)));
			return Font.font(Font.getDefault().getFamily(), size);
		}
		return font;
	}

	public static Image image(String relPath) {
		Objects.requireNonNull(relPath, "Image path cannot be NULL");
		var url = ResourceMgr.urlFromRelPath(relPath);
		if (url == null) {
			LOG.error(() -> "No image found at path '%s'".formatted(ResourceMgr.toFullPath(relPath)));
			return null;
		}
		return new Image(url.toExternalForm());
	}

	public static Background colorBackground(Color color) {
		return new Background(new BackgroundFill(color, null, null));
	}

	public static Background imageBackground(String relPath) {
		return new Background(new BackgroundImage(image(relPath), null, null, null, null));
	}
}