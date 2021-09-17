package de.amr.games.pacman.ui.fx.rendering;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;

import java.util.Map;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.model.common.GameEntity;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.pacman.Bonus;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * Standard implementation of scene rendering, using a spritesheet.
 * 
 * @author Armin Reichert
 */
public abstract class Rendering2D {

	public static final Font ARCADE_FONT = Font.loadFont(//
			Rendering2D.class.getResourceAsStream("/emulogic.ttf"), 8);

	public static Image image(String path) {
		return new Image(Rendering2D.class.getResource(path).toExternalForm());
	}

	private static final Color PACMAN_FOOD_COLOR = Color.rgb(250, 185, 176);

	private static final Color PACMAN_MAZE_WALL_SIDE_COLOR = Color.rgb(33, 33, 255);

	private static final Color MS_PACMAN_FOOD_COLOR[] = { //
			Color.rgb(222, 222, 255), //
			Color.rgb(255, 255, 0), //
			Color.rgb(255, 0, 0), //
			Color.rgb(222, 222, 255), //
			Color.rgb(0, 255, 255), //
			Color.rgb(222, 222, 255),//
	};

	private static final Color MS_PACMAN_MAZE_WALL_TOP_COLOR[] = { //
			Color.rgb(255, 183, 174), //
			Color.rgb(71, 183, 255), //
			Color.rgb(222, 151, 81), //
			Color.rgb(33, 33, 255), //
			Color.rgb(255, 183, 255), //
			Color.rgb(255, 183, 174),//
	};

	private static final Color MS_PACMAN_MAZE_WALL_SIDE_COLOR[] = { //
			Color.rgb(255, 0, 0), //
			Color.rgb(222, 222, 255), //
			Color.rgb(222, 222, 255), //
			Color.rgb(255, 183, 81), //
			Color.rgb(255, 255, 0), //
			Color.rgb(255, 0, 0),//
	};

	public static Color getMazeWallTopColor(GameVariant gameVariant, int mazeNumber) {
		return gameVariant == GameVariant.PACMAN ? getPacManMazeWallColor(mazeNumber)
				: getMsPacManMazeWallTopColor(mazeNumber);
	}

	public static Color getMazeWallSideColor(GameVariant gameVariant, int mazeNumber) {
		return gameVariant == GameVariant.PACMAN ? Color.WHITE : getMsPacManMazeWallSideColor(mazeNumber);
	}

	public static Color getFoodColor(GameVariant gameVariant, int mazeNumber) {
		return gameVariant == GameVariant.PACMAN ? getPacManFoodColor(mazeNumber) : getMsPacManFoodColor(mazeNumber);
	}

	public static Color getGhostColor(int ghostID) {
		return ghostID == 0 ? Color.RED : ghostID == 1 ? Color.PINK : ghostID == 2 ? Color.CYAN : Color.ORANGE;
	}

	public static Color getGhostBlueColor() {
		return Color.CORNFLOWERBLUE;
	}

	public static Color getPacManMazeWallColor(int mazeNumber) {
		return PACMAN_MAZE_WALL_SIDE_COLOR;
	}

	public static Color getPacManFoodColor(int mazeNumber) {
		return PACMAN_FOOD_COLOR;
	}

	private static Color getMsPacManFoodColor(int mazeNumber) {
		return MS_PACMAN_FOOD_COLOR[mazeNumber - 1];
	}

	public static Color getMsPacManMazeWallTopColor(int mazeNumber) {
		return MS_PACMAN_MAZE_WALL_TOP_COLOR[mazeNumber - 1];
	}

	public static Color getMsPacManMazeWallSideColor(int mazeNumber) {
		return MS_PACMAN_MAZE_WALL_SIDE_COLOR[mazeNumber - 1];
	}

	public static Image colorsExchanged(Image source, Map<Color, Color> exchanges) {
		WritableImage newImage = new WritableImage((int) source.getWidth(), (int) source.getHeight());
		for (int x = 0; x < source.getWidth(); ++x) {
			for (int y = 0; y < source.getHeight(); ++y) {
				Color oldColor = source.getPixelReader().getColor(x, y);
				for (Map.Entry<Color, Color> entry : exchanges.entrySet()) {
					if (oldColor.equals(entry.getKey())) {
						newImage.getPixelWriter().setColor(x, y, entry.getValue());
					}
				}
			}
		}
		return newImage;
	}

	public abstract Spritesheet spritesheet();

	/**
	 * @param bonus game bonus
	 * @return sprite bounds for bonus depending on its state
	 */
	public Rectangle2D bonusSprite(Bonus bonus) {
		if (bonus.state == Bonus.EDIBLE) {
			return getSymbolSprites().get(bonus.symbol);
		}
		if (bonus.state == Bonus.EATEN) {
			return getBonusValuesSprites().get(bonus.points);
		}
		return null; // should not happen
	}

	/**
	 * @return font used in score and game state display
	 */
	public Font getScoreFont() {
		return ARCADE_FONT;
	}

	/**
	 * Renders a game entity centered over its tile position
	 * 
	 * @param g      the graphics context
	 * @param entity the entity getting rendered
	 * @param sb     sprite bounds in spritsheet
	 */
	public void renderEntity(GraphicsContext g, GameEntity entity, Rectangle2D sb) {
		if (entity.isVisible() && sb != null) {
			g.drawImage(spritesheet().getSource(), sb.getMinX(), sb.getMinY(), sb.getWidth(), sb.getHeight(),
					entity.position().x - sb.getWidth() / 2 + HTS, entity.position().y - sb.getHeight() / 2 + HTS, sb.getWidth(),
					sb.getHeight());
		}
	}

	/**
	 * Renders a sprite at a given location.
	 * 
	 * @param g  the graphics context
	 * @param sb sprite bounds in spritesheet
	 * @param x  render location x
	 * @param y  render location y
	 */
	public void renderSprite(GraphicsContext g, Rectangle2D sb, double x, double y) {
		g.drawImage(spritesheet().getSource(), sb.getMinX(), sb.getMinY(), sb.getWidth(), sb.getHeight(), x, y,
				sb.getWidth(), sb.getHeight());
	}

	// Maze

	public abstract void renderMazeFull(GraphicsContext g, int mazeNumber, double x, double y);

	public abstract void renderMazeEmpty(GraphicsContext g, int mazeNumber, double x, double y);

	public abstract void renderMazeFlashing(GraphicsContext g, int mazeNumber, double x, double y);

	public abstract Color getMazeWallColor(int i);

	// Animations

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createPlayerMunchingAnimations();

	public abstract TimedSequence<Rectangle2D> createPlayerDyingAnimation();

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createGhostKickingAnimations(int ghostID);

	public abstract TimedSequence<Rectangle2D> createGhostFrightenedAnimation();

	public abstract TimedSequence<Rectangle2D> createGhostFlashingAnimation();

	public abstract Map<Direction, TimedSequence<Rectangle2D>> createGhostReturningHomeAnimations();

	// Sprites

	public abstract Rectangle2D getLifeSprite();

	public abstract Map<Integer, Rectangle2D> getBountyNumberSprites();

	public abstract Map<Integer, Rectangle2D> getBonusValuesSprites();

	public abstract Map<String, Rectangle2D> getSymbolSprites();
}