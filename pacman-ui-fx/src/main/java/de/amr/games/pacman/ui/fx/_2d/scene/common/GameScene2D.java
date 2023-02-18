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

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.ArcadeTheme.Palette;
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

	public static void drawTileStructure(GraphicsContext g, int tilesX, int tilesY) {
		g.save();
		g.translate(0.5, 0.5);
		g.setStroke(Palette.PALE);
		g.setLineWidth(0.2);
		for (int row = 0; row <= tilesY; ++row) {
			g.strokeLine(0, t(row), tilesX * TS, t(row));
		}
		for (int col = 0; col <= tilesY; ++col) {
			g.strokeLine(t(col), 0, t(col), tilesY * TS);
		}
		g.restore();
	}

	public final BooleanProperty overlayPaneVisiblePy = new SimpleBooleanProperty(this, "overlayPaneVisible", false);

	protected final SubScene fxSubScene;
	protected final StackPane root = new StackPane();
	protected final Pane overlayPane = new Pane();
	protected final Canvas canvas = new Canvas();
	protected final GraphicsContext g = canvas.getGraphicsContext2D();
	protected GameSceneContext context;
	protected boolean creditVisible;
	protected boolean scoresVisible = true;
	protected Vector2i size = ArcadeWorld.SIZE_PX;

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
		var aspectRatio = (float) size.x() / size.y();
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
		draw();
	}

	public abstract void update();

	public void draw() {
		var game = context.game();
		var r = context.r2D();
		var mazeNumber = game.level().map(GameLevel::number);
		var bgColor = mazeNumber.isPresent() ? r.mazeBackgroundColor(mazeNumber.get()) : Color.BLACK;
		g.setFill(bgColor);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		drawSceneContent();
		if (scoresVisible) {
			game.score()
					.ifPresent(score -> r.drawScore(g, score.points(), score.levelNumber(), "SCORE", Palette.PALE, t(1), t(1)));
			game.highScore().ifPresent(
					score -> r.drawScore(g, score.points(), score.levelNumber(), "HISCORE", Palette.PALE, t(16), t(1)));
		}
		if (creditVisible) {
			r.drawCredit(g, game.credit());
		}
		if (overlayPaneVisiblePy.get()) {
			drawOverlayPaneContent();
		}
		if (context.gameController().levelTestMode) {
			r.drawText(g, "LEVEL TEST MODE", Color.LIGHTGRAY, Font.font("Monospaced", FontWeight.MEDIUM, 12), 60, 190);
		}
	}

	protected abstract void drawSceneContent();

	protected void drawOverlayPaneContent() {
		// empty by default
	}

	@Override
	public boolean is3D() {
		return false;
	}

	@Override
	public void onEmbed(Scene parentScene) {
		resizeToHeight((float) parentScene.getHeight());
	}

	@Override
	public GameSceneContext context() {
		return context;
	}

	@Override
	public void setContext(GameSceneContext context) {
		this.context = context;
	}

	@Override
	public SubScene fxSubScene() {
		return fxSubScene;
	}
}