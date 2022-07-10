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

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.util.Ufx;
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

	protected final V2d unscaledSize = new V2d(ArcadeWorld.WORLD_SIZE);
	protected final Canvas canvas = new Canvas(unscaledSize.x, unscaledSize.y);
	protected final GraphicsContext g = canvas.getGraphicsContext2D();
	protected final Canvas overlayCanvas = new Canvas();
	protected final Pane infoPane = new Pane();
	protected final StackPane root;
	protected final SubScene fxSubScene;
	protected final DoubleProperty scalingPy = new SimpleDoubleProperty(1);

	protected SceneContext ctx;
	protected boolean creditVisible;

	protected GameScene2D() {
		root = new StackPane(canvas, overlayCanvas, infoPane);
		// without this, an ugly vertical white line appears left of the game scene:
		root.setBackground(Ufx.colorBackground(Color.BLACK));
		fxSubScene = new SubScene(root, unscaledSize.x, unscaledSize.y);
		canvas.widthProperty().bind(fxSubScene.widthProperty());
		canvas.heightProperty().bind(fxSubScene.heightProperty());
		overlayCanvas.widthProperty().bind(canvas.widthProperty());
		overlayCanvas.heightProperty().bind(canvas.heightProperty());
		overlayCanvas.visibleProperty().bind(Env.showDebugInfoPy);
		overlayCanvas.setMouseTransparent(true);
		infoPane.setVisible(Env.showDebugInfoPy.get());
		infoPane.visibleProperty().bind(Env.showDebugInfoPy);
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

	public Canvas getGameSceneCanvas() {
		return canvas;
	}

	public Canvas getOverlayCanvas() {
		return overlayCanvas;
	}

	public V2d getUnscaledSize() {
		return unscaledSize;
	}

	@Override
	public void resize(double height) {
		double aspectRatio = unscaledSize.x / unscaledSize.y;
		double width = aspectRatio * height;
		fxSubScene.setWidth(width);
		fxSubScene.setHeight(height);
		scalingPy.set(fxSubScene.getHeight() / unscaledSize.y);
		canvas.getTransforms().setAll(new Scale(scalingPy.get(), scalingPy.get()));
	}

	public double currentScaling() {
		return scalingPy.get();
	}

	/**
	 * Updates the scene. Subclasses override this method.
	 */
	protected abstract void doUpdate();

	/**
	 * Renders the scene content. Subclasses override this method.
	 */
	public abstract void drawSceneContent();

	@Override
	public final void updateAndRender() {
		doUpdate();
		clearSceneContent();
		drawSceneContent();
		drawTransparentOverlayCanvas();
	}

	private void clearSceneContent() {
		var g = canvas.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	private void drawTransparentOverlayCanvas() {
		if (overlayCanvas.isVisible()) {
			var og = overlayCanvas.getGraphicsContext2D();
			og.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
			ctx.r2D.drawTileBorders(og, scalingPy.doubleValue());
		}
	}
}