/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.bootscene;

import de.amr.basics.math.Direction;
import de.amr.basics.ui.entities.hud.HUDStyleComp;
import de.amr.basics.ui.entities.props.CanvasFill;
import de.amr.basics.ui.entities.props.textview.TextView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.engine.GameVariantRuntime;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import javafx.scene.paint.Color;

import java.util.stream.Stream;

import static de.amr.basics.TileDimension.*;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_RenderConfig.shadeOfBlue;
import static de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder.streamOfPropViews;

/**
 * Shows moving and color changing "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends AbstractGameScene {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";

    private static final float GHOST_Y = tilesPx(21.5f);

    private boolean gray;

    private final CanvasFill grayCanvasFill;
    private final TextView tengenPresentsTextView;

    private Ghost ghost;

    public TengenMsPacMan_BootScene() {
        view2D().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        view2D().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);

        grayCanvasFill = new CanvasFill(NES_Palette.color(0x10));

        tengenPresentsTextView = new TextView();
        tengenPresentsTextView.data().setText(TENGEN_PRESENTS);
        tengenPresentsTextView.data().setCenter(true);
        tengenPresentsTextView.data().setFont(GlobalFonts.ARCADE.font());
    }

    @Override
    public Stream<Renderable> renderables() {
        if (gray) return Stream.of(grayCanvasFill);
        return streamOfPropViews(tengenPresentsTextView, ghost);
    }

    @Override
    public void onActivate() {
        final GameVariantRuntime gameVariantRuntime = engine().gameVariantManager().currentRuntime();
        ghost = gameVariantRuntime.uiConfig().renderConfig().createAnimatedGhost(
            gameVariantRuntime.playConfig().systems().actorSpriteAnimController(),
            gameVariantRuntime.spriteAnimContainer(),
            GhostPersonality.RED_GHOST_SHADOW);

        game().session().setHudVisible(false);
        //TODO temporary solution
        setHUDStyle(game().session().hud());
    }

    @Override
    public void onTick(GameContext game) {
        final GameSystems systems = game.playConfig().systems();

        final int stateTick = (int) game().state().timer().tickCount();
        final Color shadeOfBlue = shadeOfBlue(stateTick);

        switch (stateTick) {
            case   1 -> fillCanvasGray(false);
            case   7 -> fillCanvasGray(true);
            case  12 -> fillCanvasGray(false);
            case  21 -> {
                tengenPresentsTextView.pos().set(NES_SCREEN_WIDTH / 2.0, view2D().unscaledHeight()); // lower border of screen
                tengenPresentsTextView.show();
                systems.motor().setVelocity(tengenPresentsTextView, 0, -HTS);
            }
            case  55 -> systems.motor().setVelocity(tengenPresentsTextView, 0, 0);
            case 113 -> {
                ghost.pos().set(view2D().unscaledWidth() - TS, GHOST_Y);
                ghost.show();
                systems.navigator().setMoveDir(ghost, Direction.LEFT);
                systems.navigator().setWishDir(ghost, Direction.LEFT);
                systems.navigator().setSpeed(ghost, TS);
            }
            case 181 -> systems.motor().setVelocity(tengenPresentsTextView, 0, TS);
            case 203 -> {
                tengenPresentsTextView.hide();
                ghost.hide();
            }
            case 204 -> fillCanvasGray(true);
            case 214 -> fillCanvasGray(false);
            case 220 -> {
                game().state().triggerTimeout();
                return;
            }
        }

        tengenPresentsTextView.data().setFillColor(shadeOfBlue);
        systems.motor().move(tengenPresentsTextView);

        systems.motor().move(ghost);
    }

    private void fillCanvasGray(boolean gray) {
        this.gray = gray;
    }

    private void setHUDStyle(HUD hud) {
        final HUDStyleComp hudStyle = engine().gameVariantManager().currentRuntime().uiConfig().renderConfig().hudStyle();
        hud.levelCounter().setComponent(HUDStyleComp.class, hudStyle);
        hud.livesCounter().setComponent(HUDStyleComp.class, hudStyle);
        hud.gameScore().setComponent(HUDStyleComp.class, hudStyle);
        hud.highScore().setComponent(HUDStyleComp.class, hudStyle);
        hud.creditDisplay().setComponent(HUDStyleComp.class, hudStyle);
    }
}