/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.model.world.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

public class ArcadePacMan_StartScene_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private final BaseDebugInfoRenderer debugRenderer;

    public ArcadePacMan_StartScene_Renderer(AbstractGameScene2D scene, Canvas canvas) {
        super(canvas);
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public void draw(AbstractGameScene2D scene, long tick) {
        clearCanvas();

        fillText("PUSH START BUTTON",       ARCADE_ORANGE, arcadeFont8(), tilesPx(6),  tilesPx(17));
        fillText("1 PLAYER ONLY",           ARCADE_CYAN,   arcadeFont8(), tilesPx(8),  tilesPx(21));
        fillText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE,   arcadeFont8(), tilesPx(1),  tilesPx(25));
        fillText("PTS",                     ARCADE_ROSE,   arcadeFont6(), tilesPx(25), tilesPx(25));
        fillText("© 1980 MIDWAY MFG.CO.",   ARCADE_PINK,   arcadeFont8(), tilesPx(4),  tilesPx(29));

        if (scene.game().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene, tick);
        }
    }
}
