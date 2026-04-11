/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

public class ArcadePacMan_StartScene_Renderer extends BaseRenderer implements GameScene2D_Renderer {

    private final BaseDebugInfoRenderer debugRenderer;

    public ArcadePacMan_StartScene_Renderer(GameScene2D scene, Canvas canvas) {
        super(canvas);
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    public void draw(GameScene2D scene) {
        clearCanvas();

        fillText("PUSH START BUTTON",       ARCADE_ORANGE, arcadeFont8(), TS(6),  TS(17));
        fillText("1 PLAYER ONLY",           ARCADE_CYAN,   arcadeFont8(), TS(8),  TS(21));
        fillText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE,   arcadeFont8(), TS(1),  TS(25));
        fillText("PTS",                     ARCADE_ROSE,   arcadeFont6(), TS(25), TS(25));
        fillText("© 1980 MIDWAY MFG.CO.",   ARCADE_PINK,   arcadeFont8(), TS(4),  TS(29));

        if (GameUI.PROPERTY_DEBUG_INFO_VISIBLE.get()) {
            debugRenderer.draw(scene);
        }
    }
}
