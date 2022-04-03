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
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Display information about the game and the UI.
 * 
 * @author Armin Reichert
 */
public class InfoPanel extends VBox {

	private final GameUI ui;
	private List<InfoText> infos = new ArrayList<>();
	private Section sectionGeneral, sectionGame, section3D, section2D, sectionKeys;

	private class Section extends TitledPane {

		private final GridPane content = new GridPane();
		private Color textColor = Color.WHITE;
		private Font textFont = Font.font("Monospace", 12);
		private Font labelFont = Font.font("Sans", 12);
		private int row;

		public Section(String title) {
			setOpacity(0.7);
			setFocusTraversable(false);
			setText(title);
			setContent(content);
			content.setBackground(U.colorBackground(new Color(0.1, 0.1, 0.1, 0.8)));
			content.setHgap(4);
			content.setVgap(1);
			content.setPadding(new Insets(5));
		}

		public InfoText info(String labelText, Supplier<Object> fnValue) {
			Label label = new Label(labelText);
			label.setTextFill(textColor);
			label.setFont(labelFont);
			label.setMinWidth(150);
			content.add(label, 0, row);

			Label separator = new Label(labelText.length() == 0 ? "" : ":");
			separator.setTextFill(textColor);
			separator.setFont(textFont);
			content.add(separator, 1, row);

			InfoText info = new InfoText(fnValue);
			info.setFill(textColor);
			info.setFont(textFont);
			infos.add(info);
			content.add(info, 2, row);

			++row;
			return info;
		}

		public InfoText info(String labelText, String value) {
			return info(labelText, () -> value);
		}
	}

	public InfoPanel(GameUI ui) {
		this.ui = ui;
		setVisible(false);

		sectionGeneral = new Section("General");
		sectionGeneral.info("Total Ticks", GameLoop.get()::getTotalTicks);
		sectionGeneral.info("Frame Rate", () -> String.format("%d Hz (target: %d Hz)", GameLoop.get().getFPS(),
				GameLoop.get().getTargetFrameRate()));
		sectionGeneral.info("Paused", () -> U.yes_no(Env.$paused.get()));
		sectionGeneral.info("Playing", () -> U.yes_no(ui.gameController.gameRunning));
		sectionGeneral.info("Attract Mode", () -> U.yes_no(ui.gameController.attractMode));
		sectionGeneral.info("Autopilot", () -> U.on_off(ui.gameController.autoControlled));
		sectionGeneral.info("Immunity", () -> U.on_off(game().player.immune));
		sectionGeneral.info("Game scene", () -> gameScene().getClass().getSimpleName());
		sectionGeneral.info("", () -> String.format("w=%.0f h=%.0f", gameScene().getFXSubScene().getWidth(),
				gameScene().getFXSubScene().getHeight()));
		sectionGeneral.info("Window", () -> String.format("w=%.0f h=%.0f", sceneWidth(), sceneHeight()));
		sectionGeneral.info("Main scene", () -> String.format("w=%.0f h=%.0f", sceneWidth(), sceneHeight()));

		sectionGame = new Section("Game");
		sectionGame.info("Game Variant", () -> ui.gameController.gameVariant);
		sectionGame.info("Game Level", () -> ui.gameController.game.levelNumber);
		sectionGame.info("Game State", this::fmtGameState);
		sectionGame.info("", () -> String.format("Running:   %s%s", stateTimer().ticked(),
				stateTimer().isStopped() ? " (STOPPED)" : ""));
		sectionGame.info("", () -> String.format("Remaining: %s",
				stateTimer().ticksRemaining() == TickTimer.INDEFINITE ? "indefinite" : stateTimer().ticksRemaining()));
		sectionGame.info("Ghost speed", () -> fmtSpeed(game().ghostSpeed));
		sectionGame.info("Ghost speed (frightened)", () -> fmtSpeed(game().ghostSpeedFrightened));
		sectionGame.info("Pac-Man speed", () -> fmtSpeed(game().playerSpeed));
		sectionGame.info("Pac-Man speed (power)", () -> fmtSpeed(game().playerSpeedPowered));
		sectionGame.info("Bonus value", () -> game().bonusValue(game().bonusSymbol));
		sectionGame.info("Maze flashings", () -> game().numFlashes);

		section3D = new Section("3D");
		section3D.info("3D Scenes", () -> U.on_off(Env.$3D.get()));
		section3D.info("Perspective", () -> Env.$perspective.get()).when(() -> gameScene().is3D());
		section3D.info("Camera", () -> scene3D().getCamController().info()).when(() -> gameScene().is3D());
		section3D.info("Draw Mode", () -> Env.$drawMode3D.get()).when(() -> gameScene().is3D());
		section3D.info("Axes", () -> U.on_off(Env.$axesVisible.get())).when(() -> gameScene().is3D());

		section2D = new Section("2D");
		section2D.info("Canvas2D", () -> {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) ui.getCurrentGameScene();
			return String.format("w=%.0f h=%.0f", scene2D.getCanvas().getWidth(), scene2D.getCanvas().getHeight());
		}).when(() -> !gameScene().is3D());

		sectionKeys = new Section("Keyboard Shortcuts");
		sectionKeys.setExpanded(false);
		sectionKeys.info("Ctrl+I", "Information On/Off");
		sectionKeys.info("Ctrl+J", "Settings On/Off");
		sectionKeys.info("Alt+A", "Autopilot On/Off");
		sectionKeys.info("Alt+E", "Eat all normal pellets").when(() -> ui.gameController.gameRunning);
		sectionKeys.info("Alt+I", "Player immunity On/Off");
		sectionKeys.info("Alt+L", "Add 3 player lives").when(() -> ui.gameController.gameRunning);
		sectionKeys.info("Alt+N", "Next Level").when(() -> ui.gameController.gameRunning);
		sectionKeys.info("Alt+Q", "Quit Scene").when(() -> gameState() != GameState.INTRO);
		sectionKeys.info("Alt+S", "Speed (SHIFT=Decrease)");
		sectionKeys.info("Alt+V", "Switch Pac-Man/Ms. Pac-Man").when(() -> gameState() == GameState.INTRO);
		sectionKeys.info("Alt+X", "Kill all hunting ghosts").when(() -> ui.gameController.gameRunning);
		sectionKeys.info("Alt+Z", "Play Intermission Scenes").when(() -> gameState() == GameState.INTRO);
		sectionKeys.info("Alt+LEFT", () -> Env.perspectiveShifted(-1).name()).when(() -> gameScene().is3D());
		sectionKeys.info("Alt+RIGHT", () -> Env.perspectiveShifted(1).name()).when(() -> gameScene().is3D());
		sectionKeys.info("Alt+3", "3D Playscene On/Off");

		getChildren().addAll(sectionGeneral, sectionGame, section3D, section2D, sectionKeys);
	}

	public void update(GameUI ui) {
		infos.forEach(InfoText::update);
	}

	private String fmtSpeed(float fraction) {
		return String.format("%.2f px/sec", GameModel.BASE_SPEED * fraction);
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