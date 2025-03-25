/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.GameActions2D;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.arcade.ArcadePacMan_SpriteSheet.MIDWAY_COPYRIGHT;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.UIGlobals.THE_GAME_CONTEXT;
import static de.amr.games.pacman.ui.UIGlobals.THE_SOUND;

/**
 * @author Armin Reichert
 */
public class StartScene extends GameScene2D {

    @Override
    public void bindGameActions() {
        bind(GameActions2D.INSERT_COIN, THE_GAME_CONTEXT.arcadeKeys().key(Arcade.Button.COIN));
        bind(GameActions2D.START_GAME, THE_GAME_CONTEXT.arcadeKeys().key(Arcade.Button.START));
    }

    @Override
    public void doInit() {
        THE_GAME_CONTEXT.setScoreVisible(true);
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
        var font8 = gr.scaledArcadeFont(8);
        var font6 = gr.scaledArcadeFont(6);
        Color cyan = Color.web(Arcade.Palette.CYAN);
        Color orange = Color.web(Arcade.Palette.ORANGE);
        Color pink = Color.web(Arcade.Palette.PINK);
        Color rose = Color.web(Arcade.Palette.ROSE);
        Color white = Color.web(Arcade.Palette.WHITE);
        gr.drawText("PUSH START BUTTON", orange, font8, tiles2Px(6), tiles2Px(17));
        gr.drawText("1 PLAYER ONLY", cyan, font8, tiles2Px(8), tiles2Px(21));
        gr.drawText("BONUS PAC-MAN FOR 10000", rose, font8, tiles2Px(1), tiles2Px(25));
        gr.drawText("PTS", rose, font6, tiles2Px(25), tiles2Px(25));
        if (THE_GAME_CONTROLLER.selectedGameVariant() == GameVariant.PACMAN) {
            gr.drawText(MIDWAY_COPYRIGHT, pink, font8, tiles2Px(4), tiles2Px(29));
        }
        gr.drawText("CREDIT %2d".formatted(THE_GAME_CONTROLLER.credit), white, font8, 2 * TS, sizeInPx().y() - 2);
        gr.drawLevelCounter(sizeInPx().x() - 4 * TS, sizeInPx().y() - 2 * TS);
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_SOUND.playInsertCoinSound();
    }
}