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

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEventAdapter;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.DebugDraw;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Spritesheet_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Spritesheet_PacMan;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.Scene;
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
public abstract class GameScene2D extends GameEventAdapter implements GameScene {

	protected final V2d unscaledSize = new V2d(ArcadeWorld.SIZE);
	protected final Canvas canvas = new Canvas(unscaledSize.x, unscaledSize.y);
	protected final Pane infoPane = new Pane();
	protected final StackPane root;
	protected final SubScene fxSubScene;

	// context
	protected GameController gameController;
	protected GameModel game;
	protected Rendering2D r2D;

	protected boolean creditVisible;

	public GameScene2D() {
		root = new StackPane(canvas, infoPane);
		root.setBackground(U.colorBackground(Color.BLACK));
		fxSubScene = new SubScene(root, unscaledSize.x, unscaledSize.y);
		canvas.widthProperty().bind(fxSubScene.widthProperty());
		canvas.heightProperty().bind(fxSubScene.heightProperty());
	}

	@Override
	public void setParent(Scene parent) {
		parent.heightProperty().addListener((x, y, height) -> resize(height.doubleValue()));
	}

	@Override
	public void setSceneContext(GameController gameController) {
		var state = gameController.state();
		var noCredit = gameController.credit() == 0;
		this.gameController = gameController;
		this.game = gameController.game();
		r2D = switch (game.variant) {
		case MS_PACMAN -> Spritesheet_MsPacMan.get();
		case PACMAN -> Spritesheet_PacMan.get();
		};
		SoundManager.get().selectGameVariant(game.variant);
		SoundManager.get().setStopped(noCredit && state != GameState.INTERMISSION_TEST);
	}

	@Override
	public SubScene getFXSubScene() {
		return fxSubScene;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public void resize(double height) {
		double aspectRatio = unscaledSize.x / unscaledSize.y;
		double width = aspectRatio * height;
		double scaling = height / unscaledSize.y;
		canvas.getTransforms().setAll(new Scale(scaling, scaling));
		fxSubScene.setWidth(width);
		fxSubScene.setHeight(height);
	}

	public double currentScaling() {
		return fxSubScene.getHeight() / unscaledSize.y;
	}

	/**
	 * Updates the scene. Subclasses override this method.
	 */
	protected abstract void doUpdate();

	/**
	 * Renders the scene content. Subclasses override this method.
	 */
	protected abstract void doRender(GraphicsContext g);

	@Override
	public final void update() {
		infoPane.setVisible(Env.$debugUI.get());
		doUpdate();
		var g = canvas.getGraphicsContext2D();
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		doRender(g);
		if (Env.$tilesVisible.get()) {
			DebugDraw.drawGrid(g);
		}
	}

	@Override
	public boolean is3D() {
		return false;
	}
}