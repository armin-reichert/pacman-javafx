/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.lib.Score;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadePalette;
import de.amr.games.pacman.ui.fx.rendering2d.SpriteAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.ClapperboardAnimation;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Base class of all 2D scenes.
 *
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

	protected static double t(double tiles) {
		return tiles * TS;
	}

	public final BooleanProperty infoVisiblePy = new SimpleBooleanProperty(this, "infoVisible", false);

	protected GameSceneContext context;
	protected Canvas canvas;
	protected GraphicsContext g;
	protected double scaling = 1;
	protected boolean scoreVisible;
	protected boolean creditVisible;

	protected GameScene2D() {
	}

	@Override
	public GameSceneContext context() {
		return context;
	}

	@Override
	public void setContext(GameSceneContext context) {
		checkNotNull(context);
		this.context = context;
	}

	public Canvas canvas() {
		return canvas;
	}

	public void setCanvas(Canvas canvas) {
		checkNotNull(canvas);
		this.canvas = canvas;
		this.g = canvas.getGraphicsContext2D();
	}

	public void setScaling(double scaling) {
		if (scaling <= 0) {
			throw new IllegalArgumentException("Scaling value must be positive but is " + scaling);
		}
		this.scaling = scaling;
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

	protected double s(double value) {
		return value * scaling;
	}

	protected Font sceneFont(double size) {
		return context.theme().font("font.arcade", s(size));
	}

	@Override
	public Node root() {
		return canvas;
	}

	@Override
	public boolean is3D() {
		return false;
	}

	public void draw() {
		clearCanvas();
		if (context == null) {
			Logger.error("Cannot render game scene {}, no context exists", getClass().getSimpleName());
			return; // TODO may this happen?
		}
		if (scoreVisible) {
			drawScore(context.game().score(), "SCORE", t(1), t(1));
			drawScore(context.game().highScore(), "HIGH SCORE", t(14), t(1));
		}
		if (creditVisible) {
			drawCredit(GameController.it().credit(), t(2), t(36) - 1);
		}
		drawSceneContent();
		if (infoVisiblePy.get()) {
			drawSceneInfo();
		}
	}

	/**
	 * Draws the scene content, e.g. the maze and the guys.
	 */
	protected abstract void drawSceneContent();

	/**
	 * Draws additional scene info, e.g. tile structure or debug info.
	 */
	protected void drawSceneInfo() {
	}

	protected void clearCanvas() {
		g.setFill(context.theme().color("canvas.background"));
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	protected void drawScore(Score score, String title, double x, double y) {
		var pointsText = String.format("%02d", score.points());
		var font = sceneFont(8);
		drawText(title, ArcadePalette.PALE, font, x, y);
		drawText(String.format("%7s", pointsText), ArcadePalette.PALE, font, x, y + TS + 1);
		if (score.points() != 0) {
			drawText("L" + score.levelNumber(), ArcadePalette.PALE, font,x + t(8),y + TS + 1);
		}
	}

	protected void drawLevelCounter() {
		drawLevelCounter(context.game().variant(), context.game().levelCounter(), t(24), t(34));
	}

	private void drawLevelCounter(GameVariant variant, Iterable<Byte> levelSymbols, double xr, double yr) {
		double x = xr;
		for (var symbol : levelSymbols) {
			drawSprite(bonusSymbolSprite(symbol, variant), x, yr);
			x -= TS * 2;
		}
	}

	private Rectangle2D bonusSymbolSprite(byte symbol, GameVariant variant) {
		return switch (variant) {
			case MS_PACMAN -> ((SpritesheetMsPacManGame) context.spritesheet()).bonusSymbolSprite(symbol);
			case PACMAN -> ((SpritesheetPacManGame) context.spritesheet()).bonusSymbolSprite(symbol);
		};
	}

	private Rectangle2D livesCounterSprite(GameVariant variant) {
		return switch (variant) {
			case MS_PACMAN -> ((SpritesheetMsPacManGame) context.spritesheet()).livesCounterSprite();
			case PACMAN -> ((SpritesheetPacManGame) context.spritesheet()).livesCounterSprite();
		};
	}

	protected void drawLivesCounter(int numLivesDisplayed) {
		if (numLivesDisplayed <= 0) {
			return;
		}
		var x = TS * 2;
		var y = TS * (GameModel.TILES_Y - 2);
		int maxLives = 5;
		for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
			drawSprite(livesCounterSprite(context.game().variant()), x + TS * (2 * i), y);
		}
		// text indicating that more lives are available than displayed
		int excessLives = numLivesDisplayed - maxLives;
		if (excessLives > 0) {
			drawText("+" + excessLives, ArcadePalette.YELLOW,
					Font.font("Serif", FontWeight.BOLD, s(8)), x + TS * 10, y + TS);
		}
	}

	protected void drawBonus(Bonus bonus) {
		switch (context.game().variant()) {
			case MS_PACMAN -> {
				var ss = (SpritesheetMsPacManGame) context.spritesheet();
				if (bonus instanceof MovingBonus movingBonus) {
					//TODO reconsider this way of implementing the jumping bonus
					g.save();
					g.translate(0, movingBonus.dy());
					if (bonus.state() == Bonus.STATE_EDIBLE) {
						drawEntitySprite(bonus.entity(), ss.bonusSymbolSprite(bonus.symbol()));
					} else if (bonus.state() == Bonus.STATE_EATEN) {
						drawEntitySprite(bonus.entity(), ss.bonusValueSprite(bonus.symbol()));
					}
					g.restore();
				}
			}
			case PACMAN -> {
				var ss = (SpritesheetPacManGame) context.spritesheet();
				if (bonus.state() == Bonus.STATE_EDIBLE) {
					drawEntitySprite(bonus.entity(), ss.bonusSymbolSprite(bonus.symbol()));
				} else if (bonus.state() == Bonus.STATE_EATEN) {
					drawEntitySprite(bonus.entity(), ss.bonusValueSprite(bonus.symbol()));
				}
			}
			default -> throw new IllegalGameVariantException(context.game().variant());
		}
	}

	protected void drawPac(Pac pac) {
		if (!pac.isVisible()) {
			return;
		}
		pac.animations().ifPresent(pa -> {
			if (pa instanceof SpriteAnimations sa) {
				drawEntitySprite(pac, sa.currentSprite());
				if (infoVisiblePy.get()) {
					drawPacInfo(pac, sa);
				}
			}
		});
	}

	private void drawPacInfo(Pac pac, SpriteAnimations animations) {
		g.setFill(Color.WHITE);
		g.setFont(Font.font("Monospaced", s(6)));
		var text = animations.currentAnimationName() + " " + animations.currentAnimation().frameIndex();
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

	protected void drawGhost(Ghost ghost) {
		if (!ghost.isVisible()) {
			return;
		}
		ghost.animations().ifPresent(ga -> {
			// WebFX does not allow Class::isInstance and Class::cast, so we do it the old way.
			if (ga instanceof SpriteAnimations sa) {
				drawEntitySprite(ghost, sa.currentSprite());
				if (infoVisiblePy.get()) {
					drawGhostInfo(ghost, sa);
				}
			}
		});
	}

	private void drawGhostInfo(Ghost ghost, SpriteAnimations sa) {
		g.setFill(Color.WHITE);
		g.setFont(Font.font("Monospaced", s(6)));
		var text = sa.currentAnimationName() + " " + sa.currentAnimation().frameIndex();
		g.fillText(text, s(ghost.position().x() + 8), s(ghost.position().y()));
	}

	/**
	 * Draws a sprite using the current scene scaling.
	 *
	 * @param source sprite sheet source
	 * @param sprite sprite sheet region ("sprite")
	 * @param x      UNSCALED x position
	 * @param y      UNSCALED y position
	 */
	protected void drawSprite(Image source, Rectangle2D sprite, double x, double y) {
		if (sprite != null) {
			g.drawImage(source,
					sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(),
					s(x), s(y), s(sprite.getWidth()), s(sprite.getHeight()));
		}
	}

	/**
	 * Draws a sprite centered over a one "square tile" large box (bounding box of creature). The position specifies the
	 * left-upper corner of the bounding box. Note that the sprites for Pac-Man and the ghosts are 16 pixels wide but the
	 * bounding box is only 8 pixels (one square tile) wide.
	 *
	 * @param sprite sprite sheet region (can be null)
	 * @param x x coordinate of left-upper corner of bounding box
	 * @param y y coordinate of left-upper corner of bounding box
	 */
	protected void drawSpriteOverBoundingBox(Rectangle2D sprite, double x, double y) {
		if (sprite != null) {
			drawSprite(sprite, x + HTS - sprite.getWidth() / 2, y + HTS - sprite.getHeight() / 2);
		}
	}

	/**
	 * Draws a sprite at the given position (upper left corner).
	 * @param sprite sprite sheet region ("sprite")
	 * @param x x coordinate of upper left corner
	 * @param y y coordinate of upper left corner
	 */
	protected void drawSprite(Rectangle2D sprite, double x, double y) {
		drawSprite(context.spritesheet().source(), sprite, x, y);
	}

	/**
	 * Draws the sprite over the bounding box of the given entity (if visible).
	 *
	 * @param entity an entity like Pac-Man or a ghost
	 * @param sprite the sprite
	 */
	protected void drawEntitySprite(Entity entity, Rectangle2D sprite) {
		if (entity.isVisible()) {
			drawSpriteOverBoundingBox(sprite, entity.position().x(), entity.position().y());
		}
	}

	protected void drawCredit(int credit, double x, double y) {
 		drawText(String.format("CREDIT %2d", credit), ArcadePalette.PALE, sceneFont(8), x, y);
	}

	protected void drawMidwayCopyright(double x, double y) {
		drawText("© 1980 MIDWAY MFG.CO.", ArcadePalette.PINK, sceneFont(8), x, y);
	}

	protected void drawMsPacManCopyright(double x, double y) {
		Image logo = context.theme().get("mspacman.logo.midway");
		g.drawImage(logo, s(x), s(y + 2), s(TS * 4 - 2), s(TS * 4));
		g.setFill(ArcadePalette.RED);
		g.setFont(sceneFont(8));
		g.fillText("©", s(x + TS * 5), s(y + TS * 2 + 2)); // (c) symbol
		g.fillText("MIDWAY MFG CO", s(x + TS * 7), s(y + TS * 2));
		g.fillText("1980/1981", s(x + TS * 8), s(y + TS * 4));
	}

	protected void drawClapperBoard(ClapperboardAnimation animation, double x, double y) {
		var ss = (SpritesheetMsPacManGame) context.spritesheet();
		var sprite = animation.currentSprite(ss.clapperboardSprites());
		if (sprite != null) {
			drawSpriteOverBoundingBox(sprite, x, y);
			g.setFont(sceneFont(8));
			g.setFill(ArcadePalette.PALE.darker());
			var numberX = s(x + sprite.getWidth() - 25);
			var numberY = s(y + 18);
			g.setFill(ArcadePalette.PALE);
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
		g.setStroke(ArcadePalette.PALE);
		g.setLineWidth(0.2);
		for (int row = 0; row <= tilesY; ++row) {
			g.strokeLine(0, s(TS * (row)), s(tilesX * TS), s(TS * (row)));
		}
		for (int col = 0; col <= tilesY; ++col) {
			g.strokeLine(s(TS * (col)), 0, s(TS * (col)), s(tilesY * TS));
		}
	}
}