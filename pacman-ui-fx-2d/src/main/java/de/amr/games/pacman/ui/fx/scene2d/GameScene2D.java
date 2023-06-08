/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.List;

import de.amr.games.pacman.lib.Score;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.app.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.SpriteAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.ClapperBoardAnimation;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;
import de.amr.games.pacman.ui.fx.scene.GameScene;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Scale;

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

	private final BorderPane root;

	protected final Canvas canvas;
	protected final GraphicsContext g;
	protected final Pane overlay;

	private final Scale overlayScale = new Scale();
	private final HelpMenu helpMenu;

	private PacManGames2dUI ui;
	private boolean scoreVisible;
	private boolean creditVisible;

	private boolean roundedCorners = true;
	private Color wallpaperColor = Color.BLACK;
	private boolean canvasScaled;

	protected GameScene2D() {
		canvas = new Canvas(WIDTH_UNSCALED, HEIGHT_UNSCALED);
		g = canvas.getGraphicsContext2D();

		helpMenu = new HelpMenu();
		helpMenu.setTranslateX(10);
		helpMenu.setTranslateY(HEIGHT_UNSCALED * 0.2);

		overlay = new Pane();
		overlay.getChildren().add(helpMenu);
		overlay.getTransforms().add(overlayScale);

		var layers = new StackPane(canvas, overlay);

		root = new BorderPane(layers);
		root.setMinWidth(WIDTH_UNSCALED);
		root.setMinHeight(HEIGHT_UNSCALED);
		root.setMaxWidth(WIDTH_UNSCALED);
		root.setMaxHeight(HEIGHT_UNSCALED);

		// always scale overlay pane to cover subscene
		root.heightProperty().addListener((py, ov, nv) -> {
			var scaling = nv.doubleValue() / HEIGHT_UNSCALED;
			overlayScale.setX(scaling);
			overlayScale.setY(scaling);
		});

		infoVisiblePy.bind(PacManGames2d.PY_SHOW_DEBUG_INFO); // should probably be elsewhere

		setCanvasScaled(false);
	}

	@Override
	public boolean isCreditVisible() {
		return creditVisible;
	}

	@Override
	public void setCreditVisible(boolean creditVisible) {
		this.creditVisible = creditVisible;
	}

	@Override
	public boolean isScoreVisible() {
		return scoreVisible;
	}

	@Override
	public void setScoreVisible(boolean scoreVisible) {
		this.scoreVisible = scoreVisible;
	}

	public boolean isCanvasScaled() {
		return canvasScaled;
	}

	public void setCanvasScaled(boolean scaled) {
		canvasScaled = scaled;
		if (scaled) {
			canvas.scaleXProperty().bind(root.widthProperty().divide(WIDTH_UNSCALED));
			canvas.scaleYProperty().bind(root.heightProperty().divide(HEIGHT_UNSCALED));
			canvas.widthProperty().unbind();
			canvas.heightProperty().unbind();
			canvas.setWidth(WIDTH_UNSCALED);
			canvas.setHeight(HEIGHT_UNSCALED);
		} else {
			canvas.scaleXProperty().unbind();
			canvas.scaleYProperty().unbind();
			canvas.setScaleX(1);
			canvas.setScaleY(1);
			canvas.widthProperty().bind(root.widthProperty());
			canvas.heightProperty().bind(root.heightProperty());
		}
	}

	protected double s(double value) {
		return canvasScaled ? value : value * root.getHeight() / HEIGHT_UNSCALED;
	}

	protected Font sceneFont() {
		return ui.theme().font("font.arcade", s(8));
	}

	public void setRoundedCorners(boolean roundedCorners) {
		this.roundedCorners = roundedCorners;
	}

	public void setWallpaperColor(Color wallpaperColor) {
		this.wallpaperColor = wallpaperColor;
	}

	@Override
	public void setUI(PacManGames2dUI ui) {
		this.ui = ui;
	}

	@Override
	public PacManGames2dUI ui() {
		return ui;
	}

	@Override
	public BorderPane root() {
		return root;
	}

	public Canvas getCanvas() {
		return canvas;
	}

	public Pane getOverlay() {
		return overlay;
	}

	public HelpMenu getHelpMenu() {
		return helpMenu;
	}

	@Override
	public void setParentScene(Scene parentScene) {
		root.minWidthProperty().bind(parentScene.heightProperty().multiply(ASPECT_RATIO));
		root.minHeightProperty().bind(parentScene.heightProperty());
		root.maxWidthProperty().bind(parentScene.heightProperty().multiply(ASPECT_RATIO));
		root.maxHeightProperty().bind(parentScene.heightProperty());
	}

	@Override
	public boolean is3D() {
		return false;
	}

	@Override
	public void render() {
		if (ui == null) {
			return;
		}
		drawSceneBackground();
		if (isScoreVisible()) {
			drawScore(game().score(), "SCORE", t(1), t(1));
			drawScore(game().highScore(), "HIGH SCORE", t(16), t(1));
		}
		if (isCreditVisible()) {
			drawCredit(game().credit(), t(2), t(36) - 1);
		}
		drawSceneContent();
		if (infoVisiblePy.get()) {
			drawSceneInfo();
		}
	}

	protected void drawSceneBackground() {
		double w = canvas.getWidth();
		double h = canvas.getHeight();
		double arc = s(20);
		if (roundedCorners) {
			g.setFill(wallpaperColor);
			g.fillRect(0, 0, w, h);
			g.setFill(Color.BLACK);
			g.fillRoundRect(0, 0, w, h, arc, arc);
		} else {
			g.setFill(Color.BLACK);
			g.fillRect(0, 0, w, h);
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
		switch (game().variant()) {
		case MS_PACMAN: {
			var ss = (SpritesheetMsPacManGame) ui.spritesheet();
			for (var symbol : levelSymbols) {
				drawSprite(ss.bonusSymbolSprite(symbol), x, yr);
				x -= TS * 2;
			}
			break;
		}
		case PACMAN: {
			var ss = (SpritesheetPacManGame) ui.spritesheet();
			for (var symbol : levelSymbols) {
				drawSprite(ss.bonusSymbolSprite(symbol), x, yr);
				x -= TS * 2;
			}
			break;
		}
		default:
			throw new IllegalGameVariantException(game().variant());
		}
	}

	protected void drawLivesCounter(int numLivesDisplayed) {
		if (numLivesDisplayed <= 0) {
			return;
		}
		var x = TS * 2;
		var y = TS * (World.TILES_Y - 2);
		int maxLives = 5;
		switch (game().variant()) {
		case MS_PACMAN: {
			var ss = (SpritesheetMsPacManGame) ui.spritesheet();
			for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
				drawSprite(ss.livesCounterSprite(), x + TS * (2 * i), y);
			}
			break;
		}
		case PACMAN: {
			var ss = (SpritesheetPacManGame) ui.spritesheet();
			for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
				drawSprite(ss.livesCounterSprite(), x + TS * (2 * i), y);
			}
			break;
		}
		default:
			throw new IllegalGameVariantException(game().variant());
		}
		// text indicating that more lives are available than displayed
		int excessLives = numLivesDisplayed - maxLives;
		if (excessLives > 0) {
			drawText("+" + excessLives, ArcadeTheme.YELLOW, Font.font("Serif", FontWeight.BOLD, s(8)), x + TS * 10, y + TS);
		}
	}

	protected void drawBonus(Bonus bonus) {
		Rectangle2D symbolSprite;
		Rectangle2D valueSprite;
		switch (game().variant()) {
		case MS_PACMAN: {
			var ss = (SpritesheetMsPacManGame) ui.spritesheet();
			symbolSprite = ss.bonusSymbolSprite(bonus.symbol());
			valueSprite = ss.bonusValueSprite(bonus.symbol());
			break;
		}
		case PACMAN: {
			var ss = (SpritesheetPacManGame) ui.spritesheet();
			symbolSprite = ss.bonusSymbolSprite(bonus.symbol());
			valueSprite = ss.bonusValueSprite(bonus.symbol());
			break;
		}
		default:
			throw new IllegalGameVariantException(game().variant());
		}

		Rectangle2D sprite = null;
		switch (bonus.state()) {
		case Bonus.STATE_INACTIVE:
			break;
		case Bonus.STATE_EDIBLE:
			sprite = symbolSprite;
			break;
		case Bonus.STATE_EATEN:
			sprite = valueSprite;
			break;
		default:
			throw new IllegalArgumentException();
		}
		if (sprite == null) {
			return;
		}
		if (bonus instanceof MovingBonus) {
			var movingBonus = (MovingBonus) bonus;
			g.save();
			g.translate(0, movingBonus.dy());
			drawEntitySprite(movingBonus.entity(), sprite);
			g.restore();
		} else {
			drawEntitySprite(bonus.entity(), sprite);
		}
	}

	protected void drawPacSprite(Pac pac) {
		pac.animations().ifPresent(animations -> {
			if (animations instanceof SpriteAnimations) {
				var sa = (SpriteAnimations) animations;
				drawEntitySprite(pac, sa.currentSprite());
				if (infoVisiblePy.get() && pac.isVisible()) {
					g.setFill(Color.WHITE);
					g.setFont(Font.font("Monospaced", s(6)));
					var text = String.format("%s %d", sa.currentAnimationName(), sa.currentAnimation().frameIndex());
					g.fillText(text, s(pac.position().x() + 8), s(pac.position().y()));
					// indicate wish direction
					float r = 2;
					var pacCenter = pac.center();
					var indicatorCenter = pac.center().plus(pac.wishDir().vector().toFloatVec().scaled(1.5f * TS));
					var indicatorTopLeft = indicatorCenter.minus(r, r);
					g.setStroke(Color.WHITE);
					g.strokeLine(s(pacCenter.x()), s(pacCenter.y()), s(indicatorCenter.x()), s(indicatorCenter.y()));
					g.setFill(Color.GREEN);
					g.fillOval(s(indicatorTopLeft.x()), s(indicatorTopLeft.y()), s(2 * r), s(2 * r));
				}
			}
		});
	}

	protected void drawGhostSprite(Ghost ghost) {
		ghost.animations().ifPresent(animations -> {
			if (animations instanceof SpriteAnimations) {
				var sa = (SpriteAnimations) animations;
				drawEntitySprite(ghost, sa.currentSprite());
				if (infoVisiblePy.get() && ghost.isVisible()) {
					g.setFill(Color.WHITE);
					g.setFont(Font.font("Monospaced", s(6)));
					var text = String.format("%s %d", sa.currentAnimationName(), sa.currentAnimation().frameIndex());
					g.fillText(text, s(ghost.position().x() + 8), s(ghost.position().y()));
				}
			}
		});
	}

	/**
	 * Draws a sprite and performs scaling if game scene has unscaled canvas
	 * 
	 * @param source spritesheet source
	 * @param sprite spritesheet region ("sprite")
	 * @param x      UNSCALED x position
	 * @param y      UNSCALED y position
	 */
	protected void drawSprite(Image source, Rectangle2D sprite, double x, double y) {
		if (sprite != null) {
			g.drawImage(source, //
					sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(), //
					s(x), s(y), s(sprite.getWidth()), s(sprite.getHeight()));
		}
	}

	protected void drawSprite(Rectangle2D sprite, double x, double y) {
		drawSprite(ui.spritesheet().source(), sprite, x, y);
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
	protected void drawSpriteOverBoundingBox(Rectangle2D r, double x, double y) {
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
	protected void drawEntitySprite(Entity entity, Rectangle2D r) {
		checkNotNull(entity);
		if (entity.isVisible()) {
			drawSpriteOverBoundingBox(r, entity.position().x(), entity.position().y());
		}
	}

	protected void drawCredit(int credit, double x, double y) {
		drawText(String.format("CREDIT %2d", credit), ArcadeTheme.PALE, sceneFont(), x, y);
	}

	protected void drawMidwayCopyright(double x, double y) {
		drawText("\u00A9 1980 MIDWAY MFG.CO.", ArcadeTheme.PINK, sceneFont(), x, y);
	}

	protected void drawMsPacManCopyright(double x, double y) {
		Image logo = ui.theme().get("mspacman.logo.midway");
		g.drawImage(logo, s(x), s(y + 2), s(TS * 4 - 2), s(TS * 4));
		g.setFill(ArcadeTheme.RED);
		g.setFont(Font.font("Dialog", s(11)));
		g.fillText("\u00a9", s(x + TS * 5), s(y + TS * 2 + 2)); // (c) symbol
		g.setFont(sceneFont());
		g.fillText("MIDWAY MFG CO", s(x + TS * 7), s(y + TS * 2));
		g.fillText("1980/1981", s(x + TS * 8), s(y + TS * 4));
	}

	protected void drawClapperBoard(ClapperBoardAnimation animation, double x, double y) {
		int spriteIndex = animation.currentSpriteIndex();
		if (spriteIndex != -1) {
			var ss = (SpritesheetMsPacManGame) ui.spritesheet();
			var sprite = ss.clapperboardSprites()[spriteIndex];
			drawSpriteOverBoundingBox(sprite, x, y);
			g.setFont(sceneFont());
			g.setFill(ArcadeTheme.PALE.darker());
			var numberX = s(x + sprite.getWidth() - 25);
			var numberY = s(y + 18);
			g.setFill(ArcadeTheme.PALE);
			g.fillText(animation.number(), numberX, numberY);
			var textX = s(x + sprite.getWidth());
			g.fillText(animation.text(), textX, numberY);
		}
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
}