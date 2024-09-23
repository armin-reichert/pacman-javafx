/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.RectArea;
import de.amr.games.pacman.ui2d.variant.ms_pacman.MsPacManGameWorldRenderer;
import de.amr.games.pacman.ui2d.variant.pacman.PacManArcadeGameWorldRenderer;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class CreditScene extends GameScene2D {

    @Override
    public boolean isCreditVisible() {
        return true;
    }

    @Override
    public void init() {
        super.init();
        context.setScoreVisible(true);
    }

    @Override
    public void update() {
        // nothing to do
    }

    @Override
    public void handleUserInput() {
        if (GameAction.ADD_CREDIT.triggered()) {
            context.addCredit();
        } else if (GameAction.START_GAME.triggered()) {
            context.startGame();
        }
    }

    @Override
    public void drawSceneContent(GameWorldRenderer renderer) {
        var font8 = sceneFont(8);
        var font6 = sceneFont(6);
        var cyan = context.assets().color("palette.cyan");
        var orange = context.assets().color("palette.orange");
        var rose = context.assets().color("palette.rose");
        switch (context.game().variant()) {
            case MS_PACMAN -> {
                RectArea livesCounterSprite = renderer.spriteSheet().livesCounterSprite();
                renderer.drawText(g, "PUSH START BUTTON", orange, font8, t(6), t(16));
                renderer.drawText(g, "1 PLAYER ONLY", orange, font8, t(8), t(18));
                renderer.drawText(g, "ADDITIONAL    AT 10000", orange, font8, t(2), t(25));
                renderer.drawSpriteScaled(g, livesCounterSprite, t(13), t(23) + 1);
                renderer.drawText(g, "PTS", orange, font6, t(25), t(25));
                var msPacManGameRenderer = (MsPacManGameWorldRenderer) renderer;
                msPacManGameRenderer.drawMsPacManMidwayCopyright(g,
                    context.assets().get("ms_pacman.logo.midway"),
                    t(6), t(28), context.assets().color("palette.red"), sceneFont(TS));
            }
            case MS_PACMAN_TENGEN -> {
                RectArea livesCounterSprite = renderer.spriteSheet().livesCounterSprite();
                renderer.drawText(g, "PUSH START BUTTON", orange, font8, t(6), t(16));
                renderer.drawText(g, "1 PLAYER ONLY", orange, font8, t(8), t(18));
                renderer.drawText(g, "ADDITIONAL    AT 10000", orange, font8, t(2), t(25));
                renderer.drawSpriteScaled(g, livesCounterSprite, t(13), t(23) + 1);
                renderer.drawText(g, "PTS", orange, font6, t(25), t(25));
            }
            case PACMAN -> {
                renderer.drawText(g, "PUSH START BUTTON", orange, font8, t(6), t(17));
                renderer.drawText(g, "1 PLAYER ONLY", cyan, font8, t(8), t(21));
                renderer.drawText(g, "BONUS PAC-MAN FOR 10000", rose, font8, t(1), t(25));
                renderer.drawText(g, "PTS", rose, font6, t(25), t(25));
                var pacManGameRenderer = (PacManArcadeGameWorldRenderer) renderer;
                pacManGameRenderer.drawMidwayCopyright(g, t(4), t(29), context.assets().color("palette.pink"), sceneFont(8));
            }
            case PACMAN_XXL -> {
                renderer.drawText(g, "PUSH START BUTTON", orange, font8, t(6), t(17));
                renderer.drawText(g, "1 PLAYER ONLY", cyan, font8, t(8), t(21));
                renderer.drawText(g, "BONUS PAC-MAN FOR 10000", rose, font8, t(1), t(25));
                renderer.drawText(g, "PTS", rose, font6, t(25), t(25));
            }
            default -> throw new IllegalArgumentException("Unsupported game variant: " + context.game().variant());
        }
    }
}