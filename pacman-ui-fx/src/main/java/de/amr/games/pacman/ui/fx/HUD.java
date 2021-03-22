package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.ui.fx.common.Env;
import de.amr.games.pacman.ui.fx.common.scene2d.AbstractGameScene2D;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class HUD extends HBox {

	private final PacManGameUI_JavaFX userInterface;
	private final Text textView;
	private String text;

	public HUD(PacManGameUI_JavaFX userInterface, Pos dockingPosition) {
		this.userInterface = userInterface;
		textView = new Text();
		textView.setFill(Color.LIGHTGREEN);
		textView.setFont(Font.font("Monospace", 14));
		getChildren().add(textView);
		visibleProperty().bind(Env.$infoViewVisible);
		StackPane.setAlignment(this, dockingPosition);
	}

	private void line(String column1, String fmtColumn2, Object... args) {
		String column2 = String.format(fmtColumn2, args) + "\n";
		text += String.format("%-10s: %s", column1, column2);
	}

	private String cameraInfo(Camera camera) {
		return camera == null ? "No camera"
				: String.format("x=%.0f y=%.0f z=%.0f rot=%.0f", camera.getTranslateX(), camera.getTranslateY(),
						camera.getTranslateZ(), camera.getRotate());
	}

	public void update() {
		text = "";
		line("Game Type", "%s", userInterface.controller.gameVariant());
		line("Game State", "%s", userInterface.controller.state);
		line("Level", "%d", userInterface.controller.game().levelNumber);
		line("Paused", "%s (Key CTRL+P)", Env.$paused.get() ? "YES" : "NO");
		line("Window", "w=%.0f h=%.0f", userInterface.mainScene.getWindow().getWidth(),
				userInterface.mainScene.getWindow().getHeight());
		line("Main scene", "w=%.0f h=%.0f", userInterface.mainScene.getWidth(), userInterface.mainScene.getHeight());
		line("Game scene", "w=%.0f h=%.0f (%s)", userInterface.currentGameScene.getFXSubScene().getWidth(),
				userInterface.currentGameScene.getFXSubScene().getHeight(),
				userInterface.currentGameScene.getClass().getSimpleName());
		if (userInterface.currentGameScene instanceof AbstractGameScene2D) {
			AbstractGameScene2D scene2D = (AbstractGameScene2D) userInterface.currentGameScene;
			line("Canvas2D", "w=%.0f h=%.0f", scene2D.getCanvas().getWidth(), scene2D.getCanvas().getHeight());
		}
		line("Camera", "%s (CTRL+S)", cameraInfo(userInterface.currentGameScene.getActiveCamera()));
		line("Autopilot", "%s (Key A)", userInterface.controller.autopilot.enabled ? "ON" : "OFF");
		line("Immunity", "%s (Key I)", userInterface.controller.game().player.immune ? "ON" : "OFF");
		line("3D scenes", "%s (Key CTRL+3)", Env.$use3DScenes.get() ? "ON" : "OFF");
		textView.setText(text);
	}
}