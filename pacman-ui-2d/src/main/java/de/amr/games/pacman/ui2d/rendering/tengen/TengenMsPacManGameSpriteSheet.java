/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.ui2d.rendering.tengen;

import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.RectangularArea;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import javafx.scene.image.Image;

public class TengenMsPacManGameSpriteSheet implements GameSpriteSheet {

    private final Image spritesImage;

    public TengenMsPacManGameSpriteSheet(String resourcePath) {
        ResourceManager rm = this::getClass;
        spritesImage = rm.loadImage(resourcePath + "spritesheet.png");
    }

    @Override
    public RectangularArea livesCounterSprite() {
        return null;
    }

    private final RectangularArea[] bonusSymbolSprites = new RectangularArea[14];
    private final RectangularArea[] bonusValueSprites = new RectangularArea[14];
    {
        int[] xs = {  8, 24, 40, 56, 76, 96, 118, 140, 162, 182, 204, 230, 250, 272 };
        int[] ws = { 16, 15, 16, 18, 18, 20,  18,  18,  18,  18,  18,  18,  18,  18 };
        for (int i = 0; i < 14; ++i) {
            bonusSymbolSprites[i] = new RectangularArea(xs[i], 66, ws[i], 20);
            bonusValueSprites[i]  = new RectangularArea(xs[i], 85, ws[i], 18);
        }
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
    public int tileSize() {
        return 16;
    }

    @Override
    public Image sourceImage() {
        return spritesImage;
    }
}
