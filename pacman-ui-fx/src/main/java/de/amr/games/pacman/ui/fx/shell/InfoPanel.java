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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._3d.scene.Perspective;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Display information about the game and the UI.
 * 
 * @author Armin Reichert
 */
public class InfoPanel extends GridPane {

	private static String yes_no(boolean b) {
		return b ? "YES" : "NO";
	}

	private static String on_off(boolean b) {
		return b ? "ON" : "OFF";
	}

	private static class InfoText extends Text {

		private Supplier<Boolean> fnEvaluate = () -> true;
		private Supplier<Object> fnValue = () -> "Value";

		public InfoText(Supplier<Object> fnValue) {
			this.fnValue = fnValue;
		}

		public InfoText when(Supplier<Boolean> fnEvaluate) {
			this.fnEvaluate = fnEvaluate;
			return this;
		}

		public void update() {
			if (fnEvaluate.get()) {
				setText(String.valueOf(fnValue.get()));
			} else {
				setText("n/a");
			}
		}
	}

	private final Color textColor = Color.WHITE;
	private final Font textFont = Font.font("Monospace", 12);
	private final Font labelFont = Font.font("Sans", 12);
	private final List<InfoText> infos = new ArrayList<>();
	private final GameUI ui;
	private int row;

	private InfoText info(String labelText, Supplier<Object> fnValue) {
		Text label = new Text(labelText);
		label.setFill(textColor);
		label.setFont(labelFont);
		add(label, 0, row);

		Text column = new Text(labelText.equals("") ? "" : ":");
		column.setFill(textColor);
		column.setFont(textFont);
		add(column, 1, row);

		InfoText info = new InfoText(fnValue);
		info.setFill(textColor);
		info.setFont(textFont);
		infos.add(info);
		add(info, 2, row);

		++row;
		return info;
	}

	private InfoText info(String labelText, String value) {
		return info(labelText, () -> value);
	}

	public InfoPanel(GameUI ui) {
		this.ui = ui;

		setBackground(U.colorBackground(new Color(0.3, 0.3, 0.3, 0.6)));
		setHgap(4);
		setVgap(1);
		setPadding(new Insets(5));
		setVisible(false);

		info("Total Ticks", ui.gameLoop::getTotalTicks);
		info("Frame Rate",
				() -> String.format("%d Hz (target: %d Hz)", ui.gameLoop.getFPS(), ui.gameLoop.getTargetFrameRate()));
		info("Paused", () -> yes_no(Env.$paused.get()));
		info("Playing", () -> yes_no(ui.gameController.gameRunning));
		info("Attract Mode", () -> yes_no(ui.gameController.attractMode));
		info("Game Variant", () -> ui.gameController.gameVariant);
		info("Game Level", () -> ui.gameController.game.levelNumber);
		info("Game State", this::fmtGameState);
		info("",
				() -> String.format("Running:   %s%s", stateTimer().ticked(), stateTimer().isStopped() ? " (STOPPED)" : ""));
		info("", () -> String.format("Remaining: %s",
				stateTimer().ticksRemaining() == TickTimer.INDEFINITE ? "indefinite" : stateTimer().ticksRemaining()));
		info("Autopilot", () -> on_off(ui.gameController.autoControlled));
		info("Immunity", () -> on_off(game().player.immune));
		info("Game scene", () -> gameScene().getClass().getSimpleName());
		info("", () -> String.format("w=%.0f h=%.0f", gameScene().getFXSubScene().getWidth(),
				gameScene().getFXSubScene().getHeight()));
		info("Window size", () -> String.format("w=%.0f h=%.0f", sceneWidth(), sceneHeight()));
		info("Scene size", () -> String.format("w=%.0f h=%.0f", sceneWidth(), sceneHeight()));

		info("3D Scenes", () -> on_off(Env.$3D.get()));
		info("Perspective", () -> Env.$perspective.get()).when(() -> gameScene().is3D());
		info("Camera", () -> scene3D().getCamController().info()).when(() -> gameScene().is3D());
		info("Draw Mode", () -> Env.$drawMode3D.get()).when(() -> gameScene().is3D());
		info("Axes", () -> on_off(Env.$axesVisible.get())).when(() -> gameScene().is3D());

//TODO		
//		info("Canvas2D", () -> String.format("w=%.0f h=%.0f", ui.canvas.getWidth(), ui.canvas.getHeight()))
//				.when(() -> !gameScene().is3D());

		info("", "");
		info("Keyboard shortcuts", "");
		info("Ctrl+I", "Information On/Off");
		info("Ctrl+J", "Settings On/Off");
		info("Alt+A", "Autopilot On/Off");
		info("Alt+E", "Eat all normal pellets").when(() -> ui.gameController.gameRunning);
		info("Alt+I", "Player immunity On/Off");
		info("Alt+L", "Add 3 player lives").when(() -> ui.gameController.gameRunning);
		info("Alt+N", "Next Level").when(() -> ui.gameController.gameRunning);
		info("Alt+Q", "Quit Scene").when(() -> gameState() != GameState.INTRO);
		info("Alt+S", "Speed (SHIFT=Decrease)");
		info("Alt+V", "Switch Pac-Man/Ms. Pac-Man").when(() -> gameState() == GameState.INTRO);
		info("Alt+X", "Kill all hunting ghosts").when(() -> ui.gameController.gameRunning);
		info("Alt+Z", "Play Intermission Scenes").when(() -> gameState() == GameState.INTRO);
		info("Alt+LEFT", () -> Env.perspectiveName(Perspective.values().length - 1)).when(() -> gameScene().is3D());
		info("Alt+RIGHT", () -> Env.perspectiveName(1)).when(() -> gameScene().is3D());
		info("Alt+3", "3D Playscene On/Off");
	}

	public void update(GameUI ui) {
		infos.forEach(InfoText::update);
	}

	private PlayScene3D scene3D() {
		return (PlayScene3D) gameScene();
	}

	private double sceneWidth() {
		return ui.stage.getScene().getWindow().getWidth();
	}

	private double sceneHeight() {
		return ui.stage.getScene().getWindow().getHeight();
	}

	private GameModel game() {
		return ui.gameController.game;
	}

	private GameState gameState() {
		return ui.gameController.state;
	}

	private GameScene gameScene() {
		return ui.getCurrentGameScene();
	}

	private String huntingPhaseName() {
		return game().inScatteringPhase() ? "Scattering" : "Chasing";
	}

	private TickTimer stateTimer() {
		return ui.gameController.stateTimer();
	}

	private String fmtGameState() {
		var game = ui.gameController.game;
		var state = ui.gameController.state;
		return state == GameState.HUNTING ? //
				String.format("%s: Phase #%d (%s)", state, game.huntingPhase, huntingPhaseName()) : state.name();
	}
}