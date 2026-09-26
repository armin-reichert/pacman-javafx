/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.introscene;

import de.amr.basics.ui.assets.SpriteSheet;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.gamescene.introscene.TengenMsPacMan_IntroScene.*;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneView;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.Optional;

import static de.amr.pacmanfx.tengenmspacman.gamescene.introscene.TengenMsPacMan_IntroScene.*;
import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SceneRendererUtils.drawJoypadKeyBinding;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_IntroScene_Renderer extends BaseRenderer {

    private final TengenMsPacMan_SpriteSheet spriteSheet = TengenMsPacMan_SpriteSheet.instance();
    private final TengenMsPacMan_UISettings uiSettings;

    public TengenMsPacMan_IntroScene_Renderer(GameVariantRuntime runtime, Canvas canvas) {
        super(canvas);
        requireNonNull(runtime);
        uiSettings = runtime.extensionValue(TengenMsPacMan_GameExtension.EXT_UI_SETTINGS, TengenMsPacMan_UISettings.class);
    }

    @Override
    public Optional<SpriteSheet<?>> optSpriteSheet() {
        return Optional.of(spriteSheet);
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case GameSceneView(TengenMsPacMan_IntroScene introScene) -> render(introScene);
            default -> {}
        }
    }

    public void render(TengenMsPacMan_IntroScene introScene) {
        final Font arcade8 = Ufx.deriveFont(GlobalFonts.ARCADE.font(), scaled(8));

        ctx.save();
        ctx.setFont(arcade8);

        switch (introScene.flow.state()) {

            case SceneState.PRESENTING_GAME -> {
                if (introScene.dark) {
                    return;
                }
                final long stateTick = introScene.flow.state().timer().tickCount();
                final boolean bright = stateTick % 60 < 30; // 0.5s dark, 0.5s bright
//                fillText(TENGEN_PRESENTS, shadeOfBlue(stateTick), introScene.presentsTextPosition.x(), introScene.presentsTextPosition.y());
                //drawSprite(spriteSheet.findSprite(SpriteID.LARGE_MS_PAC_MAN_TEXT), 7 * TS, ANCHOR_Y, true);
                if (bright) {
//                    fillText(PRESS_START, NES_Palette.color(0x20), 10 * TS, ANCHOR_Y + 9 * TS);
                }
                //fillText(NAMCO_LTD,           NES_Palette.color(0x25), 5 * TS, ANCHOR_Y + 15 * TS);
                //fillText(TENGEN_INC,          NES_Palette.color(0x25), 7 * TS, ANCHOR_Y + 16 * TS);
                //fillText(ALL_RIGHTS_RESERVED, NES_Palette.color(0x25), 6 * TS, ANCHOR_Y + 17 * TS);
            }

            //case SceneState.SHOWING_MARQUEE -> fillText(TITLE_TEXT, NES_Palette.color(0x28), ANCHOR_X + 20, ANCHOR_Y - 18);

            case SceneState.GHOSTS_MARCHING_IN -> {
                final Ghost currentGhost = introScene.currentGhost();
                final int personalityIndex = currentGhost.personality().ordinal();
                final Color ghostColor = introScene.ghostColors[personalityIndex];
                fillText(MARQUEE_TITLE_TEXT, NES_Palette.color(0x28), ANCHOR_X + 20, ANCHOR_Y - 18);
                if (introScene.ghostIndex == 0) {
                    fillText(WITH, NES_Palette.color(0x20), ANCHOR_X + 12, ANCHOR_Y + 23);
                }
                fillText(currentGhost.name().toUpperCase(), ghostColor, ANCHOR_X + 44, ANCHOR_Y + 41);
            }

            case SceneState.MS_PACMAN_MARCHING_IN -> {
                //fillText(TITLE_TEXT, NES_Palette.color(0x28), ANCHOR_X + 20, ANCHOR_Y - 18);
                fillText(STARRING, NES_Palette.color(0x20), ANCHOR_X + 12, ANCHOR_Y + 22);
                fillText(MS_PAC_MAN, NES_Palette.color(0x28), ANCHOR_X + 28, ANCHOR_Y + 38);
            }

            default -> {}
        }

        if (uiSettings.joypadBindingsDisplayed.get()) {
            drawJoypadKeyBinding(ctx, scaling(), introScene.app().input().joypad().currentKeyBinding());
        }

        ctx.restore();
    }
}