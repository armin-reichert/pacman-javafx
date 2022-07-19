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
import static de.amr.games.pacman.model.common.world.World.t;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.actors.Score;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.util.ResizableCanvas;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontSmoothingType;

/**
 * Base class of all 2D scenes that get rendered inside a canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	protected final V2d unscaledSize;
	protected final ResizableCanvas canvas;
	protected final ResizableCanvas overlayCanvas;
	protected final Pane infoPane = new Pane();
	protected final StackPane root;
	protected final SubScene fxSubScene;
	protected final DoubleProperty scalingPy = new SimpleDoubleProperty(1);

	protected SceneContext ctx;
	protected Font hudFont;
	protected boolean creditVisible;

	protected GameScene2D(V2d size) {
		unscaledSize = size;
		canvas = new ResizableCanvas(unscaledSize.x(), unscaledSize.y());
		overlayCanvas = new ResizableCanvas(unscaledSize.x(), unscaledSize.y());

		root = new StackPane(canvas, overlayCanvas, infoPane);
		// without this, an ugly vertical white line appears left of the game scene:
		root.setBackground(Ufx.colorBackground(Color.BLACK));
		fxSubScene = new SubScene(root, unscaledSize.x(), unscaledSize.y());

		canvas.widthProperty().bind(fxSubScene.widthProperty());
		canvas.heightProperty().bind(fxSubScene.heightProperty());
		overlayCanvas.widthProperty().bind(fxSubScene.widthProperty());
		overlayCanvas.heightProperty().bind(fxSubScene.heightProperty());
		overlayCanvas.visibleProperty().bind(Env.showDebugInfoPy);
		overlayCanvas.setMouseTransparent(true);
		infoPane.setVisible(Env.showDebugInfoPy.get());
		infoPane.visibleProperty().bind(Env.showDebugInfoPy);

		resize(unscaledSize.y());
	}

	protected GameScene2D() {
		this(new V2d(ArcadeWorld.WORLD_SIZE));
	}

	@Override
	public void resize(double height) {
		double aspectRatio = unscaledSize.x() / unscaledSize.y();
		double scaling = height / unscaledSize.y();
		double width = aspectRatio * height;
		fxSubScene.setWidth(width);
		fxSubScene.setHeight(height);
		scalingPy.set(scaling);
		LOGGER.info("Game scene %s resized. Canvas size: %.0f x %.0f scaling: %.2f", getClass().getSimpleName(),
				canvas.getWidth(), canvas.getHeight(), scaling);
	}

	@Override
	public final void updateAndRender() {
		update();
		render(canvas.getGraphicsContext2D());
	}

	protected void render(GraphicsContext g) {
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g.save();
		g.scale(getScaling(), getScaling());
		g.setFontSmoothingType(FontSmoothingType.LCD);
		drawSceneContent(g);
		g.restore();
		if (overlayCanvas.isVisible()) {
			var og = overlayCanvas.getGraphicsContext2D();
			og.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
			og.save();
			og.scale(getScaling(), getScaling());
			og.setFontSmoothingType(FontSmoothingType.LCD);
			ctx.r2D.drawTileBorders(og);
			og.restore();
		}
		g.setFontSmoothingType(FontSmoothingType.LCD);
		var fontFamily = ctx.r2D.getArcadeFont().getFamily();
		hudFont = Font.font(fontFamily, Math.floor(8.0 * getScaling()));
		drawHUD(g);
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

	public void drawHUD(GraphicsContext g) {
		drawScore(g, hudFont, ctx.game().scores.gameScore);
		drawScore(g, hudFont, ctx.game().scores.highScore);
		if (creditVisible) {
			drawCredit(g, hudFont, ctx.game().getCredit());
		}
	}

	public void drawText(GraphicsContext g, String text, Color color, Font font, double x, double y) {
		g.setFont(font);
		g.setFill(color);
		g.fillText(text, x, y);
	}

	public void drawCredit(GraphicsContext g, Font font, int credit) {
		drawText(g, "CREDIT  %d".formatted(credit), Color.WHITE, font, t(2) * getScaling(), t(36) * getScaling() - 1);
	}

	public void drawScore(GraphicsContext g, Font font, Score score) {
		if (score.isVisible()) {
			var pointsText = score.showContent ? "%02d".formatted(score.points) : "00";
			var levelText = score.showContent ? "L" + score.levelNumber : "";
			drawText(g, score.title, Color.WHITE, font, score.getPosition().x() * getScaling(),
					score.getPosition().y() * getScaling());
			drawText(g, "%7s".formatted(pointsText), Color.WHITE, font, score.getPosition().x() * getScaling(),
					score.getPosition().y() * getScaling() + t(1) * getScaling());
			drawText(g, levelText, Color.LIGHTGRAY, font, score.getPosition().x() * getScaling() + t(8) * getScaling(),
					score.getPosition().y() * getScaling() + t(1) * getScaling());
		}
	}

	public void drawGameStateMessage(GraphicsContext g, Font font, GameState state) {
		if (state == GameState.GAME_OVER) {
			drawText(g, "GAME  OVER", Color.RED, font, t(9) * getScaling(), t(21) * getScaling());
		} else if (state == GameState.READY) {
			drawText(g, "READY", Color.YELLOW, font, t(11) * getScaling(), t(21) * getScaling());
			g.setFont(Font.font(font.getFamily(), FontPosture.ITALIC, TS));
			g.fillText("!", t(16) * getScaling(), t(21) * getScaling());
		}
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
	public double getScaling() {
		return scalingPy.get();
	}
}