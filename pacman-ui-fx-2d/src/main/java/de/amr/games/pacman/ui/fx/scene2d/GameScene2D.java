/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.rendering2d.Rendering2D;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

/**
 * Base class of all 2D scenes. Each 2D scene has its own canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

	private static final float WIDTH = World.TILES_X * TS;
	private static final float HEIGHT = World.TILES_Y * TS;
	private static final float ASPECT_RATIO = WIDTH / HEIGHT;

	protected static double t(double tiles) {
		return tiles * TS;
	}

	public final BooleanProperty infoVisiblePy = new SimpleBooleanProperty(this, "infoVisible", false);

	protected final GameSceneContext context;
	protected final SubScene fxSubScene;
	protected final StackPane root = new StackPane();
	protected final Canvas canvas = new Canvas();
	protected final Pane overlay = new Pane();
	protected final VBox helpRoot = new VBox();

	protected GameScene2D(GameController gameController) {
		checkNotNull(gameController);
		context = new GameSceneContext(gameController);

		fxSubScene = new SubScene(root, WIDTH, HEIGHT);
		canvas.widthProperty().bind(fxSubScene.widthProperty());
		canvas.heightProperty().bind(fxSubScene.heightProperty());

		var scaling = new Scale();
		scaling.xProperty().bind(Bindings.createDoubleBinding(this::canvasScaling, fxSubScene.widthProperty()));
		scaling.yProperty().bind(Bindings.createDoubleBinding(this::canvasScaling, fxSubScene.heightProperty()));
		canvas.getTransforms().add(scaling);
		overlay.getTransforms().add(scaling);

		// TODO check this: This avoids a vertical line on the left side of the embedded 2D game scene
		root.setBackground(ResourceManager.colorBackground(Game2d.assets.wallpaperColor));
		root.getChildren().addAll(canvas, overlay);

		// anchor for help popup
		helpRoot.setTranslateX(10);
		helpRoot.setTranslateY(HEIGHT * 0.2);
		overlay.getChildren().add(helpRoot);

		infoVisiblePy.bind(Game2d.PY_SHOW_DEBUG_INFO);
	}

	/**
	 * Resizes the game scene to the given height, keeping the aspect ratio.
	 * 
	 * @param height new game scene height
	 */
	public void resize(double height) {
		if (height <= 0) {
			throw new IllegalArgumentException("Scene height must be positive");
		}
		var width = ASPECT_RATIO * height;
		fxSubScene.setWidth(width);
		fxSubScene.setHeight(height);
		Logger.trace("{} resized to {0.00} x {0.00}, scaling: {0.00}", getClass().getSimpleName(), width, height,
				canvasScaling());
	}

	@Override
	public void render() {
		var g = canvas.getGraphicsContext2D();
		g.setFill(Game2d.assets.wallpaperColor);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setFill(Color.BLACK);
		g.fillRoundRect(0, 0, WIDTH, HEIGHT, 20, 20);
		if (context.isScoreVisible()) {
			context.game().score().ifPresent(score -> Rendering2D.drawScore(g, score, "SCORE", t(1), t(1)));
			context.game().highScore().ifPresent(score -> Rendering2D.drawScore(g, score, "HIGH SCORE", t(16), t(1)));
		}
		if (context.isCreditVisible()) {
			Rendering2D.drawCredit(g, context.game().credit(), t(2), t(36) - 1);
		}
		drawSceneContent(g);
		if (infoVisiblePy.get()) {
			drawSceneInfo(g);
		}
	}

	@Override
	public GameSceneContext context() {
		return context;
	}

	@Override
	public SubScene fxSubScene() {
		return fxSubScene;
	}

	@Override
	public void onEmbedIntoParentScene(Scene parentScene) {
		resize(parentScene.getHeight());
	}

	@Override
	public void onParentSceneResize(Scene parentScene) {
		resize(parentScene.getHeight());
	}

	@Override
	public boolean is3D() {
		return false;
	}

	public Canvas canvas() {
		return canvas;
	}

	public VBox helpRoot() {
		return helpRoot;
	}

	private double canvasScaling() {
		return fxSubScene.getHeight() / HEIGHT;
	}

	/**
	 * Draws the scene content, e.g. the maze and the guys.
	 * 
	 * @param g graphics context
	 */
	protected abstract void drawSceneContent(GraphicsContext g);

	/**
	 * Draws scene info, e.g. maze structure and special tiles
	 * 
	 * @param g graphics context
	 */
	protected void drawSceneInfo(GraphicsContext g) {
		// empty by default
	}
}