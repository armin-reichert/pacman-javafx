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
import de.amr.games.pacman.ui.fx._3d.scene.Perspective;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Heads-Up-Display with information about the UI.
 * 
 * @author Armin Reichert
 */
public class InfoPanel extends VBox {

	private static String yes_no(boolean b) {
		return b ? "YES" : "NO";
	}

	private static String on_off(boolean b) {
		return b ? "ON" : "OFF";
	}

	private final Color textColor = Color.WHITE;
	private final Font textFont = Font.font("Monospace", 14);
	private final Text textUI = new Text();
	private final StringBuilder text = new StringBuilder();

	public InfoPanel(GameUI ui, int minWidth) {
		setBackground(U.colorBackground(new Color(0.3, 0.3, 0.3, 0.6)));
		setMinWidth(minWidth);
		setPadding(new Insets(5));
		setVisible(false);
		textUI.setFill(textColor);
		textUI.setFont(textFont);
		getChildren().add(textUI);
	}

	public void update(GameUI ui) {
		var width = ui.stage.getScene().getWindow().getWidth();
		var height = ui.stage.getScene().getWindow().getHeight();
		var sceneWidth = ui.stage.getScene().getWidth();
		var sceneHeight = ui.stage.getScene().getHeight();
		var game = ui.gameController.game;
		var state = ui.gameController.state;
		var huntingPhaseName = game.inScatteringPhase() ? "Scattering" : "Chasing";
		var stateTimer = ui.gameController.stateTimer();
		var gameScene = ui.getCurrentGameScene();

		text.setLength(0);
		p("Total Ticks", "%d", Env.gameLoop.$totalTicks.get()).done();
		p("Target FPS", "%d Hz", Env.gameLoop.getTargetFrameRate()).done();
		p("Current FPS", "%d Hz", Env.gameLoop.$fps.get()).done();
		p("Paused", "%s", yes_no(Env.$paused.get())).done();
		p("Playing", "%s", yes_no(ui.gameController.gameRunning)).done();
		p("Attract Mode", "%s", yes_no(ui.gameController.attractMode)).done();
		p("Game Variant", "%s", ui.gameController.gameVariant).done();
		p("Game Level", "%d", game.levelNumber).done();
		p("Game State", "%s",
				state == GameState.HUNTING ? String.format("%s: Phase #%d (%s)", state, game.huntingPhase, huntingPhaseName)
						: state).done();
		p("", "Running:   %s%s", stateTimer.ticked(), stateTimer.isStopped() ? " (STOPPED)" : "").done();
		p("", "Remaining: %s",
				stateTimer.ticksRemaining() == TickTimer.INDEFINITE ? "indefinite" : stateTimer.ticksRemaining()).done();
		p("Autopilot", "%s", on_off(ui.gameController.autoControlled)).done();
		p("Immunity", "%s", on_off(game.player.immune)).done();
		p("Game Scene", "%s", gameScene.getClass().getSimpleName()).done();
		p("", "w=%.0f h=%.0f", gameScene.getFXSubScene().getWidth(), gameScene.getFXSubScene().getHeight()).done();
		p("Window Size", "w=%.0f h=%.0f", width, height).done();
		p("Scene Size", "w=%.0f h=%.0f", sceneWidth, sceneHeight).done();
		p("3D Scenes", "%s", on_off(Env.$3D.get())).done();
		if (gameScene.is3D()) {
			// Currently PlayScene3D is the only 3D scene
			var scene3D = (PlayScene3D) gameScene;
			p("Perspective", "%s", Env.$perspective.get()).done();
			p("Camera", "%s", scene3D.getCamController().info()).done();
			p("Draw Mode", "%s", Env.$drawMode3D.get()).done();
			p("Axes", "%s", on_off(Env.$axesVisible.get())).done();
		} else {
			p("Canvas2D", "w=%.0f h=%.0f", ui.canvas.getWidth(), ui.canvas.getHeight()).done();
		}

		newline();
		p("Ctrl+I", "Information On/Off").done();
		p("Ctrl+J", "Settings On/Off").done();

		newline();
		p("Alt+A", "Autopilot On/Off").done();
		p("Alt+E", "Eat all normal pellets").when(ui.gameController.gameRunning);
		p("Alt+I", "Player immunity On/Off").done();
		p("Alt+L", "Add 3 player lives").when(ui.gameController.gameRunning);
		p("Alt+N", "Next Level").when(ui.gameController.gameRunning);
		p("Alt+Q", "Quit Scene").when(state != GameState.INTRO);
		p("Alt+S", "Speed (SHIFT=Decrease)").done();
		p("Alt+V", "Switch Pac-Man/Ms. Pac-Man").when(state == GameState.INTRO);
		p("Alt+X", "Kill all hunting ghosts").when(ui.gameController.gameRunning);
		p("Alt+Z", "Play Intermission Scenes").when(state == GameState.INTRO);
		p("Alt+LEFT", "%s", Env.perspectiveName(Perspective.values().length - 1)).when(gameScene.is3D());
		p("Alt+RIGHT", "%s", Env.perspectiveName(1)).when(gameScene.is3D());
		p("Alt+3", "3D Playscene On/Off").done();

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