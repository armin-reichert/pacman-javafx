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

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
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
	static final Color BG_COLOR = new Color(0.3, 0.3, 0.3, 0.7);

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

	public void update() {
		double w = ui.getStage().getScene().getWindow().getWidth(),
				h = ui.getStage().getScene().getWindow().getHeight();
		PacManGameState state = ui.getGameController().currentStateID;
		String huntingPhaseName = ui.getGameController().inScatteringPhase() ? "Scattering" : "Chasing";
		TickTimer stateTimer = ui.getGameController().stateTimer();
		text.setLength(0);
		row("Total Ticks", "%d", Env.gameLoop.$totalTicks.get());
		row("Current FPS", "%d Hz", Env.gameLoop.$fps.get());
		row("Target FPS", "%d Hz", Env.gameLoop.getTargetFrameRate());
		row("Game Variant", "%s", ui.getGameController().gameVariant());
		row("Paused", "%s", yes_no(Env.$paused.get()));
		row("Playing", "%s", yes_no(ui.getGameController().isGameRunning()));
		row("Attract Mode", "%s", yes_no(ui.getGameController().isAttractMode()));
		row("Autopilot", "%s", on_off(ui.getGameController().isAutoControlled()));
		row("Immunity", "%s", on_off(ui.getGameController().game().player.immune));
		row("Game Level", "%d", ui.getGameController().game().levelNumber);
		row("Game State", "%s", state == PacManGameState.HUNTING ? state + ":" + huntingPhaseName : state);
		row("", "Running:   %s", stateTimer.ticked());
		row("", "Remaining: %s",
				stateTimer.ticksRemaining() == TickTimer.INDEFINITE ? "indefinite" : stateTimer.ticksRemaining());
		row("Game Scene", "%s", ui.getCurrentGameScene().getClass().getSimpleName());
		row("", "w=%.0f h=%.0f", ui.getCurrentGameScene().getSubSceneFX().getWidth(),
				ui.getCurrentGameScene().getSubSceneFX().getHeight());
		row("Window Size", "w=%.0f h=%.0f", w, h);
		row("Scene Size", "w=%.0f h=%.0f", ui.getStage().getScene().getWidth(), ui.getStage().getScene().getHeight());
		row("3D Scenes", "%s", on_off(Env.$use3DScenes.get()));
		if (ui.getCurrentGameScene() instanceof AbstractGameScene2D) {
			row("Canvas2D", "w=%.0f h=%.0f", ui.getCanvas().getWidth(), ui.getCanvas().getHeight());
		} else {
			PlayScene3D playScene = (PlayScene3D) ui.getCurrentGameScene();
			row("Perspective", "%s", Env.$perspective.get());
			row("Camera", "%s", playScene.currentCameraController().info());
			row("Draw Mode", "%s", Env.$drawMode3D.get());
			row("Axes", "%s", on_off(Env.$axesVisible.get()));
		}

		newRow();
		row("Key V", "Toggle playing Pac-Man vs. Ms. PacMan");
		row("Key A", "Autopilot On/Off");
		row("Key E", "Eat all normal pellets");
		row("Key I", "Toggle player immunity");
		row("Key L", "Add player lives");
		row("Key N", "Enter Next Level");
		row("Key Q", "Quit Game");
		row("Key X", "Kill all hunting ghosts");
		newRow();
		row("Ctrl+C", "Next Camera Perspective");
		row("Ctrl+H", "Increase Wall Height (SHIFT=Decrease)");
		row("Ctrl+I", "Toggle information view");
		row("Ctrl+L", "Toggle 3D Drawing Mode");
		row("Ctrl+P", "Toggle Pause");
		row("Ctrl+R", "Increase Maze Resolution (SHIFT=Decrease)");
		row("Ctrl+S", "Increase Speed (SHIFT=Decrease)");
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
}