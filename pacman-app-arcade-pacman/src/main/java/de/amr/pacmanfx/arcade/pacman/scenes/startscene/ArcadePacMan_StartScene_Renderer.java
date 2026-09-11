/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes.startscene;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.rendering.Renderable;
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
    }
}
