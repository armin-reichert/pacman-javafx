/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.core.model.world.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;

/**
 * Intermission scene 3: "Junior".
 *
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 */
public class ArcadeMsPacMan_CutScene3 extends AbstractGameScene2D {

    private static final int GROUND_Y = TS * 24;

    public Pac pacMan;
    public Pac msPacMan;
    public Stork stork;
    public Bag bag;
    public Clapperboard clapperboard;

    private boolean bagReleased;
    private int numBagBounces;

    private SceneState sceneState;
    private long sceneTick;

    public ArcadeMsPacMan_CutScene3(GameAppContext appContext) {
        super(appContext);
    }

    @Override
    public void onActivate() {
        initScene();
        sceneTick = 0;
        sceneState = SceneState.CLAPPERBOARD;
    }

    @Override
    public void onTick(GameContext gameContext) {
        updateSceneState();
    }

    private void initScene() {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();

        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.animations = renderConfig.createPacAnimations(spriteAnimations);

        msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.animations = renderConfig.createPacAnimations(spriteAnimations);

        stork = new Stork(spriteAnimations);

        bag = new Bag(spriteAnimations);
        bag.setOpen(false);

        clapperboard = new Clapperboard("3", "JUNIOR");
        clapperboard.position().set(tilesPx(3), tilesPx(10));
        clapperboard.startAnimation();
    }

    // Scene controller state machine

    private enum SceneState {
        CLAPPERBOARD        (0),
        DELIVER_JUNIOR      (180),
        END                 (540);

        SceneState(int start) {
            this.start = start;
        }

        public int start() {
            return start;
        }

        private final int start;
    }

    private Optional<SceneState> transition(SceneState state) {
        return sceneTick == state.start() ? Optional.of(state) : Optional.empty();
    }

    private void updateSceneState() {
        final WorldMovementSystem worldMovementSystem = gameContext().systems().worldMovementSystem;
        switch (sceneState) {
            case CLAPPERBOARD -> transition(SceneState.DELIVER_JUNIOR)
                .ifPresentOrElse(state -> enterDeliverJuniorState(worldMovementSystem, state), this::updateClapperboardState);

            case DELIVER_JUNIOR -> transition(SceneState.END)
                .ifPresentOrElse(this::changeState, this::updateDeliverJuniorState);

            case END -> gameState().triggerTimeout();

            default -> throw new IllegalStateException("Illegal scene state: " + sceneState);
        }
        ++sceneTick;
    }

    // Generic state change
    private void changeState(SceneState newState) {
        sceneState = newState;
    }

    // State CLAPPERBOARD

    private void updateClapperboardState() {
        clapperboard.tick();
        if (sceneTick == SceneState.CLAPPERBOARD.start() + 60) {
            appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_3);
        }
    }

    // State DELIVER_JUNIOR

    private void enterDeliverJuniorState(WorldMovementSystem worldMovementSystem, SceneState newState) {
        pacMan.position().set(TS * 3, GROUND_Y - 4);
        pacMan.visibility().show();
        worldMovementSystem.setMoveDir(pacMan, Direction.RIGHT);

        pacMan.animations.select(CommonAnimationID.MR_PAC_MAN_MUNCHING);
        pacMan.animations.stopSelected();

        msPacMan.position().set(TS * 5, GROUND_Y - 4);
        msPacMan.visibility().show();
        worldMovementSystem.setMoveDir(msPacMan, Direction.RIGHT);

        msPacMan.animations.select(CommonAnimationID.PAC_MUNCHING);
        msPacMan.animations.stopSelected();

        stork.position().set(TS * 30, TS * 12);
        stork.visibility().show();
        stork.movement().setVelocity(-0.8f, 0);

        stork.animations.select(CommonAnimationID.STORK_FLYING);
        stork.animations.playSelected();

        bag.position().set(stork.position().x - 14, stork.position().y + 3);
        bag.visibility().show();
        bag.movement().setVelX(stork.movement().velX);
        bag.movement().setAcceleration(0, 0);
        bag.setOpen(false);

        bagReleased = false;
        numBagBounces = 0;

        sceneState = newState;
    }

    private void updateDeliverJuniorState() {
        // release bag from beak when stork reaches tile 20
        if (stork.position().x <= 20 * WorldMap.TS && !bagReleased) {
            bag.movement().setAcceleration(0, 0.04f); // set y-gravity to let bag fall to ground
            stork.movement().setVelocity(-1, 0); // fly faster without heavy bag
            bagReleased = true;
        }

        if (!bag.isOpen()) {
            GameContext.SYSTEMS.movementSystem.moveAccelerated(bag);
            if (bag.position().y >= GROUND_Y) {
                ++numBagBounces;
                if (numBagBounces < 3) {
                    bag.movement().setVelocity(-0.2f, -1.0f / numBagBounces); // add upwards velocity to bounce
                    bag.position().setY(GROUND_Y);
                } else {
                    bag.setOpen(true);
                    bag.position().setY(GROUND_Y);
                    bag.movement().setVelocity(0, 0);
                    bag.movement().setAcceleration(0, 0);
                    Logger.info("Delivery of Junior at tick {}", sceneTick);            }
            }
        }

        GameContext.SYSTEMS.movementSystem.moveAccelerated(stork);
    }
}