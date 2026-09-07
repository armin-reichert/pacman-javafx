/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

public class ArcadePacMan_StartScene_Renderer extends BaseRenderer {

    public ArcadePacMan_StartScene_Renderer(GameScene gameScene, Canvas canvas) {
        super(canvas);
        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
    }

    @Override
    public void render(Renderable r, long tick) {
        final Font arcade6 = Ufx.deriveFont(GlobalAssets.Fonts.ARCADE.font(), scaled(6));
        final Font arcade8 = Ufx.deriveFont(GlobalAssets.Fonts.ARCADE.font(), scaled(8));
        fillText("PUSH START BUTTON",       ARCADE_ORANGE, arcade8, tilesPx(6),  tilesPx(17));
        fillText("1 PLAYER ONLY",           ARCADE_CYAN,   arcade8, tilesPx(8),  tilesPx(21));
        fillText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE,   arcade8, tilesPx(1),  tilesPx(25));
        fillText("PTS",                     ARCADE_ROSE,   arcade6, tilesPx(25), tilesPx(25));
        fillText("© 1980 MIDWAY MFG.CO.",   ARCADE_PINK,   arcade8, tilesPx(4),  tilesPx(29));
    }
}
