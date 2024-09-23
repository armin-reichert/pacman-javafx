/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.ui2d.variant.tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import javafx.scene.image.Image;

import static de.amr.games.pacman.ui2d.rendering.RectArea.rect;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGameSpriteSheet implements GameSpriteSheet {

    private final Image source;

    private static final RectArea[] MS_PAC_MUNCHING_SPRITES = {
        rect(32, 15, 15, 15),
        rect(52, 15, 15, 15),
        rect(68, 15, 15, 15)
    };

    private static final RectArea[] GHOST_NUMBER_SPRITES = {
        rect(259, 172, 16, 10),
        rect(279, 172, 16, 10),
        rect(259, 183, 16, 10),
        rect(279, 183, 16, 10)
    };

    private static final RectArea[] BONUS_SYMBOL_SPRITES = new RectArea[14];
    private static final RectArea[] BONUS_VALUE_SPRITES = new RectArea[14];
    static {
        int[] xs = {  8, 24, 40, 56, 76, 96, 118, 140, 162, 182, 204, 230, 250, 272 };
        int[] ws = { 16, 15, 16, 18, 18, 20,  18,  18,  18,  18,  18,  18,  18,  18 };
        int[] dy = {  3,  4,  3,  0,  0,  0,   0,   0,   0,   0,   0,   0,   0,   0 };
        for (int i = 0; i < 14; ++i) {
            BONUS_SYMBOL_SPRITES[i] = new RectArea(xs[i], 66 + dy[i], ws[i], 20 - dy[i]);
            BONUS_VALUE_SPRITES[i]  = new RectArea(xs[i], 85, ws[i], 18);
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
    public RectArea[] pacMunchingSprites(Direction dir) {
        return MS_PAC_MUNCHING_SPRITES;
    }

    @Override
    public RectArea[] pacDyingSprites() {
        return NO_SPRITES;
    }

    @Override
    public RectArea[] ghostEyesSprites(Direction dir) {
        return NO_SPRITES;
    }

    @Override
    public RectArea[] ghostFlashingSprites() {
        return NO_SPRITES;
    }

    @Override
    public RectArea[] ghostFrightenedSprites() {
        return NO_SPRITES;
    }

    @Override
    public RectArea[] ghostNumberSprites() {
        return GHOST_NUMBER_SPRITES;
    }

    @Override
    public RectArea[] ghostNormalSprites(byte id, Direction dir) {
        return NO_SPRITES;
    }

    @Override
    public RectArea livesCounterSprite() {
        return rect(241, 15, 16, 16);
    }

    @Override
    public RectArea bonusSymbolSprite(byte symbol) {
        return BONUS_SYMBOL_SPRITES[symbol];
    }

    @Override
    public RectArea bonusValueSprite(byte symbol) {
        return BONUS_VALUE_SPRITES[symbol];
    }

    public RectArea[] clapperboardSprites() {
        return NO_SPRITES;
    }

    @Override
    public RectArea[] pacManMunchingSprites(Direction dir) {
        return NO_SPRITES;
    }

    @Override
    public RectArea heartSprite() {
        return NO_SPRITE;
    }

    @Override
    public RectArea blueBagSprite() {
        return NO_SPRITE;
    }

    @Override
    public RectArea juniorPacSprite() {
        return NO_SPRITE;
    }
}