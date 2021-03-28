package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.ui.fx.scenes.common.Env;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import javafx.scene.Camera;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Displays information about the current game UI.
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

	private final PacManGameUI_JavaFX ui;
	private final Text textView;
	private String text;

	public HUD(PacManGameUI_JavaFX ui) {
		this.ui = ui;
		textView = new Text();
		textView.setFill(Color.LIGHTGREEN);
		textView.setFont(Font.font("Monospace", 14));
		getChildren().add(textView);
		visibleProperty().bind(Env.$infoViewVisible);
	}

	private void line(String column1, String fmtColumn2, Object... args) {
		String column2 = String.format(fmtColumn2, args) + "\n";
		text += String.format("%-20s: %s", column1, column2);
	}

	private void line() {
		text += "\n";
	}

	private String cameraInfo(Camera camera) {
		return camera == null ? "No camera"
				: String.format("x=%.0f y=%.0f z=%.0f rot=%.0f", camera.getTranslateX(), camera.getTranslateY(),
						camera.getTranslateZ(), camera.getRotate());
	}

	public void update() {
		TickTimer stateTimer = ui.getGameController().stateTimer();
		text = "";
		line("Paused (CTRL+P)", "%s", yesNo(Env.$paused.get()));
		line();
		line("Game Variant", "%s", ui.getGameController().gameVariant());
		line("Playing", "%s", yesNo(ui.getGameController().isGameRunning()));
		line("Attract Mode", "%s", yesNo(ui.getGameController().isAttractMode()));
		line("Game Level", "%d", ui.getGameController().game().currentLevelNumber);
		line("Game State", "%s", ui.getGameController().state);
		line("", "Running:   %s", stateTimer.ticked());
		line("", "Remaining: %s",
				stateTimer.ticksRemaining() == Long.MAX_VALUE ? "indefinite" : stateTimer.ticksRemaining());
		line();
		line("Autopilot (A)", "%s", onOff(ui.getGameController().autopilot.enabled));
		line("Immunity (I)", "%s", onOff(ui.getGameController().isPlayerImmune()));
		line();
		line("Window Size", "w=%.0f h=%.0f", ui.getMainScene().getWindow().getWidth(),
				ui.getMainScene().getWindow().getHeight());
		line("Main Scene Size", "w=%.0f h=%.0f", ui.getMainScene().getWidth(), ui.getMainScene().getHeight());
		line("3D Scenes (CTRL+3)", "%s", onOff(Env.$use3DScenes.get()));
		line();
		line("Game Scene", "%s", ui.getCurrentGameScene().getClass().getSimpleName());
		line("Game Scene Size", "w=%.0f h=%.0f", ui.getCurrentGameScene().getFXSubScene().getWidth(),
				ui.getCurrentGameScene().getFXSubScene().getHeight());
		if (ui.getCurrentGameScene() instanceof AbstractGameScene2D) {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) ui.getCurrentGameScene();
			line("Canvas2D", "w=%.0f h=%.0f", scene2D.getCanvas().getWidth(), scene2D.getCanvas().getHeight());
		} else {
			line("3D Camera (CTRL+S)", "%s", cameraInfo(ui.getCurrentGameScene().getActiveCamera()));
			line("3D Draw Mode (CTRL+L)", "%s", Env.$drawMode.get());
		}
		textView.setText(text);
	}
}