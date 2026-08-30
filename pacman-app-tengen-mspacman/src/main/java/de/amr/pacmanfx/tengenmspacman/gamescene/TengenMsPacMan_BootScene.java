/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_RenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
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

    public TengenMsPacMan_BootScene(GameAppContext app) {
        super(app);
        components().setComp(CanvasRenderingComp.class, new CanvasRenderingComp());
        reqCanvasRendering().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        reqCanvasRendering().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    @Override
    public void onActivate() {
        movingText = new GameEntity();
        movingText.setComp(MovementComp.class, new MovementComp());
        movingText.pos().set(tilesPx(9), reqCanvasRendering().unscaledHeight()); // lower border of screen

        final GameVariant gameVariant = app().gameVariants().currentGameVariant();
        ghost = gameVariant.uiConfig().renderConfig().createAnimatedGhost(
            gameVariant.config().systems().actorSpriteAnimController(),
            gameVariant.spriteAnimContainer(),
            GhostPersonality.RED_GHOST_SHADOW);

        game().session().hud().hide();
    }

    @Override
    public void onTick(GameContext game) {
        final GameSystems systems = game.variant().systems();

        final int stateTick = (int) game().state().timer().tickCount();
        switch (stateTick) {
            case   1 -> blackBackground();
            case   7 -> grayBackground();
            case  12 -> blackBackground();
            case  21 -> {
                movingText.show();
                systems.motor().setVelocity(movingText, 0, -WorldMap.HTS);
            }
            case  55 -> {
                movingText.pos().set(tilesPx(9), tilesPx(13));
                systems.motor().setVelocity(movingText, 0, 0);
            }
            case 113 -> {
                ghost.pos().set(reqCanvasRendering().unscaledWidth() - WorldMap.TS, GHOST_Y);
                ghost.show();
                systems.navigator().setMoveDir(ghost, Direction.LEFT);
                systems.navigator().setWishDir(ghost, Direction.LEFT);
                systems.navigator().setMoveDirSpeed(ghost, WorldMap.TS);
            }
            case 181 -> systems.motor().setVelocity(movingText, 0, WorldMap.TS);
            case 203 -> {
                movingText.hide();
                ghost.hide();
            }
            case 204 -> grayBackground();
            case 214 -> blackBackground();
            case 220 -> {
                game().state().triggerTimeout();
                return;
            }
        }
        shadeOfBlue = TengenMsPacMan_RenderConfig.shadeOfBlue(stateTick);
        systems.motor().move(ghost);
        systems.motor().move(movingText);
    }

    private void blackBackground() {
        gray = false;

    }
    private void grayBackground() {
        gray = true;
    }
}