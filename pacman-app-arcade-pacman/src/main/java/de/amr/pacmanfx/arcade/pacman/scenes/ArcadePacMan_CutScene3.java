/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.WorldMovement;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.core.model.GameModel.RED_GHOST_SHADOW;

/**
 * Third cut scene in Arcade Pac-Man game:<br>
 * Red ghost in damaged dress chases Pac-Man from right to left over the screen.
 * After they have disappeared, a naked, shaking ghost runs from left over the screen.
 */
public class ArcadePacMan_CutScene3 extends AbstractGameScene2D {

    public static final int TICK_ANIMATION_START      = 120;
    public static final int TICK_BLINKY_RUNNING_NAKED = TICK_ANIMATION_START + 400;
    public static final int TICK_ANIMATION_ENDS       = TICK_ANIMATION_START + 700;

    public int sceneTick;
    public Pac pacMan;
    public Ghost blinky;

    public ArcadePacMan_CutScene3(GameAppContext appContext) {
        super(appContext);
    }

    @Override
    public void onActivate() {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer container = appContext().ui().sprites().animations();
        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.animations = renderConfig.createPacAnimations(container);
        blinky = renderConfig.createAnimatedGhost(container, RED_GHOST_SHADOW);
        sceneTick = -1;
    }

    @Override
    public void onTick(GameContext gameContext) {
        ++sceneTick;
        if (sceneTick < TICK_ANIMATION_START) {
            return;
        }
        switch (sceneTick) {
            case TICK_ANIMATION_START      -> startAnimation();
            case TICK_BLINKY_RUNNING_NAKED -> startBlinkyRunningNaked();
            case TICK_ANIMATION_ENDS       -> gameState().triggerTimeout();
        }
        pacMan.move();
        blinky.move();
    }

    private void startAnimation() {
        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_3, 2);
        startBlinkyChasingPacMan();
    }

    private void startBlinkyRunningNaked() {
        WorldMovement.SYSTEM.placeAtTile(blinky, -1, 20);
        blinky.setMoveDir(Direction.RIGHT);
        blinky.setWishDir(Direction.RIGHT);
        blinky.animations.select(CommonAnimationID.BLINKY_NAKED);
        blinky.animations.playSelected();
    }

    private void startBlinkyChasingPacMan() {
        WorldMovement.SYSTEM.placeAtTile(pacMan, 29, 20);
        pacMan.setMoveDir(Direction.LEFT);
        WorldMovement.SYSTEM.setSpeed(pacMan, 1.25f);
        pacMan.visibility.show();
        pacMan.animations.select(CommonAnimationID.PAC_MUNCHING);
        pacMan.animations.playSelected();

        WorldMovement.SYSTEM.placeAtTile(blinky, 35, 20);
        blinky.setMoveDir(Direction.LEFT);
        blinky.setWishDir(Direction.LEFT);
        WorldMovement.SYSTEM.setSpeed(blinky, 1.25f);
        blinky.visibility.show();
        blinky.animations.select(CommonAnimationID.BLINKY_PATCHED);
        blinky.animations.playSelected();
    }
}