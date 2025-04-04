/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.ui._2d.GameAction;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.ui._2d.GameSpriteSheet;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.Globals.THE_UI;

/**
 * @author Armin Reichert
 */
public class StartScene extends GameScene2D {

    @Override
    public void bindGameActions() {
        bind(GameAction.INSERT_COIN, THE_UI.keyboard().currentArcadeKeyBinding().key(Arcade.Button.COIN));
        bind(GameAction.START_GAME, THE_UI.keyboard().currentArcadeKeyBinding().key(Arcade.Button.START));
    }

    @Override
    public void doInit() {
        game().setScoreVisible(true);
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
        Font font = THE_UI.assets().scaledArcadeFont(scaled(TS));
        Font font6 = THE_UI.assets().scaledArcadeFont(scaled(6));
        gr.setScaling(scaling());
        gr.fillCanvas(backgroundColor());
        if (game().isScoreVisible()) {
            gr.drawScores(game().scoreManager(), Color.web(Arcade.Palette.WHITE), font);
        }
        GameSpriteSheet spriteSheet = gr.spriteSheet();
        Color orange = Color.web(Arcade.Palette.ORANGE), red = Color.web(Arcade.Palette.RED), white = Color.web(Arcade.Palette.WHITE);
        gr.drawText("PUSH START BUTTON", orange, font, tiles_to_px(6), tiles_to_px(16));
        gr.drawText("1 PLAYER ONLY", orange, font, tiles_to_px(8), tiles_to_px(18));
        gr.drawText("ADDITIONAL    AT 10000", orange, font, tiles_to_px(2), tiles_to_px(25));
        gr.drawSpriteScaled(spriteSheet.livesCounterSprite(), tiles_to_px(13), tiles_to_px(23) + 1);
        gr.drawText("PTS", orange, font6, tiles_to_px(25), tiles_to_px(25));
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) {
            r.drawMsPacManMidwayCopyright(tiles_to_px(6), tiles_to_px(28), red, font);
        }
        gr.drawText("CREDIT %2d".formatted(THE_COIN_STORE.numCoins()), white, font,
            tiles_to_px(2), sizeInPx().y() - 2);
        gr.drawLevelCounter(0, game().levelCounter(), sizeInPx().x() - tiles_to_px(4), sizeInPx().y() - tiles_to_px(2));
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_UI.sound().playInsertCoinSound();
    }
}