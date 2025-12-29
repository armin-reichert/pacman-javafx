/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;

public class ArcadePacMan_StartScene_Renderer extends GameScene2D_Renderer {

    public ArcadePacMan_StartScene_Renderer(UIPreferences prefs, GameScene2D scene, Canvas canvas) {
        super(canvas);
        createDefaultDebugInfoRenderer(prefs, scene, canvas);
    }

    public void draw(GameScene2D scene) {
        clearCanvas();

        fillText("PUSH START BUTTON",       ARCADE_ORANGE, arcadeFont8(), TS(6),  TS(17));
        fillText("1 PLAYER ONLY",           ARCADE_CYAN,   arcadeFont8(), TS(8),  TS(21));
        fillText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE,   arcadeFont8(), TS(1),  TS(25));
        fillText("PTS",                     ARCADE_ROSE,   arcadeFont6(), TS(25), TS(25));
        fillText("© 1980 MIDWAY MFG.CO.",   ARCADE_PINK,   arcadeFont8(), TS(4),  TS(29));

        if (scene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }
}
