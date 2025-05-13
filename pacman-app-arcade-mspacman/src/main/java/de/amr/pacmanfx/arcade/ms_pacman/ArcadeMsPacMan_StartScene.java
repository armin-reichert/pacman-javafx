/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameSpriteSheet;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.theGame;
import static de.amr.pacmanfx.Globals.theCoinMechanism;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.GameAssets.*;
import static de.amr.pacmanfx.ui.PacManGamesEnv.theAssets;
import static de.amr.pacmanfx.ui.PacManGamesEnv.theSound;

/**
 * @author Armin Reichert
 */
public class ArcadeMsPacMan_StartScene extends GameScene2D {

    @Override
    public void bindActions() {
        bindArcadeInsertCoinAction();
        bindArcadeStartGameAction();
    }

    @Override
    public void doInit() {
        theGame().scoreManager().setScoreVisible(true);
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
        Font font = arcadeFontScaledTS();
        Font font6 = theAssets().arcadeFontAtSize(scaled(6));
        gr.fillCanvas(backgroundColor());
        gr.drawScores(theGame().scoreManager(), ARCADE_WHITE, font);
        GameSpriteSheet spriteSheet = gr.spriteSheet();
        gr.fillTextAtScaledTilePosition("PUSH START BUTTON", ARCADE_ORANGE, font, 6, 16);
        gr.fillTextAtScaledTilePosition("1 PLAYER ONLY", ARCADE_ORANGE, font, 8, 18);
        gr.fillTextAtScaledTilePosition("ADDITIONAL    AT 10000", ARCADE_ORANGE, font, 2, 25);
        gr.drawSpriteScaled(spriteSheet.livesCounterSprite(), tiles_to_px(13), tiles_to_px(23) + 1); //TODO check this
        gr.fillTextAtScaledTilePosition("PTS", ARCADE_ORANGE, font6, 25, 25);
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) {
            r.drawMidwayCopyright(6, 28, ARCADE_RED, font);
        }
        gr.fillTextAtScaledPosition("CREDIT %2d".formatted(theCoinMechanism().numCoins()), ARCADE_WHITE, font,
            tiles_to_px(2), sizeInPx().y() - 2);
        gr.drawLevelCounter(theGame().levelCounter(), sizeInPx());
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        theSound().playInsertCoinSound();
    }
}