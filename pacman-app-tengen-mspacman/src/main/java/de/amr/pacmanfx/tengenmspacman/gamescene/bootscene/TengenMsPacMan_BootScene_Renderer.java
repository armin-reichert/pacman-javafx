/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.bootscene;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;

public class TengenMsPacMan_BootScene_Renderer extends BaseRenderer {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";

    private final Renderer entityRenderer;

    public TengenMsPacMan_BootScene_Renderer(
        GameVariantRenderConfig renderConfig, GameScene gameScene, ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);

        // set scaling and background color binding
        final SceneCanvasRenderingComp canvasRendering = gameScene.reqComp(SceneCanvasRenderingComp.class);
        entityRenderer = canvasRendering.configureRenderer(renderConfig.createEntityRenderer(animSystem, canvas));

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
        else {
            //TODO let boot scene produce renderable for color changing and moving text
            final Font arcade8 = Ufx.deriveFont(GlobalAssets.Fonts.ARCADE.font(), scaled(8));
            fillText(TENGEN_PRESENTS, bootScene.shadeOfBlue, arcade8, bootScene.movingText.pos().x(), bootScene.movingText.pos().y());

            //TODO let boot scene produce renderable for moving ghost
            entityRenderer.render(bootScene.ghost, tick);
        }
    }
}