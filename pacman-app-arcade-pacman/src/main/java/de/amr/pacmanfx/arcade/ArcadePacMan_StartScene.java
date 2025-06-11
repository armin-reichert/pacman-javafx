/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui.PacManGames_ActionBinding;
import de.amr.pacmanfx.ui._2d.GameScene2D;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.arcade.ArcadePalette.*;
import static de.amr.pacmanfx.ui.PacManGames_Actions.ACTION_ARCADE_INSERT_COIN;
import static de.amr.pacmanfx.ui.PacManGames_Actions.ACTION_ARCADE_START_GAME;
import static de.amr.pacmanfx.ui.PacManGames_Env.theSound;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D implements PacManGames_ActionBinding {

    @Override
    public void doInit() {
        theGame().setScoreVisible(true);
        bindAction(ACTION_ARCADE_INSERT_COIN, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_ARCADE_START_GAME, COMMON_ACTION_BINDINGS);
    }

    @Override
    public void update() {}

    @Override
    public void onCreditAdded(GameEvent e) {
        theSound().playInsertCoinSound();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        gr().fillTextAtTile("PUSH START BUTTON", ARCADE_ORANGE, normalArcadeFont(), 6, 17);
        gr().fillTextAtTile("1 PLAYER ONLY", ARCADE_CYAN, normalArcadeFont(), 8, 21);
        gr().fillTextAtTile("BONUS PAC-MAN FOR 10000", ARCADE_ROSE, normalArcadeFont(), 1, 25);
        gr().fillTextAtTile("PTS", ARCADE_ROSE, smallArcadeFont(), 25, 25);
        gr().fillTextAtTile("© 1980 MIDWAY MFG.CO.", ARCADE_PINK, normalArcadeFont(), 4, 29);
        gr().fillText("CREDIT %2d".formatted(theCoinMechanism().numCoins()),
                scoreColor(), normalArcadeFont(), 2 * TS, sizeInPx().y() - 2);
        gr().drawLevelCounter(theGame().levelCounter(), sizeInPx());
    }
}