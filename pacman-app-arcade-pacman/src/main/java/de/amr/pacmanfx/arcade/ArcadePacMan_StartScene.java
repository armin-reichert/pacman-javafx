/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.UsefulFunctions;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.GameAssets.*;
import static de.amr.pacmanfx.ui.PacManGamesEnvironment.THE_ASSETS;
import static de.amr.pacmanfx.ui.PacManGamesEnvironment.THE_SOUND;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D {

    @Override
    public void bindActions() {
        bindArcadeInsertCoinAction();
        bindArcadeStartGameAction();
    }

    @Override
    public void doInit() {
        game().scoreManager().setScoreVisible(true);
    }

    @Override
    public void update() {}

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        final Font font8 = arcadeFontScaledTS();
        final Font font6 = THE_ASSETS.arcadeFontAtSize(scaled(6));
        gr.drawScores(game().scoreManager(), ARCADE_WHITE, font8);
        gr.fillTextAtScaledPosition("PUSH START BUTTON", ARCADE_ORANGE, font8, UsefulFunctions.tiles_to_px(6), UsefulFunctions.tiles_to_px(17));
        gr.fillTextAtScaledPosition("1 PLAYER ONLY", ARCADE_CYAN, font8, UsefulFunctions.tiles_to_px(8), UsefulFunctions.tiles_to_px(21));
        gr.fillTextAtScaledPosition("BONUS PAC-MAN FOR 10000", ARCADE_ROSE, font8, UsefulFunctions.tiles_to_px(1), UsefulFunctions.tiles_to_px(25));
        gr.fillTextAtScaledPosition("PTS", ARCADE_ROSE, font6, UsefulFunctions.tiles_to_px(25), UsefulFunctions.tiles_to_px(25));
        if (gr instanceof ArcadePacMan_GameRenderer r) {
            r.drawMidwayCopyright(4, 29, ARCADE_PINK, font8);
        }
        gr.fillTextAtScaledPosition("CREDIT %2d".formatted(THE_COIN_MECHANISM.numCoins()), ARCADE_WHITE, font8, 2 * TS, sizeInPx().y() - 2);
        gr.drawLevelCounter(game().levelCounter(), sizeInPx());
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_SOUND.playInsertCoinSound();
    }
}