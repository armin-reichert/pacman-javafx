/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import javafx.animation.PauseTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Useful methods.
 * 
 * @author Armin Reichert
 */
public class U {

	/**
	 * @param from from value
	 * @param to   to value
	 * @param t    time between 0 and 1
	 * @return linear interpolation between {@code from} and {@code to} values
	 */
	public static double lerp(double from, double to, double t) {
		return (1 - t) * from + t * to;
	}

	/**
	 * @param value some value
	 * @param min   lower bound of interval
	 * @param max   upper bound of interval
	 * @return the value if inside the interval, the lower bound if the value is smaller, the upper bound if the value is
	 *         larger
	 */
	public static double clamp(double value, double min, double max) {
		return value < min ? min : value > max ? max : value;
	}

	/**
	 * @param value some value
	 * @param min   lower bound of interval
	 * @param max   upper bound of interval
	 * @return the value if inside the interval, the lower bound if the value is smaller, the upper bound if the value is
	 *         larger
	 */
	public static int clamp(int value, int min, int max) {
		return value < min ? min : value > max ? max : value;
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

	public static Image image(String path) {
		return new Image(U.class.getResource(path).toString());
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

	public static String yes_no(boolean b) {
		return b ? "YES" : "NO";
	}

	public static String on_off(boolean b) {
		return b ? "ON" : "OFF";
	}
}