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

import static de.amr.games.pacman.ui.fx._2d.rendering.GameRenderer.drawTileStructure;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Scale;

/**
 * Base class of all 2D scenes that get rendered inside a canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

	private static final Logger LOG = LogManager.getFormatterLogger();

	public final BooleanProperty overlayPaneVisiblePy = new SimpleBooleanProperty(this, "overlayPaneVisible", false);

	protected final StackPane root = new StackPane();
	protected final Pane overlayPane = new Pane();
	protected final SubScene fxSubScene;
	protected final Canvas canvas = new Canvas();
	protected final GraphicsContext g = canvas.getGraphicsContext2D();
	protected GameSceneContext ctx;
	private boolean creditVisible;
	private Vector2i size = ArcadeWorld.SIZE_PX;

	protected GameScene2D() {
		fxSubScene = new SubScene(root, size.x(), size.y());
		canvas.widthProperty().bind(fxSubScene.widthProperty());
		canvas.heightProperty().bind(fxSubScene.heightProperty());
		overlayPane.visibleProperty().bind(overlayPaneVisiblePy);
		overlayPane.setMouseTransparent(true);
		root.getChildren().addAll(canvas, overlayPane);
	}

	@Override
	public void resizeToHeight(double height) {
		var aspectRatio = (double) size.x() / (double) size.y();
		var width = aspectRatio * height;
		var scaling = height / size.y();
		fxSubScene.setWidth(width);
		fxSubScene.setHeight(height);
		canvas.getTransforms().setAll(new Scale(scaling, scaling));
		LOG.debug("2D game scene resized: %.0f x %.0f scaled: %.2f (%s)", fxSubScene.getWidth(), fxSubScene.getHeight(),
				scaling, getClass().getSimpleName());
	}

	@Override
	public final void onTick() {
		update();
		clear();
		drawSceneContent();
		if (overlayPaneVisiblePy.get()) {
			ctx.level().ifPresent(this::drawDebugInfo);
		}
		drawHUD();
		if (ctx.gameController().levelTestMode) {
			ctx.r2D().drawText(g, "LEVEL TEST MODE", Color.WHITE, Font.font("Monospaced", FontWeight.MEDIUM, 12), 60, 190);
		}
	}

	protected void drawDebugInfo(GameLevel level) {
		drawTileStructure(g, size.x() / World.TS, size.y() / World.TS);
	}

	public void clear() {
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	public void drawHUD() {
		ctx.r2D().drawHUD(g, ctx.game(), creditVisible);
	}

	/**
	 * Updates the scene.
	 */
	public abstract void update();

	/**
	 * Draws the scene content.
	 */
	public abstract void drawSceneContent();

	public boolean isCreditVisible() {
		return creditVisible;
	}

	public void setCreditVisible(boolean creditVisible) {
		this.creditVisible = creditVisible;
	}

	@Override
	public boolean is3D() {
		return false;
	}

	@Override
	public void embedInto(Scene parentScene) {
		resizeToHeight((float) parentScene.getHeight());
	}

	@Override
	public GameSceneContext ctx() {
		return ctx;
	}

	@Override
	public void setContext(GameSceneContext context) {
		ctx = context;
	}

	@Override
	public SubScene fxSubScene() {
		return fxSubScene;
	}
}