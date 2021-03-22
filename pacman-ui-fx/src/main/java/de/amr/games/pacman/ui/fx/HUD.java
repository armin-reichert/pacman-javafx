package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.ui.fx.scenes.common.Env;
import de.amr.games.pacman.ui.fx.scenes.common.scene2d.AbstractGameScene2D;
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
	}

	private void line(String column1, String fmtColumn2, Object... args) {
		String column2 = String.format(fmtColumn2, args) + "\n";
		text += String.format("%-12s: %s", column1, column2);
	}

	private String cameraInfo(Camera camera) {
		return camera == null ? "No camera"
				: String.format("x=%.0f y=%.0f z=%.0f rot=%.0f", camera.getTranslateX(), camera.getTranslateY(),
						camera.getTranslateZ(), camera.getRotate());
	}

	public void update() {
		text = "";
		line("Game Variant", "%s", ui.controller.gameVariant());
		line("Game State", "%s", ui.controller.state);
		TickTimer stateTimer = ui.controller.timer();
		line("", "Running:   %s", stateTimer.ticked());
		line("", "Remaining: %s",
				stateTimer.ticksRemaining() == Long.MAX_VALUE ? "indefinite" : stateTimer.ticksRemaining());
		line("Level", "%d", ui.controller.game().levelNumber);
		line("Paused", "%s (Key CTRL+P)", Env.$paused.get() ? "YES" : "NO");
		line("Window", "w=%.0f h=%.0f", ui.mainScene.getWindow().getWidth(), ui.mainScene.getWindow().getHeight());
		line("Main scene", "w=%.0f h=%.0f", ui.mainScene.getWidth(), ui.mainScene.getHeight());
		line("Game scene:", "%s", ui.currentGameScene.getClass().getSimpleName());
		line("", "w=%.0f h=%.0f", ui.currentGameScene.getFXSubScene().getWidth(),
				ui.currentGameScene.getFXSubScene().getHeight());
		if (ui.currentGameScene instanceof AbstractGameScene2D) {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) ui.currentGameScene;
			line("Canvas2D", "w=%.0f h=%.0f", scene2D.getCanvas().getWidth(), scene2D.getCanvas().getHeight());
		}
		line("Camera", "%s (CTRL+S)", cameraInfo(ui.currentGameScene.getActiveCamera()));
		line("Autopilot", "%s (Key A)", ui.controller.autopilot.enabled ? "ON" : "OFF");
		line("Immunity", "%s (Key I)", ui.controller.game().player.immune ? "ON" : "OFF");
		line("3D scenes", "%s (Key CTRL+3)", Env.$use3DScenes.get() ? "ON" : "OFF");
		textView.setText(text);
	}
}