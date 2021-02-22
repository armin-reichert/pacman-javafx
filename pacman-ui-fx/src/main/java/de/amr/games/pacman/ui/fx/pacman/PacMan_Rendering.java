package de.amr.games.pacman.ui.fx.pacman;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.guys.Bonus;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.ui.fx.common.Helper;
import de.amr.games.pacman.ui.fx.common.SpritesheetBasedRendering;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * Rendering for the scenes of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacMan_Rendering extends SpritesheetBasedRendering {

	private final Image mazeFull = new Image("/pacman/graphics/maze_full.png", false);
	private final Image mazeEmpty = new Image("/pacman/graphics/maze_empty.png", false);

	public PacMan_Rendering() {
		super(new Image("/pacman/graphics/sprites.png", false));

		symbolRegions = Arrays.asList(tileRegion(2, 3), tileRegion(3, 3), tileRegion(4, 3), tileRegion(5, 3),
				tileRegion(6, 3), tileRegion(7, 3), tileRegion(8, 3), tileRegion(9, 3));

		//@formatter:off
		bonusValueRegions = new HashMap<>();
		bonusValueRegions.put(100,  tileRegion(0, 9, 1, 1));
		bonusValueRegions.put(300,  tileRegion(1, 9, 1, 1));
		bonusValueRegions.put(500,  tileRegion(2, 9, 1, 1));
		bonusValueRegions.put(700,  tileRegion(3, 9, 1, 1));
		bonusValueRegions.put(1000, tileRegion(4, 9, 2, 1)); // left-aligned 
		bonusValueRegions.put(2000, tileRegion(3, 10, 3, 1));
		bonusValueRegions.put(3000, tileRegion(3, 11, 3, 1));
		bonusValueRegions.put(5000, tileRegion(3, 12, 3, 1));

		bountyValueRegions = new HashMap<>();
		bountyValueRegions.put(200,  tileRegion(0, 8, 1, 1));
		bountyValueRegions.put(400,  tileRegion(1, 8, 1, 1));
		bountyValueRegions.put(800,  tileRegion(2, 8, 1, 1));
		bountyValueRegions.put(1600, tileRegion(3, 8, 1, 1));
		//@formatter:on

		// Animations

		Image mazeEmptyBright = Helper.exchangeColors(mazeEmpty, Map.of(getMazeWallBorderColor(0), Color.WHITE));
		mazeFlashingAnim = Arrays.asList(Animation.of(mazeEmptyBright, mazeEmpty).frameDuration(15));

		pacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			Animation<Rectangle2D> animation = Animation.of(tileRegion(2, 0), tileRegion(1, index(dir)),
					tileRegion(0, index(dir)), tileRegion(1, index(dir)));
			animation.frameDuration(2).endless().run();
			pacManMunchingAnim.put(dir, animation);
		}

		pacDyingAnim = Animation.of(tileRegion(3, 0), tileRegion(4, 0), tileRegion(5, 0), tileRegion(6, 0),
				tileRegion(7, 0), tileRegion(8, 0), tileRegion(9, 0), tileRegion(10, 0), tileRegion(11, 0), tileRegion(12, 0),
				tileRegion(13, 0));
		pacDyingAnim.frameDuration(8);

		ghostsKickingAnim = new ArrayList<>(4);
		for (int id = 0; id < 4; ++id) {
			EnumMap<Direction, Animation<Rectangle2D>> walkingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				Animation<Rectangle2D> animation = Animation.of(tileRegion(2 * index(dir), 4 + id),
						tileRegion(2 * index(dir) + 1, 4 + id));
				animation.frameDuration(10).endless();
				walkingTo.put(dir, animation);
			}
			ghostsKickingAnim.add(walkingTo);
		}

		ghostEyesAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnim.put(dir, Animation.of(tileRegion(8 + index(dir), 5)));
		}

		ghostBlueAnim = Animation.of(tileRegion(8, 4), tileRegion(9, 4));
		ghostBlueAnim.frameDuration(20).endless();

		ghostFlashingAnim = Animation.of(tileRegion(8, 4), tileRegion(9, 4), tileRegion(10, 4), tileRegion(11, 4));
		ghostFlashingAnim.frameDuration(5).endless();
	}

	@Override
	public Color getMazeWallBorderColor(int mazeIndex) {
		return Color.rgb(33, 33, 255);
	}

	@Override
	public Color getMazeWallColor(int mazeIndex) {
		return Color.BLACK;
	}

	@Override
	public void drawMaze(GraphicsContext g, int mazeNumber, int x, int y, boolean flashing) {
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y);
		} else {
			g.drawImage(mazeFull, x, y);
		}
	}

	@Override
	public void drawLifeCounterSymbol(GraphicsContext g, int x, int y) {
		drawRegion(g, tileRegion(8, 1), x, y);
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		drawCreature(g, bonus, bonusSpriteRegion(bonus));
	}

	@Override
	public Animation<Rectangle2D> playerMunching(Pac pac, Direction dir) {
		return pacManMunchingAnim.get(ensureDirection(dir));
	}
}