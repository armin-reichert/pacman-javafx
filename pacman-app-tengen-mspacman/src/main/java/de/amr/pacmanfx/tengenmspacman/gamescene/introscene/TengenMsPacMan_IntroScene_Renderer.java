/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.introscene;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.gamescene.introscene.TengenMsPacMan_IntroScene.SceneState;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.tengenmspacman.gamescene.introscene.TengenMsPacMan_IntroScene.ANCHOR_X;
import static de.amr.pacmanfx.tengenmspacman.gamescene.introscene.TengenMsPacMan_IntroScene.ANCHOR_Y;
import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_RenderConfig.shadeOfBlue;
import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SceneRendererUtils.drawJoypadKeyBinding;
import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_IntroScene_Renderer extends BaseRenderer implements SpriteRenderer {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";
    public static final String PRESS_START = "PRESS START";
    public static final String NAMCO_LTD = "MS PAC-MAN TM NAMCO LTD";
    public static final String TENGEN_INC = "©1990 TENGEN INC";
    public static final String ALL_RIGHTS_RESERVED = "ALL RIGHTS RESERVED";
    public static final String WITH = "WITH";
    public static final String STARRING = "STARRING";
    public static final String MS_PAC_MAN = "MS PAC-MAN";
    public static final String QUOTED_MS_PACMAN = "\"MS PAC-MAN\"";

    private final TengenMsPacMan_UISettings uiSettings;

    public TengenMsPacMan_IntroScene_Renderer(GameVariantRenderConfig renderConfig, GameScene gameScene, Canvas canvas) {
        super(canvas);
        requireNonNull(renderConfig);
        requireNonNull(gameScene);
        requireNonNull(canvas);

        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));

        uiSettings = gameScene.app().currentGameVariantUIConfig().extensionValue(
            TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Renderable r, long tick) {
        if (!(r instanceof TengenMsPacMan_IntroScene introScene)) {
            return;
        }

        final Font arcade8 = Ufx.deriveFont(GlobalAssets.Fonts.ARCADE.font(), scaled(8));

        ctx.save();
        ctx.setFont(arcade8);

        switch (introScene.flow.state()) {

            case SceneState.SHOWING_MARQUEE -> fillText(QUOTED_MS_PACMAN, NES_Palette.color(0x28), ANCHOR_X + 20, ANCHOR_Y - 18);

            case SceneState.GHOSTS_MARCHING_IN -> {
                final Ghost currentGhost = introScene.currentGhost();
                final int personalityIndex = currentGhost.personality().ordinal();
                final Color ghostColor = introScene.ghostColors[personalityIndex];
                fillText(QUOTED_MS_PACMAN, NES_Palette.color(0x28), ANCHOR_X + 20, ANCHOR_Y - 18);
                if (introScene.ghostIndex == 0) {
                    fillText(WITH, NES_Palette.color(0x20), ANCHOR_X + 12, ANCHOR_Y + 23);
                }
                fillText(currentGhost.name().toUpperCase(), ghostColor, ANCHOR_X + 44, ANCHOR_Y + 41);
            }

            case SceneState.MS_PACMAN_MARCHING_IN -> {
                fillText(QUOTED_MS_PACMAN, NES_Palette.color(0x28), ANCHOR_X + 20, ANCHOR_Y - 18);
                fillText(STARRING, NES_Palette.color(0x20), ANCHOR_X + 12, ANCHOR_Y + 22);
                fillText(MS_PAC_MAN, NES_Palette.color(0x28), ANCHOR_X + 28, ANCHOR_Y + 38);
            }

            case SceneState.WAITING_FOR_START -> {
                if (introScene.dark) {
                    return;
                }
                final long stateTick = introScene.flow.state().timer().tickCount();
                final boolean bright = stateTick % 60 < 30; // 0.5s dark, 0.5s bright
                fillText(TENGEN_PRESENTS, shadeOfBlue(stateTick),
                    introScene.presentsTextPosition.x(), introScene.presentsTextPosition.y());
                drawSprite(spriteSheet().findSprite(SpriteID.LARGE_MS_PAC_MAN_TEXT), 5 * TS, ANCHOR_Y, true);
                if (bright) {
                    fillText(PRESS_START, NES_Palette.color(0x20), 10 * TS, ANCHOR_Y + 9 * TS);
                }
                fillText(NAMCO_LTD,           NES_Palette.color(0x25), 5 * TS, ANCHOR_Y + 15 * TS);
                fillText(TENGEN_INC,          NES_Palette.color(0x25), 7 * TS, ANCHOR_Y + 16 * TS);
                fillText(ALL_RIGHTS_RESERVED, NES_Palette.color(0x25), 6 * TS, ANCHOR_Y + 17 * TS);
            }

            default -> {}
        }

        if (uiSettings.joypadBindingsDisplayed.get()) {
            drawJoypadKeyBinding(ctx, scaling(), introScene.app().input().joypad().currentKeyBinding());
        }

        ctx.restore();
    }
}