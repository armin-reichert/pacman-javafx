/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui.PacManGames_Actions;
import de.amr.pacmanfx.ui._2d.GameScene2D;

import static de.amr.pacmanfx.Globals.theCoinMechanism;
import static de.amr.pacmanfx.Globals.theGame;
import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.arcade.ArcadePalette.ARCADE_ORANGE;
import static de.amr.pacmanfx.arcade.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.ui.PacManGames_Env.theSound;

public class ArcadeMsPacMan_StartScene extends GameScene2D {

    @Override
    public void doInit() {
        theGame().setScoreVisible(true);
        bindActionToCommonKeys(PacManGames_Actions.ARCADE_INSERT_COIN);
        bindActionToCommonKeys(PacManGames_Actions.ARCADE_START_GAME);
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
        gr().fillTextAtTile("PUSH START BUTTON", ARCADE_ORANGE, normalArcadeFont(), 6, 16);
        gr().fillTextAtTile("1 PLAYER ONLY", ARCADE_ORANGE, normalArcadeFont(), 8, 18);
        gr().fillTextAtTile("ADDITIONAL    AT 10000", ARCADE_ORANGE, normalArcadeFont(), 2, 25);
        gr().drawSpriteScaled(gr().spriteSheet().livesCounterSprite(), tiles_to_px(13), tiles_to_px(23) + 1); //TODO check this
        gr().fillTextAtTile("PTS", ARCADE_ORANGE, smallArcadeFont(), 25, 25);
        if (gr() instanceof ArcadeMsPacMan_GameRenderer r) {
            r.drawMsPacManCopyrightAtTile(ARCADE_RED, normalArcadeFont(), 6, 28);
        }
        gr().fillText("CREDIT %2d".formatted(theCoinMechanism().numCoins()),
                scoreColor(), normalArcadeFont(), tiles_to_px(2), sizeInPx().y() - 2);
        gr().drawLevelCounter(theGame().levelCounter(), sizeInPx());
    }
}