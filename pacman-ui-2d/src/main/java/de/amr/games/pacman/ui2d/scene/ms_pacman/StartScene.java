/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.model.pacman.PacManArcadeGame.ARCADE_MAP_SIZE_IN_PIXELS;

/**
 * @author Armin Reichert
 */
public class StartScene extends GameScene2D {

    @Override
    public void bindGameActions() {
        bind(GameActions2D.ADD_CREDIT, context.arcade().keyCombination(Arcade.Controls.COIN));
        bind(GameActions2D.START_GAME, context.arcade().keyCombination(Arcade.Controls.START));
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
    public void drawSceneContent(GameRenderer renderer) {
        MsPacManArcadeGameRenderer r = (MsPacManArcadeGameRenderer) renderer;
        GameSpriteSheet spriteSheet = r.spriteSheet();
        Color orange = Color.valueOf(Arcade.Palette.ORANGE), red = Color.valueOf(Arcade.Palette.RED), white = Color.valueOf(Arcade.Palette.WHITE);
        Font font8 = r.scaledArcadeFont(8), font6 = r.scaledArcadeFont(6);
        r.drawText("PUSH START BUTTON", orange, font8, t(6), t(16));
        r.drawText("1 PLAYER ONLY", orange, font8, t(8), t(18));
        r.drawText("ADDITIONAL    AT 10000", orange, font8, t(2), t(25));
        r.drawSpriteScaled(spriteSheet.livesCounterSprite(), t(13), t(23) + 1);
        r.drawText("PTS", orange, font6, t(25), t(25));
        r.drawMsPacManMidwayCopyright(t(6), t(28), red, r.scaledArcadeFont(TS));
        r.drawText("CREDIT %2d".formatted(context.gameController().coinControl().credit()), white, renderer.scaledArcadeFont(TS),
            2 * TS, size().y() - 2);
        r.drawLevelCounter(context, size());
    }
}