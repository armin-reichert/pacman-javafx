/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.model.pacman.PacManGame.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui2d.scene.pacman.PacManGameSpriteSheet.MIDWAY_COPYRIGHT;

/**
 * @author Armin Reichert
 */
public class StartScene extends GameScene2D {

    @Override
    public void bindGameActions() {
        bind(GameActions2D.ADD_CREDIT, context.arcade().key(Arcade.Button.COIN));
        bind(GameActions2D.START_GAME, context.arcade().key(Arcade.Button.START));
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
        var font8 = renderer.scaledArcadeFont(8);
        var font6 = renderer.scaledArcadeFont(6);
        Color cyan = Color.valueOf(Arcade.Palette.CYAN);
        Color orange = Color.valueOf(Arcade.Palette.ORANGE);
        Color pink = Color.valueOf(Arcade.Palette.PINK);
        Color rose = Color.valueOf(Arcade.Palette.ROSE);
        Color white = Color.valueOf(Arcade.Palette.WHITE);
        renderer.drawText("PUSH START BUTTON", orange, font8, t(6), t(17));
        renderer.drawText("1 PLAYER ONLY", cyan, font8, t(8), t(21));
        renderer.drawText("BONUS PAC-MAN FOR 10000", rose, font8, t(1), t(25));
        renderer.drawText("PTS", rose, font6, t(25), t(25));
        if (context.gameVariant() == GameVariant.PACMAN) {
            renderer.drawText(MIDWAY_COPYRIGHT, pink, font8, t(4), t(29));
        }
        renderer.drawText("CREDIT %2d".formatted(context.gameController().coinControl().credit()), white, font8, 2 * TS, size().y() - 2);
        renderer.drawLevelCounter(context, size().x() - 4 * TS, size().y() - 2 * TS);
    }
}