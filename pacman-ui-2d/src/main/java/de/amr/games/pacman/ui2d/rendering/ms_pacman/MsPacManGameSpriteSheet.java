/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.SpriteArea;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
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

    public MsPacManGameSpriteSheet(String resourcePath) {
        ResourceManager rm = this::getClass;
        source = rm.loadImage(resourcePath + "mspacman_spritesheet.png");
        flashingMazesImage = rm.loadImage(resourcePath + "mazes_flashing.png");
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
    private SpriteArea sprite(int tileX, int tileY) {
        return new SpriteArea(THIRD_COLUMN + r(tileX), r(tileY), raster(), raster());
    }

    private final SpriteArea[] ghostNumberSprites = array(
        sprite(0, 8), sprite(1, 8), sprite(2, 8), sprite(3, 8));

    public SpriteArea[] ghostNumberSprites() {
        return ghostNumberSprites;
    }

    private final SpriteArea[] bonusSymbolSprites = IntStream.range(0, 8)
        .mapToObj(symbol -> sprite(3 + symbol, 0))
        .toArray(SpriteArea[]::new);

    @Override
    public SpriteArea bonusSymbolSprite(byte symbol) {
        return bonusSymbolSprites[symbol];
    }

    private final SpriteArea[] bonusValueSprites = IntStream.range(0, 8)
        .mapToObj(symbol -> sprite(3 + symbol, 1))
        .toArray(SpriteArea[]::new);

    @Override
    public SpriteArea bonusValueSprite(byte symbol) {
        return bonusValueSprites[symbol];
    }

    private final SpriteArea livesCounterSprite = sprite(1, 0);

    @Override
    public SpriteArea livesCounterSprite() {
        return livesCounterSprite;
    }

    private final SpriteArea[][] msPacManMunchingSprites = new SpriteArea[4][];
    {
        for (byte d = 0; d < 4; ++d) {
            var wide = sprite(0, d);
            var open = sprite(1, d);
            var closed = sprite(2, d);
            msPacManMunchingSprites[d] = array(open, open, wide, wide, open, open, open, closed, closed);
        }
    }

    public SpriteArea[] msPacManMunchingSprites(Direction dir) {
        return msPacManMunchingSprites[ORDER.indexOf(dir)];
    }

    private final SpriteArea[] msPacManDyingSprites;
    {
        var right = sprite(1, 0);
        var left = sprite(1, 1);
        var up = sprite(1, 2);
        var down = sprite(1, 3);
        // TODO: this is not yet 100% correct
        msPacManDyingSprites = array(down, left, up, right, down, left, up, right, down, left, up);
    }

    public SpriteArea[] msPacManDyingSprites() {
        return msPacManDyingSprites;
    }

    private final SpriteArea[][][] ghostsNormalSprites = new SpriteArea[4][4][];
    {
        for (byte id = 0; id < 4; ++id) {
            for (byte d = 0; d < 4; ++d) {
                ghostsNormalSprites[id][d] = array(sprite(2 * d, 4 + id), sprite(2 * d + 1, 4 + id));
            }
        }
    }

    public SpriteArea[] ghostNormalSprites(byte id, Direction dir) {
        return ghostsNormalSprites[id][ORDER.indexOf(dir)];
    }

    @Override
    public SpriteArea ghostFacingRight(byte id) {
        return ghostNormalSprites(id, Direction.RIGHT)[0];
    }

    private final SpriteArea[] ghostFrightenedSprites = array(sprite(8, 4), sprite(9, 4));

    public SpriteArea[] ghostFrightenedSprites() {
        return ghostFrightenedSprites;
    }

    private final SpriteArea[] ghostFlashingSprites = array(
        sprite(8, 4), sprite(9, 4), sprite(10, 4), sprite(11, 4));

    public SpriteArea[] ghostFlashingSprites() {
        return ghostFlashingSprites;
    }

    private final SpriteArea[][] ghostEyesSprites = new SpriteArea[4][];
    {
        for (byte d = 0; d < 4; ++d) {
            ghostEyesSprites[d] = array(sprite(8 + d, 5));
        }
    }

    public SpriteArea[] ghostEyesSprites(Direction dir) {
        return ghostEyesSprites[ORDER.indexOf(dir)];
    }

    // Ms. Pac-Man specific:

    private static final int MS_PACMAN_MAZE_COUNT = 6;

    public Image getFlashingMazesImage() {
        return flashingMazesImage;
    }

    private final SpriteArea[] highlightedMazeSprites = new SpriteArea[MS_PACMAN_MAZE_COUNT];
    {
        for (byte mazeNumber = 1; mazeNumber <= 6; ++mazeNumber) {
            highlightedMazeSprites[mazeNumber - 1] = new SpriteArea(
                0, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT,
                MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
        }
    }

    public SpriteArea highlightedMaze(int mazeNumber) {
        return highlightedMazeSprites[mazeNumber - 1];
    }

    private final SpriteArea[] emptyMazeSprites = new SpriteArea[MS_PACMAN_MAZE_COUNT];
    {
        for (byte mazeNumber = 1; mazeNumber <= 6; ++mazeNumber) {
            emptyMazeSprites[mazeNumber - 1] = rect(SECOND_COLUMN, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT,
                MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
        }
    }

    public SpriteArea emptyMaze(int mazeNumber) {
        return emptyMazeSprites[mazeNumber - 1];
    }

    private final SpriteArea[] filledMazeSprites = new SpriteArea[MS_PACMAN_MAZE_COUNT];
    {
        for (byte mazeNumber = 1; mazeNumber <= 6; ++mazeNumber) {
            filledMazeSprites[mazeNumber - 1] = rect(0, (mazeNumber - 1) * MAZE_IMAGE_HEIGHT,
                MAZE_IMAGE_WIDTH, MAZE_IMAGE_HEIGHT);
        }
    }

    public SpriteArea filledMaze(int mazeNumber) {
        return filledMazeSprites[mazeNumber - 1];
    }

    private final SpriteArea[][] pacManMunchingSprites = new SpriteArea[4][];
    {
        for (byte d = 0; d < 4; ++d) {
            pacManMunchingSprites[d] = array(sprite(0, 9 + d), sprite(1, 9 + d), sprite(2, 9));
        }
    }

    public SpriteArea[] pacManMunchingSprites(Direction dir) {
        return pacManMunchingSprites[ORDER.indexOf(dir)];
    }

    private final SpriteArea heartSprite = sprite(2, 10);

    public SpriteArea heartSprite() {
        return heartSprite;
    }

    private final SpriteArea blueBagSprite = rect(488, 199, 8, 8);

    public SpriteArea blueBagSprite() {
        return blueBagSprite;
    }

    private final SpriteArea juniorPacSprite = rect(509, 200, 8, 8);

    public SpriteArea juniorPacSprite() {
        return juniorPacSprite;
    }

    private final SpriteArea[] clapperboardSprites = array(
        rect(456, 208, 32, 32),  // open
        rect(488, 208, 32, 32),  // middle
        rect(520, 208, 32, 32)); // closed

    public SpriteArea[] clapperboardSprites() {
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