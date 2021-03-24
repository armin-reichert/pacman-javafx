package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.ui.fx.scenes.common.Env;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class HUD extends HBox {

	private final PacManGameUI_JavaFX ui;
	private final Text textView;
	private String text;

	public HUD(PacManGameUI_JavaFX ui, Pos dockingPosition) {
		this.ui = ui;
		textView = new Text();
		textView.setFill(Color.LIGHTGREEN);
		textView.setFont(Font.font("Monospace", 14));
		getChildren().add(textView);
		visibleProperty().bind(Env.$infoViewVisible);
		StackPane.setAlignment(this, dockingPosition);
		StackPane.setMargin(this, new Insets(10));
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
		text = "";
		line("Paused (CTRL+P)", "%s", Env.$paused.get() ? "YES" : "NO");
		line();
		line("Game Variant", "%s", ui.controller.gameVariant());
		line("Playing", "%s", ui.controller.isGameRunning() ? "YES" : "NO");
		line("Game Level", "%d", ui.controller.game().levelNumber);
		line("Game State", "%s", ui.controller.state);
		TickTimer stateTimer = ui.controller.stateTimer();
		line("", "Running:   %s", stateTimer.ticked());
		line("", "Remaining: %s",
				stateTimer.ticksRemaining() == Long.MAX_VALUE ? "indefinite" : stateTimer.ticksRemaining());
		line();
		line("Autopilot (A)", "%s", ui.controller.autopilot.enabled ? "ON" : "OFF");
		line("Immunity (I)", "%s", ui.controller.game().player.immune ? "ON" : "OFF");
		line();
		line("Window Size", "w=%.0f h=%.0f", ui.mainScene.getWindow().getWidth(), ui.mainScene.getWindow().getHeight());
		line("Main Scene Size", "w=%.0f h=%.0f", ui.mainScene.getWidth(), ui.mainScene.getHeight());
		line("3D Scenes (CTRL+3)", "%s", Env.$use3DScenes.get() ? "ON" : "OFF");
		line();
		line("Game Scene", "%s", ui.currentGameScene.getClass().getSimpleName());
		line("Game Scene Size", "w=%.0f h=%.0f", ui.currentGameScene.getFXSubScene().getWidth(),
				ui.currentGameScene.getFXSubScene().getHeight());
		if (ui.currentGameScene instanceof AbstractGameScene2D) {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) ui.currentGameScene;
			line("Canvas2D", "w=%.0f h=%.0f", scene2D.getCanvas().getWidth(), scene2D.getCanvas().getHeight());
		} else {
			line("3D Camera (CTRL+S)", "%s", cameraInfo(ui.currentGameScene.getActiveCamera()));
			line("3D Draw Mode (CTRL+L)", "%s", Env.$drawMode.get());
		}
		textView.setText(text);
	}
}