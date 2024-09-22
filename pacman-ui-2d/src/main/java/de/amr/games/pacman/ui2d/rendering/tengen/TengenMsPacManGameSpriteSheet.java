/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.ui2d.rendering.tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.rendering.RectangularArea;
import de.amr.games.pacman.ui2d.rendering.SpriteSheet;
import javafx.scene.image.Image;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGameSpriteSheet implements SpriteSheet {

    private final Image source;

    private final RectangularArea[] bonusSymbolSprites = new RectangularArea[14];
    private final RectangularArea[] bonusValueSprites = new RectangularArea[14];
    {
        int[] xs = {  8, 24, 40, 56, 76, 96, 118, 140, 162, 182, 204, 230, 250, 272 };
        int[] ws = { 16, 15, 16, 18, 18, 20,  18,  18,  18,  18,  18,  18,  18,  18 };
        int[] dy = {  3,  4,  3,  0,  0,  0,   0,   0,   0,   0,   0,   0,   0,   0 };
        for (int i = 0; i < 14; ++i) {
            bonusSymbolSprites[i] = new RectangularArea(xs[i], 66 + dy[i], ws[i], 20 - dy[i]);
            bonusValueSprites[i]  = new RectangularArea(xs[i], 85, ws[i], 18);
        }
    }




    public TengenMsPacManGameSpriteSheet(Image source) {
        this.source = source;
    }

    @Override
    public Image sourceImage() {
        return source;
    }

    @Override
    public RectangularArea[] pacMunchingSprites(Direction dir) {
        return NO_SPRITES;
    }

    @Override
    public RectangularArea[] pacDyingSprites() {
        return NO_SPRITES;
    }

    @Override
    public RectangularArea[] ghostEyesSprites(Direction dir) {
        return NO_SPRITES;
    }

    @Override
    public RectangularArea[] ghostFlashingSprites() {
        return NO_SPRITES;
    }

    @Override
    public RectangularArea[] ghostFrightenedSprites() {
        return NO_SPRITES;
    }

    @Override
    public RectangularArea[] ghostNumberSprites() {
        return NO_SPRITES;
    }

    @Override
    public RectangularArea[] ghostNormalSprites(byte id, Direction dir) {
        return NO_SPRITES;
    }

    @Override
    public RectangularArea livesCounterSprite() {
        return NO_SPRITE;
    }

    @Override
    public RectangularArea bonusSymbolSprite(byte symbol) {
        return bonusSymbolSprites[symbol];
    }

    @Override
    public RectangularArea bonusValueSprite(byte symbol) {
        return bonusValueSprites[symbol];
    }

    public RectangularArea[] clapperboardSprites() {
        return NO_SPRITES;
    }

    @Override
    public RectangularArea[] pacManMunchingSprites(Direction dir) {
        return NO_SPRITES;
    }

    @Override
    public RectangularArea heartSprite() {
        return NO_SPRITE;
    }

    @Override
    public RectangularArea blueBagSprite() {
        return NO_SPRITE;
    }

    @Override
    public RectangularArea juniorPacSprite() {
        return NO_SPRITE;
    }
}