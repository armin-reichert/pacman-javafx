/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_IntroScene;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_IntroScene.SceneState;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.core.model.world.WorldMap.TS;
import static de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_IntroScene.MARQUEE_X;
import static de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_IntroScene.MARQUEE_Y;
import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_RenderConfig.shadeOfBlue;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_IntroScene_Renderer extends BaseRenderer
    implements GameScene2D_Renderer, SpriteRendererMixin, TengenMsPacMan_SceneRendererMixin {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";
    public static final String PRESS_START = "PRESS START";
    public static final String NAMCO_LTD = "MS PAC-MAN TM NAMCO LTD";
    public static final String TENGEN_INC = "©1990 TENGEN INC";
    public static final String ALL_RIGHTS_RESERVED = "ALL RIGHTS RESERVED";
    public static final String WITH = "WITH";
    public static final String STARRING = "STARRING";
    public static final String MS_PAC_MAN = "MS PAC-MAN";
    public static final String QUOTED_MS_PACMAN = "\"MS PAC-MAN\"";

    private final ActorRenderer actorRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    private final TengenMsPacMan_UISettings uiSettings;

    public TengenMsPacMan_IntroScene_Renderer(GameVariantRenderConfig renderConfig, AbstractGameScene2D scene, Canvas canvas) {
        super(canvas);
        requireNonNull(renderConfig);
        requireNonNull(scene);
        actorRenderer = scene.configureRenderer(renderConfig.createActorRenderer(canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
        uiSettings = scene.appContext().getExtensionValue(
            TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);
    }

    @Override
    public GameScene2D_Renderer renderer() {
        return this;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void draw(AbstractGameScene2D scene, long globalTick) {
        clearCanvas();

        final TengenMsPacMan_IntroScene intro = (TengenMsPacMan_IntroScene) scene;
        final State<TengenMsPacMan_IntroScene> introState = intro.flow.state();
        final long stateTick = intro.flow.state().timer().tickCount();

        ctx.setFont(arcadeFont8());
        ctx.setImageSmoothing(false);

        switch (introState) {

            case SceneState.WAITING_FOR_START -> {
                if (!intro.dark) {
                    final boolean bright = stateTick % 60 < 30; // 0.5s dark, 0.5s bright
                    fillText(TENGEN_PRESENTS, shadeOfBlue(stateTick), intro.presents.x(), intro.presents.y());
                    drawSprite(spriteSheet().findSprite(SpriteID.LARGE_MS_PAC_MAN_TEXT), 6 * TS, MARQUEE_Y, true);
                    if (bright) {
                        fillText(PRESS_START, NES_Palette.color(0x20), 11 * TS, MARQUEE_Y + 9 * TS);
                    }
                    fillText(NAMCO_LTD,           NES_Palette.color(0x25), 6 * TS, MARQUEE_Y + 15 * TS);
                    fillText(TENGEN_INC,          NES_Palette.color(0x25), 8 * TS, MARQUEE_Y + 16 * TS);
                    fillText(ALL_RIGHTS_RESERVED, NES_Palette.color(0x25), 7 * TS, MARQUEE_Y + 17 * TS);
                }
            }

            case SceneState.SHOWING_MARQUEE -> {
                intro.marquee.draw(ctx());
                fillText(QUOTED_MS_PACMAN, NES_Palette.color(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
            }

            case SceneState.GHOSTS_MARCHING_IN -> {
                intro.marquee.draw(ctx());
                fillText(QUOTED_MS_PACMAN, NES_Palette.color(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
                if (intro.ghostIndex == 0) {
                    fillText(WITH, NES_Palette.color(0x20), MARQUEE_X + 12, MARQUEE_Y + 23);
                }
                final Ghost currentGhost = intro.ghosts.get(intro.ghostIndex);
                final Color ghostColor = intro.ghostColors[currentGhost.personality()];
                fillText(currentGhost.name().toUpperCase(), ghostColor, MARQUEE_X + 44, MARQUEE_Y + 41);
                intro.ghosts.forEach(actorRenderer::drawActor);
            }

            case SceneState.MS_PACMAN_MARCHING_IN -> {
                intro.marquee.draw(ctx());
                fillText(QUOTED_MS_PACMAN, NES_Palette.color(0x28), MARQUEE_X + 20, MARQUEE_Y - 18);
                fillText(STARRING, NES_Palette.color(0x20), MARQUEE_X + 12, MARQUEE_Y + 22);
                fillText(MS_PAC_MAN, NES_Palette.color(0x28), MARQUEE_X + 28, MARQUEE_Y + 38);
                intro.ghosts.forEach(actorRenderer::drawActor);
                actorRenderer.drawActor(intro.msPacMan);
            }

            default -> {}
        }

        if (uiSettings.joypadBindingsDisplayed.get()) {
            drawJoypadKeyBinding(scene.input().joypad().currentKeyBinding());
        }

        if (scene.appContext().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene, globalTick);
        }
    }
}