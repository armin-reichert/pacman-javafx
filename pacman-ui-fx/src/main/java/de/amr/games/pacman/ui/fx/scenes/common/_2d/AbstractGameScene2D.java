package de.amr.games.pacman.ui.fx.scenes.common._2d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

/**
 * Base class of all 2D scenes that use a canvas for being rendered.
 * 
 * @author Armin Reichert
 */
public abstract class AbstractGameScene2D<RENDERING extends Rendering2D> implements GameScene {

	protected final SubScene scene;
	protected final Canvas canvas;
	protected final GraphicsContext gc;
	protected final double unscaledWidth;
	protected final double unscaledHeight;
	protected final double aspectRatio;
	protected final RENDERING rendering;
	protected final SoundManager sounds;
	protected PacManGameController gameController;

	public AbstractGameScene2D(double unscaledWidth, double unscaledHeight, RENDERING rendering, SoundManager sounds) {
		this.rendering = rendering;
		this.sounds = sounds;
		this.unscaledWidth = unscaledWidth;
		this.unscaledHeight = unscaledHeight;
		aspectRatio = unscaledWidth / unscaledHeight;
		canvas = new Canvas(unscaledWidth, unscaledHeight);
		gc = canvas.getGraphicsContext2D();
		scene = new SubScene(new Group(canvas), unscaledWidth, unscaledHeight);
		scene.widthProperty().bind(canvas.widthProperty());
		scene.heightProperty().bind(canvas.heightProperty());
	}

	@Override
	public final OptionalDouble aspectRatio() {
		return OptionalDouble.of(aspectRatio);
	}

	@Override
	public PacManGameController getGameController() {
		return gameController;
	}

	@Override
	public void setGameController(PacManGameController controller) {
		this.gameController = controller;
	}

	public Rendering2D getRendering() {
		return rendering;
	}

	@Override
	public void stretchTo(double width, double height) {
		width = aspectRatio().getAsDouble() * height;
		canvas.setWidth(width);
		canvas.setHeight(height);
		double scaling = height / unscaledHeight;
		canvas.getTransforms().setAll(new Scale(scaling, scaling));
	}

	@Override
	public void init() {
		log("Game scene %s: start", this);
	}

	@Override
	public void end() {
		log("Game scene %s: end", this);
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
	}

	@Override
	public void update() {
	}

	public final void render() {
		gc.setFill(Color.BLACK);
		gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		doRender();
	}

	protected abstract void doRender();

	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public SubScene getSubScene() {
		return scene;
	}

	protected void renderLevelCounter(V2i tileRight) {
		int levelNumber = game().currentLevel().number;
		int x = tileRight.x * TS, y = tileRight.y * TS;
		int firstLevel = Math.max(1, levelNumber - 6);
		for (int level = firstLevel; level <= levelNumber; ++level) {
			Rectangle2D sprite = rendering.getSymbolSprites().get(game().levelSymbol(level));
			rendering.renderSprite(gc, sprite, x, y);
			x -= t(2);
		}
	}
}