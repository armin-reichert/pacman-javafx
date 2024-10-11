/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene2D;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui2d.GameAssets2D.*;
import static de.amr.games.pacman.ui2d.scene.pacman.PacManGameSpriteSheet.MIDWAY_COPYRIGHT;

/**
 * @author Armin Reichert
 */
public class MsPacManStartScene extends GameScene2D {

    @Override
    public void init() {
        context.setScoreVisible(true);
    }

    @Override
    public void end() {
    }

    @Override
    public void update() {
    }

    @Override
    public void handleInput() {
        context.execFirstCalledAction(Stream.of(GameAction2D.ADD_CREDIT, GameAction2D.START_GAME));
    }

    @Override
    public void drawSceneContent(GameWorldRenderer renderer) {
        var font8 = renderer.scaledArcadeFont(8);
        var font6 = renderer.scaledArcadeFont(6);
        switch (context.gameVariant()) {
            case MS_PACMAN -> {
                renderer.drawText("PUSH START BUTTON", PALETTE_ORANGE, font8, t(6), t(16));
                renderer.drawText("1 PLAYER ONLY", PALETTE_ORANGE, font8, t(8), t(18));
                renderer.drawText("ADDITIONAL    AT 10000", PALETTE_ORANGE, font8, t(2), t(25));
                renderer.drawSpriteScaled(context.spriteSheet(), context.spriteSheet().livesCounterSprite(), t(13), t(23) + 1);
                renderer.drawText("PTS", PALETTE_ORANGE, font6, t(25), t(25));
                renderer.drawMsPacManMidwayCopyright(context.assets().get("ms_pacman.logo.midway"),
                    t(6), t(28), PALETTE_RED, renderer.scaledArcadeFont(TS));
            }
            case MS_PACMAN_TENGEN -> {
                renderer.drawText("PUSH START BUTTON", PALETTE_ORANGE, font8, t(6), t(16));
                renderer.drawText("1 PLAYER ONLY", PALETTE_ORANGE, font8, t(8), t(18));
                renderer.drawText("ADDITIONAL    AT 10000", PALETTE_ORANGE, font8, t(2), t(25));
                renderer.drawSpriteScaled(context.spriteSheet(), context.spriteSheet().livesCounterSprite(), t(13), t(23) + 1);
                renderer.drawText("PTS", PALETTE_ORANGE, font6, t(25), t(25));
            }
            case PACMAN -> {
                renderer.drawText("PUSH START BUTTON", PALETTE_ORANGE, font8, t(6), t(17));
                renderer.drawText("1 PLAYER ONLY", PALETTE_CYAN, font8, t(8), t(21));
                renderer.drawText("BONUS PAC-MAN FOR 10000", PALETTE_ROSE, font8, t(1), t(25));
                renderer.drawText("PTS", PALETTE_ROSE, font6, t(25), t(25));
                renderer.drawText(MIDWAY_COPYRIGHT, PALETTE_PINK, renderer.scaledArcadeFont(8),  t(4), t(29));
            }
            case PACMAN_XXL -> {
                renderer.drawText("PUSH START BUTTON", PALETTE_ORANGE, font8, t(6), t(17));
                renderer.drawText("1 PLAYER ONLY", PALETTE_CYAN, font8, t(8), t(21));
                renderer.drawText("BONUS PAC-MAN FOR 10000", PALETTE_ROSE, font8, t(1), t(25));
                renderer.drawText("PTS", PALETTE_ROSE, font6, t(25), t(25));
            }
            default -> throw new IllegalArgumentException("Unsupported game variant: " + context.gameVariant());
        }
        drawCredit(renderer, context.worldSizeTilesOrDefault());
        drawLevelCounter(renderer, context.worldSizeTilesOrDefault());
    }
}