/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.Globals.THE_ASSETS;
import static de.amr.games.pacman.ui.Globals.THE_SOUND;
import static de.amr.games.pacman.uilib.input.Keyboard.naked;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D {

    public static final Color CYAN = Color.web(Arcade.Palette.CYAN);
    public static final Color ORANGE = Color.web(Arcade.Palette.ORANGE);
    public static final Color PINK = Color.web(Arcade.Palette.PINK);
    public static final Color ROSE = Color.web(Arcade.Palette.ROSE);
    public static final Color WHITE = Color.web(Arcade.Palette.WHITE);

    @Override
    public void bindActions() {
        bind(GameAction.INSERT_COIN,  naked(KeyCode.DIGIT5), naked(KeyCode.NUMPAD5));
        bind(GameAction.START_GAME,   naked(KeyCode.DIGIT1), naked(KeyCode.NUMPAD1));
    }

    @Override
    public void doInit() {
        game().scoreVisibleProperty().set(true);
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
        gr.fillCanvas(backgroundColor());
        if (game().isScoreVisible()) {
            gr.drawScores(game(), Color.web(Arcade.Palette.WHITE), font8);
        }
        gr.fillTextAtScaledPosition("PUSH START BUTTON", ORANGE, font8, tiles_to_px(6), tiles_to_px(17));
        gr.fillTextAtScaledPosition("1 PLAYER ONLY", CYAN, font8, tiles_to_px(8), tiles_to_px(21));
        gr.fillTextAtScaledPosition("BONUS PAC-MAN FOR 10000", ROSE, font8, tiles_to_px(1), tiles_to_px(25));
        gr.fillTextAtScaledPosition("PTS", ROSE, font6, tiles_to_px(25), tiles_to_px(25));
        if (gr instanceof ArcadePacMan_GameRenderer r) {
            r.drawMidwayCopyright(4, 29, PINK, font8);
        }
        gr.fillTextAtScaledPosition("CREDIT %2d".formatted(THE_COIN_MECHANISM.numCoins()), WHITE, font8, 2 * TS, sizeInPx().y() - 2);
        gr.drawLevelCounter(game().levelCounter(), sizeInPx());
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_SOUND.playInsertCoinSound();
    }
}