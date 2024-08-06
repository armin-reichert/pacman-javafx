/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.ui2d.ActionHandler;
import de.amr.games.pacman.ui2d.GameKeys;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class PacManCreditScene extends GameScene2D {

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
    }

    @Override
    public void handleKeyboardInput(ActionHandler handler) {
        if (GameKeys.ADD_CREDIT.pressed()) {
            handler.addCredit();
        } else if (GameKeys.START_GAME.pressed()) {
            handler.startGame();
        }
    }

    @Override
    public void drawSceneContent() {
        var font8 = sceneFont(8);
        var font6 = sceneFont(6);
        spriteRenderer.drawText(g, "PUSH START BUTTON", context.assets().color("palette.orange"), font8, t(6), t(17));
        spriteRenderer.drawText(g, "1 PLAYER ONLY", context.assets().color("palette.cyan"), font8, t(8), t(21));
        spriteRenderer.drawText(g, "BONUS PAC-MAN FOR 10000", context.assets().color("palette.rose"), font8, t(1), t(25));
        spriteRenderer.drawText(g, "PTS", context.assets().color("palette.rose"), font6, t(25), t(25));
        drawMidwayCopyright(t(4), t(29));
        drawLevelCounter(g);
    }
}