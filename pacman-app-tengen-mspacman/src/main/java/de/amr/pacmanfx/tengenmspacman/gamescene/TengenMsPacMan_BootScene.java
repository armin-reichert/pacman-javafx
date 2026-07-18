/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.gameplay.FrameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.actors.Ghost;
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
        movingText.setPosition(tilesPx(9), unscaledHeight()); // lower border of screen
        ghost = appContext().variants().currentVariant().config().renderConfig().createAnimatedGhost(
            appContext().ui().sprites().animations(), GameModel.RED_GHOST_SHADOW);
    }

    @Override
    public void onTick(FrameContext frame) {
        final GameState gameState = gameState();
        final int t = (int) gameState.timer().tickCount();
        switch (t) {
            case   1 -> gray(false);
            case   7 -> gray(true);
            case  12 -> gray(false);
            case  21 -> {
                movingText.setVelocity(0, -WorldMap.HTS);
                movingText.show();
            }
            case  55 -> {
                movingText.setPosition(tilesPx(9), tilesPx(13));
                movingText.setVelocity(0, 0);
            }
            case 113 -> {
                ghost.setPosition(unscaledWidth() - WorldMap.TS, GHOST_Y);
                ghost.setMoveDir(Direction.LEFT);
                ghost.setWishDir(Direction.LEFT);
                ghost.setSpeed(WorldMap.TS);
                ghost.show();
            }
            case 181 -> movingText.setVelocity(0, WorldMap.TS);
            case 203 -> {
                movingText.hide();
                ghost.hide();
            }
            case 204 -> gray(true);
            case 214 -> gray(false);
            case 220 -> {
                gameState.triggerTimeout();
                return;
            }
        }
        shadeOfBlue = TengenMsPacMan_RenderConfig.shadeOfBlue(t);
        ghost.move();
        movingText.move();
    }

    private void gray(boolean b)  { gray = b; }
}