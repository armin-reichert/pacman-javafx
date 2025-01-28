/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.ui2d.action.GameActions2D;
import de.amr.games.pacman.ui2d.assets.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.tiles2Px;
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
        MsPacManGameRenderer r = (MsPacManGameRenderer) gr;
        GameSpriteSheet spriteSheet = r.spriteSheet();
        Color orange = Color.valueOf(Arcade.Palette.ORANGE), red = Color.valueOf(Arcade.Palette.RED), white = Color.valueOf(Arcade.Palette.WHITE);
        Font font8 = r.scaledArcadeFont(8), font6 = r.scaledArcadeFont(6);
        r.drawText("PUSH START BUTTON", orange, font8, tiles2Px(6), tiles2Px(16));
        r.drawText("1 PLAYER ONLY", orange, font8, tiles2Px(8), tiles2Px(18));
        r.drawText("ADDITIONAL    AT 10000", orange, font8, tiles2Px(2), tiles2Px(25));
        r.drawSpriteScaled(spriteSheet.livesCounterSprite(), tiles2Px(13), tiles2Px(23) + 1);
        r.drawText("PTS", orange, font6, tiles2Px(25), tiles2Px(25));
        r.drawMsPacManMidwayCopyright(tiles2Px(6), tiles2Px(28), red, r.scaledArcadeFont(TS));
        r.drawText("CREDIT %2d".formatted(context.gameController().credit), white, r.scaledArcadeFont(TS),
            tiles2Px(2), size().y() - 2);
        r.drawLevelCounter(context, size().x() - tiles2Px(4), size().y() - tiles2Px(2));
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        context.sound().playInsertCoinSound();
    }
}