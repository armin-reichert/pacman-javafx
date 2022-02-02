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
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
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

	static final int MAX_WIDTH = 400;
	static final Color BG_COLOR = new Color(0.3, 0.3, 0.3, 0.6);

	private static String yes_no(boolean b) {
		return b ? "YES" : "NO";
	}

	private static String on_off(boolean b) {
		return b ? "ON" : "OFF";
	}

	private final Text textUI = new Text();
	private final StringBuilder text = new StringBuilder();

	public HUD() {
		setVisible(false);
		setMaxWidth(MAX_WIDTH);
		setBackground(new Background(new BackgroundFill(BG_COLOR, CornerRadii.EMPTY, Insets.EMPTY)));
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

	public void update(PacManGameUI_JavaFX ui) {
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
		row("Total Ticks", "%d", Env.gameLoop.$totalTicks.get());
		row("Target FPS", "%d Hz", Env.gameLoop.getTargetFrameRate());
		row("Current FPS", "%d Hz", Env.gameLoop.$fps.get());
		row("Paused", "%s", yes_no(Env.$paused.get()));
		row("Playing", "%s", yes_no(gameCtrl.gameRunning));
		row("Attract Mode", "%s", yes_no(gameCtrl.attractMode));
		row("Game Variant", "%s", gameCtrl.gameVariant);
		row("Game Level", "%d", game.levelNumber);
		row("Game State", "%s",
				state == GameState.HUNTING ? String.format("%s: Phase #%d (%s)", state, game.huntingPhase, huntingPhaseName)
						: state);
		row("", "Running:   %s%s", stateTimer.ticked(), stateTimer.isStopped() ? " (STOPPED)" : "");
		row("", "Remaining: %s",
				stateTimer.ticksRemaining() == TickTimer.INDEFINITE ? "indefinite" : stateTimer.ticksRemaining());
		row("Autopilot", "%s", on_off(gameCtrl.autoControlled));
		row("Immunity", "%s", on_off(game.player.immune));
		row("Game Scene", "%s", gameScene.getClass().getSimpleName());
		row("", "w=%.0f h=%.0f", gameScene.getSubScene().getWidth(), gameScene.getSubScene().getHeight());
		row("Window Size", "w=%.0f h=%.0f", width, height);
		row("Scene Size", "w=%.0f h=%.0f", sceneWidth, sceneHeight);
		row("3D Scenes", "%s", on_off(Env.$3D.get()));
		if (gameScene.is3D()) {
			// Currently PlayScene3D is the only 3D scene
			var scene3D = (PlayScene3D) gameScene;
			row("Perspective", "%s", Env.$perspective.get());
			row("Camera", "%s", scene3D.cam().info());
			row("Draw Mode", "%s", Env.$drawMode3D.get());
			row("Axes", "%s", on_off(Env.$axesVisible.get()));
		} else {
			row("Canvas2D", "w=%.0f h=%.0f", ui.canvas.getWidth(), ui.canvas.getHeight());
		}

		row();
		when(state == GameState.INTRO, () -> row("V", "Switch Pac-Man/Ms. PacMan"));
		row("A", "Autopilot On/Off");
		when(gameCtrl.gameRunning, () -> row("E", "Eat all normal pellets"));
		row("I", "Player immunity On/Off");
		when(gameCtrl.gameRunning, () -> row("L", "Add 3 player lives"));
		when(gameCtrl.gameRunning, () -> row("N", "Next Level"));
		when(state != GameState.INTRO, () -> row("Q", "Quit Screen"));
		when(gameCtrl.gameRunning, () -> row("X", "Kill all hunting ghosts"));

		row();
		when(gameScene.is3D(), () -> row("Ctrl+C", "Next Perspective"));
		when(gameScene.is3D(), () -> row("Ctrl+H", "Wall Height (SHIFT=Decrease)"));
		row("Ctrl+I", "Information On/Off");
		when(gameScene.is3D(), () -> row("Ctrl+L", "Wireframe Mode On/Off"));
		row("Ctrl+P", "Pause On/Off");
		when(gameScene.is3D(), () -> row("Ctrl+R", "Maze resolution (SHIFT=Decrease)"));
		row("Ctrl+S", "Speed (SHIFT=Decrease)");
		when(gameScene.is3D(), () -> row("Ctrl+X", "Axes On/Off"));
		when(state == GameState.INTRO, () -> row("Ctrl+1", "Play Intermission Scenes"));
		row("Ctrl+3", "3D Play Scene On/Off");

		textUI.setText(text.toString());
	}

	private void when(boolean condition, Runnable code) {
		if (condition) {
			code.run();
		}
	}

	private void row(String column1, String pattern, Object... args) {
		String column2 = String.format(pattern, args);
		text.append(String.format("%-12s: %s\n", column1, column2));
	}

	private void row() {
		text.append("\n");
	}
}