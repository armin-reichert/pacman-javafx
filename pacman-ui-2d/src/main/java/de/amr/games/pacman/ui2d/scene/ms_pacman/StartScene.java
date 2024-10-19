/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.pacman.PacManArcadeGame;
import de.amr.games.pacman.ui2d.GlobalGameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui2d.GameAssets2D.*;

/**
 * @author Armin Reichert
 */
public class StartScene extends GameScene2D {

    @Override
    public void init() {
        context.setScoreVisible(true);
    }

    @Override
    public void end() {
    }

    @Override
    public void update() {
    }

    @Override
    public void handleInput() {
        context.doFirstCalledAction(GlobalGameActions2D.ADD_CREDIT, GlobalGameActions2D.START_GAME);
    }

    @Override
    public Vector2f size() {
        return new Vector2f(PacManArcadeGame.ARCADE_MAP_SIZE_X, PacManArcadeGame.ARCADE_MAP_SIZE_Y);
    }

    @Override
    public void drawSceneContent(GameRenderer renderer) {
        MsPacManGameRenderer r = (MsPacManGameRenderer) renderer;
        GameSpriteSheet spriteSheet = r.spriteSheet();
        Font font8 = r.scaledArcadeFont(8), font6 = r.scaledArcadeFont(6);
        r.drawText("PUSH START BUTTON", ARCADE_ORANGE, font8, t(6), t(16));
        r.drawText("1 PLAYER ONLY", ARCADE_ORANGE, font8, t(8), t(18));
        r.drawText("ADDITIONAL    AT 10000", ARCADE_ORANGE, font8, t(2), t(25));
        r.drawSpriteScaled(spriteSheet.livesCounterSprite(), t(13), t(23) + 1);
        r.drawText("PTS", ARCADE_ORANGE, font6, t(25), t(25));
        r.drawMsPacManMidwayCopyright(t(6), t(28), ARCADE_RED, r.scaledArcadeFont(TS));
        r.drawText("CREDIT %2d".formatted(context.gameController().coinControl().credit()), ARCADE_PALE, renderer.scaledArcadeFont(TS),
            2 * TS, size().y() - 2);
        r.drawLevelCounter(context.game().levelNumber(), context.game().isDemoLevel(),
            context.game().levelCounter(), size());
    }
}