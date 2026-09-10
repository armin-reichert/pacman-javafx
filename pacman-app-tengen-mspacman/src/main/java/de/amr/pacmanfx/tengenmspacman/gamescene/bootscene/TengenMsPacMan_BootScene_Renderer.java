/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.bootscene;

import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;

public class TengenMsPacMan_BootScene_Renderer extends BaseRenderer {

    public TengenMsPacMan_BootScene_Renderer(GameScene gameScene, Canvas canvas) {
        super(canvas);
        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
    }

    @Override
    public void render(Renderable r, long tick) {
        if (!(r instanceof TengenMsPacMan_BootScene bootScene)) {
            return;
        }
        if (bootScene.gray) {
            // TODO let boot scene produce renderable for gray screen fill
            fillCanvas(NES_Palette.color(0x10));
        }
    }
}