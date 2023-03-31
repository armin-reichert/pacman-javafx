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

import java.util.Map;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

/**
 * Useful JavaFX helper methods.
 * 
 * @author Armin Reichert
 */
public class Ufx {

	private Ufx() {
	}

	public static void toggle(BooleanProperty booleanProperty) {
		booleanProperty.set(!booleanProperty.get());
	}

	public static PhongMaterial createColorBoundMaterial(ObjectProperty<Color> colorProperty) {
		var material = new PhongMaterial();
		bindMaterialColorProperties(material, colorProperty);
		return material;
	}

	public static void bindMaterialColorProperties(PhongMaterial material, ObjectProperty<Color> colorProperty) {
		material.diffuseColorProperty().bind(colorProperty);
		material.specularColorProperty()
				.bind(Bindings.createObjectBinding(() -> material.getDiffuseColor().brighter(), colorProperty));
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
}