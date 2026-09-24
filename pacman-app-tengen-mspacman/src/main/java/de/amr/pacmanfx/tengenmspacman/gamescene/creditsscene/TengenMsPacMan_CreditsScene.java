/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.creditsscene;

import de.amr.basics.math.Vector2f;
import de.amr.basics.timer.TickTimer;
import de.amr.basics.ui.entities.props.textdisplay.TextView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.gamescene.optionsscene.MenuSeparatorBarView;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.basics.ui.rendering.Renderer.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;

/**
 * Gives credit to the people that helped in making the game, original and remake authors.
 */
public class TengenMsPacMan_CreditsScene extends AbstractGameScene {

    public static final int DISPLAY_TICKS = 20 * 60;

    public enum DisplayMode { ORIGINAL_AUTHORS, REMAKE_AUTHORS }

    private static TextView makeText(String text, int paletteIndex, int column, int row) {
        final TextView textView = new TextView();
        textView.pos().set(column * TS, row * TS);
        textView.data().setText(text);
        textView.data().setFillColor(NES_Palette.color(paletteIndex));
        textView.data().setFont(GlobalFonts.ARCADE.font(TS));
        textView.show();
        return textView;
    }

    private static final List<TextView> ORIGINAL_CREDITS = List.of(
        makeText("CREDITS FOR MS PAC-MAN",  0x20,  3,  7),
        makeText("GAME PROGRAMMER:",        0x23,  4, 11),
        makeText("FRANZ LANZINGER",         0x23, 10, 13),
        makeText("SPECIAL THANKS:",         0x23,  4, 16),
        makeText("JEFF YONAN",              0x23, 10, 18),
        makeText("DAVE O'RIVA",             0x23, 10, 19),
        makeText("MS PAC-MAN TM NAMCO LTD", 0x19,  5, 23),
        makeText("©1990 TENGEN INC",        0x19,  7, 24),
        makeText("ALL RIGHTS RESERVED",     0x19,  6, 25)
    );

    private static final List<TextView> REMAKE_CREDITS = List.of(
        makeText("CREDITS FOR JAVAFX REMAKE",  0x20,  3, 7),
        makeText("GAME PROGRAMMER:",           0x23,  4, 11),
        makeText("ARMIN REICHERT",             0x23, 10, 13),
        makeText("SPECIAL THANKS:",            0x23,  4, 16),
        makeText("@RUSSIANMANSMWC",            0x23, 10, 18),
        makeText("@FLICKY1211",                0x23, 10, 19),
        makeText("ANDYANA JONSEPH",            0x23, 10, 20),
        makeText("GITHUB.COM/ARMIN-REICHERT",  0x19,  3, 23),
        makeText("©2021 MIT LICENSE",          0x19,  6, 24),
        makeText("ALL RIGHTS GRANTED",         0x19,  5, 25)
    );

    private final MenuSeparatorBarView topBarView;
    private final MenuSeparatorBarView botBarView;

    public DisplayMode displayMode = DisplayMode.ORIGINAL_AUTHORS;

    //TODO Fading effect does not yet work with new rendering model
    public float fadeProgress = 0;

    public TengenMsPacMan_CreditsScene() {
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
        reqCanvasRendering().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        reqCanvasRendering().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);

        topBarView = new MenuSeparatorBarView(NES_SCREEN_WIDTH, 8, new Vector2f(0,  2.5f * TS));
        botBarView = new MenuSeparatorBarView(NES_SCREEN_WIDTH, 8, new Vector2f(0, 26.5f * TS));
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(
            topBarView, botBarView,
            creditsView(displayMode == DisplayMode.ORIGINAL_AUTHORS ? ORIGINAL_CREDITS : REMAKE_CREDITS)
        );
    }

    private Stream<Renderable> creditsView(List<TextView> textViews) {
        return textViews.stream().map(GameEntityViewBuilder::propView);
    }

    @Override
    public void onActivate() {
        final var actions = app().variantManager().currentRuntime()
            .extensionValue(TengenMsPacMan_GameExtension.EXT_ACTIONS, TengenMsPacMan_Actions.class);

        final var bindingsMap = actionBindings().registry();
        bindingsMap.selectAnyMatchingBinding(actions.actionEnterStartScreen(), actions.localBindings());

        fadeProgress = 0;
        displayMode = DisplayMode.ORIGINAL_AUTHORS;

        game().session().setHudVisible(false);
    }

    @Override
    public void onTick(GameContext game) {
        final TickTimer stateTimer = game().state().timer();
        if (stateTimer.tickCount() == DISPLAY_TICKS) {
            game().state().triggerTimeout();
            return;
        }
        if (stateTimer.tickCount() == DISPLAY_TICKS / 2) {
            displayMode = DisplayMode.REMAKE_AUTHORS;
        }
        if (stateTimer.tickCount() > DISPLAY_TICKS / 2) {
            fadeProgress = Math.min(fadeProgress + 0.005f, 1f); // Clamp to 1.0
        }
    }
}