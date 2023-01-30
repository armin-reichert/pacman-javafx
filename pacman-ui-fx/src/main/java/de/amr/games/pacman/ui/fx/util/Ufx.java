/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * Useful methods.
 * 
 * @author Armin Reichert
 */
public class Ufx {

	private Ufx() {
	}

	private static final Logger LOG = LogManager.getFormatterLogger();

	public static void toggle(BooleanProperty booleanProperty) {
		booleanProperty.set(!booleanProperty.get());
	}

	public static PhongMaterial createColorBoundMaterial(ObjectProperty<Color> pyColor) {
		var mat = new PhongMaterial();
		bindMaterialColor(mat, pyColor);
		return mat;
	}

	public static void bindMaterialColor(PhongMaterial mat, ObjectProperty<Color> pyColor) {
		mat.diffuseColorProperty().bind(pyColor);
		mat.specularColorProperty().bind(Bindings.createObjectBinding(() -> pyColor.get().brighter(), pyColor));
	}

	/**
	 * Pauses for the given number of seconds.
	 * 
	 * @param seconds number of seconds
	 * @return pause transition
	 */
	public static PauseTransition pause(double seconds) {
		return new PauseTransition(Duration.seconds(seconds));
	}

	/**
	 * Prepends a pause of the given duration (in seconds) before the given action can be run. Note that you have to call
	 * {@link Animation#play()} to execute the action!
	 * <p>
	 * NOTE: Do NOT start an animation in the code!
	 * 
	 * @param seconds number of seconds
	 * @param action  code to run
	 * @return pause transition
	 */
	public static PauseTransition afterSeconds(double seconds, Runnable action) {
		PauseTransition pause = pause(seconds);
		pause.setOnFinished(e -> action.run());
		return pause;
	}

	/**
	 * @param source    source image
	 * @param exchanges map of color exchanges
	 * @return copy of source image with colors exchanged
	 */
	public static Image colorsExchanged(Image source, Map<Color, Color> exchanges) {
		WritableImage result = new WritableImage((int) source.getWidth(), (int) source.getHeight());
		PixelWriter out = result.getPixelWriter();
		for (int x = 0; x < source.getWidth(); ++x) {
			for (int y = 0; y < source.getHeight(); ++y) {
				Color color = source.getPixelReader().getColor(x, y);
				if (exchanges.containsKey(color)) {
					out.setColor(x, y, exchanges.get(color));
				}
			}
		}
		return result;
	}

	public static Background colorBackground(Color color) {
		return new Background(new BackgroundFill(color, null, null));
	}

	public static Background imageBackground(String relPath) {
		return new Background(new BackgroundImage(Ufx.image(relPath), null, null, null, null));
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
}