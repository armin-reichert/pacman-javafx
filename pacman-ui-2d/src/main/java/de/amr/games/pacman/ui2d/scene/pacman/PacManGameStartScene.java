/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene2D;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui2d.GameAssets2D.*;
import static de.amr.games.pacman.ui2d.scene.pacman.PacManGameSpriteSheet.MIDWAY_COPYRIGHT;

/**
 * @author Armin Reichert
 */
public class PacManGameStartScene extends GameScene2D {

    static final List<GameAction> ACTIONS = List.of(
        GameAction2D.ADD_CREDIT,
        GameAction2D.START_GAME
    );

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
        context.execFirstCalledAction(ACTIONS);
    }

    @Override
    public void drawSceneContent(GameWorldRenderer renderer) {
        var font8 = renderer.scaledArcadeFont(8);
        var font6 = renderer.scaledArcadeFont(6);
        renderer.drawText("PUSH START BUTTON", ARCADE_ORANGE, font8, t(6), t(17));
        renderer.drawText("1 PLAYER ONLY", ARCADE_CYAN, font8, t(8), t(21));
        renderer.drawText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE, font8, t(1), t(25));
        renderer.drawText("PTS", ARCADE_ROSE, font6, t(25), t(25));
        if (context.gameVariant() == GameVariant.PACMAN) {
            renderer.drawText(MIDWAY_COPYRIGHT, ARCADE_PINK, renderer.scaledArcadeFont(8), t(4), t(29));
        }
        drawCredit(renderer, context.worldSizeTilesOrDefault());
        drawLevelCounter(renderer, context.worldSizeTilesOrDefault());
    }
}