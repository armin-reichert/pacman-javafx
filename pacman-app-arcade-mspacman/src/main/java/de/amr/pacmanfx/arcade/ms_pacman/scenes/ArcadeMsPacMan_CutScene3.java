/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.model.actors.ArcadeMsPacMan_AnimationID;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.game.GameVariantConfig;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.model.world.WorldMap.TS;
import static de.amr.pacmanfx.model.world.WorldMap.tilesPx;

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

    public ArcadeMsPacMan_CutScene3(Game game) {
        super(game);
    }

    @Override
    public void onActivate() {
        initScene();
        sceneTick = 0;
        sceneState = SceneState.CLAPPERBOARD;
    }

    @Override
    public void onTick(long tick) {
        updateSceneState();
    }

    private void initScene() {
        final GameVariantConfig gameVariantConfig = game().variants().currentVariant().config();
        final SpriteAnimationContainer spriteAnimations = game().ui().sprites().animations();

        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.setAnimations(gameVariantConfig.createPacAnimations(spriteAnimations));

        msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAnimations(gameVariantConfig.createPacAnimations(spriteAnimations));

        stork = new Stork(spriteAnimations);

        bag = new Bag(spriteAnimations);
        bag.setOpen(false);

        clapperboard = new Clapperboard("3", "JUNIOR");
        clapperboard.setPosition(tilesPx(3), tilesPx(10));
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
        switch (sceneState) {

            case CLAPPERBOARD -> transition(SceneState.DELIVER_JUNIOR)
                .ifPresentOrElse(this::enterDeliverJuniorState, this::updateClapperboardState);

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
            game().ui().sounds().play(PacManGameSoundID.INTERMISSION_3);
        }
    }

    // State DELIVER_JUNIOR

    private void enterDeliverJuniorState(SceneState newState) {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(TS * 3, GROUND_Y - 4);
        pacMan.animations().select(ArcadeMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
        pacMan.animations().stopSelected();
        pacMan.show();

        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.setPosition(TS * 5, GROUND_Y - 4);
        msPacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        msPacMan.animations().stopSelected();
        msPacMan.show();

        stork.setPosition(TS * 30, TS * 12);
        stork.setVelocity(-0.8f, 0);
        stork.show();
        stork.animations().select(ArcadeMsPacMan_AnimationID.STORK_FLYING);
        stork.animations().playSelected();

        bag.setPosition(stork.x() - 14, stork.y() + 3);
        bag.setVelX(stork.velX());
        bag.setAcceleration(0, 0);
        bag.show();
        bag.setOpen(false);

        bagReleased = false;
        numBagBounces = 0;

        sceneState = newState;
    }

    private void updateDeliverJuniorState() {
        // release bag from beak when stork reaches tile 20
        if (stork.x() <= 20 * WorldMap.TS && !bagReleased) {
            bag.setAcceleration(0, 0.04f); // set y-gravity to let bag fall to ground
            stork.setVelocity(-1, 0); // fly faster without heavy bag
            bagReleased = true;
        }

        if (!bag.isOpen()) {
            bag.move();
            if (bag.y() >= GROUND_Y) {
                ++numBagBounces;
                if (numBagBounces < 3) {
                    bag.setVelocity(-0.2f, -1.0f / numBagBounces); // add upwards velocity to bounce
                    bag.setY(GROUND_Y);
                } else {
                    bag.setOpen(true);
                    bag.setY(GROUND_Y);
                    bag.setVelocity(0, 0);
                    bag.setAcceleration(0, 0);
                    Logger.info("Delivery of Junior at tick {}", sceneTick);            }
            }
        }

        stork.move();
    }
}