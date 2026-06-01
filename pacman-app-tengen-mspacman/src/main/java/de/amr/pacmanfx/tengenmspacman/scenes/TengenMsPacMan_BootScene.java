/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.basics.fsm.State;
import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.core.Globals.*;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.*;

/**
 * Shows moving and color changing "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends GameScene2D {

    private static final float GHOST_Y = TS(21.5f);

    public boolean gray;
    public Actor movingText;
    public Ghost ghost;
    public Color shadeOfBlue;

    public TengenMsPacMan_BootScene(AppContext context) {
        super(context);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    @Override
    public void onActivate(UIConfig uiConfig) {
        movingText = new Actor();
        movingText.setPosition(TS(9), getUnscaledHeight()); // lower border of screen
        ghost = uiConfig.createGhostWithAnimations(context.ui().sprites().animationSet(), RED_GHOST_SHADOW);
    }

    @Override
    public void onTick(GameClock clock) {
        final State<Game> gameState = context().currentGameState();
        final int t = (int) gameState.timer().tickCount();
        switch (t) {
            case   1 -> gray(false);
            case   7 -> gray(true);
            case  12 -> gray(false);
            case  21 -> {
                movingText.setVelocity(0, -HTS);
                movingText.show();
            }
            case  55 -> {
                movingText.setPosition(TS(9), TS(13));
                movingText.setVelocity(0, 0);
            }
            case 113 -> {
                ghost.setPosition(getUnscaledWidth() - TS, GHOST_Y);
                ghost.setMoveDir(Direction.LEFT);
                ghost.setWishDir(Direction.LEFT);
                ghost.setSpeed(TS);
                ghost.show();
            }
            case 181 -> movingText.setVelocity(0, TS);
            case 203 -> {
                movingText.hide();
                ghost.hide();
            }
            case 204 -> gray(true);
            case 214 -> gray(false);
            case 220 -> {
                gameState.expire();
                return;
            }
        }
        shadeOfBlue = shadeOfBlue(t);
        ghost.move();
        movingText.move();
    }

    private void gray(boolean b)  { gray = b; }
}