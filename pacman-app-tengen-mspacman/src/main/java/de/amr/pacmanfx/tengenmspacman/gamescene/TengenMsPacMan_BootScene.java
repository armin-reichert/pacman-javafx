/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.state.TimedGameState;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.ui.game.PacManGamesCollection;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.config.TengenMsPacManGameVariant.*;

/**
 * Shows moving and color changing "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends AbstractGameScene2D {

    private static final float GHOST_Y = tilesPx(21.5f);

    public boolean gray;
    public Actor movingText;
    public Ghost ghost;
    public Color shadeOfBlue;

    public TengenMsPacMan_BootScene(PacManGamesCollection game) {
        super(game);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    @Override
    public void onActivate() {
        actionBindings().dispose();
        movingText = new Actor();
        movingText.setPosition(tilesPx(9), unscaledHeight()); // lower border of screen
        ghost = game().variants().currentVariant().config().createAnimatedGhost(
            game().ui().sprites().animations(), GameModel.RED_GHOST_SHADOW);
    }

    @Override
    public void onTick(long tick) {
        final TimedGameState gameState = gameState();
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
        shadeOfBlue = shadeOfBlue(t);
        ghost.move();
        movingText.move();
    }

    private void gray(boolean b)  { gray = b; }
}