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
import java.util.stream.Stream;

import javafx.animation.PauseTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * Useful methods.
 * 
 * @author Armin Reichert
 */
public class U {

	private U() {
	}

	public static boolean oneOf(Object value, Object... alternatives) {
		return Stream.of(alternatives).anyMatch(value::equals);
	}

	/**
	 * @param value1 value1
	 * @param value2 value2
	 * @param t      "time" between 0 and 1
	 * @return linear interpolation between {@code value1} and {@code value2} values
	 */
	public static double lerp(double value1, double value2, double t) {
		return (1 - t) * value1 + t * value2;
	}

	/**
	 * @param value some value
	 * @param min   lower bound of interval
	 * @param max   upper bound of interval
	 * @return the value if inside the interval, the lower bound if the value is smaller, the upper bound if the value is
	 *         larger
	 */
	public static double clamp(double value, double min, double max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	/**
	 * @param value some value
	 * @param min   lower bound of interval
	 * @param max   upper bound of interval
	 * @return the value if inside the interval, the lower bound if the value is smaller, the upper bound if the value is
	 *         larger
	 */
	public static int clamp(int value, int min, int max) {
		if (value < min) {
			return min;
		}
		if (value > max) {
			return max;
		}
		return value;
	}

	/**
	 * Pauses for the given number of seconds.
	 * 
	 * @param seconds number of seconds
	 * @return pause transition
	 */
	public static PauseTransition pauseSec(double seconds) {
		return new PauseTransition(Duration.seconds(seconds));
	}

	/**
	 * Runs the given code after the given number of seconds.
	 * <p>
	 * NOTE: Do NOT start an animation in the code!
	 * 
	 * @param seconds  number of seconds
	 * @param runnable code to run
	 * @return pause transition
	 */
	public static PauseTransition pauseSec(double seconds, Runnable runnable) {
		PauseTransition p = new PauseTransition(Duration.seconds(seconds));
		p.setOnFinished(e -> runnable.run());
		return p;
	}

	public static Font font(String path, double size) {
		return Font.loadFont(U.class.getResourceAsStream(path), size);
	}

	public static Image image(String path) {
		return new Image(U.class.getResourceAsStream(path));
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

	public static Background imageBackground(String path) {
		return new Background(new BackgroundImage(image(path), null, null, null, null));
	}

	public static ImageView imageView(String path) {
		return new ImageView(image(path));
	}

	public static String yesNo(boolean b) {
		return b ? "YES" : "NO";
	}

	public static String onOff(boolean b) {
		return b ? "ON" : "OFF";
	}
}