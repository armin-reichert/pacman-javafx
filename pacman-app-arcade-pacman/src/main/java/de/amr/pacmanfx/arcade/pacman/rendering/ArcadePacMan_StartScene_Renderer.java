/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.arcade.pacman.scenes.ArcadePacMan_StartScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

public class ArcadePacMan_StartScene_Renderer extends BaseRenderer {

    private final BaseGameSceneDebugInfoRenderer debugRenderer;

    public ArcadePacMan_StartScene_Renderer(GameScene scene, Canvas canvas) {
        super(canvas);
        debugRenderer = createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof ArcadePacMan_StartScene startScene)){
            return;
        }

        fillText("PUSH START BUTTON",       ARCADE_ORANGE, arcadeFont8(), tilesPx(6),  tilesPx(17));
        fillText("1 PLAYER ONLY",           ARCADE_CYAN,   arcadeFont8(), tilesPx(8),  tilesPx(21));
        fillText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE,   arcadeFont8(), tilesPx(1),  tilesPx(25));
        fillText("PTS",                     ARCADE_ROSE,   arcadeFont6(), tilesPx(25), tilesPx(25));
        fillText("© 1980 MIDWAY MFG.CO.",   ARCADE_PINK,   arcadeFont8(), tilesPx(4),  tilesPx(29));

        if (startScene.viewModel().debugModeOnProperty().get()) {
            debugRenderer.render(startScene, tick);
        }
    }
}
