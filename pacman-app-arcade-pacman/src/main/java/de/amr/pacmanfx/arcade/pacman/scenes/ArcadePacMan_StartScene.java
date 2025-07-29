/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;

import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.GameUI.DEFAULT_ACTION_BINDINGS;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_ARCADE_INSERT_COIN;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_ARCADE_START_GAME;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D {

    public ArcadePacMan_StartScene(GameUI ui) {
        super(ui);
    }

    @Override
    public void doInit() {
        gameContext().theGame().theHUD().credit(true).score(true).levelCounter(true).livesCounter(false);
        actionBindings.use(ACTION_ARCADE_INSERT_COIN, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_ARCADE_START_GAME, DEFAULT_ACTION_BINDINGS);
    }

    @Override
    protected void doEnd() {
    }

    @Override
    public void update() {}

    @Override
    public void onCreditAdded(GameEvent e) {
        ui.theSound().play(SoundID.COIN_INSERTED);
    }

    @Override
    public Vector2f sizeInPx() { return ARCADE_MAP_SIZE_IN_PIXELS; }

    @Override
    public void drawSceneContent() {
        gameRenderer.fillTextAtScaledTilePosition("PUSH START BUTTON", ARCADE_ORANGE, scaledArcadeFont8(), 6, 17);
        gameRenderer.fillTextAtScaledTilePosition("1 PLAYER ONLY", ARCADE_CYAN, scaledArcadeFont8(), 8, 21);
        gameRenderer.fillTextAtScaledTilePosition("BONUS PAC-MAN FOR 10000", ARCADE_ROSE, scaledArcadeFont8(), 1, 25);
        gameRenderer.fillTextAtScaledTilePosition("PTS", ARCADE_ROSE, scaledArcadeFont6(), 25, 25);
        gameRenderer.fillTextAtScaledTilePosition("© 1980 MIDWAY MFG.CO.", ARCADE_PINK, scaledArcadeFont8(), 4, 29);
    }
}