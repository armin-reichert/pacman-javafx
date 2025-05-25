/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.theCoinMechanism;
import static de.amr.pacmanfx.Globals.theGame;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.GameAssets.ARCADE_ORANGE;
import static de.amr.pacmanfx.ui.GameAssets.ARCADE_RED;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;

public class ArcadeMsPacMan_StartScene extends GameScene2D {

    @Override
    public void doInit() {
        theGame().scoreManager().setScoreVisible(true);
        bindArcadeInsertCoinAction();
        bindArcadeStartGameAction();
    }

    @Override
    public void update() {
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        Font font6 = theAssets().arcadeFontAtSize(scaled(6));
        gr().fillCanvas(backgroundColor());
        gr().drawScores(theGame().scoreManager(), scoreColor(), defaultSceneFont());
        gr().fillTextAtScaledTilePosition("PUSH START BUTTON", ARCADE_ORANGE, defaultSceneFont(), 6, 16);
        gr().fillTextAtScaledTilePosition("1 PLAYER ONLY", ARCADE_ORANGE, defaultSceneFont(), 8, 18);
        gr().fillTextAtScaledTilePosition("ADDITIONAL    AT 10000", ARCADE_ORANGE, defaultSceneFont(), 2, 25);
        gr().drawSpriteScaled(gr().spriteSheet().livesCounterSprite(), tiles_to_px(13), tiles_to_px(23) + 1); //TODO check this
        gr().fillTextAtScaledTilePosition("PTS", ARCADE_ORANGE, font6, 25, 25);
        if (gr() instanceof ArcadeMsPacMan_GameRenderer r) {
            r.drawMidwayCopyright(6, 28, ARCADE_RED, defaultSceneFont());
        }
        gr().fillTextAtScaledPosition("CREDIT %2d".formatted(theCoinMechanism().numCoins()),
                scoreColor(), defaultSceneFont(), tiles_to_px(2), sizeInPx().y() - 2);
        gr().drawLevelCounter(theGame().levelCounter(), sizeInPx());
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        theSound().playInsertCoinSound();
    }
}