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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeRendererBase;
import de.amr.games.pacman.ui.fx._2d.rendering.common.HUD;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.util.ResizableCanvas;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

	protected final V2d unscaledSize;
	protected final DoubleProperty scalingPy = new SimpleDoubleProperty(1);
	protected final StackPane root = new StackPane();
	protected final SubScene fxSubScene;
	protected final ResizableCanvas sceneCanvas;
	protected final ResizableCanvas overlayCanvas;
	protected final Pane infoLayer = new Pane();
	protected final HUD hud = new HUD();

	protected SceneContext ctx;

	protected GameScene2D() {
		this(new V2d(ArcadeWorld.WORLD_SIZE));
	}

	protected GameScene2D(V2d size) {
		unscaledSize = size;

		fxSubScene = new SubScene(root, unscaledSize.x(), unscaledSize.y());

		sceneCanvas = new ResizableCanvas();
		sceneCanvas.widthProperty().bind(fxSubScene.widthProperty());
		sceneCanvas.heightProperty().bind(fxSubScene.heightProperty());
		scale(sceneCanvas);
		scalingPy.addListener((obs, oldVal, newVal) -> scale(sceneCanvas));

		overlayCanvas = new ResizableCanvas();
		overlayCanvas.widthProperty().bind(fxSubScene.widthProperty());
		overlayCanvas.heightProperty().bind(fxSubScene.heightProperty());
		overlayCanvas.visibleProperty().bind(Env.showDebugInfoPy);
		overlayCanvas.setMouseTransparent(true);
		scale(overlayCanvas);
		scalingPy.addListener((obs, oldVal, newVal) -> scale(overlayCanvas));

		infoLayer.setVisible(Env.showDebugInfoPy.get());
		infoLayer.visibleProperty().bind(Env.showDebugInfoPy);

		root.getChildren().addAll(sceneCanvas, overlayCanvas, infoLayer);

		hud.widthPy.bind(sceneCanvas.widthProperty());
		hud.heightPy.bind(sceneCanvas.heightProperty());
	}

	private void scale(Canvas canvas) {
		canvas.getTransforms().setAll(new Scale(getScaling(), getScaling()));
	}

	@Override
	public void resize(double height) {
		double aspectRatio = unscaledSize.x() / unscaledSize.y();
		double scaling = height / unscaledSize.y();
		double width = aspectRatio * height;
		fxSubScene.setWidth(width);
		fxSubScene.setHeight(height);
		scalingPy.set(scaling);
		LOGGER.trace("Scene resized: %.0f x %.0f scaled: %.2f (%s)", sceneCanvas.getWidth(), sceneCanvas.getHeight(),
				scaling, getClass().getSimpleName());
	}

	@Override
	public final void updateAndRender() {
		update();
		renderScene();
		drawHUD(sceneCanvas.getGraphicsContext2D());
		if (overlayCanvas.isVisible()) {
			renderOverlayCanvas();
		}
	}

	public void renderScene() {
		var g = sceneCanvas.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, sceneCanvas.getWidth(), sceneCanvas.getHeight());
		drawSceneContent(g);
	}

	protected void renderOverlayCanvas() {
		var g = overlayCanvas.getGraphicsContext2D();
		g.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
		ArcadeRendererBase.drawTileStructure(g);
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
	public void setResizeBehavior(DoubleExpression width, DoubleExpression height) {
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

	public Canvas getGameSceneCanvas() {
		return sceneCanvas;
	}

	public Canvas getOverlayCanvas() {
		return overlayCanvas;
	}

	public V2d getUnscaledSize() {
		return unscaledSize;
	}

	@Override
	public double getScaling() {
		return scalingPy.get();
	}
}