/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.bootscene;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_EntityRenderer;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;

public class TengenMsPacMan_BootScene_Renderer extends BaseRenderer {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";

    private final TengenMsPacMan_EntityRenderer actorRenderer;

    public TengenMsPacMan_BootScene_Renderer(
        GameVariantRenderConfig renderConfig, GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);

        final SceneCanvasRenderingComp r2D = gameScene.reqComp(SceneCanvasRenderingComp.class);
        actorRenderer = r2D.configureRenderer((TengenMsPacMan_EntityRenderer) renderConfig.createEntityRenderer(animSystem, canvas));
        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
    }

    @Override
    public void render(Renderable r, long tick) {
        if (!(r instanceof TengenMsPacMan_BootScene bootScene)) {
            return;
        }

        if (bootScene.gray) {
            fillCanvas(NES_Palette.color(0x10));
        }
        else {
            final Font arcade8 = Ufx.deriveFont(GlobalAssets.Fonts.ARCADE.font(), scaled(8));
            fillText(TENGEN_PRESENTS, bootScene.shadeOfBlue, arcade8,
                bootScene.movingText.pos().x(),
                bootScene.movingText.pos().y());
            actorRenderer.render(bootScene.ghost, tick);
        }
    }
}