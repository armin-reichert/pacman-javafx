/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.ui2d.GameKeys;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.Keyboard;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class MsPacManCreditScene extends GameScene2D {

    private MsPacManGameSpriteSheet ss;

    @Override
    public boolean isCreditVisible() {
        return true;
    }

    @Override
    public void init() {
        setScoreVisible(true);
        ss = (MsPacManGameSpriteSheet) context.getSpriteSheet(context.game().variant());
    }

    @Override
    public void update() {
    }

    @Override
    public void handleKeyboardInput() {
        if (Keyboard.pressed(GameKeys.ADD_CREDIT)) {
            context.actionHandler().addCredit();
        } else if (Keyboard.pressed(GameKeys.START_GAME)) {
            context.actionHandler().startGame();
        }
    }

    @Override
    public void drawSceneContent() {
        var font6 = sceneFont(6); // TODO looks bad
        var font8 = sceneFont(8);
        var color = context.theme().color("palette.orange");
        classicRenderer.drawText(g, "PUSH START BUTTON", color, font8, t(6), t(16));
        classicRenderer.drawText(g, "1 PLAYER ONLY", color, font8, t(8), t(18));
        classicRenderer.drawText(g, "ADDITIONAL    AT 10000", color, font8, t(2), t(25));
        classicRenderer.drawSpriteScaled(g, ss.source(), ss.livesCounterSprite(), t(13), t(23) + 1);
        classicRenderer.drawText(g, "PTS", color, font6, t(25), t(25));
        drawMsPacManCopyright(t(6), t(28));
        drawLevelCounter(g);
    }
}