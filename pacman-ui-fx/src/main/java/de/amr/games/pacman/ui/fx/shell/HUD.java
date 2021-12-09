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
import javafx.geometry.Insets;
import javafx.scene.Camera;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Heads-Up-Display with information about the UI.
 * 
 * @author Armin Reichert
 */
public class HUD extends VBox {

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
		getChildren().add(textUI);
		visibleProperty().bind(Env.$isHUDVisible);
		setMaxWidth(400);
		Color bgColor = new Color(0.3, 0.3, 0.3, 0.6);
		setBackground(new Background(new BackgroundFill(bgColor, CornerRadii.EMPTY, Insets.EMPTY)));
		textUI.setFill(Color.WHITE);
		textUI.setFont(Font.font("Monospace", 14));
	}

	public void update() {
		TickTimer stateTimer = ui.getGameController().stateTimer();
		text.setLength(0);
		line("Total Ticks", "%d", Env.$totalTicks.get());
		line("Frame rate", "%d Hz", Env.$fps.get());
		line("Target frame rate", "%d Hz", Env.gameLoop.getTargetFrameRate());
		line("Paused (CTRL+P)", "%s", yes_no(Env.$paused.get()));
		newline();
		line("Game Variant", "%s", ui.getGameController().gameVariant());
		line("Playing", "%s", yes_no(ui.getGameController().isGameRunning()));
		line("Attract Mode", "%s", yes_no(ui.getGameController().isAttractMode()));
		line("Game Level", "%d", ui.getGameController().game().level().number);
		PacManGameState state = ui.getGameController().state;
		String huntingPhaseName = ui.getGameController().inScatteringPhase() ? "Scattering" : "Chasing";
		line("Game State", "%s", state == PacManGameState.HUNTING ? state + ":" + huntingPhaseName : state);
		line("", "Running:   %s", stateTimer.ticked());
		line("", "Remaining: %s",
				stateTimer.ticksRemaining() == TickTimer.INDEFINITE ? "indefinite" : stateTimer.ticksRemaining());
		newline();
		line("Game Scene", "%s", ui.getCurrentGameScene().getClass().getSimpleName());
		line("", "w=%.0f h=%.0f", ui.getCurrentGameScene().getSubSceneFX().getWidth(),
				ui.getCurrentGameScene().getSubSceneFX().getHeight());
		newline();
		double w = ui.getStage().getScene().getWindow().getWidth(), h = ui.getStage().getScene().getWindow().getHeight();
		line("Window Size", "w=%.0f h=%.0f", w, h);
		line("Scene Size", "w=%.0f h=%.0f", ui.getStage().getScene().getWidth(), ui.getStage().getScene().getHeight());
		newline();
		line("3D Scenes (CTRL+3)", "%s", on_off(Env.$use3DScenes.get()));
		if (ui.getCurrentGameScene() instanceof AbstractGameScene2D) {
			line("Canvas2D", "w=%.0f h=%.0f", ui.getCanvas().getWidth(), ui.getCanvas().getHeight());
		} else {
			if (ui.getCurrentGameScene() instanceof PlayScene3D) {
				PlayScene3D playScene = (PlayScene3D) ui.getCurrentGameScene();
				line("Camera (CTRL+C)", "%s", playScene.selectedCam());
				line("", "%s", cameraInfo(playScene.getSubSceneFX().getCamera()));
			}
			newline();
			line("Draw Mode (CTRL+L)", "%s", Env.$drawMode3D.get());
			line("Axes (CTRL+X)", "%s", on_off(Env.$axesVisible.get()));
		}
		newline();
		line("Autopilot", "%s", on_off(ui.getGameController().isAutoControlled()));
		line("Immunity", "%s", on_off(ui.getGameController().game().player().immune));

		newline();
		line("Keyboard shortcuts", "");
		line("Key V", "Intro Screen: Pac-Man <-> Ms. PacMan");
		line("Key A", "Autopilot On/Off");
		line("Key E", "Eat all normal pellets");
		line("Key I", "Toggle player immunity");
		line("Key L", "Add player lives");
		line("Key N", "Enter next level");
		line("Key Q", "Quit");
		line("Key X", "Kill all hunting ghosts");
		textUI.setText(text.toString());
	}

	private void line(String column1, String fmtColumn2, Object... args) {
		String column2 = String.format(fmtColumn2, args) + "\n";
		text.append(String.format("%-20s: %s", column1, column2));
	}

	private void newline() {
		text.append("\n");
	}

	private String cameraInfo(Camera camera) {
		return camera == null ? "No camera"
				: String.format("x=%.0f y=%.0f z=%.0f rot=%.0f", camera.getTranslateX(), camera.getTranslateY(),
						camera.getTranslateZ(), camera.getRotate());
	}
}