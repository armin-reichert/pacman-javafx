/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.oneOf;

import java.util.List;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.anim.Animated;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.actors.Clapperboard;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.GestureHandler;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.GameSpritesheet;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
	public static final int WIDTH_UNSCALED = 224;
	public static final int HEIGHT_UNSCALED = 288;
	public static final float ASPECT_RATIO = 28f / 36f;

	protected static float t(double tiles) {
		return (float) tiles * TS;
	}

	public final BooleanProperty infoVisiblePy = new SimpleBooleanProperty(this, "infoVisible", false);

	public final BooleanProperty canvasScaledPy = new SimpleBooleanProperty(this, "canvasScaled", true) {
		@Override
		protected void invalidated() {
			updateCanvasScaling(get());
		}
	};

	protected final StackPane root = new StackPane();
	protected final BorderPane subSceneContainer;
	protected final Canvas canvas = new Canvas(WIDTH_UNSCALED, HEIGHT_UNSCALED);
	protected final GraphicsContext g = canvas.getGraphicsContext2D();
	protected final Pane overlay = new Pane();
	protected final Scale overlayScale = new Scale();
	protected final VBox helpRoot = new VBox();
	protected final FadeTransition helpMenuAnimation;
	protected GameSceneContext context;
	private boolean roundedCorners = true;
	private Color wallpaperColor = Color.BLACK;

	protected GameScene2D() {
		subSceneContainer = new BorderPane(root);
		subSceneContainer.setMinWidth(WIDTH_UNSCALED);
		subSceneContainer.setMinHeight(HEIGHT_UNSCALED);
		subSceneContainer.setMaxWidth(WIDTH_UNSCALED);
		subSceneContainer.setMaxHeight(HEIGHT_UNSCALED);

		root.getChildren().addAll(canvas, overlay);
		overlay.getChildren().add(helpRoot);

		overlay.getTransforms().add(overlayScale);

		helpRoot.setTranslateX(10);
		helpRoot.setTranslateY(HEIGHT_UNSCALED * 0.2);

		helpMenuAnimation = new FadeTransition(Duration.seconds(0.5), helpRoot);
		helpMenuAnimation.setFromValue(1);
		helpMenuAnimation.setToValue(0);

		// scale overlay pane to cover subscene
		subSceneContainer.heightProperty().addListener((py, ov, nv) -> {
			var scaling = nv.doubleValue() / HEIGHT_UNSCALED;
			overlayScale.setX(scaling);
			overlayScale.setY(scaling);
		});

		infoVisiblePy.bind(PacManGames2d.PY_SHOW_DEBUG_INFO); // should probably be elsewhere
		updateCanvasScaling(canvasScaledPy.get());
	}

	protected double s(double value) {
		return canvasScaledPy.get() ? value : value * subSceneContainer.getHeight() / HEIGHT_UNSCALED;
	}

	protected GameSpritesheet r() {
		return context.spritesheet();
	}

	protected Font sceneFont() {
		return context.ui().theme().font("font.arcade", s(8));
	}

	public void setSceneCanvasScaled(boolean scaled) {
		canvasScaledPy.set(scaled);
	}

	public StackPane root() {
		return root;
	}

	public void setRoundedCorners(boolean roundedCorners) {
		this.roundedCorners = roundedCorners;
	}

	public void setWallpaperColor(Color wallpaperColor) {
		this.wallpaperColor = wallpaperColor;
	}

	private void updateCanvasScaling(boolean scaled) {
		if (scaled) {
			canvas.scaleXProperty().bind(subSceneContainer.widthProperty().divide(WIDTH_UNSCALED));
			canvas.scaleYProperty().bind(subSceneContainer.heightProperty().divide(HEIGHT_UNSCALED));
			canvas.widthProperty().unbind();
			canvas.heightProperty().unbind();
			canvas.setWidth(WIDTH_UNSCALED);
			canvas.setHeight(HEIGHT_UNSCALED);
		} else {
			canvas.scaleXProperty().unbind();
			canvas.scaleYProperty().unbind();
			canvas.setScaleX(1);
			canvas.setScaleY(1);
			canvas.widthProperty().bind(subSceneContainer.widthProperty());
			canvas.heightProperty().bind(subSceneContainer.heightProperty());
		}
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
	public GameSceneContext context() {
		return context;
	}

	@Override
	public BorderPane sceneContainer() {
		return subSceneContainer;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	@Override
	public void setParentScene(Scene parentScene) {
		subSceneContainer.minWidthProperty().bind(parentScene.heightProperty().multiply(ASPECT_RATIO));
		subSceneContainer.minHeightProperty().bind(parentScene.heightProperty());
		subSceneContainer.maxWidthProperty().bind(parentScene.heightProperty().multiply(ASPECT_RATIO));
		subSceneContainer.maxHeightProperty().bind(parentScene.heightProperty());
	}

	@Override
	public boolean is3D() {
		return false;
	}

	@Override
	public void render() {
		if (context == null) {
			return;
		}
		drawSceneBackground();
		if (context.isScoreVisible()) {
			drawScore(context.game().score(), "SCORE", t(1), t(1));
			drawScore(context.game().highScore(), "HIGH SCORE", t(16), t(1));
		}
		if (context.isCreditVisible()) {
			drawCredit(context.game().credit(), t(2), t(36) - 1);
		}
		drawSceneContent();
		if (infoVisiblePy.get()) {
			drawSceneInfo();
		}
	}

	protected void drawSceneBackground() {
		if (roundedCorners) {
			g.setFill(wallpaperColor);
			g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
			g.setFill(Color.BLACK);
			g.fillRoundRect(0, 0, canvas.getWidth(), canvas.getHeight(), s(20), s(20));
		} else {
			g.setFill(Color.BLACK);
			g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
		}
	}

	protected void drawScore(Score score, String title, double x, double y) {
		drawText(title, ArcadeTheme.PALE, sceneFont(), x, y);
		var pointsText = String.format("%02d", score.points());
		drawText(String.format("%7s", pointsText), ArcadeTheme.PALE, sceneFont(), x, (y + TS + 1));
		if (score.points() != 0) {
			drawText(String.format("L%d", score.levelNumber()), ArcadeTheme.PALE, sceneFont(), x + TS * 8, y + TS + 1);
		}
	}

	protected void drawLevelCounter(double xr, double yr, List<Byte> levelSymbols) {
		double x = xr;
		for (var symbol : levelSymbols) {
			drawSprite(r().bonusSymbolSprite(symbol), x, yr);
			x -= TS * 2;
		}
	}

	protected void drawLivesCounter(int numLivesDisplayed) {
		if (numLivesDisplayed <= 0) {
			return;
		}
		var x = TS * 2;
		var y = TS * (World.TILES_Y - 2);
		int maxLives = 5;
		for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
			// TODO check reason for blitzers
			drawSprite(r().livesCounterSprite(), x + TS * (2 * i), y);
		}
		// text indicating that more lives are available than displayed
		int excessLives = numLivesDisplayed - maxLives;
		if (excessLives > 0) {
			drawText("+" + excessLives, ArcadeTheme.YELLOW, Font.font("Serif", FontWeight.BOLD, s(8)), x + TS * 10, y + TS);
		}
	}

	protected void drawPacSprite(Pac pac) {
		pac.animation().ifPresent(animation -> {
			if (pac.isVisible()) {
				var sprite = (Rectangle2D) animation.frame();
				var x = pac.position().x() + HTS - sprite.getWidth() / 2;
				var y = pac.position().y() + HTS - sprite.getHeight() / 2;
				// TODO check the blitzer cause and remove -1 workaround
				g.drawImage(r().spritesheet().source(), sprite.getMinX(), sprite.getMinY(), sprite.getWidth() - 1,
						sprite.getHeight() - 1, s(x), s(y), s(sprite.getWidth()), s(sprite.getHeight()));
			}
		});
	}

	protected void drawGhostSprite(Ghost ghost) {
		ghost.animation().ifPresent(animation -> {
			if (ghost.isVisible()) {
				var sprite = (Rectangle2D) animation.frame();
				var x = ghost.position().x() + HTS - sprite.getWidth() / 2;
				var y = ghost.position().y() + HTS - sprite.getHeight() / 2;
				drawSprite(sprite, x, y);
			}
		});
	}

	protected void drawSprite(Image source, Rectangle2D sprite, double x, double y) {
		if (sprite != null) {
			g.drawImage(source, //
					sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(), //
					s(x), s(y), s(sprite.getWidth()), s(sprite.getHeight()));
		}
	}

	protected void drawSprite(Rectangle2D sprite, double x, double y) {
		drawSprite(r().spritesheet().source(), sprite, x, y);
	}

	/**
	 * Draws a sprite centered over a one "square tile" large box (bounding box of creature). The position specifies the
	 * left-upper corner of the bounding box. Note that the sprites for Pac-Man and the ghosts are 16 pixels wide but the
	 * bounding box is only 8 pixels (one square tile) wide.
	 * 
	 * @param r spritesheet region (may be null)
	 * @param x x coordinate of left-upper corner of bounding box
	 * @param y y coordinate of left-upper corner of bounding box
	 */
	public void drawSpriteOverBoundingBox(Rectangle2D r, double x, double y) {
		if (r != null) {
			drawSprite(r, x + HTS - r.getWidth() / 2, y + HTS - r.getHeight() / 2);
		}
	}

	/**
	 * Draws the sprite over the bounding box of the given entity (if visible).
	 * 
	 * @param entity an entity like Pac-Man or a ghost
	 * @param r      the sprite
	 */
	public void drawEntitySprite(Entity entity, Rectangle2D r) {
		checkNotNull(entity);
		if (entity.isVisible()) {
			drawSpriteOverBoundingBox(r, entity.position().x(), entity.position().y());
		}
	}

	protected void drawCredit(int credit, double x, double y) {
		drawText(String.format("CREDIT %2d", credit), ArcadeTheme.PALE, sceneFont(), x, y);
	}

	protected void drawClap(Clapperboard clap) {
		if (clap.isVisible()) {
			clap.animation().map(Animated::animate).ifPresent(frame -> {
				var sprite = (Rectangle2D) frame;
				if (clap.isVisible()) {
					drawSpriteOverBoundingBox(sprite, clap.position().x(), clap.position().y());
				}
				g.setFont(sceneFont());
				g.setFill(ArcadeTheme.PALE);
				var numberX = s(clap.position().x() + sprite.getWidth() - 25);
				var numberY = s(clap.position().y() + 18);
				g.fillText(clap.number(), numberX, numberY);
				var textX = s(clap.position().x() + sprite.getWidth());
				g.fillText(clap.text(), textX, numberY);
			});
		}
	}

	protected void drawMidwayCopyright(double x, double y) {
		drawText("\u00A9 1980 MIDWAY MFG.CO.", ArcadeTheme.PINK, sceneFont(), x, y);
	}

	protected void drawMsPacManCopyright(double x, double y) {
		g.drawImage(context.ui().theme().image("mspacman.logo.midway"), s(x), s(y + 2), s(TS * 4 - 2), s(TS * 4));
		g.setFill(ArcadeTheme.RED);
		g.setFont(Font.font("Dialog", s(11)));
		g.fillText("\u00a9", s(x + TS * 5), s(y + TS * 2 + 2)); // (c) symbol
		g.setFont(sceneFont());
		g.fillText("MIDWAY MFG CO", s(x + TS * 7), s(y + TS * 2));
		g.fillText("1980/1981", s(x + TS * 8), s(y + TS * 4));
	}

	protected void drawText(String text, Color color, Font font, double x, double y) {
		g.setFont(font);
		g.setFill(color);
		g.fillText(text, s(x), s(y));
	}

	protected void drawTileGrid(int tilesX, int tilesY) {
		g.save();
		g.translate(0.5, 0.5);
		g.setStroke(ArcadeTheme.PALE);
		g.setLineWidth(0.2);
		for (int row = 0; row <= tilesY; ++row) {
			g.strokeLine(0, s(TS * (row)), s(tilesX * TS), s(TS * (row)));
		}
		for (int col = 0; col <= tilesY; ++col) {
			g.strokeLine(s(TS * (col)), 0, s(TS * (col)), s(tilesY * TS));
		}
		g.restore();
	}

	protected void drawWishDirIndicator(Pac pac) {
		g.setFill(Color.RED);
		float r = 4;
		var center = pac.center().plus(pac.wishDir().vector().toFloatVec().scaled(8f)).minus(r, r);
		g.fillOval(s(center.x()), s(center.y()), s(2 * r), s(2 * r));
	}

	/**
	 * Draws the scene content, e.g. the maze and the guys.
	 */
	protected abstract void drawSceneContent();

	/**
	 * Draws scene info, e.g. maze structure and special tiles
	 */
	protected void drawSceneInfo() {
		// empty by default
	}

	public void addTouchSupport() {
		var touchPad = new Rectangle(WIDTH_UNSCALED, HEIGHT_UNSCALED);
		touchPad.setScaleX(0.9);
		touchPad.setScaleY(0.9);
		overlay.getChildren().add(touchPad);
		var gestureHandler = new GestureHandler(touchPad);
		gestureHandler.setOnDirectionRecognized(dir -> {
			context.game().level().ifPresent(level -> {
				level.pac().setWishDir(dir);
			});
		});
	}
}