/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.SpriteArea;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import javafx.scene.image.Image;

import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Armin Reichert
 */
public class PacManGameSpriteSheet implements GameSpriteSheet {

    private static final List<Direction> ORDER = List.of(Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);
    private static final short OFF_X = 456;

    private final Image source;
    private final Image flashingMazeImage;

    public PacManGameSpriteSheet(String resourcePath) {
        ResourceManager rm = this::getClass;
        source = rm.loadImage(resourcePath + "pacman_spritesheet.png");
        flashingMazeImage = rm.loadImage(resourcePath + "maze_flashing.png");
    }

    @Override
    public Image getFlashingMazesImage() {
        return null;
    }

    @Override
    public SpriteArea emptyMaze(int mapNumber) {
        return null;
    }

    @Override
    public SpriteArea filledMaze(int mapNumber) {
        return null;
    }

    @Override
    public SpriteArea[] clapperboardSprites() {
        return new SpriteArea[0];
    }

    @Override
    public Image source() {
        return source;
    }

    @Override
    public int raster() {
        return 16;
    }

    @Override
    public Image getFlashingMazeImage() {
        return flashingMazeImage;
    }

    private final SpriteArea fullMazeSprite = rect(0, 0, 224, 248);

    @Override
    public SpriteArea getFullMazeSprite() {
        return fullMazeSprite;
    }

    private final SpriteArea emptyMazeSprite = rect(228, 0, 224, 248);

    @Override
    public SpriteArea getEmptyMazeSprite() {
        return emptyMazeSprite;
    }

    @Override
    public SpriteArea highlightedMaze(int mapNumber) {
        return null;
    }

    private final SpriteArea energizerSprite = rect(8, 24, 8, 8);

    public SpriteArea getEnergizerSprite() {
        return energizerSprite;
    }

    private final SpriteArea[] ghostNumberSprites = array(
        rect(456, 133, 15, 7),  // 200
        rect(472, 133, 15, 7),  // 400
        rect(488, 133, 15, 7),  // 800
        rect(504, 133, 16, 7)); // 1600

    public SpriteArea[] ghostNumberSprites() {
        return ghostNumberSprites;
    }

    private final SpriteArea[] bonusSymbolSprites = IntStream.range(0, 8)
        .mapToObj(symbolPosition -> rect(OFF_X + r(2 + symbolPosition), r(3) /*+ 0.5*/, r(1), r(1) /*- 0.5*/))
        .toArray(SpriteArea[]::new);

    @Override
    public SpriteArea bonusSymbolSprite(byte symbol) {
        return bonusSymbolSprites[symbol];
    }

    private final SpriteArea[] bonusValueSprites = new SpriteArea[8];
    {
        for (byte symbol = 0; symbol < 8; ++symbol) {
            bonusValueSprites[symbol] = switch (symbol) {
                case 0 -> rect(457, 148, 14, 7); //  100
                case 1 -> rect(472, 148, 15, 7); //  300
                case 2 -> rect(488, 148, 15, 7); //  500
                case 3 -> rect(504, 148, 15, 7); //  700
                case 4 -> rect(520, 148, 18, 7); // 1000
                case 5 -> rect(518, 164, 20, 7); // 2000
                case 6 -> rect(518, 180, 20, 7); // 3000
                case 7 -> rect(518, 196, 20, 7); // 5000
                default -> null;
            };
        }
    }

    @Override
    public SpriteArea bonusValueSprite(byte symbol) {
        return bonusValueSprites[symbol];
    }

    private final SpriteArea[] ghostFacingRightSprites = new SpriteArea[4];
    {
        for (byte id = 0; id < 4; ++id) {
            int rx = 2 * ORDER.indexOf(Direction.RIGHT);
            int ry = 4 + id;
            ghostFacingRightSprites[id] = rect(OFF_X + r(rx), r(ry), raster(), raster());
        }
    }

    @Override
    public SpriteArea ghostFacingRight(byte ghostID) {
        return ghostFacingRightSprites[ghostID];
    }

    private final SpriteArea livesCounterSprite = rect(OFF_X + 129, 15, 16, 16);

    @Override
    public SpriteArea livesCounterSprite() {
        return livesCounterSprite;
    }

    private final SpriteArea[][] pacMunchingSprites = new SpriteArea[4][];
    {
        short margin = 1;
        int size = 16 - 2 * margin;
        for (byte d = 0; d < 4; ++d) {
            var wide   = rect(OFF_X      + margin, d * 16 + margin, size, size);
            var middle = rect(OFF_X + 16 + margin, d * 16 + margin, size, size);
            var closed = rect(OFF_X + 32 + margin,          margin, size, size);
            pacMunchingSprites[d] = array(closed, closed, middle, middle, wide, wide, middle, middle);
        }
    }

    public SpriteArea[] pacMunchingSprites(Direction dir) {
        return pacMunchingSprites[ORDER.indexOf(dir)];
    }

    private final SpriteArea[] pacDyingSprites = new SpriteArea[11];
    {
        for (int i = 0; i < pacDyingSprites.length; ++i) {
            boolean last = i == pacDyingSprites.length - 1;
            pacDyingSprites[i] = rect(504 + i * 16, 0, 15, last?16:15);
        }
    }

    public SpriteArea[] pacDyingSprites() {
        return pacDyingSprites;
    }

    private final SpriteArea[][][] ghostNormalSprites = new SpriteArea[4][4][];
    {
        for (byte id = 0; id < 4; ++id) {
            for (byte d = 0; d < 4; ++d) {
                ghostNormalSprites[id][d] = tilesRightOf(OFF_X, 2 * d, 4 + id, 2);
            }
        }
    }

    public SpriteArea[] ghostNormalSprites(byte id, Direction dir) {
        return ghostNormalSprites[id][ORDER.indexOf(dir)];
    }

    private final SpriteArea[] ghostFrightenedSprites = array(
        rect(OFF_X + r(8), r(4), r(1), r(1)),
        rect(OFF_X + r(9), r(4), r(1), r(1)));

    public SpriteArea[] ghostFrightenedSprites() {
        return ghostFrightenedSprites;
    }

    private final SpriteArea[] ghostFlashingSprites = tilesRightOf(OFF_X, 8, 4, 4);

    public SpriteArea[] ghostFlashingSprites() {
        return ghostFlashingSprites;
    }

    private final SpriteArea[][] ghostEyesSprites = new SpriteArea[4][];
    {
        for (byte d = 0; d < 4; ++d) {
            ghostEyesSprites[d] = array(rect(OFF_X + r(8 + d), r(5), r(1), r(1)));
        }
    }

    public SpriteArea[] ghostEyesSprites(Direction dir) {
        return ghostEyesSprites[ORDER.indexOf(dir)];
    }

    // Pac-Man specific:

    private final SpriteArea[] bigPacManSprites = array(
        rect(OFF_X + 32, 16, 32, 32),
        rect(OFF_X + 64, 16, 32, 32),
        rect(OFF_X + 96, 16, 32, 32));

    public SpriteArea[] bigPacManSprites() {
        return bigPacManSprites;
    }

    private final SpriteArea[] blinkyStretchedSprites = new SpriteArea[5];
    {
        int size = raster();
        for (int i = 0; i < 5; ++i) {
            blinkyStretchedSprites[i] = rect(OFF_X + r(8 + i), r(6), size, size);
        }
    }

    public SpriteArea[] blinkyStretchedSprites() {
        return blinkyStretchedSprites;
    }

    private final SpriteArea[] blinkyDamagedSprites = new SpriteArea[2];
    {
        int m = 1;
        int size = raster() - 2 * m;
        blinkyDamagedSprites[0] = rect(OFF_X + r(8) + m, r(7) + m, size, size);
        blinkyDamagedSprites[1] = rect(OFF_X + r(9) + m, r(7) + m, size, size);
    }

    public SpriteArea[] blinkyDamagedSprites() {
        return blinkyDamagedSprites;
    }

    private final SpriteArea[] blinkyPatchedSprites = new SpriteArea[2];
    {
        int size = raster();
        blinkyPatchedSprites[0] = rect(OFF_X + r(10), r(7), size, size);
        blinkyPatchedSprites[1] = rect(OFF_X + r(11), r(7), size, size);
    }

    public SpriteArea[] blinkyPatchedSprites() {
        return blinkyPatchedSprites;
    }

    private final SpriteArea[] blinkyNakedSprites = new SpriteArea[2];
    {
        int size = raster();
        blinkyNakedSprites[0] = rect(OFF_X + r(8), r(8), 2 * size, size);
        blinkyNakedSprites[1] = rect(OFF_X + r(10), r(8), 2 * size, size);
    }

    public SpriteArea[] blinkyNakedSprites() {
        return blinkyNakedSprites;
    }
}