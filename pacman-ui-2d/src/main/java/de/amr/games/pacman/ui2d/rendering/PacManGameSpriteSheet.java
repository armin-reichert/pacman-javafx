/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;

import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Armin Reichert
 */
public class PacManGameSpriteSheet implements GameSpriteSheet {

    private static final List<Direction> ORDER = List.of(Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);
    private static final int OFF_X = 456;

    private final Image source;
    private final Image flashingMazeImage;

    public PacManGameSpriteSheet() {
        ResourceManager rm = this::getClass;
        this.source = rm.loadImage("/de/amr/games/pacman/ui2d/graphics/pacman/pacman_spritesheet.png");
        this.flashingMazeImage = rm.loadImage("/de/amr/games/pacman/ui2d/graphics/pacman/maze_flashing.png");
    }

    @Override
    public Image getFlashingMazesImage() {
        return null;
    }

    @Override
    public Rectangle2D emptyMaze(int mapNumber) {
        return null;
    }

    @Override
    public Rectangle2D filledMaze(int mapNumber) {
        return null;
    }

    @Override
    public Rectangle2D[] clapperboardSprites() {
        return new Rectangle2D[0];
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

    private final Rectangle2D fullMazeSprite = rect(0, 0, 224, 248);

    @Override
    public Rectangle2D getFullMazeSprite() {
        return fullMazeSprite;
    }

    private final Rectangle2D emptyMazeSprite = rect(228, 0, 224, 248);

    @Override
    public Rectangle2D getEmptyMazeSprite() {
        return emptyMazeSprite;
    }

    @Override
    public Rectangle2D highlightedMaze(int mapNumber) {
        return null;
    }

    private final Rectangle2D energizerSprite = rect(8, 24, 8, 8);

    public Rectangle2D getEnergizerSprite() {
        return energizerSprite;
    }

    private final Rectangle2D[] ghostNumberSprites = array(
        rect(456, 133, 15, 7),  // 200
        rect(472, 133, 15, 7),  // 400
        rect(488, 133, 15, 7),  // 800
        rect(504, 133, 16, 7)); // 1600

    public Rectangle2D[] ghostNumberSprites() {
        return ghostNumberSprites;
    }

    private final Rectangle2D[] bonusSymbolSprites = IntStream.range(0, 8)
        .mapToObj(sym -> rect(OFF_X + r(2 + sym), r(3) + 0.5, r(1), r(1) - 0.5))
        .toArray(Rectangle2D[]::new);

    @Override
    public Rectangle2D bonusSymbolSprite(byte symbol) {
        return bonusSymbolSprites[symbol];
    }

    private final Rectangle2D[] bonusValueSprites = new Rectangle2D[8];
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
    public Rectangle2D bonusValueSprite(byte symbol) {
        return bonusValueSprites[symbol];
    }

    private final Rectangle2D[] ghostFacingRightSprites = new Rectangle2D[4];
    {
        for (byte id = 0; id < 4; ++id) {
            int rx = 2 * ORDER.indexOf(Direction.RIGHT);
            int ry = 4 + id;
            ghostFacingRightSprites[id] = rect(OFF_X + r(rx), r(ry), raster(), raster());
        }
    }

    @Override
    public Rectangle2D ghostFacingRight(byte ghostID) {
        return ghostFacingRightSprites[ghostID];
    }

    private final Rectangle2D livesCounterSprite = rect(OFF_X + 129, 15, 16, 16);

    @Override
    public Rectangle2D livesCounterSprite() {
        return livesCounterSprite;
    }

    private final Rectangle2D[][] pacMunchingSprites = new Rectangle2D[4][];
    {
        double m = 1; // margin
        double size = 16 - 2 * m;
        for (byte d = 0; d < 4; ++d) {
            var wide   = rect(OFF_X +  0 + m, d * 16 + m, size, size);
            var middle = rect(OFF_X + 16 + m, d * 16 + m, size, size);
            var closed = rect(OFF_X + 32 + m, 0      + m, size, size);
            pacMunchingSprites[d] = array(closed, closed, middle, middle, wide, wide, middle, middle);
        }
    }

    public Rectangle2D[] pacMunchingSprites(Direction dir) {
        return pacMunchingSprites[ORDER.indexOf(dir)];
    }

    private final Rectangle2D[] pacDyingSprites = new Rectangle2D[11];
    {
        for (int i = 0; i < pacDyingSprites.length; ++i) {
            boolean last = i == pacDyingSprites.length - 1;
            pacDyingSprites[i] = rect(504 + i * 16, 0, 15, last?16:15);
        }
    }

    public Rectangle2D[] pacDyingSprites() {
        return pacDyingSprites;
    }

    private final Rectangle2D[][][] ghostNormalSprites = new Rectangle2D[4][4][];
    {
        for (byte id = 0; id < 4; ++id) {
            for (byte d = 0; d < 4; ++d) {
                ghostNormalSprites[id][d] = tilesRightOf(OFF_X, 2 * d, 4 + id, 2);
            }
        }
    }

    public Rectangle2D[] ghostNormalSprites(byte id, Direction dir) {
        return ghostNormalSprites[id][ORDER.indexOf(dir)];
    }

    private final Rectangle2D[] ghostFrightenedSprites = array(
        rect(OFF_X + r(8), r(4), r(1), r(1)),
        rect(OFF_X + r(9), r(4), r(1), r(1)));

    public Rectangle2D[] ghostFrightenedSprites() {
        return ghostFrightenedSprites;
    }

    private final Rectangle2D[] ghostFlashingSprites = tilesRightOf(OFF_X, 8, 4, 4);

    public Rectangle2D[] ghostFlashingSprites() {
        return ghostFlashingSprites;
    }

    private final Rectangle2D[][] ghostEyesSprites = new Rectangle2D[4][];
    {
        for (byte d = 0; d < 4; ++d) {
            ghostEyesSprites[d] = array(rect(OFF_X + r(8 + d), r(5), r(1), r(1)));
        }
    }

    public Rectangle2D[] ghostEyesSprites(Direction dir) {
        return ghostEyesSprites[ORDER.indexOf(dir)];
    }

    // Pac-Man specific:

    private final Rectangle2D[] bigPacManSprites = array(
        rect(OFF_X + 32, 16, 32, 32),
        rect(OFF_X + 64, 16, 32, 32),
        rect(OFF_X + 96, 16, 32, 32));

    public Rectangle2D[] bigPacManSprites() {
        return bigPacManSprites;
    }

    private final Rectangle2D[] blinkyDamagedSprites = new Rectangle2D[2];
    private final Rectangle2D[] blinkyStretchedSprites = new Rectangle2D[5];
    private final Rectangle2D[] blinkyPatchedSprites = new Rectangle2D[2];
    private final Rectangle2D[] blinkyNakedSprites = new Rectangle2D[2];

    {
        double m = 0; // margin
        double size = raster() - 2 * m;
        for (int i = 0; i < 5; ++i) {
            blinkyStretchedSprites[i] = rect(OFF_X + r(8 + i) + m, r(6) + m, size, size);
        }

        m = 1;
        size = raster() - 2 * m;
        blinkyDamagedSprites[0] = rect(OFF_X + r(8) + m, r(7) + m, size, size);
        blinkyDamagedSprites[1] = rect(OFF_X + r(9) + m, r(7) + m, size, size);

        size = raster() - 2 * m;
        blinkyPatchedSprites[0] = rect(OFF_X + r(10) + m, r(7) + m, size, size);
        blinkyPatchedSprites[1] = rect(OFF_X + r(11) + m, r(7) + m, size, size);

        m = 0;
        size = raster() - 2 * m;
        blinkyNakedSprites[0] = rect(OFF_X + r(8)  + m, r(8) + m, 2*size, size);
        blinkyNakedSprites[1] = rect(OFF_X + r(10) + m, r(8) + m, 2*size, size);
}

    public Rectangle2D[] blinkyDamagedSprites() {
        return blinkyDamagedSprites;
    }

    public Rectangle2D[] blinkyStretchedSprites() {
        return blinkyStretchedSprites;
    }

    public Rectangle2D[] blinkyPatchedSprites() {
        return blinkyPatchedSprites;
    }

    public Rectangle2D[] blinkyNakedSprites() {
        return blinkyNakedSprites;
    }
}