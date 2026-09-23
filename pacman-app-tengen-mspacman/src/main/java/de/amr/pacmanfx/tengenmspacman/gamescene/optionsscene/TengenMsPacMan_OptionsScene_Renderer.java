/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.optionsscene;

import de.amr.basics.math.RectShort;
import de.amr.basics.ui.assets.SpriteSheet;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptions;
import static de.amr.pacmanfx.tengenmspacman.gamescene.optionsscene.TengenMsPacMan_OptionsScene.RenderableMenuOption;
import static de.amr.pacmanfx.tengenmspacman.gamescene.optionsscene.TengenMsPacMan_OptionsScene.RenderableMenuSeparatorBar;
import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SceneRendererUtils.drawJoypadKeyBinding;

public class TengenMsPacMan_OptionsScene_Renderer extends BaseRenderer {

    private static final int COL_ARROW = 2 * TS;
    private static final int COL_LABEL = 4 * TS;

    private static final Color NES_YELLOW = NES_Palette.color(0x28);
    private static final Color NES_WHITE = NES_Palette.color(0x20);

    public TengenMsPacMan_OptionsScene_Renderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case TengenMsPacMan_OptionsScene optionsScene -> renderMenu(optionsScene);
            case RenderableMenuOption menuOption -> renderMenuOption(menuOption);
            case RenderableMenuSeparatorBar bar -> renderBar(bar);
            default -> {}
        }
    }

    private void renderMenuOption(RenderableMenuOption menuOption) {
        final float y = menuOption.offset().y();
        final double sepX = menuOption.separatorTileX() * TS;
        final double valueX = sepX + 2 * TS;
        final Font arcade8 = Ufx.deriveFont(GlobalFonts.ARCADE.font(), scaled(8));

        ctx.save();
        ctx.setFont(arcade8);
        if (menuOption.selected()) {
            ctx.setFill(NES_YELLOW);
            ctx.fillRect(scaled(COL_ARROW + 2.25), scaled(y - 4.5), scaled(7.5), scaled(1.75));
            fillText(">", NES_YELLOW, arcade8, COL_ARROW + 3, y);
        }
        fillText(menuOption.label(), NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, sepX, y);
        fillText(menuOption.value(), NES_WHITE, valueX, y);
        ctx.restore();
    }

    private void renderBar(RenderableMenuSeparatorBar bar) {
        ctx.save();
        ctx.scale(scaling(), scaling());
        ctx.setFill(NES_Palette.color(0x20));
        ctx.fillRect(0, 0, bar.width(), bar.height());
        ctx.setFill(NES_Palette.color(0x21));
        ctx.fillRect(0, 1, bar.width(), bar.height() - 2);
        ctx.restore();
    }

    //TODO refactor
    private void renderMenu(TengenMsPacMan_OptionsScene optionsScene) {
        final TengenMsPacMan_UISettings uiSettings = optionsScene.app().variantManager().currentRuntime()
            .extensionValue(TengenMsPacMan_GameExtension.EXT_UI_SETTINGS, TengenMsPacMan_UISettings.class);

        final Font arcade8 = Ufx.deriveFont(GlobalFonts.ARCADE.font(), scaled(8));
        ctx.setFont(arcade8);

        if (uiSettings.joypadBindingsDisplayed.get()) {
            drawJoypadKeyBinding(ctx, scaling(), optionsScene.app().input().joypad().currentKeyBinding());
        }
    }

}