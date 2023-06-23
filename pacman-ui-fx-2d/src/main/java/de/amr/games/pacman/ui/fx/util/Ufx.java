/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.util;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

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
	public static PauseTransition pauseSeconds(double seconds) {
		return new PauseTransition(Duration.seconds(seconds));
	}

	/**
	 * Prepends a pause of the given duration (in seconds) before the given action can be run. Note that you have to call
	 * {@link Animation#play()} to execute the action!
	 * <p>
	 * NOTE: Do NOT start an animation in the code!
	 * 
	 * @param delaySeconds number of seconds
	 * @param action  code to run
	 * @return pause transition
	 */
	public static PauseTransition actionAfterSeconds(double delaySeconds, Runnable action) {
		checkNotNull(action);
		PauseTransition pause = pauseSeconds(delaySeconds);
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

	public static KeyCodeCombination just(KeyCode code) {
		return new KeyCodeCombination(code);
	}

	public static KeyCodeCombination alt(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.ALT_DOWN);
	}

	public static KeyCodeCombination shift(KeyCode code) {
		return new KeyCodeCombination(code, KeyCombination.SHIFT_DOWN);
	}

}