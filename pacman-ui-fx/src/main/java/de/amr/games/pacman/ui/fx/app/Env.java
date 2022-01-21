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
package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import de.amr.games.pacman.ui.fx._3d.scene.Perspective;
import de.amr.games.pacman.ui.fx.util.RandomEntrySelector;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.shape.DrawMode;

/**
 * Global stuff.
 * 
 * @author Armin Reichert
 */
public class Env {

	// UI messages

	public static final ResourceBundle MESSAGES = ResourceBundle.getBundle("/common/messages");

	public static String message(String pattern, Object... args) {
		return MessageFormat.format(MESSAGES.getString(pattern), args);
	}

	// Trash talk

	public static final RandomEntrySelector<String> CHEAT_TALK = load("/common/cheating_talk");
	public static final RandomEntrySelector<String> LEVEL_COMPLETE_TALK = load("/common/level_complete_talk");
	public static final RandomEntrySelector<String> GAME_OVER_TALK = load("/common/game_over_talk");

	private static RandomEntrySelector<String> load(String bundleName) {
		ResourceBundle bundle = ResourceBundle.getBundle(bundleName);
		return new RandomEntrySelector<>(bundle.keySet().stream().sorted().map(bundle::getString).toArray(String[]::new));
	}

	public static GameLoop gameLoop;

	public static final BooleanProperty $axesVisible = new SimpleBooleanProperty(false);
	public static final ObjectProperty<DrawMode> $drawMode3D = new SimpleObjectProperty<DrawMode>(DrawMode.FILL);
	public static final BooleanProperty $isTimeMeasured = new SimpleBooleanProperty(false);
	public static final IntegerProperty $mazeResolution = new SimpleIntegerProperty(8);
	public static final DoubleProperty $mazeWallHeight = new SimpleDoubleProperty(3.5);
	public static final BooleanProperty $paused = new SimpleBooleanProperty(false);
	public static final BooleanProperty $tilesVisible = new SimpleBooleanProperty(false);
	public static final BooleanProperty $3D = new SimpleBooleanProperty(true);

	public static final ObjectProperty<Perspective> $perspective = new SimpleObjectProperty<Perspective>(
			Perspective.CAM_FOLLOWING_PLAYER);

	public static void nextPerspective() {
		int next = ($perspective.get().ordinal() + 1) % Perspective.values().length;
		$perspective.set(Perspective.values()[next]);
	}

	public static void changeMazeResolution(boolean up) {
		int res = $mazeResolution.get();
		if (up) {
			$mazeResolution.set(Math.min(res * 2, 8));
		} else {
			$mazeResolution.set(Math.max(res / 2, 1));
		}
		log("Maze resolution is now %d", $mazeResolution.get());
	}

	public static void changeMazeWallHeight(boolean up) {
		double height = $mazeWallHeight.get();
		if (up) {
			$mazeWallHeight.set(Math.min(height + 0.2, 8.0));
		} else {
			$mazeWallHeight.set(Math.max(height - 0.2, 0.1));
		}
		log("Maze wall height is now %.2f", $mazeWallHeight.get());
	}
}