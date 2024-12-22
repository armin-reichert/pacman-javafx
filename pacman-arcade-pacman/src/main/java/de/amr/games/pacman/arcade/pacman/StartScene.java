/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.action.GameActions2D;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.arcade.pacman.PacManGameSpriteSheet.MIDWAY_COPYRIGHT;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.toPx;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;

/**
 * @author Armin Reichert
 */
public class StartScene extends GameScene2D {

    @Override
    public void bindGameActions() {
        bind(GameActions2D.INSERT_COIN, context.arcadeKeys().key(Arcade.Button.COIN));
        bind(GameActions2D.START_GAME, context.arcadeKeys().key(Arcade.Button.START));
    }

    @Override
    public void doInit() {
        context.setScoreVisible(true);
    }

    @Override
    public void update() {
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        var font8 = gr.scaledArcadeFont(8);
        var font6 = gr.scaledArcadeFont(6);
        Color cyan = Color.valueOf(Arcade.Palette.CYAN);
        Color orange = Color.valueOf(Arcade.Palette.ORANGE);
        Color pink = Color.valueOf(Arcade.Palette.PINK);
        Color rose = Color.valueOf(Arcade.Palette.ROSE);
        Color white = Color.valueOf(Arcade.Palette.WHITE);
        gr.drawText("PUSH START BUTTON", orange, font8, toPx(6), toPx(17));
        gr.drawText("1 PLAYER ONLY", cyan, font8, toPx(8), toPx(21));
        gr.drawText("BONUS PAC-MAN FOR 10000", rose, font8, toPx(1), toPx(25));
        gr.drawText("PTS", rose, font6, toPx(25), toPx(25));
        if (context.gameVariant() == GameVariant.PACMAN) {
            gr.drawText(MIDWAY_COPYRIGHT, pink, font8, toPx(4), toPx(29));
        }
        gr.drawText("CREDIT %2d".formatted(context.gameController().coinControl().credit()), white, font8, 2 * TS, size().y() - 2);
        gr.drawLevelCounter(context, size().x() - 4 * TS, size().y() - 2 * TS);
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        context.sound().playInsertCoinSound();
    }
}