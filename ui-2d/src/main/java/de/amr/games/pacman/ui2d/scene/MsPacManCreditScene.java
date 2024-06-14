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
public class MsPacManCreditScene extends GameScene2D {

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
    public void handleKeyboardInput() {
        if (GameKeys.ADD_CREDIT.pressed()) {
            context.actionHandler().addCredit();
        } else if (GameKeys.START_GAME.pressed()) {
            context.actionHandler().startGame();
        }
    }

    @Override
    public void drawSceneContent() {
        var font6 = sceneFont(6); // TODO looks bad
        var font8 = sceneFont(8);
        var color = context.theme().color("palette.orange");
        spriteRenderer.drawText(g, "PUSH START BUTTON", color, font8, t(6), t(16));
        spriteRenderer.drawText(g, "1 PLAYER ONLY", color, font8, t(8), t(18));
        spriteRenderer.drawText(g, "ADDITIONAL    AT 10000", color, font8, t(2), t(25));
        spriteRenderer.drawSpriteScaled(g, spriteRenderer.getSpriteSheet().livesCounterSprite(), t(13), t(23) + 1);
        spriteRenderer.drawText(g, "PTS", color, font6, t(25), t(25));
        drawMsPacManCopyright(t(6), t(28));
        drawLevelCounter(g);
    }
}