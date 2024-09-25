/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.ui2d.variant.tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.graph.Dir;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import javafx.scene.image.Image;

import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.ui2d.rendering.GameSpriteSheet.rectArray;
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
        {rect(77,75,14,13),  rect(93, 75, 14, 13)},
        {rect(110,75,14,13), rect(126, 75, 14, 13)},
        {rect(142,75,14,13), rect(158, 75, 14, 13)},
        {rect(174,75,14,13), rect(190, 75, 14, 13)},
    };
    private static final RectArea[][] PINK_GHOST_SPRITES = {
            {rect(77,90,14,13),  rect(93, 90, 14, 13)},
            {rect(110,90,14,13), rect(126, 90, 14, 13)},
            {rect(142,90,14,13), rect(158, 90, 14, 13)},
            {rect(174,90,14,13), rect(190, 90, 14, 13)},
    };
    private static final RectArea[][] CYAN_GHOST_SPRITES = {
            {rect(77,105,14,13),  rect(93, 105, 14, 13)},
            {rect(110,105,14,13), rect(126, 105, 14, 13)},
            {rect(142,105,14,13), rect(158, 105, 14, 13)},
            {rect(174,105,14,13), rect(190, 105, 14, 13)},
    };
    private static final RectArea[][] ORANGE_GHOST_SPRITES = {
            {rect(77,120,14,13),  rect(93, 120, 14, 13)},
            {rect(110,120,14,13), rect(126, 120, 14, 13)},
            {rect(142,120,14,13), rect(158, 120, 14, 13)},
            {rect(174,120,14,13), rect(190, 120, 14, 13)},
    };

    private static final RectArea[] GHOST_FRIGHTENED_SPRITES = {
        rect(206, 75, 14, 13), rect(222, 75, 14, 13)
    };

    private static final RectArea[] GHOST_FLASHING_SPRITES = {
        rect(206, 90, 14, 13),
        rect(222, 90, 14, 13),
        rect(222, 105, 14, 13),
        rect(206, 105, 14, 13),
    };

    private static final RectArea[] GHOST_EYES_SPRITES = {
        rect(208, 128, 10, 5),
        rect(208, 121, 10, 5),
        rect(221, 121, 10, 5),
        rect(221, 128, 10, 5),
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
        return NO_SPRITES;
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