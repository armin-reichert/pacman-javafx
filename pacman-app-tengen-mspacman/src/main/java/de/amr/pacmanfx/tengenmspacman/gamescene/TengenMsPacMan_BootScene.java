/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_RenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantUIConfig.NES_SCREEN_WIDTH;

/**
 * Shows moving and color changing "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends GameScene {

    private static final float GHOST_Y = tilesPx(21.5f);

    public boolean gray;
    public GameEntity movingText;
    public Ghost ghost;
    public Color shadeOfBlue;

    public TengenMsPacMan_BootScene(GameAppContext appContext) {
        super(appContext);
        rendering2D().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        rendering2D().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    @Override
    public void onActivate() {
        movingText = new GameEntity();
        movingText.setComp(MovementComp.class, new MovementComp());
        movingText.pos().set(tilesPx(9), rendering2D().unscaledHeight()); // lower border of screen
        ghost = app().gameVariants().currentGameVariant().uiConfig().renderConfig().createAnimatedGhost(
            game(),
            app().ui().sprites().animations(),
            GhostPersonality.RED_GHOST_SHADOW);

        game().session().hud().hide();
    }

    @Override
    public void onTick(GameContext game) {
        final GameSystems sys = game.variant().systems();

        final int stateTick = (int) game().state().timer().tickCount();
        switch (stateTick) {
            case   1 -> gray(false);
            case   7 -> gray(true);
            case  12 -> gray(false);
            case  21 -> {
                movingText.show();
                sys.motor().setVelocity(movingText, 0, -WorldMap.HTS);
            }
            case  55 -> {
                movingText.pos().set(tilesPx(9), tilesPx(13));
                sys.motor().setVelocity(movingText, 0, 0);
            }
            case 113 -> {
                ghost.pos().set(rendering2D().unscaledWidth() - WorldMap.TS, GHOST_Y);
                ghost.show();
                sys.worldNavigator().setMoveDir(ghost, Direction.LEFT);
                sys.worldNavigator().setWishDir(ghost, Direction.LEFT);
                sys.worldNavigator().setSpeed(ghost, WorldMap.TS);
            }
            case 181 -> sys.motor().setVelocity(movingText, 0, WorldMap.TS);
            case 203 -> {
                movingText.hide();
                ghost.hide();
            }
            case 204 -> gray(true);
            case 214 -> gray(false);
            case 220 -> {
                game().state().triggerTimeout();
                return;
            }
        }
        shadeOfBlue = TengenMsPacMan_RenderConfig.shadeOfBlue(stateTick);
        sys.motor().move(ghost);
        sys.motor().move(movingText);
    }

    private void gray(boolean b)  { gray = b; }
}