/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui2d.GameAssets2D.*;
import static de.amr.games.pacman.ui2d.scene.pacman.PacManGameSpriteSheet.MIDWAY_COPYRIGHT;

/**
 * @author Armin Reichert
 */
public class StartScene extends GameScene2D {

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
        context.doFirstCalledAction(GameAction2D.ADD_CREDIT, GameAction2D.START_GAME);
    }

    @Override
    public void drawSceneContent(GameRenderer renderer) {
        MsPacManGameRenderer r = (MsPacManGameRenderer) renderer;
        GameSpriteSheet spriteSheet = r.spriteSheet();
        Font font8 = r.scaledArcadeFont(8), font6 = r.scaledArcadeFont(6);
        switch (context.gameVariant()) {
            case MS_PACMAN -> {
                r.drawText("PUSH START BUTTON", ARCADE_ORANGE, font8, t(6), t(16));
                r.drawText("1 PLAYER ONLY", ARCADE_ORANGE, font8, t(8), t(18));
                r.drawText("ADDITIONAL    AT 10000", ARCADE_ORANGE, font8, t(2), t(25));
                r.drawSpriteScaled(spriteSheet.livesCounterSprite(), t(13), t(23) + 1);
                r.drawText("PTS", ARCADE_ORANGE, font6, t(25), t(25));
                r.drawMsPacManMidwayCopyright(t(6), t(28), ARCADE_RED, r.scaledArcadeFont(TS));
            }
            case MS_PACMAN_TENGEN -> {
                r.drawText("PUSH START BUTTON", ARCADE_ORANGE, font8, t(6), t(16));
                r.drawText("1 PLAYER ONLY", ARCADE_ORANGE, font8, t(8), t(18));
                r.drawText("ADDITIONAL    AT 10000", ARCADE_ORANGE, font8, t(2), t(25));
                r.drawSpriteScaled(spriteSheet.livesCounterSprite(), t(13), t(23) + 1);
                r.drawText("PTS", ARCADE_ORANGE, font6, t(25), t(25));
            }
            case PACMAN -> {
                r.drawText("PUSH START BUTTON", ARCADE_ORANGE, font8, t(6), t(17));
                r.drawText("1 PLAYER ONLY", ARCADE_CYAN, font8, t(8), t(21));
                r.drawText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE, font8, t(1), t(25));
                r.drawText("PTS", ARCADE_ROSE, font6, t(25), t(25));
                r.drawText(MIDWAY_COPYRIGHT, ARCADE_PINK, font8,  t(4), t(29));
            }
            case PACMAN_XXL -> {
                r.drawText("PUSH START BUTTON", ARCADE_ORANGE, font8, t(6), t(17));
                r.drawText("1 PLAYER ONLY", ARCADE_CYAN, font8, t(8), t(21));
                r.drawText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE, font8, t(1), t(25));
                r.drawText("PTS", ARCADE_ROSE, font6, t(25), t(25));
            }
            default -> throw new IllegalArgumentException("Unsupported game variant: " + context.gameVariant());
        }
        drawCredit(r, context.worldSizeTilesOrDefault());
        drawLevelCounter(r, context.worldSizeTilesOrDefault());
    }
}