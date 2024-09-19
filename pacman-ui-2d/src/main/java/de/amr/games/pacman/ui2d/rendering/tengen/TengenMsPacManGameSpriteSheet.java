/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.ui2d.rendering.tengen;

import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.SpriteArea;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import javafx.scene.image.Image;

public class TengenMsPacManGameSpriteSheet implements GameSpriteSheet {

    private final Image spritesImage;

    public TengenMsPacManGameSpriteSheet(String resourcePath) {
        ResourceManager rm = this::getClass;
        spritesImage = rm.loadImage(resourcePath + "spritesheet.png");
    }

    @Override
    public SpriteArea livesCounterSprite() {
        return null;
    }

    private final SpriteArea[] bonusSymbolSprites = new SpriteArea[14];
    private final SpriteArea[] bonusValueSprites = new SpriteArea[14];
    {
        int[] xs = {  8, 24, 40, 56, 76, 96, 118, 140, 162, 182, 204, 230, 250, 272 };
        int[] ws = { 16, 15, 16, 18, 18, 20,  18,  18,  18,  18,  18,  18,  18,  18 };
        for (int i = 0; i < 14; ++i) {
            bonusSymbolSprites[i] = new SpriteArea(xs[i], 66, ws[i], 20);
            bonusValueSprites[i]  = new SpriteArea(xs[i], 85, ws[i], 18);
        }
    }

    @Override
    public SpriteArea bonusSymbolSprite(byte symbol) {
        return bonusSymbolSprites[symbol];
    }

    @Override
    public SpriteArea bonusValueSprite(byte symbol) {
        return bonusValueSprites[symbol];
    }

    @Override
    public int raster() {
        return 16;
    }

    @Override
    public Image source() {
        return spritesImage;
    }
}
