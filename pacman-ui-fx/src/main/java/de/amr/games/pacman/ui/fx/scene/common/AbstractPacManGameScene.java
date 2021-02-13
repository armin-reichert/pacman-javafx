package de.amr.games.pacman.ui.fx.scene.common;

import java.util.Optional;

import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.PacManGameAnimation;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering.MsPacManSceneRendering;
import de.amr.games.pacman.ui.fx.rendering.PacManSceneRendering;
import de.amr.games.pacman.ui.fx.rendering.SceneRendering;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Common base class for scenes. Each game scene corresponds to a JavaFX scene.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractPacManGameScene implements PacManGameScene {

	protected final Scene scene;
	protected final Keyboard keyboard;
	protected final PacManGameModel game;
	protected final GraphicsContext g;
	protected final SceneRendering rendering;
	protected final SoundManager soundManager;

	public AbstractPacManGameScene(PacManGameModel game, SoundManager soundManager, double width, double height,
			double scaling, boolean msPacMan) {
		this.game = game;
		this.soundManager = soundManager;
		Canvas canvas = new Canvas(width, height);
		g = canvas.getGraphicsContext2D();
		g.scale(scaling, scaling);
		StackPane pane = new StackPane();
		pane.getChildren().add(canvas);
		scene = new Scene(pane, width, height);
		keyboard = new Keyboard(scene);
		rendering = msPacMan ? new MsPacManSceneRendering(g) : new PacManSceneRendering(g);
	}

	public void fill(Color color) {
		g.setFill(color);
		g.fillRect(0, 0, g.getCanvas().getWidth(), g.getCanvas().getHeight());
	}

	@Override
	public void start() {
	}

	@Override
	public void end() {
	}

	@Override
	public Scene getFXScene() {
		return scene;
	}

	@Override
	public Keyboard keyboard() {
		return keyboard;
	}

	@Override
	public Optional<PacManGameAnimation> animation() {
		return rendering instanceof PacManGameAnimation ? Optional.of(rendering) : Optional.empty();
	}

	public Rectangle2D tileRegion(int tileX, int tileY, int cols, int rows) {
		return new Rectangle2D(tileX * 16, tileY * 16, cols * 16, rows * 16);
	}
}