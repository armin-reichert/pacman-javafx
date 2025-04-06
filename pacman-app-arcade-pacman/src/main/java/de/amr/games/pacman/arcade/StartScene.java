/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.arcade.ArcadePacMan_SpriteSheet.MIDWAY_COPYRIGHT;
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
        Font font8 = THE_UI.assets().scaledArcadeFont(scaled(8));
        Font font6 = THE_UI.assets().scaledArcadeFont(scaled(6));
        gr.setScaling(scaling());
        gr.fillCanvas(backgroundColor());
        if (game().isScoreVisible()) {
            gr.drawScores(game().scoreManager(), Color.web(Arcade.Palette.WHITE), font8);
        }
        Color cyan = Color.web(Arcade.Palette.CYAN);
        Color orange = Color.web(Arcade.Palette.ORANGE);
        Color pink = Color.web(Arcade.Palette.PINK);
        Color rose = Color.web(Arcade.Palette.ROSE);
        Color white = Color.web(Arcade.Palette.WHITE);
        gr.drawText("PUSH START BUTTON", orange, font8, tiles_to_px(6), tiles_to_px(17));
        gr.drawText("1 PLAYER ONLY", cyan, font8, tiles_to_px(8), tiles_to_px(21));
        gr.drawText("BONUS PAC-MAN FOR 10000", rose, font8, tiles_to_px(1), tiles_to_px(25));
        gr.drawText("PTS", rose, font6, tiles_to_px(25), tiles_to_px(25));
        if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.PACMAN)) {
            gr.drawText(MIDWAY_COPYRIGHT, pink, font8, tiles_to_px(4), tiles_to_px(29));
        }
        gr.drawText("CREDIT %2d".formatted(THE_COIN_STORE.numCoins()), white, font8, 2 * TS, sizeInPx().y() - 2);
        gr.drawLevelCounter(0, game().levelCounter(), sizeInPx().x() - 4 * TS, sizeInPx().y() - 2 * TS);
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_UI.sound().playInsertCoinSound();
    }
}