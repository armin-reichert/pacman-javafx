/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.component.common.Movement;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_RenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.NES_SCREEN_WIDTH;

/**
 * Shows moving and color changing "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends AbstractGameScene2D {

    private static final float GHOST_Y = tilesPx(21.5f);

    public boolean gray;
    public Actor movingText;
    public Ghost ghost;
    public Color shadeOfBlue;

    public TengenMsPacMan_BootScene(GameAppContext appContext) {
        super(appContext);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    @Override
    public void onActivate() {
        actionBindings().dispose();
        movingText = new Actor();
        movingText.setComponent(Movement.class, new Movement());
        movingText.position().set(tilesPx(9), unscaledHeight()); // lower border of screen
        ghost = appContext().variants().currentVariant().config().renderConfig().createAnimatedGhost(
            gameContext(),
            appContext().ui().sprites().animations(),
            GhostPersonality.RED_GHOST_SHADOW);
    }

    @Override
    public void onTick(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        final GameState gameState = gameState();
        final int stateTick = (int) gameState.timer().tickCount();
        switch (stateTick) {
            case   1 -> gray(false);
            case   7 -> gray(true);
            case  12 -> gray(false);
            case  21 -> {
                movingText.visibility().show();
                sys.motor().setVelocity(movingText, 0, -WorldMap.HTS);
            }
            case  55 -> {
                movingText.position().set(tilesPx(9), tilesPx(13));
                sys.motor().setVelocity(movingText, 0, 0);
            }
            case 113 -> {
                ghost.position().set(unscaledWidth() - WorldMap.TS, GHOST_Y);
                ghost.visibility().show();
                sys.navigator().setMoveDir(ghost, Direction.LEFT);
                sys.navigator().setWishDir(ghost, Direction.LEFT);
                sys.navigator().setSpeed(ghost, WorldMap.TS);
            }
            case 181 -> sys.motor().setVelocity(movingText, 0, WorldMap.TS);
            case 203 -> {
                movingText.visibility().hide();
                ghost.visibility().hide();
            }
            case 204 -> gray(true);
            case 214 -> gray(false);
            case 220 -> {
                gameState.triggerTimeout();
                return;
            }
        }
        shadeOfBlue = TengenMsPacMan_RenderConfig.shadeOfBlue(stateTick);
        sys.motor().moveAccelerated(ghost);
        sys.motor().moveAccelerated(movingText);
    }

    private void gray(boolean b)  { gray = b; }
}