/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui.ActionBindingSupport;
import de.amr.pacmanfx.ui._2d.GameScene2D;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.*;
import static de.amr.pacmanfx.ui.PacManGames.theSound;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D implements ActionBindingSupport {

    @Override
    public void doInit() {
        theGame().hud().showScore(true);
        theGame().hud().showLevelCounter(true);
        theGame().hud().showLivesCounter(false);
        bindAction(ACTION_ARCADE_INSERT_COIN, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_ARCADE_START_GAME,  COMMON_ACTION_BINDINGS);
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
        gr().fillTextAtScaledTilePosition("PUSH START BUTTON", ARCADE_ORANGE, arcadeFont8(), 6, 17);
        gr().fillTextAtScaledTilePosition("1 PLAYER ONLY", ARCADE_CYAN, arcadeFont8(), 8, 21);
        gr().fillTextAtScaledTilePosition("BONUS PAC-MAN FOR 10000", ARCADE_ROSE, arcadeFont8(), 1, 25);
        gr().fillTextAtScaledTilePosition("PTS", ARCADE_ROSE, arcadeFont6(), 25, 25);
        gr().fillTextAtScaledTilePosition("© 1980 MIDWAY MFG.CO.", ARCADE_PINK, arcadeFont8(), 4, 29);
        gr().fillTextAtScaledPosition("CREDIT %2d".formatted(theCoinMechanism().numCoins()),
                ARCADE_WHITE, arcadeFont8(), 2 * TS, sizeInPx().y() - 2);
    }
}