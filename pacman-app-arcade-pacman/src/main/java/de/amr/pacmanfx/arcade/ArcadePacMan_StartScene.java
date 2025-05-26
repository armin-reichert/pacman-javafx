/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.GameAssets.*;
import static de.amr.pacmanfx.ui.PacManGamesEnv.theAssets;
import static de.amr.pacmanfx.ui.PacManGamesEnv.theSound;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D {

    @Override
    public void doInit() {
        theGame().scoreManager().setScoreVisible(true);
        bindArcadeInsertCoinAction();
        bindArcadeStartGameAction();
    }

    @Override
    public void update() {}

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        final Font smallFont = theAssets().arcadeFontAtSize(scaled(6));
        gr().drawScores(theGame().scoreManager(), scoreColor(), defaultSceneFont());
        gr().fillTextAtScaledTilePosition("PUSH START BUTTON", ARCADE_ORANGE, defaultSceneFont(), 6, 17);
        gr().fillTextAtScaledTilePosition("1 PLAYER ONLY", ARCADE_CYAN, defaultSceneFont(), 8, 21);
        gr().fillTextAtScaledTilePosition("BONUS PAC-MAN FOR 10000", ARCADE_ROSE, defaultSceneFont(), 1, 25);
        gr().fillTextAtScaledTilePosition("PTS", ARCADE_ROSE, smallFont, 25, 25);
        gr().fillTextAtScaledTilePosition("© 1980 MIDWAY MFG.CO.", ARCADE_PINK, defaultSceneFont(), 4, 29);
        gr().fillTextAtScaledPosition("CREDIT %2d".formatted(theCoinMechanism().numCoins()),
                scoreColor(), defaultSceneFont(), 2 * TS, sizeInPx().y() - 2);
        gr().drawLevelCounter(theGame().levelCounter(), sizeInPx());
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        theSound().playInsertCoinSound();
    }
}