/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.ui2d.GameAction;
import javafx.geometry.Rectangle2D;

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
    public void handleKeyboardInput() {
        if (GameAction.ADD_CREDIT.requested()) {
            context.addCredit();
        } else if (GameAction.START_GAME.requested()) {
            context.startGame();
        }
    }

    @Override
    public void drawSceneContent() {
        var font8 = sceneFont(8);
        var font6 = sceneFont(6);
        var cyan = context.assets().color("palette.cyan");
        var orange = context.assets().color("palette.orange");
        var rose = context.assets().color("palette.rose");
        switch (context.game().variant()) {
            case MS_PACMAN -> {
                Rectangle2D livesCounterSprite = spriteRenderer.spriteSheet().livesCounterSprite();
                spriteRenderer.drawText(g, "PUSH START BUTTON", orange, font8, t(6), t(16));
                spriteRenderer.drawText(g, "1 PLAYER ONLY", orange, font8, t(8), t(18));
                spriteRenderer.drawText(g, "ADDITIONAL    AT 10000", orange, font8, t(2), t(25));
                spriteRenderer.drawSpriteScaled(g, livesCounterSprite, t(13), t(23) + 1);
                spriteRenderer.drawText(g, "PTS", orange, font6, t(25), t(25));
                drawMsPacManCopyright(t(6), t(28));
            }
            case PACMAN, PACMAN_XXL -> {
                spriteRenderer.drawText(g, "PUSH START BUTTON", orange, font8, t(6), t(17));
                spriteRenderer.drawText(g, "1 PLAYER ONLY", cyan, font8, t(8), t(21));
                spriteRenderer.drawText(g, "BONUS PAC-MAN FOR 10000", rose, font8, t(1), t(25));
                spriteRenderer.drawText(g, "PTS", rose, font6, t(25), t(25));
                drawMidwayCopyright(t(4), t(29));
            }
        }
        drawLevelCounter(g);
    }
}