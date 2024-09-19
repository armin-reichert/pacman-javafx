/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.rendering.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.rendering.RectangularArea;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteSheet;
import javafx.scene.image.Image;

import java.util.List;
import java.util.stream.IntStream;

/**
 * @author Armin Reichert
 */
public class MsPacManGameSpriteSheet implements SpriteSheet {

    private static final List<Direction> ORDER = List.of(Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN);

    private final Image source;
    private final Image flashingMazesImage;

    public MsPacManGameSpriteSheet(String resourcePath) {
        ResourceManager rm = this::getClass;
        source = rm.loadImage(resourcePath + "mspacman_spritesheet.png");
        flashingMazesImage = rm.loadImage(resourcePath + "mazes_flashing.png");
    }

    @Override
    public Image sourceImage() {
        return source;
    }

    @Override
    public int tileSize() {
        return 16;
    }

    // third column contains the sprites (first two columns the maze images)
    private RectangularArea sprite(int tileX, int tileY) {
        return rect(456 + tiles(tileX), tiles(tileY), tileSize(), tileSize());
    }

    private final RectangularArea[] ghostNumberSprites = rectArray(
        sprite(0, 8), sprite(1, 8), sprite(2, 8), sprite(3, 8));

    public RectangularArea[] ghostNumberSprites() {
        return ghostNumberSprites;
    }

    private final RectangularArea[] bonusSymbolSprites = IntStream.range(0, 8)
        .mapToObj(symbol -> sprite(3 + symbol, 0))
        .toArray(RectangularArea[]::new);

    @Override
    public RectangularArea bonusSymbolSprite(byte symbol) {
        return bonusSymbolSprites[symbol];
    }

    private final RectangularArea[] bonusValueSprites = IntStream.range(0, 8)
        .mapToObj(symbol -> sprite(3 + symbol, 1))
        .toArray(RectangularArea[]::new);

    @Override
    public RectangularArea bonusValueSprite(byte symbol) {
        return bonusValueSprites[symbol];
    }

    private final RectangularArea livesCounterSprite = sprite(1, 0);

    @Override
    public RectangularArea livesCounterSprite() {
        return livesCounterSprite;
    }

    private final RectangularArea[][] msPacManMunchingSprites = new RectangularArea[4][];
    {
        for (byte d = 0; d < 4; ++d) {
            var wide = sprite(0, d);
            var open = sprite(1, d);
            var closed = sprite(2, d);
            msPacManMunchingSprites[d] = rectArray(open, open, wide, wide, open, open, open, closed, closed);
        }
    }

    public RectangularArea[] msPacManMunchingSprites(Direction dir) {
        return msPacManMunchingSprites[ORDER.indexOf(dir)];
    }

    private final RectangularArea[] msPacManDyingSprites;
    {
        var right = sprite(1, 0);
        var left = sprite(1, 1);
        var up = sprite(1, 2);
        var down = sprite(1, 3);
        // TODO: this is not yet 100% correct
        msPacManDyingSprites = rectArray(down, left, up, right, down, left, up, right, down, left, up);
    }

    public RectangularArea[] msPacManDyingSprites() {
        return msPacManDyingSprites;
    }

    private final RectangularArea[][][] ghostsNormalSprites = new RectangularArea[4][4][];
    {
        for (byte id = 0; id < 4; ++id) {
            for (byte d = 0; d < 4; ++d) {
                ghostsNormalSprites[id][d] = rectArray(sprite(2 * d, 4 + id), sprite(2 * d + 1, 4 + id));
            }
        }
    }

    public RectangularArea[] ghostNormalSprites(byte id, Direction dir) {
        return ghostsNormalSprites[id][ORDER.indexOf(dir)];
    }

    private final RectangularArea[] ghostFrightenedSprites = rectArray(sprite(8, 4), sprite(9, 4));

    public RectangularArea[] ghostFrightenedSprites() {
        return ghostFrightenedSprites;
    }

    private final RectangularArea[] ghostFlashingSprites = rectArray(
        sprite(8, 4), sprite(9, 4), sprite(10, 4), sprite(11, 4));

    public RectangularArea[] ghostFlashingSprites() {
        return ghostFlashingSprites;
    }

    private final RectangularArea[][] ghostEyesSprites = new RectangularArea[4][];
    {
        for (byte d = 0; d < 4; ++d) {
            ghostEyesSprites[d] = rectArray(sprite(8 + d, 5));
        }
    }

    public RectangularArea[] ghostEyesSprites(Direction dir) {
        return ghostEyesSprites[ORDER.indexOf(dir)];
    }

    // Ms. Pac-Man specific:

    public Image getFlashingMazesImage() {
        return flashingMazesImage;
    }

    private final RectangularArea[][] pacManMunchingSprites = new RectangularArea[4][];
    {
        for (byte d = 0; d < 4; ++d) {
            pacManMunchingSprites[d] = rectArray(sprite(0, 9 + d), sprite(1, 9 + d), sprite(2, 9));
        }
    }

    public RectangularArea[] pacManMunchingSprites(Direction dir) {
        return pacManMunchingSprites[ORDER.indexOf(dir)];
    }

    private final RectangularArea heartSprite = sprite(2, 10);

    public RectangularArea heartSprite() {
        return heartSprite;
    }

    private final RectangularArea blueBagSprite = rect(488, 199, 8, 8);

    public RectangularArea blueBagSprite() {
        return blueBagSprite;
    }

    private final RectangularArea juniorPacSprite = rect(509, 200, 8, 8);

    public RectangularArea juniorPacSprite() {
        return juniorPacSprite;
    }

    private final RectangularArea[] clapperboardSprites = rectArray(
        rect(456, 208, 32, 32),  // open
        rect(488, 208, 32, 32),  // middle
        rect(520, 208, 32, 32)); // closed

    public RectangularArea[] clapperboardSprites() {
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