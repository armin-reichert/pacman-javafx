/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.shadeOfBlue;

/**
 * Shows moving and color changing "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends GameScene2D {

    private static final float GHOST_Y = TS(21.5f);

    public boolean gray;
    public Actor movingText;
    public Ghost ghost;
    public Color shadeOfBlue;

    public TengenMsPacMan_BootScene() {}

    @Override
    public void doInit(Game game) {
        game.hud().hide();
        movingText = new Actor();
        movingText.setPosition(TS(9), unscaledSize().y()); // lower border of screen
        final GameUI_Config uiConfig = ui.currentConfig();
        ghost = uiConfig.createGhostWithAnimations(RED_GHOST_SHADOW);
    }

    @Override
    protected void doEnd(Game game) {}

    @Override
    public void update(Game game) {
        int tick = (int) game.control().state().timer().tickCount();
        shadeOfBlue = shadeOfBlue(tick);
        switch (tick) {
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
                game.control().terminateCurrentGameState();
                return;
            }
        }
        ghost.move();
        movingText.move();
    }

    @Override
    public Vector2i unscaledSize() {
        return NES_SIZE_PX;
    }

    private void gray(boolean b)  { gray = b; }
}