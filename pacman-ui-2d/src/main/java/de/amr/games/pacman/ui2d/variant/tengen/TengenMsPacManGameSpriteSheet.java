/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.ui2d.variant.tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import javafx.scene.image.Image;

import static de.amr.games.pacman.model.GameModel.*;
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

    // red ghost rr ll uu dd
    private static final RectArea[][] RED_GHOST_SPRITES = {
        {rect(46, 77,14,13), rect(62,  77, 14, 13)},
        {rect(78, 77,14,13), rect(94,  77, 14, 13)},
        {rect(110,77,14,13), rect(126, 77, 14, 13)},
        {rect(142,77,14,13), rect(158, 77, 14, 13)},
    };
    private static final RectArea[][] PINK_GHOST_SPRITES = {
            {rect(46, 92,14,13), rect(62,  92, 14, 13)},
            {rect(78, 92,14,13), rect(94,  92, 14, 13)},
            {rect(110,92,14,13), rect(126, 92, 14, 13)},
            {rect(142,92,14,13), rect(158, 92, 14, 13)},
    };
    private static final RectArea[][] CYAN_GHOST_SPRITES = {
            {rect(46, 107,14,13), rect(62,  107, 14, 13)},
            {rect(78, 107,14,13), rect(94,  107, 14, 13)},
            {rect(110,107,14,13), rect(126, 107, 14, 13)},
            {rect(142,107,14,13), rect(158, 107, 14, 13)},
    };
    private static final RectArea[][] ORANGE_GHOST_SPRITES = {
            {rect(46, 122,14,13), rect(62,  122, 14, 13)},
            {rect(78, 122,14,13), rect(94,  122, 14, 13)},
            {rect(110,122,14,13), rect(126, 122, 14, 13)},
            {rect(142,122,14,13), rect(158, 122, 14, 13)},
    };

    private static final RectArea[] GHOST_FRIGHTENED_SPRITES = {
        rect(174, 77, 14, 13), rect(174, 77, 14, 13)
    };

    private static final RectArea[] GHOST_FLASHING_SPRITES = {
        rect(174, 92, 14, 13),  rect(190, 92, 14, 13), // white/red eyes+mouth
        rect(174, 107, 14, 13), rect(190, 107, 14, 13), // white/blue eyes+mouth
    };

    private static final RectArea[] GHOST_EYES_SPRITES = {
        rect(176, 123, 10, 5), // left
        rect(189, 123, 10, 5), // right
        rect(176, 130, 10, 5), // up
        rect(189, 130, 10, 5), // down
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

    private static final RectArea[] CLAPPERBOARD_SPRITES = { // open, middle, closed
        rect(91, 361, 32, 32),
        rect(53, 361, 32, 32),
        rect(14, 361, 32, 32),
    };

    private static final RectArea HEART_SPRITE = rect(162, 270, 18, 18);
    private static final RectArea BLUE_BAG_SPRITE = rect(239, 361, 12, 12);
    private static final RectArea JUNIOR_PAC_SPRITE = rect(165, 303, 10, 10);

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
        return GHOST_EYES_SPRITES;
    }

    @Override
    public RectArea[] ghostFlashingSprites() {
        return GHOST_FLASHING_SPRITES;
    }

    @Override
    public RectArea[] ghostFrightenedSprites() {
        return GHOST_FRIGHTENED_SPRITES;
    }

    @Override
    public RectArea[] ghostNumberSprites() {
        return GHOST_NUMBER_SPRITES;
    }

    @Override
    public RectArea[] ghostNormalSprites(byte id, Direction dir) {
        return switch (id) {
            case RED_GHOST    -> RED_GHOST_SPRITES[dirIndex(dir)];
            case PINK_GHOST   -> PINK_GHOST_SPRITES[dirIndex(dir)];
            case CYAN_GHOST   -> CYAN_GHOST_SPRITES[dirIndex(dir)];
            case ORANGE_GHOST -> ORANGE_GHOST_SPRITES[dirIndex(dir)];
            default -> throw new IllegalArgumentException();
        };
    }

    private int dirIndex(Direction dir) {
        return switch (dir) {
            case Direction.RIGHT -> 0;
            case Direction.LEFT -> 1;
            case Direction.UP -> 2;
            case Direction.DOWN -> 3;
        };
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
        return CLAPPERBOARD_SPRITES;
    }

    @Override
    public RectArea[] pacManMunchingSprites(Direction dir) {
        return NO_SPRITES;
    }

    @Override
    public RectArea heartSprite() {
        return HEART_SPRITE;
    }

    @Override
    public RectArea blueBagSprite() {
        return BLUE_BAG_SPRITE;
    }

    @Override
    public RectArea juniorPacSprite() {
        return JUNIOR_PAC_SPRITE;
    }
}