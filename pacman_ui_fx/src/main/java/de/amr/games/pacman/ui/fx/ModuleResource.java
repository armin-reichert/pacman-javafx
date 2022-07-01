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

package de.amr.games.pacman.ui.fx;

import java.net.URL;
import java.util.MissingResourceException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class ModuleResource {

	private static final Logger logger = LogManager.getFormatterLogger();

	private ModuleResource() {
	}

	public static String path(String relPath) {
		return "/de/amr/games/pacman/ui/fx/" + relPath;
	}

	public static String urlString(String relPath) {
		return url(relPath).toExternalForm();
	}

	public static URL url(String relPath) {
		var path = path(relPath);
		var url = ModuleResource.class.getResource(path);
		if (url != null) {
			return url;
		}
		throw new MissingResourceException("No resource found for path", "", path);
	}

	public static Font font(String relPath, double size) {
		return Font.loadFont(urlString(relPath), size);
	}

	public static Image image(String relPath) {
		return new Image(urlString(relPath));
	}

	public static Background colorBackground(Color color) {
		return new Background(new BackgroundFill(color, null, null));
	}

	public static Background imageBackground(String path) {
		return new Background(new BackgroundImage(image(path), null, null, null, null));
	}

	public static ImageView imageView(String path) {
		return new ImageView(image(path));
	}
}