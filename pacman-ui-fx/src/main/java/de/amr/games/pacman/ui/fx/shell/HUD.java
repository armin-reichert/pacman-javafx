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
package de.amr.games.pacman.ui.fx.shell;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * Heads-Up-Display with information about the UI.
 * 
 * @author Armin Reichert
 */
public class HUD extends VBox {

	private static final int MAX_WIDTH = 400;

	private static String yes_no(boolean b) {
		return b ? "YES" : "NO";
	}

	private static String on_off(boolean b) {
		return b ? "ON" : "OFF";
	}

	private static HUD IT = new HUD();

	public static HUD get() {
		return IT;
	}

	private final Text textUI = new Text();
	private final StringBuilder text = new StringBuilder();

	private HUD() {
		setVisible(false);
		setMaxWidth(MAX_WIDTH);
		setBackground(new Background(new BackgroundFill(new Color(0.3, 0.3, 0.3, 0.6), null, null)));
		textUI.setFill(Color.WHITE);
		textUI.setFont(Font.font("Monospace", 14));
		getChildren().add(textUI);
	}

	public void show() {
		setVisible(true);
		setOpacity(1);
		setTranslateX(-MAX_WIDTH);
		TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), this);
		transition.setFromX(-MAX_WIDTH);
		transition.setToX(0);
		transition.setInterpolator(Interpolator.EASE_IN);
		transition.play();
	}

	public void hide() {
		FadeTransition fade = new FadeTransition(Duration.seconds(0.5), this);
		fade.setFromValue(1);
		fade.setToValue(0);
		fade.setOnFinished(e -> setVisible(false));
		fade.play();
	}

	public void update(GameUI ui) {
		var width = ui.stage.getScene().getWindow().getWidth();
		var height = ui.stage.getScene().getWindow().getHeight();
		var sceneWidth = ui.stage.getScene().getWidth();
		var sceneHeight = ui.stage.getScene().getHeight();
		var gameCtrl = ui.gameController;
		var game = gameCtrl.game;
		var state = gameCtrl.currentStateID;
		var huntingPhaseName = game.inScatteringPhase() ? "Scattering" : "Chasing";
		var stateTimer = gameCtrl.stateTimer();
		var gameScene = ui.currentScene;

		text.setLength(0);
		p("Total Ticks", "%d", Env.gameLoop.$totalTicks.get()).done();
		p("Target FPS", "%d Hz", Env.gameLoop.getTargetFrameRate()).done();
		p("Current FPS", "%d Hz", Env.gameLoop.$fps.get()).done();
		p("Paused", "%s", yes_no(Env.$paused.get())).done();
		p("Playing", "%s", yes_no(gameCtrl.gameRunning)).done();
		p("Attract Mode", "%s", yes_no(gameCtrl.attractMode)).done();
		p("Game Variant", "%s", gameCtrl.gameVariant).done();
		p("Game Level", "%d", game.levelNumber).done();
		p("Game State", "%s",
				state == GameState.HUNTING ? String.format("%s: Phase #%d (%s)", state, game.huntingPhase, huntingPhaseName)
						: state).done();
		p("", "Running:   %s%s", stateTimer.ticked(), stateTimer.isStopped() ? " (STOPPED)" : "").done();
		p("", "Remaining: %s",
				stateTimer.ticksRemaining() == TickTimer.INDEFINITE ? "indefinite" : stateTimer.ticksRemaining()).done();
		p("Autopilot", "%s", on_off(gameCtrl.autoControlled)).done();
		p("Immunity", "%s", on_off(game.player.immune)).done();
		p("Game Scene", "%s", gameScene.getClass().getSimpleName()).done();
		p("", "w=%.0f h=%.0f", gameScene.getSubScene().getWidth(), gameScene.getSubScene().getHeight()).done();
		p("Window Size", "w=%.0f h=%.0f", width, height).done();
		p("Scene Size", "w=%.0f h=%.0f", sceneWidth, sceneHeight).done();
		p("3D Scenes", "%s", on_off(Env.$3D.get())).done();
		if (gameScene.is3D()) {
			// Currently PlayScene3D is the only 3D scene
			var scene3D = (PlayScene3D) gameScene;
			p("Perspective", "%s", Env.$perspective.get()).done();
			p("Camera", "%s", scene3D.cam().info()).done();
			p("Draw Mode", "%s", Env.$drawMode3D.get()).done();
			p("Axes", "%s", on_off(Env.$axesVisible.get())).done();
		} else {
			p("Canvas2D", "w=%.0f h=%.0f", ui.canvas.getWidth(), ui.canvas.getHeight()).done();
		}

		newline();
		p("V", "Switch Pac-Man/Ms. PacMan").when(state == GameState.INTRO);
		p("A", "Autopilot On/Off").done();
		p("E", "Eat all normal pellets").when(gameCtrl.gameRunning);
		p("I", "Player immunity On/Off").done();
		p("L", "Add 3 player lives").when(gameCtrl.gameRunning);
		p("N", "Next Level").when(gameCtrl.gameRunning);
		p("Q", "Quit Screen").when(state != GameState.INTRO);
		p("X", "Kill all hunting ghosts").when(gameCtrl.gameRunning);
		p("Z", "Play Intermission Scenes").when(state == GameState.INTRO);

		newline();
		p("Ctrl+C", "Next Perspective").when(gameScene.is3D());
		p("Ctrl+H", "Wall Height (SHIFT=Decrease)").when(gameScene.is3D());
		p("Ctrl+I", "Information On/Off").done();
		p("Ctrl+L", "Wireframe Mode On/Off").when(gameScene.is3D());
		p("Ctrl+P", "Pause On/Off").done();
		p("Ctrl+R", "Maze resolution (SHIFT=Decrease)").when(gameScene.is3D());
		p("Ctrl+S", "Speed (SHIFT=Decrease)").done();
		p("Ctrl+X", "Axes On/Off").when(gameScene.is3D());
		p("Ctrl+Y", "Toggle tile grid").done();
		p("Ctrl+3", "3D Play Scene On/Off").done();

		textUI.setText(text.toString());
	}

	private final RowBuffer rowBuffer = new RowBuffer();

	private RowBuffer p(String firstColumn, String secondColumnPattern, Object... args) {
		rowBuffer.clear();
		rowBuffer.sb.append(String.format("%-12s: %s\n", firstColumn, String.format(secondColumnPattern, args)));
		return rowBuffer;
	}

	private void newline() {
		text.append("\n");
	}

	private class RowBuffer {

		final StringBuilder sb = new StringBuilder();

		void clear() {
			sb.setLength(0);
		}

		RowBuffer when(boolean condition) {
			if (!condition) {
				sb.setLength(0);
			}
			done();
			return this;
		}

		void done() {
			text.append(sb.toString());
		}
	}
}