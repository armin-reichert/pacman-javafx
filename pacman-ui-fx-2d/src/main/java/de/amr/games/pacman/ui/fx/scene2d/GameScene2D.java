/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.lib.Score;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.ActionHandler;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.app.SoundHandler;
import de.amr.games.pacman.ui.fx.rendering2d.ArcadeTheme;
import de.amr.games.pacman.ui.fx.rendering2d.SpriteAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.ClapperBoardAnimation;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * Base class of all 2D scenes.
 *
 * @author Armin Reichert
 */
public abstract class GameScene2D implements GameScene {

	public static final int TILES_X = 28;
	public static final int TILES_Y = 36;

	protected static float t(double tiles) {
		return (float) tiles * TS;
	}

	public final BooleanProperty infoVisiblePy = new SimpleBooleanProperty(this, "infoVisible", false);

	protected Theme theme;
	protected Spritesheet spritesheet;
	protected ActionHandler actionHandler;
	protected SoundHandler soundHandler;
	protected Canvas canvas;
	protected GraphicsContext g;
	protected double scaling = 1;
	protected boolean scoreVisible;
	protected boolean creditVisible;

	protected GameScene2D() {
		infoVisiblePy.bind(PacManGames2d.PY_SHOW_DEBUG_INFO); // should probably be elsewhere
	}

	public void setActionHandler(ActionHandler actionHandler) {
		this.actionHandler = actionHandler;
	}

	@Override
	public Optional<ActionHandler> actionHandler() {
		return Optional.ofNullable(actionHandler);
	}

	public void setTheme(Theme theme) {
		this.theme = theme;
	}

	public Theme getTheme() {
		return theme;
	}

	public void setSpritesheet(Spritesheet spritesheet) {
		this.spritesheet = spritesheet;
	}

	public Spritesheet getSpritesheet() {
		return spritesheet;
	}

	public void setSoundHandler(SoundHandler soundHandler) {
		this.soundHandler = soundHandler;
	}

	public SoundHandler getSoundHandler() {
		return soundHandler;
	}

	public void setCanvas(Canvas canvas) {
		this.canvas = canvas;
		this.g = canvas.getGraphicsContext2D();
	}

	public void setScaling(double scaling) {
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

	@Override
	public void setParentScene(Scene parentScene) {
	}

	protected double s(double value) {
		return value * scaling;
	}

	protected Font sceneFont() {
		double size = Math.floor(TS * scaling);
		return theme.font("font.arcade", size);
	}

	public Canvas canvas() {
		return canvas;
	}

	@Override
	public Node root() {
		return canvas;
	}

	@Override
	public boolean is3D() {
		return false;
	}

	public void render() {
		if (theme == null || spritesheet == null) {
			return;
		}
		clearCanvas();
		if (scoreVisible) {
			drawScore(game().score(), "SCORE", t(1), t(1));
			drawScore(game().highScore(), "HIGH SCORE", t(14), t(1));
		}
		if (creditVisible) {
			drawCredit(game().credit(), t(2), t(36) - 1);
		}
		drawSceneContent();
		if (infoVisiblePy.get()) {
			drawSceneInfo();
		}
	}

	public void clearCanvas() {
		g.setFill(Color.BLACK);
		g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
	}

	protected void drawScore(Score score, String title, double x, double y) {
		var font = sceneFont();
		drawText(title,  theme.color("palette.pale"), font, x, y);
		var pointsText = String.format("%02d", score.points());
		drawText(String.format("%7s", pointsText), theme.color("palette.pale"), font, x, (y + TS + 1));
		if (score.points() != 0) {
			drawText(String.format("L%d", score.levelNumber()), theme.color("palette.pale"), font, x + TS * 8, y + TS + 1);
		}
	}

	protected void drawLevelCounter(double xr, double yr, List<Byte> levelSymbols) {
		double x = xr;
		switch (game().variant()) {
			case MS_PACMAN: {
				var ss = (SpritesheetMsPacManGame) spritesheet;
				for (var symbol : levelSymbols) {
					drawSprite(ss.bonusSymbolSprite(symbol), x, yr);
					x -= TS * 2;
				}
				break;
			}
			case PACMAN: {
				var ss = (SpritesheetPacManGame) spritesheet;
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
				var ss = (SpritesheetMsPacManGame) spritesheet;
				for (int i = 0; i < Math.min(numLivesDisplayed, maxLives); ++i) {
					drawSprite(ss.livesCounterSprite(), x + TS * (2 * i), y);
				}
				break;
			}
			case PACMAN: {
				var ss = (SpritesheetPacManGame) spritesheet;
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
			drawText("+" + excessLives, theme.color("palette.yellow"), Font.font("Serif", FontWeight.BOLD, s(8)), x + TS * 10, y + TS);
		}
	}

	protected void drawBonus(Bonus bonus) {
		Rectangle2D symbolSprite;
		Rectangle2D valueSprite;
		switch (game().variant()) {
			case MS_PACMAN: {
				var ss = (SpritesheetMsPacManGame) spritesheet;
				symbolSprite = ss.bonusSymbolSprite(bonus.symbol());
				valueSprite = ss.bonusValueSprite(bonus.symbol());
				break;
			}
			case PACMAN: {
				var ss = (SpritesheetPacManGame) spritesheet;
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

	protected void drawPac(Pac pac) {
		if (!pac.isVisible()) {
			return;
		}
		pac.animations().ifPresent(pa -> {
			if (pa instanceof SpriteAnimations) {
				var sa = (SpriteAnimations) pa;
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
			if (ga instanceof SpriteAnimations) {
				var sa = (SpriteAnimations) ga;
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
	 * @param sprite sprite sheet region (may be null)
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
	 * @param sprite spritesheet region ("sprite")
	 * @param x x coordinate of upper left corner
	 * @param y y coordinate of upper left corner
	 */
	protected void drawSprite(Rectangle2D sprite, double x, double y) {
		drawSprite(spritesheet.source(), sprite, x, y);
	}

	/**
	 * Draws the sprite over the bounding box of the given entity (if visible).
	 *
	 * @param entity an entity like Pac-Man or a ghost
	 * @param sprite the sprite
	 */
	protected void drawEntitySprite(Entity entity, Rectangle2D sprite) {
		checkNotNull(entity);
		if (entity.isVisible()) {
			drawSpriteOverBoundingBox(sprite, entity.position().x(), entity.position().y());
		}
	}

	protected void drawCredit(int credit, double x, double y) {
 		drawText(String.format("CREDIT %2d", credit), theme.color("palette.pale"), sceneFont(), x, y);
	}

	protected void drawMidwayCopyright(double x, double y) {
		drawText("© 1980 MIDWAY MFG.CO.", theme.color("palette.pink"), sceneFont(), x, y);
	}

	protected void drawMsPacManCopyright(double x, double y) {
		Image logo = theme.get("mspacman.logo.midway");
		g.drawImage(logo, s(x), s(y + 2), s(TS * 4 - 2), s(TS * 4));
		g.setFill(theme.color("palette.red"));
		g.setFont(sceneFont());
		g.fillText("©", s(x + TS * 5), s(y + TS * 2 + 2)); // (c) symbol
		g.fillText("MIDWAY MFG CO", s(x + TS * 7), s(y + TS * 2));
		g.fillText("1980/1981", s(x + TS * 8), s(y + TS * 4));
	}

	protected void drawClapperBoard(ClapperBoardAnimation animation, double x, double y) {
		int spriteIndex = animation.currentSpriteIndex();
		if (spriteIndex != -1) {
			var ss = (SpritesheetMsPacManGame) spritesheet;
			var sprite = ss.clapperboardSprites()[spriteIndex];
			drawSpriteOverBoundingBox(sprite, x, y);
			g.setFont(sceneFont());
			g.setFill(theme.color("palette.pale").darker());
			var numberX = s(x + sprite.getWidth() - 25);
			var numberY = s(y + 18);
			g.setFill(theme.color("palette.pale"));
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
		g.setStroke(theme.color("palette.pale"));
		g.setLineWidth(0.2);
		for (int row = 0; row <= tilesY; ++row) {
			g.strokeLine(0, s(TS * (row)), s(tilesX * TS), s(TS * (row)));
		}
		for (int col = 0; col <= tilesY; ++col) {
			g.strokeLine(s(TS * (col)), 0, s(TS * (col)), s(tilesY * TS));
		}
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