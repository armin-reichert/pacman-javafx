package de.amr.games.pacman.ui.fx.shell;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.scenes.common._2d.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlayScene3D;
import javafx.scene.Camera;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Heads-Up-Display with information about the UI.
 * 
 * @author Armin Reichert
 */
public class HUD extends HBox {

	private static String yesNo(boolean b) {
		return b ? "YES" : "NO";
	}

	private static String onOff(boolean b) {
		return b ? "ON" : "OFF";
	}

	private final Text textView = new Text();
	private String text;

	public HUD() {
		visibleProperty().bind(Env.$isHUDVisible);
		textView.setFill(Color.WHITE);
		textView.setFont(Font.font("Monospace", 14));
		getChildren().add(textView);
	}

	public void update(PacManGameUI_JavaFX ui) {
		TickTimer stateTimer = ui.gameController.stateTimer();
		text = "";
		line("Total Ticks", "%d", Env.$totalTicks.get());
		line("Frame rate", "%d Hz", Env.$fps.get());
		line("Speed (CTRL/SHIFT+S)", "%.0f%%", 100.0 / Env.$slowDown.get());
		line("Paused (CTRL+P)", "%s", yesNo(Env.$paused.get()));
		skip();
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
		skip();
		line("Game Scene", "%s", ui.currentGameScene.getClass().getSimpleName());
		line("", "w=%.0f h=%.0f", ui.currentGameScene.getSubSceneFX().getWidth(),
				ui.currentGameScene.getSubSceneFX().getHeight());
		skip();
		line("Window Size", "w=%.0f h=%.0f", ui.mainScene.getWindow().getWidth(), ui.mainScene.getWindow().getHeight());
		line("Main Scene Size", "w=%.0f h=%.0f", ui.mainScene.getWidth(), ui.mainScene.getHeight());
		skip();
		line("3D Scenes (CTRL+3)", "%s", onOff(Env.$use3DScenes.get()));
		if (ui.currentGameScene instanceof AbstractGameScene2D) {
			line("Canvas2D", "w=%.0f h=%.0f", ui.canvas.getWidth(), ui.canvas.getHeight());
		} else {
			if (ui.currentGameScene instanceof PlayScene3D) {
				PlayScene3D playScene = (PlayScene3D) ui.currentGameScene;
				line("Perspective (CTRL+C)", "%s", playScene.selectedPerspective());
				line("Camera", "%s", cameraInfo(playScene.getSubSceneFX().getCamera()));
			}
			skip();
			line("Draw Mode (CTRL+L)", "%s", Env.$drawMode3D.get());
			line("Axes (CTRL+X)", "%s", onOff(Env.$axesVisible.get()));
		}
		skip();
		line("Autopilot (A)", "%s", onOff(ui.gameController.isAutoControlled()));
		line("Immunity (I)", "%s", onOff(ui.gameController.isPlayerImmune()));
		textView.setText(text);
	}

	private void line(String column1, String fmtColumn2, Object... args) {
		String column2 = String.format(fmtColumn2, args) + "\n";
		text += String.format("%-20s: %s", column1, column2);
	}

	private void skip() {
		text += "\n";
	}

	private String cameraInfo(Camera camera) {
		return camera == null ? "No camera"
				: String.format("x=%.0f y=%.0f z=%.0f rot=%.0f", camera.getTranslateX(), camera.getTranslateY(),
						camera.getTranslateZ(), camera.getRotate());
	}
}