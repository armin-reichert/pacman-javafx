package de.amr.games.pacman.ui.fx.mspacman;

import static de.amr.games.pacman.world.PacManGameWorld.t;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.guys.Bonus;
import de.amr.games.pacman.model.guys.Pac;
import de.amr.games.pacman.ui.fx.common.Helper;
import de.amr.games.pacman.ui.fx.common.SpritesheetBasedRendering;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Rendering for the scenes of the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_Rendering extends SpritesheetBasedRendering {

	private final Map<Direction, Animation<Rectangle2D>> msPacMunchingAnim;
	private final Animation<Integer> bonusJumpAnim;
	private final Animation<Rectangle2D> birdAnim;

	/* Tiles in right half of spritesheet */
	private Rectangle2D s(int tileX, int tileY) {
		return tileRegionAt(456, 0, tileX, tileY, 1, 1);
	}

	public MsPacMan_Rendering() {
		super(new Image("/mspacman/graphics/sprites.png", false));

		symbolRegions = Arrays.asList(s(3, 0), s(4, 0), s(5, 0), s(6, 0), s(7, 0), s(8, 0), s(9, 0));

		//@formatter:off
		bonusValueRegions = new HashMap<>();
		bonusValueRegions.put(100,  s(3, 1));
		bonusValueRegions.put(200,  s(4, 1));
		bonusValueRegions.put(500,  s(5, 1));
		bonusValueRegions.put(700,  s(6, 1));
		bonusValueRegions.put(1000, s(7, 1));
		bonusValueRegions.put(2000, s(8, 1));
		bonusValueRegions.put(5000, s(9, 1));
		
		bountyValueRegions = new HashMap<>();
		bountyValueRegions.put(200,  s(0, 8));
		bountyValueRegions.put(400,  s(1, 8));
		bountyValueRegions.put(800,  s(2, 8));
		bountyValueRegions.put(1600, s(3, 8));
		//@formatter:on

		// Animations

		mazeFlashingAnim = new ArrayList<>(6);
		for (int mazeIndex = 0; mazeIndex < 6; ++mazeIndex) {
			Map<Color, Color> exchanges = Map.of(getMazeWallBorderColor(mazeIndex), Color.WHITE, getMazeWallColor(mazeIndex),
					Color.BLACK);
			WritableImage mazeEmpty = new WritableImage(226, 248);
			mazeEmpty.getPixelWriter().setPixels(0, 0, 226, 248, spritesheet.getPixelReader(), 226, 248 * mazeIndex);
			Image mazeEmptyBright = Helper.exchangeColors(mazeEmpty, exchanges);
			mazeFlashingAnim.add(Animation.of(mazeEmptyBright, mazeEmpty).frameDuration(15));
		}

		msPacMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			Animation<Rectangle2D> munching = Animation.of(s(1, d), s(1, d), s(2, d), s(0, d));
			munching.frameDuration(2).endless();
			msPacMunchingAnim.put(dir, munching);
		}

		pacDyingAnim = Animation.of(s(0, 3), s(0, 0), s(0, 1), s(0, 2));
		pacDyingAnim.frameDuration(10).repetitions(2);

		pacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			pacManMunchingAnim.put(dir, Animation.of(s(0, 9 + d), s(1, 9 + d), s(2, 9)).frameDuration(2).endless());
		}

		ghostsKickingAnim = new ArrayList<>(4);
		for (int ghostType = 0; ghostType < 4; ++ghostType) {
			EnumMap<Direction, Animation<Rectangle2D>> kickingTo = new EnumMap<>(Direction.class);
			for (Direction dir : Direction.values()) {
				int d = index(dir);
				Animation<Rectangle2D> kicking = Animation.of(s(2 * d, 4 + ghostType), s(2 * d + 1, 4 + ghostType));
				kicking.frameDuration(4).endless();
				kickingTo.put(dir, kicking);
			}
			ghostsKickingAnim.add(kickingTo);
		}

		ghostEyesAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			ghostEyesAnim.put(dir, Animation.of(s(8 + index(dir), 5)));
		}

		ghostBlueAnim = Animation.of(s(8, 4), s(9, 4)).frameDuration(20).endless().run();
		ghostFlashingAnim = Animation.of(s(8, 4), s(9, 4), s(10, 4), s(11, 4)).frameDuration(5).endless();

		bonusJumpAnim = Animation.of(0, 2, 0, -2).frameDuration(20).endless().run();
		birdAnim = Animation.of(//
				new Rectangle2D(489, 176, 32, 16), //
				new Rectangle2D(521, 176, 32, 16));
		birdAnim.endless().frameDuration(10);
	}

	public Map<Direction, Animation<Rectangle2D>> pacManMunching() {
		return pacManMunchingAnim;
	}

	public Rectangle2D getHeart() {
		return s(2, 10);
	}

	public Animation<Rectangle2D> getBirdAnim() {
		return birdAnim;
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	@Override
	public Color getMazeWallColor(int mazeIndex) {
		switch (mazeIndex) {
		case 0:
			return Color.rgb(255, 183, 174);
		case 1:
			return Color.rgb(71, 183, 255);
		case 2:
			return Color.rgb(222, 151, 81);
		case 3:
			return Color.rgb(33, 33, 255);
		case 4:
			return Color.rgb(255, 183, 255);
		case 5:
			return Color.rgb(255, 183, 174);
		default:
			return Color.WHITE;
		}
	}

	/**
	 * Note: maze numbers are 1-based, maze index as stored here is 0-based.
	 * 
	 * @param mazeIndex
	 * @return
	 */
	@Override
	public Color getMazeWallBorderColor(int mazeIndex) {
		switch (mazeIndex) {
		case 0:
			return Color.rgb(255, 0, 0);
		case 1:
			return Color.rgb(222, 222, 255);
		case 2:
			return Color.rgb(222, 222, 255);
		case 3:
			return Color.rgb(255, 183, 81);
		case 4:
			return Color.rgb(255, 255, 0);
		case 5:
			return Color.rgb(255, 0, 0);
		default:
			return Color.WHITE;
		}
	}

	@Override
	public void drawMaze(GraphicsContext g, int mazeNumber, int x, int y, boolean flashing) {
		int index = mazeNumber - 1;
		if (flashing) {
			g.drawImage(mazeFlashing(mazeNumber).animate(), x, y);
		} else {
			Rectangle2D fullMazeRegion = new Rectangle2D(0, 248 * index, 226, 248);
			g.drawImage(spritesheet, fullMazeRegion.getMinX(), fullMazeRegion.getMinY(), fullMazeRegion.getWidth(),
					fullMazeRegion.getHeight(), x, y, fullMazeRegion.getWidth(), fullMazeRegion.getHeight());
		}
	}

	@Override
	public void drawLivesCounter(GraphicsContext g, GameModel game, int x, int y) {
		int maxLivesDisplayed = 5;
		int livesDisplayed = game.started ? game.lives - 1 : game.lives;
		for (int i = 0; i < Math.min(livesDisplayed, maxLivesDisplayed); ++i) {
			g.drawImage(spritesheet, 456 + RASTER, 0, RASTER, RASTER, x + t(2 * i), y, RASTER, RASTER);
		}
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		g.save();
		g.translate(0, bonusJumpAnim.animate());
		drawCreature(g, bonus, bonusSpriteRegion(bonus));
		g.restore();
	}

	public void drawMrPacMan(GraphicsContext g, Pac pacMan) {
		if (pacMan.visible) {
			Animation<Rectangle2D> munching = pacManMunching().get(pacMan.dir);
			drawRegion(g, pacMan.speed > 0 ? munching.animate() : munching.frame(1), pacMan.position.x - 4,
					pacMan.position.y - 4);
		}
	}

	public void drawBirdAnim(GraphicsContext g, double x, double y) {
		birdAnim.animate();
		drawRegion(g, birdAnim.frame(), x + 4 - birdAnim.frame().getWidth() / 2, y + 4 - birdAnim.frame().getHeight() / 2);
	}

	public void drawBlueBag(GraphicsContext g, double x, double y) {
		drawRegion(g, new Rectangle2D(488, 199, 8, 8), x, y);
	}

	public void drawJunior(GraphicsContext g, double x, double y) {
		drawRegion(g, new Rectangle2D(509, 200, 8, 8), x, y);
	}

	@Override
	public Animation<Rectangle2D> pacMunchingToDir(Pac pac, Direction dir) {
		return msPacMunchingAnim.get(ensureDirection(dir));
	}
}