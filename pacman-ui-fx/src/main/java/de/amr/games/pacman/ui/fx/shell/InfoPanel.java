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

import java.util.stream.Stream;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.GameLoop;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.layout.VBox;

/**
 * Display information about the game and the UI.
 * 
 * @author Armin Reichert
 */
public class InfoPanel extends VBox {

	private final GameUI ui;
	private InfoSection sectionGeneral, sectionGame, section3D, section2D, sectionKeys;

	public InfoPanel(GameUI ui) {
		this.ui = ui;
		setVisible(false);

		sectionGeneral = new InfoSection("General");
		sectionGeneral.addInfo("Total Ticks", GameLoop.get()::getTotalTicks);
		sectionGeneral.addInfo("Frame Rate", () -> String.format("%d Hz (target: %d Hz)", GameLoop.get().getFPS(),
				GameLoop.get().getTargetFrameRate()));
		sectionGeneral.addInfo("Paused", () -> U.yes_no(Env.$paused.get()));
		sectionGeneral.addInfo("Playing", () -> U.yes_no(ui.gameController.gameRunning));
		sectionGeneral.addInfo("Attract Mode", () -> U.yes_no(ui.gameController.attractMode));
		sectionGeneral.addInfo("Autopilot", () -> U.on_off(ui.gameController.autoControlled));
		sectionGeneral.addInfo("Immunity", () -> U.on_off(game().player.immune));
		sectionGeneral.addInfo("Game scene", () -> gameScene().getClass().getSimpleName());
		sectionGeneral.addInfo("", () -> String.format("w=%.0f h=%.0f", gameScene().getFXSubScene().getWidth(),
				gameScene().getFXSubScene().getHeight()));
		sectionGeneral.addInfo("Window", () -> String.format("w=%.0f h=%.0f", sceneWidth(), sceneHeight()));
		sectionGeneral.addInfo("Main scene", () -> String.format("w=%.0f h=%.0f", sceneWidth(), sceneHeight()));

		sectionGame = new InfoSection("Game");
		sectionGame.addInfo("Game Variant", () -> ui.gameController.gameVariant);
		sectionGame.addInfo("Game Level", () -> ui.gameController.game.levelNumber);
		sectionGame.addInfo("Game State", this::fmtGameState);
		sectionGame.addInfo("", () -> String.format("Running:   %s%s", stateTimer().ticked(),
				stateTimer().isStopped() ? " (STOPPED)" : ""));
		sectionGame.addInfo("", () -> String.format("Remaining: %s",
				stateTimer().ticksRemaining() == TickTimer.INDEFINITE ? "indefinite" : stateTimer().ticksRemaining()));
		sectionGame.addInfo("Ghost speed", () -> fmtSpeed(game().ghostSpeed));
		sectionGame.addInfo("Ghost speed (frightened)", () -> fmtSpeed(game().ghostSpeedFrightened));
		sectionGame.addInfo("Pac-Man speed", () -> fmtSpeed(game().playerSpeed));
		sectionGame.addInfo("Pac-Man speed (power)", () -> fmtSpeed(game().playerSpeedPowered));
		sectionGame.addInfo("Bonus value", () -> game().bonusValue(game().bonusSymbol));
		sectionGame.addInfo("Maze flashings", () -> game().numFlashes);

		section3D = new InfoSection("3D");
		section3D.addInfo("3D Scenes", () -> U.on_off(Env.$3D.get()));
		section3D.addInfo("Perspective", () -> Env.$perspective.get()).when(() -> gameScene().is3D());
		section3D.addInfo("Camera", () -> scene3D().getCamController().info()).when(() -> gameScene().is3D());
		section3D.addInfo("Draw Mode", () -> Env.$drawMode3D.get()).when(() -> gameScene().is3D());
		section3D.addInfo("Axes", () -> U.on_off(Env.$axesVisible.get())).when(() -> gameScene().is3D());

		section2D = new InfoSection("2D");
		section2D.addInfo("Canvas2D", () -> {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) ui.getCurrentGameScene();
			return String.format("w=%.0f h=%.0f", scene2D.getCanvas().getWidth(), scene2D.getCanvas().getHeight());
		}).when(() -> !gameScene().is3D());

		sectionKeys = new InfoSection("Keyboard Shortcuts");
		sectionKeys.setExpanded(false);
		sectionKeys.addInfo("Ctrl+I", "Information On/Off");
		sectionKeys.addInfo("Ctrl+J", "Settings On/Off");
		sectionKeys.addInfo("Alt+A", "Autopilot On/Off");
		sectionKeys.addInfo("Alt+E", "Eat all normal pellets").when(() -> ui.gameController.gameRunning);
		sectionKeys.addInfo("Alt+I", "Player immunity On/Off");
		sectionKeys.addInfo("Alt+L", "Add 3 player lives").when(() -> ui.gameController.gameRunning);
		sectionKeys.addInfo("Alt+N", "Next Level").when(() -> ui.gameController.gameRunning);
		sectionKeys.addInfo("Alt+Q", "Quit Scene").when(() -> gameState() != GameState.INTRO);
		sectionKeys.addInfo("Alt+S", "Speed (SHIFT=Decrease)");
		sectionKeys.addInfo("Alt+V", "Switch Pac-Man/Ms. Pac-Man").when(() -> gameState() == GameState.INTRO);
		sectionKeys.addInfo("Alt+X", "Kill all hunting ghosts").when(() -> ui.gameController.gameRunning);
		sectionKeys.addInfo("Alt+Z", "Play Intermission Scenes").when(() -> gameState() == GameState.INTRO);
		sectionKeys.addInfo("Alt+LEFT", () -> Env.perspectiveShifted(-1).name()).when(() -> gameScene().is3D());
		sectionKeys.addInfo("Alt+RIGHT", () -> Env.perspectiveShifted(1).name()).when(() -> gameScene().is3D());
		sectionKeys.addInfo("Alt+3", "3D Playscene On/Off");

		getChildren().addAll(sectionGeneral, sectionGame, section3D, section2D, sectionKeys);
	}

	public void update(GameUI ui) {
		Stream.of(sectionGeneral, sectionGame, section3D, section2D, sectionKeys)
				.forEach(section -> section.infos.forEach(InfoText::update));
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