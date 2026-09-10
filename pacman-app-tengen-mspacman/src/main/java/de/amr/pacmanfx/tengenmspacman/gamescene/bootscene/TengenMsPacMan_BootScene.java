/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.bootscene;

import de.amr.basics.math.Direction;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.rendering.ColoredRect;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.TextDisplay;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_RenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import javafx.scene.paint.Color;

import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;

/**
 * Shows moving and color changing "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends GameScene {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";

    private static final float GHOST_Y = tilesPx(21.5f);

    public boolean gray;
    public Color shadeOfBlue;

    private final ColoredRect grayRect;
    private final TextDisplay tengenPresentsText;

    private Ghost ghost;

    public TengenMsPacMan_BootScene(GameAppContext app) {
        super(app);

        final var rendering = new SceneCanvasRenderingComp();
        setComp(SceneCanvasRenderingComp.class, rendering);
        rendering.unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        rendering.unscaledHeightProperty().set(NES_SCREEN_HEIGHT);

        grayRect = new ColoredRect(0, 0, NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT, NES_Palette.color(0x10));

        tengenPresentsText = new TextDisplay();
        tengenPresentsText.data().setText(TENGEN_PRESENTS);
        tengenPresentsText.data().setFont(GlobalAssets.Fonts.ARCADE.font());
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(gray
            ? grayRect
            : tengenPresentsText, ghost);
    }

    @Override
    public void onActivate() {
        final GameVariant gameVariant = app().gameVariants().currentGameVariant();
        ghost = gameVariant.uiConfig().renderConfig().createAnimatedGhost(
            gameVariant.config().systems().actorSpriteAnimController(),
            gameVariant.spriteAnimContainer(),
            GhostPersonality.RED_GHOST_SHADOW);

        game().session().setHudVisible(false);
    }

    @Override
    public void onTick(GameContext game) {
        final GameSystems systems = game.variant().systems();

        final int stateTick = (int) game().state().timer().tickCount();
        shadeOfBlue = TengenMsPacMan_RenderConfig.shadeOfBlue(stateTick);

        switch (stateTick) {
            case   1 -> blackBackground();
            case   7 -> grayBackground();
            case  12 -> blackBackground();
            case  21 -> {
                tengenPresentsText.pos().set(NES_SCREEN_WIDTH / 2.0, reqCanvasRendering().unscaledHeight()); // lower border of screen
                tengenPresentsText.show();
                systems.motor().setVelocity(tengenPresentsText, 0, -WorldMap.HTS);
            }
            case  55 -> {
                systems.motor().setVelocity(tengenPresentsText, 0, 0);
            }
            case 113 -> {
                ghost.pos().set(reqCanvasRendering().unscaledWidth() - WorldMap.TS, GHOST_Y);
                ghost.show();
                systems.navigator().setMoveDir(ghost, Direction.LEFT);
                systems.navigator().setWishDir(ghost, Direction.LEFT);
                systems.navigator().setMoveDirSpeed(ghost, WorldMap.TS);
            }
            case 181 -> systems.motor().setVelocity(tengenPresentsText, 0, WorldMap.TS);
            case 203 -> {
                tengenPresentsText.hide();
                ghost.hide();
            }
            case 204 -> grayBackground();
            case 214 -> blackBackground();
            case 220 -> {
                game().state().triggerTimeout();
                return;
            }
        }

        tengenPresentsText.data().setFillColor(shadeOfBlue);
        systems.motor().move(tengenPresentsText);

        systems.motor().move(ghost);
    }

    private void blackBackground() {
        gray = false;

    }
    private void grayBackground() {
        gray = true;
    }
}