/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_GameRenderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.ActionBindingSupport;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static de.amr.pacmanfx.Globals.theCoinMechanism;
import static de.amr.pacmanfx.Globals.theGame;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.ARCADE_ORANGE;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.ARCADE_RED;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.ui.PacManGames.theSound;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;

public class ArcadeMsPacMan_StartScene extends GameScene2D implements ActionBindingSupport {

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
        @SuppressWarnings("unchecked") SpriteSheet<SpriteID> spriteSheet = (SpriteSheet<SpriteID>) theUI().configuration().spriteSheet();

        gr().fillTextAtTile("PUSH START BUTTON", ARCADE_ORANGE, arcadeFont8(), 6, 16);
        gr().fillTextAtTile("1 PLAYER ONLY", ARCADE_ORANGE, arcadeFont8(), 8, 18);
        gr().fillTextAtTile("ADDITIONAL    AT 10000", ARCADE_ORANGE, arcadeFont8(), 2, 25);
        gr().drawSpriteScaled(spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL), tiles_to_px(13), tiles_to_px(23) + 1);
        gr().fillTextAtTile("PTS", ARCADE_ORANGE, arcadeFont6(), 25, 25);
        if (gr() instanceof ArcadeMsPacMan_GameRenderer r) {
            r.drawMsPacManCopyrightAtTile(ARCADE_RED, arcadeFont8(), 6, 28);
        }
        gr().fillText("CREDIT %2d".formatted(theCoinMechanism().numCoins()),
                scoreColor(), arcadeFont8(), tiles_to_px(2), sizeInPx().y() - 2);
        gr().drawActor(theGame().levelCounter());
    }
}