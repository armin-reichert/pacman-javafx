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

	public static final int TILES_X = 28;
	public static final int TILES_Y = 36;
	public static final int WIDTH = TILES_X * TS;
	public static final int HEIGHT = TILES_Y * TS;
	public static final float ASPECT_RATIO = (float) WIDTH / HEIGHT;

	protected static float t(double tiles) {
		return (float) tiles * TS;
	}

	public final BooleanProperty infoVisiblePy = new SimpleBooleanProperty(this, "infoVisible", false);

	protected final SubScene fxSubScene;
	protected final StackPane root;
	protected final Canvas canvas;
	protected final Pane overlay;
	protected final VBox helpRoot;

	protected GameSceneContext context;

	protected GameScene2D() {
		infoVisiblePy.bind(Game2d.PY_SHOW_DEBUG_INFO);

		root = new StackPane();
		// This avoids a vertical line on the left side of the embedded 2D game scene
		root.setBackground(ResourceManager.colorBackground(Game2d.assets.wallpaperColor));

		fxSubScene = new SubScene(root, WIDTH, HEIGHT);

		var scaling = new Scale();
		scaling.xProperty().bind(Bindings.createDoubleBinding(this::sceneScaling, fxSubScene.widthProperty()));
		scaling.yProperty().bind(Bindings.createDoubleBinding(this::sceneScaling, fxSubScene.heightProperty()));

		canvas = new Canvas();
		canvas.widthProperty().bind(fxSubScene.widthProperty());
		canvas.heightProperty().bind(fxSubScene.heightProperty());
		canvas.getTransforms().add(scaling);

		overlay = new Pane();
		overlay.getTransforms().add(scaling);

		// anchor for help popup
		helpRoot = new VBox();
		helpRoot.setTranslateX(10);
		helpRoot.setTranslateY(HEIGHT * 0.2);
		overlay.getChildren().add(helpRoot);

		root.getChildren().addAll(canvas, overlay);
	}

	@Override
	public void setContext(GameSceneContext context) {
		checkNotNull(context);
		this.context = context;
	}

	protected double sceneScaling() {
		return fxSubScene.getHeight() / HEIGHT;
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
				sceneScaling());
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