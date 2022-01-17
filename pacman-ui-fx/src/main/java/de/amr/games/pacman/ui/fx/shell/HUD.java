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

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.AbstractGameScene;
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

	private final PacManGameUI_JavaFX ui;
	private final Text textUI = new Text();
	private final StringBuilder text = new StringBuilder();

	public HUD(PacManGameUI_JavaFX ui) {
		this.ui = ui;
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

	public void update() {
		final PacManGameController gameController = ui.getGameController();
		final GameModel game = gameController.game();
		final PacManGameState state = gameController.currentStateID;
		final String huntingPhaseName = game.inScatteringPhase() ? "Scattering" : "Chasing";
		final TickTimer stateTimer = gameController.stateTimer();
		final double width = ui.getStage().getScene().getWindow().getWidth();
		final double height = ui.getStage().getScene().getWindow().getHeight();
		final AbstractGameScene currentScene = ui.getCurrentGameScene();
		final double sceneWidth = ui.getStage().getScene().getWidth();
		final double sceneHeight = ui.getStage().getScene().getHeight();

		text.setLength(0);
		row("Total Ticks", "%d", Env.gameLoop.$totalTicks.get());
		row("Target FPS", "%d Hz", Env.gameLoop.getTargetFrameRate());
		row("Current FPS", "%d Hz", Env.gameLoop.$fps.get());
		row("Paused", "%s", yes_no(Env.$paused.get()));
		row("Playing", "%s", yes_no(gameController.isGameRunning()));
		row("Attract Mode", "%s", yes_no(gameController.isAttractMode()));
		row("Game Variant", "%s", gameController.gameVariant());
		row("Game Level", "%d", game.levelNumber);
		row("Game State", "%s",
				state == PacManGameState.HUNTING
						? String.format("%s: Phase #%d (%s)", state, game.huntingPhase, huntingPhaseName)
						: state);
		row("", "Running:   %s%s", stateTimer.ticked(), stateTimer.isStopped() ? " (STOPPED)" : "");
		row("", "Remaining: %s",
				stateTimer.ticksRemaining() == TickTimer.INDEFINITE ? "indefinite" : stateTimer.ticksRemaining());
		row("Autopilot", "%s", on_off(gameController.isAutoControlled()));
		row("Immunity", "%s", on_off(game.player.immune));
		row("Game Scene", "%s", currentScene.getClass().getSimpleName());
		row("", "w=%.0f h=%.0f", currentScene.getSubSceneFX().getWidth(), currentScene.getSubSceneFX().getHeight());
		row("Window Size", "w=%.0f h=%.0f", width, height);
		row("Scene Size", "w=%.0f h=%.0f", sceneWidth, sceneHeight);
		row("3D Scenes", "%s", on_off(Env.$use3DScenes.get()));
		if (currentScene.is3D()) {
			row("Perspective", "%s", Env.$perspective.get());
			row("Camera", "%s", currentScene.currentCameraController().info());
			row("Draw Mode", "%s", Env.$drawMode3D.get());
			row("Axes", "%s", on_off(Env.$axesVisible.get()));
		} else {
			row("Canvas2D", "w=%.0f h=%.0f", ui.getCanvas().getWidth(), ui.getCanvas().getHeight());
		}

		newRow();
		row("Key V", "Switch Pac-Man <-> Ms. PacMan");
		row("Key A", "Autopilot On/Off");
		row("Key E", "Eat all normal pellets");
		row("Key I", "Toggle player immunity");
		row("Key L", "Add player lives");
		row("Key N", "Enter Next Level");
		row("Key Q", "Quit Game");
		row("Key X", "Kill all hunting ghosts");

		newRow();
		row("Ctrl+C", "Next Camera Perspective");
		row("Ctrl+H", "In-/(SHIFT=Decrease) Wall Height");
		row("Ctrl+I", "Toggle information view");
		row("Ctrl+L", "Toggle 3D Drawing Mode");
		row("Ctrl+P", "Toggle Pause");
		row("Ctrl+R", "In-/(SHIFT=Decrease) Resolution");
		row("Ctrl+S", "In-/(SHIFT=Decrease) Speed");
		row("Ctrl+X", "Toggle Show Axes");
		row("Ctrl+1", "Play Intermission Scenes");
		row("Ctrl+3", "Toggle 2D-3D Play Scene");

		textUI.setText(text.toString());
	}

	private void row(String column1, String fmtColumn2, Object... args) {
		String column2 = String.format(fmtColumn2, args);
		text.append(String.format("%-12s: %s\n", column1, column2));
	}

	private void newRow() {
		text.append("\n");
	}
}