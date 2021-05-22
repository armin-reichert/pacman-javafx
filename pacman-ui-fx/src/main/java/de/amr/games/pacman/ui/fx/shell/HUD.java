package de.amr.games.pacman.ui.fx.shell;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScene3DBase;
import javafx.scene.Camera;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Heads-Up-Display with information about the UI.
 * 
 * @author Armin Reichert
 */
public class HUD extends Text {

	private static String yesNo(boolean b) {
		return b ? "YES" : "NO";
	}

	private static String onOff(boolean b) {
		return b ? "ON" : "OFF";
	}

	private final StringBuilder text = new StringBuilder();

	public HUD() {
		visibleProperty().bind(Env.$isHUDVisible);
		setFill(Color.WHITE);
		setFont(Font.font("Monospace", 14));
	}

	public void update(PacManGameUI_JavaFX ui) {
		TickTimer stateTimer = ui.gameController.stateTimer();
		text.setLength(0);
		line("Total Ticks", "%d", Env.$totalTicks.get());
		line("Frame rate", "%d Hz", Env.$fps.get());
		line("Speed (CTRL/SHIFT+S)", "%.0f%%", 100.0 / Env.$slowDown.get());
		line("Paused (CTRL+P)", "%s", yesNo(Env.$paused.get()));
		newline();
		line("Game Variant", "%s", ui.gameController.game().variant());
		line("Playing", "%s", yesNo(ui.gameController.isGameRunning()));
		line("Attract Mode", "%s", yesNo(ui.gameController.isAttractMode()));
		line("Game Level", "%d", ui.gameController.game().currentLevel().number);
		PacManGameState state = ui.gameController.state;
		String huntingPhase = ui.gameController.huntingPhase % 2 == 0 ? "Scattering" : "Chasing";
		line("Game State", "%s", state == PacManGameState.HUNTING ? state + ":" + huntingPhase : state);
		line("", "Running:   %s", stateTimer.ticked());
		line("", "Remaining: %s",
				stateTimer.ticksRemaining() == Long.MAX_VALUE ? "indefinite" : stateTimer.ticksRemaining());
		newline();
		line("Game Scene", "%s", ui.currentGameScene.getClass().getSimpleName());
		line("", "w=%.0f h=%.0f", ui.currentGameScene.getSubSceneFX().getWidth(),
				ui.currentGameScene.getSubSceneFX().getHeight());
		newline();
		line("Window Size", "w=%.0f h=%.0f", ui.mainScene.getWindow().getWidth(), ui.mainScene.getWindow().getHeight());
		line("Main Scene Size", "w=%.0f h=%.0f", ui.mainScene.getWidth(), ui.mainScene.getHeight());
		newline();
		line("3D Scenes (CTRL+3)", "%s", onOff(Env.$use3DScenes.get()));
		if (ui.currentGameScene instanceof AbstractGameScene2D) {
			line("Canvas2D", "w=%.0f h=%.0f", ui.canvas.getWidth(), ui.canvas.getHeight());
		} else {
			if (ui.currentGameScene instanceof PlayScene3DBase) {
				PlayScene3DBase playScene = (PlayScene3DBase) ui.currentGameScene;
				line("Perspective (CTRL+C)", "%s", playScene.selectedPerspective());
				line("Camera", "%s", cameraInfo(playScene.getSubSceneFX().getCamera()));
			}
			newline();
			line("Draw Mode (CTRL+L)", "%s", Env.$drawMode3D.get());
			line("Axes (CTRL+X)", "%s", onOff(Env.$axesVisible.get()));
		}
		newline();
		line("Autopilot (A)", "%s", onOff(ui.gameController.isAutoControlled()));
		line("Immunity (I)", "%s", onOff(ui.gameController.isPlayerImmune()));
		setText(text.toString());
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