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
import static de.amr.games.pacman.lib.Globals.oneOf;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.rendering2d.GameRenderer;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Camera;
import javafx.scene.ParallelCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

/**
 * Base class of all 2D scenes. Each 2D scene has its own canvas.
 * 
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

	public static final int TILES_X = 28;
	public static final int TILES_Y = 36;
	public static final int WIDTH = 224;
	public static final int HEIGHT = 288;
	public static final float ASPECT_RATIO = 28f / 36f;

	protected static float t(double tiles) {
		return (float) tiles * TS;
	}

	public final BooleanProperty infoVisiblePy = new SimpleBooleanProperty(this, "infoVisible", false);

	protected final Camera camera;
	protected final SubScene fxSubScene; // we probably could just use some pane instead
	protected final Canvas canvas;
	protected final Pane overlay;

	protected final VBox helpRoot;
	private final FadeTransition helpMenuAnimation;

	protected GameSceneContext context;

	protected GameScene2D() {
		canvas = new Canvas(WIDTH, HEIGHT);
		overlay = new Pane();

		var root = new StackPane();
		// This avoids a vertical line on the left side of the embedded 2D game scene
		root.setBackground(ResourceManager.coloredBackground(PacManGames2d.assets.wallpaperColor));
		root.getChildren().addAll(canvas, overlay);

		helpRoot = new VBox();
		helpRoot.setTranslateX(10);
		helpRoot.setTranslateY(HEIGHT * 0.2);

		helpMenuAnimation = new FadeTransition(Duration.seconds(0.5), helpRoot);
		helpMenuAnimation.setFromValue(1);
		helpMenuAnimation.setToValue(0);

		overlay.getChildren().add(helpRoot);

		// scale overlay pane to cover subscene
		fxSubScene = new SubScene(root, WIDTH, HEIGHT);
		fxSubScene.heightProperty().addListener((py, ov, nv) -> {
			var s = nv.doubleValue() / HEIGHT;
			overlay.getTransforms().setAll(new Scale(s, s));
		});

		camera = new ParallelCamera();
		fxSubScene.setCamera(camera);

		canvas.scaleXProperty().bind(fxSubScene.widthProperty().divide(WIDTH));
		canvas.scaleYProperty().bind(fxSubScene.heightProperty().divide(HEIGHT));

		infoVisiblePy.bind(PacManGames2d.PY_SHOW_DEBUG_INFO); // should probably be elsewhere
	}

	// TODO: not sure if this logic belongs here...
	private void updateHelpMenu(HelpMenus help) {
		var gameState = context.state();
		Pane menu = null;
		if (gameState == GameState.INTRO) {
			menu = help.menuIntro(context.gameController());
		} else if (gameState == GameState.CREDIT) {
			menu = help.menuCredit(context.gameController());
		} else if (oneOf(gameState, GameState.READY, GameState.HUNTING, GameState.PACMAN_DYING, GameState.GHOST_DYING)) {
			var level = context.level();
			if (level.isPresent()) {
				menu = level.get().isDemoLevel() ? help.menuDemoLevel(context.gameController())
						: help.menuPlaying(context.gameController());
			}
		}
		if (menu == null) {
			helpRoot.getChildren().clear();
		} else {
			helpRoot.getChildren().setAll(menu);
		}
	}

	/**
	 * Makes the help root visible for given duration and then plays the close animation.
	 * 
	 * @param openDuration duration the menu stays open
	 */
	public void showHelpMenu(HelpMenus menus, Duration openDuration) {
		updateHelpMenu(menus);
		helpRoot.setOpacity(1);
		if (helpMenuAnimation.getStatus() == Status.RUNNING) {
			helpMenuAnimation.playFromStart();
		}
		helpMenuAnimation.setDelay(openDuration);
		helpMenuAnimation.play();
	}

	@Override
	public void setContext(GameSceneContext context) {
		checkNotNull(context);
		this.context = context;
	}

	@Override
	public void render() {
		var g = canvas.getGraphicsContext2D();
		g.setFill(PacManGames2d.assets.wallpaperColor);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		g.setFill(Color.BLACK);
		g.fillRoundRect(0, 0, WIDTH, HEIGHT, 20, 20);
		if (context == null) {
			return;
		}
		if (context.isScoreVisible()) {
			GameRenderer.drawScore(g, context.game().score(), "SCORE", t(1), t(1));
			GameRenderer.drawScore(g, context.game().highScore(), "HIGH SCORE", t(16), t(1));
		}
		if (context.isCreditVisible()) {
			GameRenderer.drawCredit(g, context.game().credit(), t(2), t(36) - 1);
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

	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public void setParentScene(Scene parentScene) {
		fxSubScene.widthProperty().bind(parentScene.heightProperty().multiply(ASPECT_RATIO));
		fxSubScene.heightProperty().bind(parentScene.heightProperty());
	}

	@Override
	public boolean is3D() {
		return false;
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