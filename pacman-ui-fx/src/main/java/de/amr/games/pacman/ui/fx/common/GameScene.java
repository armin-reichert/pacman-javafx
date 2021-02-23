package de.amr.games.pacman.ui.fx.common;

import java.util.Optional;

import de.amr.games.pacman.lib.CountdownTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.FlashMessage;
import de.amr.games.pacman.ui.fx.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.rendering.DefaultRendering;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * Each game scene corresponds to a JavaFX scene.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene extends Scene {

	protected final Text flashMessageView;
	protected final GraphicsContext g;
	protected final DefaultRendering rendering;
	protected GameModel game;

	protected Camera camera;

	public GameScene(Group root, double width, double height, double scaling, DefaultRendering rendering) {
		super(root, width, height, Color.BLACK);
		this.rendering = rendering;
		Canvas canvas = new Canvas(width, height);
		g = canvas.getGraphicsContext2D();
		g.scale(scaling, scaling);
		StackPane pane = new StackPane();
		root.getChildren().add(pane);
		flashMessageView = new Text();
		flashMessageView.setFont(Font.font("Serif", FontWeight.BOLD, 10 * scaling));
		flashMessageView.setFill(Color.YELLOW);
		StackPane.setAlignment(flashMessageView, Pos.BOTTOM_CENTER);
		pane.getChildren().addAll(canvas, flashMessageView);

//		camera = new PerspectiveCamera();
//		setOnKeyPressed(e -> {
//			if (e.isControlDown()) {
//				switch (e.getCode()) {
//				case DIGIT0:
//					camera.setTranslateX(0);
//					camera.setTranslateY(0);
//					camera.setTranslateZ(0);
//					break;
//				case LEFT:
//					camera.setTranslateX(camera.getTranslateX() + 10);
//					break;
//				case RIGHT:
//					camera.setTranslateX(camera.getTranslateX() - 10);
//					break;
//				case UP:
//					camera.setTranslateY(camera.getTranslateY() + 10);
//					break;
//				case DOWN:
//					camera.setTranslateY(camera.getTranslateY() - 10);
//					break;
//				case PLUS:
//					camera.setTranslateZ(camera.getTranslateZ() + 10);
//					break;
//				case MINUS:
//					camera.setTranslateZ(camera.getTranslateZ() - 10);
//					break;
//				default:
//					break;
//				}
//			}
//		});
//		setCamera(camera);
	}

	public abstract void update();

	public abstract void render();

	public void setGame(GameModel game) {
		this.game = game;
	}

	public Optional<GameModel> game() {
		return Optional.ofNullable(game);
	}

	public void clear() {
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
	}

	public GraphicsContext gc() {
		return g;
	}

	public void start() {
	}

	public void end() {
	}

	protected void drawFlashMessage() {
		Optional<FlashMessage> message = PacManGameUI_JavaFX.flashMessage();
		if (message.isPresent()) {
			CountdownTimer timer = message.get().timer;
			double alpha = Math.cos((timer.running() * Math.PI / 2.0) / timer.getDuration());
			flashMessageView.setFill(Color.rgb(255, 255, 0, alpha));
		}
		flashMessageView.setText(message.map(msg -> msg.text).orElse(null));
	}
}