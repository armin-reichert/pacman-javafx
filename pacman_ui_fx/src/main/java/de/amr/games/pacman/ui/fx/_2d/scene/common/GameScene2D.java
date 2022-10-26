/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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
package de.amr.games.pacman.ui.fx._2d.scene.common;

import static de.amr.games.pacman.model.common.world.World.TS;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.pacman.PacManGame;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.rendering.HUD;
import de.amr.games.pacman.ui.fx._2d.rendering.RendererCommon;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.util.ResizableCanvas;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;

/**
 * Base class of all 2D scenes that get rendered inside a canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	protected final V2i unscaledSize;
	protected final DoubleProperty scalingPy = new SimpleDoubleProperty(1);
	protected final StackPane root = new StackPane();
	protected final SubScene fxSubScene;
	protected final ResizableCanvas canvas;
	protected final Pane infoLayer = new Pane();
	protected final HUD hud = new HUD();

	protected SceneContext ctx;

	protected GameScene2D() {
		this(DEFAULT_SIZE);
	}

	protected GameScene2D(V2i size) {
		unscaledSize = size;

		fxSubScene = new SubScene(root, unscaledSize.x(), unscaledSize.y());

		canvas = new ResizableCanvas();
		canvas.widthProperty().bind(fxSubScene.widthProperty());
		canvas.heightProperty().bind(fxSubScene.heightProperty());

		scale(canvas);
		scalingPy.addListener((obs, oldVal, newVal) -> scale(canvas));

		infoLayer.visibleProperty().bind(Env.showDebugInfoPy);
		infoLayer.setMouseTransparent(true);

		root.getChildren().addAll(canvas, infoLayer);

		hud.widthPy.bind(canvas.widthProperty());
		hud.heightPy.bind(canvas.heightProperty());
	}

	private void scale(Canvas canvas) {
		canvas.getTransforms().setAll(new Scale(getScaling(), getScaling()));
	}

	@Override
	public void resize(double height) {
		double aspectRatio = (double) unscaledSize.x() / (double) unscaledSize.y();
		double scaling = height / unscaledSize.y();
		double width = aspectRatio * height;
		fxSubScene.setWidth(width);
		fxSubScene.setHeight(height);
		scalingPy.set(scaling);
		LOGGER.trace("Scene resized: %.0f x %.0f scaled: %.2f (%s)", canvas.getWidth(), canvas.getHeight(), scaling,
				getClass().getSimpleName());
	}

	@Override
	public final void updateAndRender() {
		update();
		renderScene();
		if (Env.showDebugInfoPy.get()) {
			var g = canvas.getGraphicsContext2D();
			RendererCommon.drawTileStructure(g, ArcadeWorld.TILES_X, ArcadeWorld.TILES_Y);
			if (ctx.gameVariant() == GameVariant.PACMAN && this instanceof PlayScene2D) {
				g.setFill(Color.RED);
				PacManGame.RED_ZONE.forEach(tile -> g.fillRect(tile.x() * TS, tile.y() * TS, TS, TS));
			}
		}
		drawHUD(canvas.getGraphicsContext2D());
	}

	public void renderScene() {
		var g = canvas.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		drawSceneContent(g);
	}

	public void drawHUD(GraphicsContext g) {
		hud.draw(g, ctx.game());
	}

	/**
	 * Updates the scene. Subclasses override this method.
	 */
	public void update() {
	}

	/**
	 * Draws the scene content. Subclasses override this method.
	 */
	public void drawSceneContent(GraphicsContext g) {
	}

	@Override
	public boolean is3D() {
		return false;
	}

	@Override
	public void setResizeBehavior(ObservableDoubleValue width, ObservableDoubleValue height) {
		height.addListener((x, y, h) -> resize(h.doubleValue()));
	}

	@Override
	public SceneContext getSceneContext() {
		return ctx;
	}

	@Override
	public void setSceneContext(SceneContext context) {
		ctx = context;
	}

	@Override
	public SubScene getFXSubScene() {
		return fxSubScene;
	}

	public StackPane getRoot() {
		return root;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public int getUnscaledWidth() {
		return unscaledSize.x();
	}

	public int getUnscaledHeight() {
		return unscaledSize.y();
	}

	@Override
	public double getScaling() {
		return scalingPy.get();
	}
}