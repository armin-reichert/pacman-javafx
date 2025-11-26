/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_BootScene_Renderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.HUD_Renderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.shadeOfBlue;

/**
 * Shows moving and color changing "TENGEN PRESENTS" text and ghost running through scene.
 */
public class TengenMsPacMan_BootScene extends GameScene2D {

    private static final float GHOST_Y = TS(21.5f);

    public boolean gray;
    public Actor movingText;
    public Ghost ghost;
    public Color shadeOfBlue;

    private TengenMsPacMan_BootScene_Renderer sceneRenderer;

    public TengenMsPacMan_BootScene(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        sceneRenderer = new TengenMsPacMan_BootScene_Renderer(this, canvas);
    }

    @Override
    protected HUD_Renderer hudRenderer() {
        return null;
    }

    @Override
    public TengenMsPacMan_BootScene_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    @Override
    public void doInit() {
        movingText = new Actor();
        movingText.setPosition(TS(9), sizeInPx().y()); // lower border of screen

        final GameUI_Config uiConfig = ui.currentConfig();
        ghost = uiConfig.createAnimatedGhost(RED_GHOST_SHADOW);
    }

    @Override
    protected void doEnd() {}

    @Override
    public void update() {
        int tick = (int) context().currentGameState().timer().tickCount();
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
                ghost.setPosition(sizeInPx().x() - TS, GHOST_Y);
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
                context().currentGame().changeState(TengenMsPacMan_GameState.INTRO);
                return;
            }
        }
        ghost.move();
        movingText.move();
    }

    @Override
    public Vector2i sizeInPx() {
        return NES_SIZE_PX;
    }

    private void gray(boolean b)  { gray = b; }
}