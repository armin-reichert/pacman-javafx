/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;

public class ArcadePacMan_StartScene_Renderer extends GameScene2DRenderer {

    public ArcadePacMan_StartScene_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);
    }

    public void draw() {
        fillText("PUSH START BUTTON",       ARCADE_ORANGE, arcadeFont8(), TS(6),  TS(17));
        fillText("1 PLAYER ONLY",           ARCADE_CYAN,   arcadeFont8(), TS(8),  TS(21));
        fillText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE,   arcadeFont8(), TS(1),  TS(25));
        fillText("PTS",                     ARCADE_ROSE,   arcadeFont6(), TS(25), TS(25));
        fillText("© 1980 MIDWAY MFG.CO.",   ARCADE_PINK,   arcadeFont8(), TS(4),  TS(29));
    }
}
