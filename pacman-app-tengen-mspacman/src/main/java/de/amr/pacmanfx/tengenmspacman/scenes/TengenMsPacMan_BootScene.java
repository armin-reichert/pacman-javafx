/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.*;

/**
 * Shows moving and color changing "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends GameScene2D {

    public static final Vector2i SIZE = new Vector2i(NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT);

    private static final float GHOST_Y = TS(21.5f);

    public boolean gray;
    public Actor movingText;
    public Ghost ghost;
    public Color shadeOfBlue;

    public TengenMsPacMan_BootScene(GameUI ui) {
        super(ui);
    }

    @Override
    public void onSceneStart() {
        movingText = new Actor();
        movingText.setPosition(TS(9), unscaledSize().y()); // lower border of screen
        final UIConfig uiConfig = ui.currentConfig();
        ghost = uiConfig.createGhostWithAnimations(ui.spriteAnimationDriver(), RED_GHOST_SHADOW);
    }

    @Override
    public void onTick(long tick) {
        final State<Game> gameState = gameContext().game().flow().state();
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
                movingText.setVelocity(Vector2f.ZERO);
            }
            case 113 -> {
                ghost.setPosition(unscaledSize().x() - TS, GHOST_Y);
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

    @Override
    public Vector2i unscaledSize() {
        return SIZE;
    }

    private void gray(boolean b)  { gray = b; }
}