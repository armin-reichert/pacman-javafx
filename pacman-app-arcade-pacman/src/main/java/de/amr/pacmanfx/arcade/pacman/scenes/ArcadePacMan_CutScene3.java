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
import de.amr.pacmanfx.core.model.systems.MovementSystem;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;
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
        blinky = renderConfig.createAnimatedGhost(gameContext(), container, RED_GHOST_SHADOW);
        sceneTick = -1;
    }

    @Override
    public void onTick(GameContext gameContext) {
        ++sceneTick;
        if (sceneTick < TICK_ANIMATION_START) {
            return;
        }
        final MovementSystem movementSystem = gameContext.systems().movementSystem;
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        switch (sceneTick) {
            case TICK_ANIMATION_START      -> startAnimation(worldMovementSystem);
            case TICK_BLINKY_RUNNING_NAKED -> startBlinkyRunningNaked(worldMovementSystem);
            case TICK_ANIMATION_ENDS       -> gameState().triggerTimeout();
        }
        movementSystem.moveAccelerated(pacMan);
        movementSystem.moveAccelerated(blinky);
    }

    private void startAnimation(WorldMovementSystem worldMovementSystem) {
        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_3, 2);
        startBlinkyChasingPacMan(worldMovementSystem);
    }

    private void startBlinkyRunningNaked(WorldMovementSystem worldMovementSystem) {
        worldMovementSystem.placeAtTile(blinky, -1, 20);
        worldMovementSystem.setMoveDir(blinky, Direction.RIGHT);
        worldMovementSystem.setWishDir(blinky, Direction.RIGHT);
        blinky.animations.select(CommonAnimationID.BLINKY_NAKED);
        blinky.animations.playSelected();
    }

    private void startBlinkyChasingPacMan(WorldMovementSystem worldMovementSystem) {
        worldMovementSystem.placeAtTile(pacMan, 29, 20);
        worldMovementSystem.setMoveDir(pacMan, Direction.LEFT);
        worldMovementSystem.setSpeed(pacMan, 1.25f);

        pacMan.visibility().show();

        pacMan.animations.select(CommonAnimationID.PAC_MUNCHING);
        pacMan.animations.playSelected();

        worldMovementSystem.placeAtTile(blinky, 35, 20);
        worldMovementSystem.setMoveDir(blinky, Direction.LEFT);
        worldMovementSystem.setWishDir(blinky, Direction.LEFT);
        worldMovementSystem.setSpeed(blinky, 1.25f);

        blinky.visibility().show();

        blinky.animations.select(CommonAnimationID.BLINKY_PATCHED);
        blinky.animations.playSelected();
    }
}