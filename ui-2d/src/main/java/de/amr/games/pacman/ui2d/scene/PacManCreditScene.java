/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

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
        setScoreVisible(true);
    }

    @Override
    public void update() {
    }

    @Override
    public void handleKeyboardInput() {
        if (GameKeys.ADD_CREDIT.pressed()) {
            context.actionHandler().addCredit();
        } else if (GameKeys.START_GAME.pressed()) {
            context.actionHandler().startGame();
        }
    }

    @Override
    public void drawSceneContent() {
        var font8 = sceneFont(8);
        var font6 = sceneFont(6);
        classicRenderer.drawText(g, "PUSH START BUTTON", context.theme().color("palette.orange"), font8, t(6), t(17));
        classicRenderer.drawText(g, "1 PLAYER ONLY", context.theme().color("palette.cyan"), font8, t(8), t(21));
        classicRenderer.drawText(g, "BONUS PAC-MAN FOR 10000", context.theme().color("palette.rose"), font8, t(1), t(25));
        classicRenderer.drawText(g, "PTS", context.theme().color("palette.rose"), font6, t(25), t(25));
        drawMidwayCopyright(t(4), t(29));
        drawLevelCounter(g);
    }
}