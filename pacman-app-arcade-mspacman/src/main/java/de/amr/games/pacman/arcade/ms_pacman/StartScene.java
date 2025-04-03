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
        GameSpriteSheet spriteSheet = gr.spriteSheet();
        Color orange = Color.web(Arcade.Palette.ORANGE), red = Color.web(Arcade.Palette.RED), white = Color.web(Arcade.Palette.WHITE);
        Font font8 = gr.scaledArcadeFont(8), font6 = gr.scaledArcadeFont(6);
        gr.drawText("PUSH START BUTTON", orange, font8, tiles2Px(6), tiles2Px(16));
        gr.drawText("1 PLAYER ONLY", orange, font8, tiles2Px(8), tiles2Px(18));
        gr.drawText("ADDITIONAL    AT 10000", orange, font8, tiles2Px(2), tiles2Px(25));
        gr.drawSpriteScaled(spriteSheet.livesCounterSprite(), tiles2Px(13), tiles2Px(23) + 1);
        gr.drawText("PTS", orange, font6, tiles2Px(25), tiles2Px(25));
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) {
            r.drawMsPacManMidwayCopyright(tiles2Px(6), tiles2Px(28), red, gr.scaledArcadeFont(TS));
        }
        gr.drawText("CREDIT %2d".formatted(THE_GAME_CONTROLLER.credit), white, gr.scaledArcadeFont(TS),
            tiles2Px(2), sizeInPx().y() - 2);
        gr.drawLevelCounter(sizeInPx().x() - tiles2Px(4), sizeInPx().y() - tiles2Px(2));
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_UI.sound().playInsertCoinSound();
    }
}