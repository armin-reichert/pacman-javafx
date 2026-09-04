/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.GameSceneRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

public class ArcadePacMan_StartScene_Renderer extends GameSceneRenderer {

    public ArcadePacMan_StartScene_Renderer(GameScene gameScene, Canvas canvas) {
        super(canvas);
        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
    }

    @Override
    public void render(Object r, long tick) {
        final Font font8 = arcadeFont8();
        final Font font6 = arcadeFont6();
        fillText("PUSH START BUTTON",       ARCADE_ORANGE, font8, tilesPx(6),  tilesPx(17));
        fillText("1 PLAYER ONLY",           ARCADE_CYAN,   font8, tilesPx(8),  tilesPx(21));
        fillText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE,   font8, tilesPx(1),  tilesPx(25));
        fillText("PTS",                     ARCADE_ROSE,   font6, tilesPx(25), tilesPx(25));
        fillText("© 1980 MIDWAY MFG.CO.",   ARCADE_PINK,   font8, tilesPx(4),  tilesPx(29));
    }
}
