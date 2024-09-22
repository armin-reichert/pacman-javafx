/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.rendering.RectangularArea;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteSheet;
import javafx.scene.image.Image;

import java.util.List;
import java.util.stream.IntStream;

import static de.amr.games.pacman.ui2d.rendering.RectangularArea.rect;

/**
 * @author Armin Reichert
 */
public class MsPacManGameSpriteSheet implements SpriteSheet {

    private static final byte RASTER_SIZE = 16;
    private static final List<Direction> ORDER = List.of(Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

    private final Image source;

    private final RectangularArea[] ghostNumberSprites = rectArray(
            spriteRegion(0, 8), spriteRegion(1, 8), spriteRegion(2, 8), spriteRegion(3, 8));

    private final RectangularArea[] bonusSymbolSprites = IntStream.range(0, 8)
            .mapToObj(symbol -> spriteRegion(3 + symbol, 0))
            .toArray(RectangularArea[]::new);

    private final RectangularArea[] bonusValueSprites = IntStream.range(0, 8)
            .mapToObj(symbol -> spriteRegion(3 + symbol, 1))
            .toArray(RectangularArea[]::new);

    private final RectangularArea livesCounterSprite = spriteRegion(1, 0);

    private final RectangularArea[][] munchingSprites = new RectangularArea[4][];
    {
        for (byte d = 0; d < 4; ++d) {
            var wide = spriteRegion(0, d);
            var open = spriteRegion(1, d);
            var closed = spriteRegion(2, d);
            munchingSprites[d] = rectArray(open, open, wide, wide, open, open, open, closed, closed);
        }
    }

    private final RectangularArea[] msPacManDyingSprites;
    {
        var right = spriteRegion(1, 0);
        var left = spriteRegion(1, 1);
        var up = spriteRegion(1, 2);
        var down = spriteRegion(1, 3);
        // TODO: this is not yet 100% correct
        msPacManDyingSprites = rectArray(down, left, up, right, down, left, up, right, down, left, up);
    }

    private final RectangularArea[][][] ghostsNormalSprites = new RectangularArea[4][4][];
    {
        for (byte id = 0; id < 4; ++id) {
            for (byte d = 0; d < 4; ++d) {
                ghostsNormalSprites[id][d] = rectArray(spriteRegion(2 * d, 4 + id), spriteRegion(2 * d + 1, 4 + id));
            }
        }
    }

    private final RectangularArea[] ghostFrightenedSprites = rectArray(spriteRegion(8, 4), spriteRegion(9, 4));

    private final RectangularArea[] ghostFlashingSprites = rectArray(
            spriteRegion(8, 4), spriteRegion(9, 4), spriteRegion(10, 4), spriteRegion(11, 4));

    private final RectangularArea[][] ghostEyesSprites = new RectangularArea[4][];
    {
        for (byte d = 0; d < 4; ++d) {
            ghostEyesSprites[d] = rectArray(spriteRegion(8 + d, 5));
        }
    }

    private final RectangularArea[][] pacManMunchingSprites = new RectangularArea[4][];
    {
        for (byte d = 0; d < 4; ++d) {
            pacManMunchingSprites[d] = rectArray(spriteRegion(0, 9 + d), spriteRegion(1, 9 + d), spriteRegion(2, 9));
        }
    }

    private final RectangularArea heartSprite = spriteRegion(2, 10);
    private final RectangularArea blueBagSprite = rect(488, 199, 8, 8);
    private final RectangularArea juniorPacSprite = rect(509, 200, 8, 8);

    private final RectangularArea[] clapperboardSprites = rectArray(
            rect(456, 208, 32, 32),  // open
            rect(488, 208, 32, 32),  // middle
            rect(520, 208, 32, 32)); // closed

    // third "column" contains the sprites (first two columns the maze images)
    private RectangularArea spriteRegion(int tileX, int tileY) {
        return rect(456 + tiles(tileX), tiles(tileY), RASTER_SIZE, RASTER_SIZE);
    }

    public MsPacManGameSpriteSheet(Image source) {
        this.source = source;
    }

    @Override
    public Image sourceImage() {
        return source;
    }

    @Override
    public int tileSize() {
        return RASTER_SIZE;
    }

    @Override
    public RectangularArea[] ghostNumberSprites() {
        return ghostNumberSprites;
    }

    @Override
    public RectangularArea bonusSymbolSprite(byte symbol) {
        return bonusSymbolSprites[symbol];
    }

    @Override
    public RectangularArea bonusValueSprite(byte symbol) {
        return bonusValueSprites[symbol];
    }

    @Override
    public RectangularArea livesCounterSprite() {
        return livesCounterSprite;
    }

    @Override
    public RectangularArea[] msPacManMunchingSprites(Direction dir) {
        return munchingSprites[ORDER.indexOf(dir)];
    }

    @Override
    public RectangularArea[] msPacManDyingSprites() {
        return msPacManDyingSprites;
    }

    @Override
    public RectangularArea[] ghostNormalSprites(byte id, Direction dir) {
        return ghostsNormalSprites[id][ORDER.indexOf(dir)];
    }

    @Override
    public RectangularArea[] ghostFrightenedSprites() {
        return ghostFrightenedSprites;
    }

    @Override
    public RectangularArea[] ghostFlashingSprites() {
        return ghostFlashingSprites;
    }

    @Override
    public RectangularArea[] ghostEyesSprites(Direction dir) {
        return ghostEyesSprites[ORDER.indexOf(dir)];
    }

    @Override
    public RectangularArea[] pacManMunchingSprites(Direction dir) {
        return pacManMunchingSprites[ORDER.indexOf(dir)];
    }

    @Override
    public RectangularArea[] pacMunchingSprites(Direction dir) {
        return new RectangularArea[0]; //TODO check this
    }

    @Override
    public RectangularArea[] pacDyingSprites() {
        return new RectangularArea[0];//TODO check this
    }

    @Override
    public RectangularArea heartSprite() {
        return heartSprite;
    }

    @Override
    public RectangularArea blueBagSprite() {
        return blueBagSprite;
    }

    @Override
    public RectangularArea juniorPacSprite() {
        return juniorPacSprite;
    }

    @Override
    public RectangularArea[] clapperboardSprites() {
        return clapperboardSprites;
    }

    @Override
    public SpriteAnimation createStorkFlyingAnimation() {
        return SpriteAnimation.begin()
            .sprites(rect(489, 176, 32, 16), rect(521, 176, 32, 16))
            .frameTicks(8)
            .loop()
            .end();
    }
}