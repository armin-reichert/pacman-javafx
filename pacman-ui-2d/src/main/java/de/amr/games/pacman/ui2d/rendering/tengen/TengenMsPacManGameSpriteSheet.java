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

    @Override
    public SpriteArea bonusSymbolSprite(byte symbol) {
        return null;
    }

    @Override
    public SpriteArea bonusValueSprite(byte symbol) {
        return null;
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
