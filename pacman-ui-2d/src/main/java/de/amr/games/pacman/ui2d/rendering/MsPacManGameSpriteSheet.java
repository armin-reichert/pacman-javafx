/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Armin Reichert
 */
public class MsPacManGameSpriteSheet implements GameSpriteSheet {

    private static final List<Direction> ORDER = List.of(Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

    private static final int MAZE_IMAGE_WIDTH = 226;
    private static final int MAZE_IMAGE_HEIGHT = 248;

    private static final int SECOND_COLUMN = 228;
    private static final int THIRD_COLUMN = 456;

    private final Image source;
    private final Image flashingMazesImage;

    public MsPacManGameSpriteSheet() {
        ResourceManager rm = this::getClass;
        this.source = rm.loadImage("/de/amr/games/pacman/ui2d/graphics/mspacman/mspacman_spritesheet.png");
        this.flashingMazesImage = rm.loadImage("/de/amr/games/pacman/ui2d/graphics/mspacman/mazes_flashing.png");
    }

    @Override
    public Image source() {
        return source;
    }

    @Override
    public int raster() {
        return 16;
    }

    // third column contains the sprites (first two columns the maze images)
    private Rectangle2D sprite(int tileX, int tileY) {
        return new Rectangle2D(THIRD_COLUMN + r(tileX), r(tileY), raster(), raster());
    }

    private final Rectangle2D[] ghostNumberSprites = array(
        sprite(0, 8), sprite(1, 8), sprite(2, 8), sprite(3, 8));

    public Rectangle2D[] ghostNumberSprites() {
        return ghostNumberSprites;
    }

    private final Rectangle2D[] bonusSymbolSprites = IntStream.range(0, 8)
        .mapToObj(symbol -> sprite(3 + symbol, 0))
        .toArray(Rectangle2D[]::new);

    @Override
    public Rectangle2D bonusSymbolSprite(byte symbol) {
        return bonusSymbolSprites[symbol];
    }

    private final Rectangle2D[] bonusValueSprites = IntStream.range(0, 8)
        .mapToObj(symbol -> sprite(3 + symbol, 1))
        .toArray(Rectangle2D[]::new);

    @Override
    public Rectangle2D bonusValueSprite(byte symbol) {
        return bonusValueSprites[symbol];
    }

    private final Rectangle2D livesCounterSprite = sprite(1, 0);

    @Override
    public Rectangle2D livesCounterSprite() {
        return livesCounterSprite;
    }

    private final Rectangle2D[][] msPacManMunchingSprites = new Rectangle2D[4][];

    {
        for (byte d = 0; d < 4; ++d) {
            var wide = sprite(0, d);
            var open = sprite(1, d);
            var closed = sprite(2, d);
            msPacManMunchingSprites[d] = array(open, open, wide, wide, open, open, open, closed, closed);
        }
    }

    public Rectangle2D[] msPacManMunchingSprites(Direction dir) {
        return msPacManMunchingSprites[ORDER.indexOf(dir)];
    }

    private final Rectangle2D[] msPacManDyingSprites;

    {
        var right = sprite(1, 0);
        var left = sprite(1, 1);
        var up = sprite(1, 2);
        var down = sprite(1, 3);
        // TODO: this is not yet 100% correct
        msPacManDyingSprites = array(down, left, up, right, down, left, up, right, down, left, up);
    }

    public Rectangle2D[] msPacManDyingSprites() {
        return msPacManDyingSprites;
    }

    private final Rectangle2D[][][] ghostsNormalSprites = new Rectangle2D[4][4][];

    {
        for (byte id = 0; id < 4; ++id) {
            for (byte d = 0; d < 4; ++d) {
                ghostsNormalSprites[id][d] = array(sprite(2 * d, 4 + id), sprite(2 * d + 1, 4 + id));
            }
        }
    }

    public Rectangle2D[] ghostNormalSprites(byte id, Direction dir) {
        return ghostsNormalSprites[id][ORDER.indexOf(dir)];
    }

    @Override
    public Rectangle2D ghostFacingRight(byte id) {
        return ghostNormalSprites(id, Direction.RIGHT)[0];
    }

    private final Rectangle2D[] ghostFrightenedSprites = array(sprite(8, 4), sprite(9, 4));

    public Rectangle2D[] ghostFrightenedSprites() {
        return ghostFrightenedSprites;
    }

    private final Rectangle2D[] ghostFlashingSprites = array(
        sprite(8, 4), sprite(9, 4), sprite(10, 4), sprite(11, 4));

    public Rectangle2D[] ghostFlashingSprites() {
        return ghostFlashingSprites;
    }

    private final Rectangle2D[][] ghostEyesSprites = new Rectangle2D[4][];

    {
        for (byte d = 0; d < 4; ++d) {
            ghostEyesSprites[d] = array(sprite(8 + d, 5));
        }
    }

    public Rectangle2D[] ghostEyesSprites(Direction dir) {
        return ghostEyesSprites[ORDER.indexOf(dir)];
    }

    @Override
    public Image getFlashingMazeImage() {
        return null;
    }

    @Override
    public Rectangle2D getEmptyMazeSprite() {
        return null;
    }

    @Override
    public Rectangle2D getFullMazeSprite() {
        return null;
    }

    // Ms. Pac-Man specific:

    private static final int MS_PACMAN_MAZE_COUNT = 6;

    @Override
    public Image getFlashingMazesImage() {
        return flashingMazesImage;
    }

    private final Rectangle2D[] highlightedMazeSprites = new Rectangle2D[MS_PACMAN_MAZE_COUNT];

    {
        for (byte mazeNumber = 1; mazeNumber <= 6; ++mazeNumber) {
            highlightedMazeSprites[mazeNumber - 1] = new Rectangle2D(
                0, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT,
                MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
        }
    }

    @Override
    public Rectangle2D highlightedMaze(int mazeNumber) {
        return highlightedMazeSprites[mazeNumber - 1];
    }

    private final Rectangle2D[] emptyMazeSprites = new Rectangle2D[MS_PACMAN_MAZE_COUNT];

    {
        for (byte mazeNumber = 1; mazeNumber <= 6; ++mazeNumber) {
            emptyMazeSprites[mazeNumber - 1] = rect(SECOND_COLUMN, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT,
                MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
        }
    }

    @Override
    public Rectangle2D emptyMaze(int mazeNumber) {
        return emptyMazeSprites[mazeNumber - 1];
    }

    private final Rectangle2D[] filledMazeSprites = new Rectangle2D[MS_PACMAN_MAZE_COUNT];

    {
        for (byte mazeNumber = 1; mazeNumber <= 6; ++mazeNumber) {
            filledMazeSprites[mazeNumber - 1] = rect(0, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT,
                MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
        }
    }

    @Override
    public Rectangle2D filledMaze(int mazeNumber) {
        return filledMazeSprites[mazeNumber - 1];
    }

    @Override
    public Rectangle2D getEnergizerSprite() {
        return null; // not needed in Ms. Pac-Man
    }

    private final Rectangle2D[][] pacManMunchingSprites = new Rectangle2D[4][];

    {
        for (byte d = 0; d < 4; ++d) {
            pacManMunchingSprites[d] = array(sprite(0, 9 + d), sprite(1, 9 + d), sprite(2, 9));
        }
    }

    public Rectangle2D[] pacManMunchingSprites(Direction dir) {
        return pacManMunchingSprites[ORDER.indexOf(dir)];
    }

    private final Rectangle2D heartSprite = sprite(2, 10);

    public Rectangle2D heartSprite() {
        return heartSprite;
    }

    private final Rectangle2D blueBagSprite = rect(488, 199, 8, 8);

    public Rectangle2D blueBagSprite() {
        return blueBagSprite;
    }

    private final Rectangle2D juniorPacSprite = rect(509, 200, 8, 8);

    public Rectangle2D juniorPacSprite() {
        return juniorPacSprite;
    }

    private final Rectangle2D[] clapperboardSprites = array(
        rect(456, 208, 32, 32),  // open
        rect(488, 208, 32, 32),  // middle
        rect(520, 208, 32, 32)); // closed

    @Override
    public Rectangle2D[] clapperboardSprites() {
        return clapperboardSprites;
    }

    public SpriteAnimation createStorkFlyingAnimation() {
        return SpriteAnimation.begin()
            .sprites(rect(489, 176, 32, 16), rect(521, 176, 32, 16))
            .frameTicks(8)
            .loop()
            .end();
    }
}