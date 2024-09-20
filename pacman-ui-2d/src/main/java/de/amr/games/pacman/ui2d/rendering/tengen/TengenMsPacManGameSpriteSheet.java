/*
 * Copyright (c) 2021-2024 Armin Reichert (MIT License) See file LICENSE in repository root directory for details.
 */
package de.amr.games.pacman.ui2d.rendering.tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.ui2d.rendering.RectangularArea;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import de.amr.games.pacman.ui2d.util.SpriteSheet;
import javafx.scene.image.Image;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGameSpriteSheet implements SpriteSheet {

    private final MsPacManGameSpriteSheet msPacManSpriteSheet;
    private final Image tengenSpriteImage;

    //TODO: TEMPORARY
    boolean useMsPacManSprites;

    public TengenMsPacManGameSpriteSheet(String resourcePath) {
        ResourceManager rm = this::getClass;
        tengenSpriteImage = rm.loadImage(resourcePath + "spritesheet.png");
        msPacManSpriteSheet = new MsPacManGameSpriteSheet("/de/amr/games/pacman/ui2d/graphics/mspacman/");
        useMsPacManSprites = true;
    }

    @Override
    public int tileSize() {
        return 16;
    }

    @Override
    public Image sourceImage() {
        return useMsPacManSprites ? msPacManSpriteSheet.sourceImage() : tengenSpriteImage;
    }

    @Override
    public RectangularArea[] pacManMunchingSprites(Direction dir) {
        return msPacManSpriteSheet.pacManMunchingSprites(dir);
    }

    @Override
    public RectangularArea[] pacMunchingSprites(Direction dir) {
        return msPacManSpriteSheet.pacMunchingSprites(dir);
    }

    @Override
    public RectangularArea[] pacDyingSprites() {
        return msPacManSpriteSheet.pacDyingSprites();
    }

    @Override
    public RectangularArea[] blinkyNakedSprites() {
        return new RectangularArea[0];
    }

    @Override
    public RectangularArea[] blinkyPatchedSprites() {
        return new RectangularArea[0];
    }

    @Override
    public RectangularArea[] blinkyDamagedSprites() {
        return new RectangularArea[0];
    }

    @Override
    public RectangularArea[] blinkyStretchedSprites() {
        return new RectangularArea[0];
    }

    @Override
    public RectangularArea[] bigPacManSprites() {
        return new RectangularArea[0];
    }

    @Override
    public RectangularArea[] msPacManDyingSprites() {
        return msPacManSpriteSheet.msPacManDyingSprites();
    }

    @Override
    public RectangularArea[] msPacManMunchingSprites(Direction dir) {
        return msPacManSpriteSheet.msPacManMunchingSprites(dir);
    }

    @Override
    public RectangularArea[] ghostEyesSprites(Direction dir) {
        return msPacManSpriteSheet.ghostEyesSprites(dir);
    }

    @Override
    public RectangularArea[] ghostFlashingSprites() {
        return msPacManSpriteSheet.ghostFlashingSprites();
    }

    @Override
    public RectangularArea[] ghostFrightenedSprites() {
        return msPacManSpriteSheet.ghostFrightenedSprites();
    }

    @Override
    public RectangularArea[] ghostNumberSprites() {
        return msPacManSpriteSheet.ghostNumberSprites();
    }

    @Override
    public RectangularArea[] ghostNormalSprites(byte id, Direction dir) {
        return msPacManSpriteSheet.ghostNormalSprites(id, dir);
    }

    @Override
    public RectangularArea livesCounterSprite() {
        return msPacManSpriteSheet.livesCounterSprite();
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

    public RectangularArea[] clapperboardSprites() {
        return msPacManSpriteSheet.clapperboardSprites();
    }

    @Override
    public RectangularArea heartSprite() {
        return msPacManSpriteSheet.heartSprite();
    }

    @Override
    public RectangularArea blueBagSprite() {
        return msPacManSpriteSheet.blueBagSprite();
    }

    @Override
    public RectangularArea juniorPacSprite() {
        return msPacManSpriteSheet.juniorPacSprite();
    }
}