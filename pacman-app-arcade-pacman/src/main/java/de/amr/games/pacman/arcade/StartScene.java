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
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.arcade.ArcadePacMan_SpriteSheet.MIDWAY_COPYRIGHT;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.Globals.THE_ASSETS;
import static de.amr.games.pacman.ui.Globals.THE_SOUND;
import static de.amr.games.pacman.uilib.Keyboard.naked;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class StartScene extends GameScene2D {

    public static final Color CYAN = Color.web(Arcade.Palette.CYAN);
    public static final Color ORANGE = Color.web(Arcade.Palette.ORANGE);
    public static final Color PINK = Color.web(Arcade.Palette.PINK);
    public static final Color ROSE = Color.web(Arcade.Palette.ROSE);
    public static final Color WHITE = Color.web(Arcade.Palette.WHITE);

    @Override
    public void bindGameActions() {
        bind(GameAction.INSERT_COIN,  naked(KeyCode.DIGIT5), naked(KeyCode.NUMPAD5));
        bind(GameAction.START_GAME,   naked(KeyCode.DIGIT1), naked(KeyCode.NUMPAD1));
    }

    @Override
    public void doInit() {
        game().setScoreVisible(true);
    }

    @Override
    public void update() {}

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        Font font8 = THE_ASSETS.arcadeFontAtSize(scaled(8));
        Font font6 = THE_ASSETS.arcadeFontAtSize(scaled(6));
        gr.setScaling(scaling());
        gr.fillCanvas(backgroundColor());
        if (game().isScoreVisible()) {
            gr.drawScores(game().scoreManager(), Color.web(Arcade.Palette.WHITE), font8);
        }
        gr.fillTextAtScaledPosition("PUSH START BUTTON", ORANGE, font8, tiles_to_px(6), tiles_to_px(17));
        gr.fillTextAtScaledPosition("1 PLAYER ONLY", CYAN, font8, tiles_to_px(8), tiles_to_px(21));
        gr.fillTextAtScaledPosition("BONUS PAC-MAN FOR 10000", ROSE, font8, tiles_to_px(1), tiles_to_px(25));
        gr.fillTextAtScaledPosition("PTS", ROSE, font6, tiles_to_px(25), tiles_to_px(25));
        if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.PACMAN)) {
            gr.fillTextAtScaledPosition(MIDWAY_COPYRIGHT, PINK, font8, tiles_to_px(4), tiles_to_px(29));
        }
        gr.fillTextAtScaledPosition("CREDIT %2d".formatted(THE_COIN_STORE.numCoins()), WHITE, font8, 2 * TS, sizeInPx().y() - 2);
        gr.drawLevelCounter(0, game().levelCounter(), sizeInPx().x() - 4 * TS, sizeInPx().y() - 2 * TS);
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_SOUND.playInsertCoinSound();
    }
}