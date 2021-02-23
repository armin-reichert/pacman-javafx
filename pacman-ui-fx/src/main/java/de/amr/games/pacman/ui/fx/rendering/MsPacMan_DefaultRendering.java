package de.amr.games.pacman.ui.fx.rendering;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.pacman.Bonus;
import de.amr.games.pacman.ui.fx.common.Helper;
import de.amr.games.pacman.ui.fx.mspacman.entities.Flap;
import de.amr.games.pacman.ui.fx.mspacman.entities.Heart;
import de.amr.games.pacman.ui.fx.mspacman.entities.JuniorBag;
import de.amr.games.pacman.ui.fx.mspacman.entities.Stork;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Default rendering for the the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacMan_DefaultRendering extends DefaultRendering {

	private final Map<Direction, Animation<Rectangle2D>> msPacManMunchingAnim;
	private final Animation<Integer> bonusJumpingAnim;

	/* Tiles in right half of spritesheet */
	public Rectangle2D s(int tileX, int tileY) {
		return spriteRegionAt(456, 0, tileX, tileY, 1, 1);
	}

	public MsPacMan_DefaultRendering() {

		super("/mspacman/graphics/sprites.png");

		symbolSprites = Arrays.asList(s(3, 0), s(4, 0), s(5, 0), s(6, 0), s(7, 0), s(8, 0), s(9, 0));

		//@formatter:off
		bonusValueSprites = new HashMap<>();
		bonusValueSprites.put(100,  s(3, 1));
		bonusValueSprites.put(200,  s(4, 1));
		bonusValueSprites.put(500,  s(5, 1));
		bonusValueSprites.put(700,  s(6, 1));
		bonusValueSprites.put(1000, s(7, 1));
		bonusValueSprites.put(2000, s(8, 1));
		bonusValueSprites.put(5000, s(9, 1));
		
		bountyValueSprites = new HashMap<>();
		bountyValueSprites.put(200,  s(0, 8));
		bountyValueSprites.put(400,  s(1, 8));
		bountyValueSprites.put(800,  s(2, 8));
		bountyValueSprites.put(1600, s(3, 8));
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

		msPacManMunchingAnim = new EnumMap<>(Direction.class);
		for (Direction dir : Direction.values()) {
			int d = index(dir);
			Animation<Rectangle2D> munching = Animation.of(s(1, d), s(1, d), s(2, d), s(0, d));
			munching.frameDuration(2).endless();
			msPacManMunchingAnim.put(dir, munching);
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
		Direction.stream().forEach(dir -> ghostEyesAnim.put(dir, Animation.of(s(8 + index(dir), 5))));

		ghostBlueAnim = Animation.of(s(8, 4), s(9, 4)).frameDuration(20).endless().run();
		ghostFlashingAnim = Animation.of(s(8, 4), s(9, 4), s(10, 4), s(11, 4)).frameDuration(5).endless();

		bonusJumpingAnim = Animation.of(0, 2, 0, -2).frameDuration(20).endless().run();
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
	public Animation<Rectangle2D> playerMunching(Pac pac, Direction dir) {
		return msPacManMunchingAnim.get(ensureDirection(dir));
	}

	@Override
	public Animation<Rectangle2D> spouseMunching(Pac spouse, Direction dir) {
		return pacManMunchingAnim.get(dir);
	}

	@Override
	public Animation<?> storkFlying() {
		return Animation.of(//
				new Rectangle2D(489, 176, 32, 16), //
				new Rectangle2D(521, 176, 32, 16)//
		).endless().frameDuration(10);
	}

	@Override
	public Animation<?> bigPacMan() {
		return null;
	}

	@Override
	public Animation<?> blinkyDamaged() {
		return null;
	}

	@Override
	public Animation<?> blinkyNaked() {
		return null;
	}

	@Override
	public void drawMaze(GraphicsContext g, int mazeNumber, int x, int y, boolean flashing) {
		int index = mazeNumber - 1;
		if (flashing) {
			g.drawImage((Image) mazeFlashing(mazeNumber).animate(), x, y);
		} else {
			Rectangle2D fullMazeRegion = new Rectangle2D(0, 248 * index, 226, 248);
			g.drawImage(spritesheet, fullMazeRegion.getMinX(), fullMazeRegion.getMinY(), fullMazeRegion.getWidth(),
					fullMazeRegion.getHeight(), x, y, fullMazeRegion.getWidth(), fullMazeRegion.getHeight());
		}
	}

	@Override
	public void drawLifeCounterSymbol(GraphicsContext g, int x, int y) {
		g.drawImage(spritesheet, 456 + RASTER, 0, RASTER, RASTER, x, y, RASTER, RASTER);
	}

	@Override
	public void drawBonus(GraphicsContext g, Bonus bonus) {
		g.save();
		g.translate(0, bonusJumpingAnim.animate());
		drawEntity(g, bonus, bonusSprite(bonus));
		g.restore();
	}

	@Override
	public void drawSpouse(GraphicsContext g, Pac pacMan) {
		if (pacMan.visible) {
			Animation<Rectangle2D> munching = spouseMunching(pacMan, pacMan.dir);
			drawSprite(g, pacMan.speed > 0 ? munching.animate() : munching.frame(1), pacMan.position.x - 4,
					pacMan.position.y - 4);
		}
	}

	@Override
	public void drawFlap(GraphicsContext g, Flap flap) {
		if (flap.visible) {
			drawSprite(g, flap.flapping.animate(), flap.position.x, flap.position.y);
			g.setFont(Font.font(getScoreFont().getName(), FontWeight.THIN, 8));
			g.setFill(Color.rgb(222, 222, 225, 0.75));
			g.fillText(flap.sceneNumber + "", flap.position.x + 20, flap.position.y + 30);
			g.setFont(getScoreFont());
			g.fillText(flap.sceneTitle, flap.position.x + 40, flap.position.y + 20);
		}
	}

	@Override
	public void drawStork(GraphicsContext g, Stork stork) {
		if (stork.visible) {
			drawSprite(g, stork.flying.animate(), stork.position.x, stork.position.y);
		}
	}

	@Override
	public void drawJuniorBag(GraphicsContext g, JuniorBag bag) {
		if (bag.visible) {
			if (bag.open) {
				drawEntity(g, bag, new Rectangle2D(509, 200, 8, 8));
			} else {
				drawEntity(g, bag, new Rectangle2D(488, 199, 8, 8));
			}
		}
	}

	@Override
	public void drawHeart(GraphicsContext g, Heart heart) {
		drawEntity(g, heart, s(2, 10));
	}
}