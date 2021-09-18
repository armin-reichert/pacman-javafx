package de.amr.games.pacman.ui.fx.shell;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.scene.common.AbstractGameScene2D;
import de.amr.games.pacman.ui.fx._3d.scene.PlayScene3D;
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

	private static String yes_no(boolean b) {
		return b ? "YES" : "NO";
	}

	private static String on_off(boolean b) {
		return b ? "ON" : "OFF";
	}

	private final PacManGameUI_JavaFX ui;
	private final StringBuilder text = new StringBuilder();

	public HUD(PacManGameUI_JavaFX ui) {
		this.ui = ui;
		visibleProperty().bind(Env.$isHUDVisible);
		setFill(Color.WHITE);
		setFont(Font.font("Monospace", 14));
		Env.$totalTicks.addListener((totalTicks, oldTicks, newTicks) -> update());
	}

	public void update() {
		TickTimer stateTimer = ui.getGameController().stateTimer();
		text.setLength(0);
		line("Total Ticks", "%d", Env.$totalTicks.get());
		line("Frame rate", "%d Hz", Env.$fps.get());
		line("Speed (CTRL/SHIFT+S)", "%.0f%%", 100.0 / Env.$slowDown.get());
		line("Paused (CTRL+P)", "%s", yes_no(Env.$paused.get()));
		newline();
		line("Game Variant", "%s", ui.getGameController().game().variant());
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
		double w = ui.getStage().getScene().getWindow().getWidth(),
				h = ui.getStage().getScene().getWindow().getHeight();
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
		line("Autopilot (A)", "%s", on_off(ui.getGameController().isAutoControlled()));
		line("Immunity (I)", "%s", on_off(ui.getGameController().isPlayerImmune()));
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