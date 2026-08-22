/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.arcade.ms_pacman.entities.bag.ArcadeMsPacMan_BagSAM;
import de.amr.pacmanfx.arcade.ms_pacman.entities.stork.ArcadeMsPacMan_StorkSAM;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.Bag;
import de.amr.pacmanfx.core.entities.Clapperboard;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.clapperboard.system.ClapperboardStateSystem;
import de.amr.pacmanfx.core.entities.stork.Stork;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;

/**
 * Intermission scene 3: "Junior".
 *
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 */
public class ArcadeMsPacMan_CutScene3 extends GameScene {

    private static final int GROUND_Y = TS * 24;

    public Pac pacMan;
    public Pac msPacMan;
    public Stork stork;
    public Bag bag;
    public Clapperboard clapperboard;

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
    public void onTick(GameContext game) {
        updateSceneState();
    }

    private void initScene() {
        final GameVariant variant = app().gameVariants().currentGameVariant();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimContainer animContainer    = variant.spriteAnimContainer();
        final var actorFactory = new ArcadeMsPacMan_ActorFactory();

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        msPacMan = actorFactory.createMsPacMan();
        msPacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));

        stork = new Stork();
        stork.setBagReleasedFromBeak(false);
        stork.spriteAnim().setSpriteAnimations(new ArcadeMsPacMan_StorkSAM(animContainer));

        bag = new Bag();
        bag.spriteAnim().setSpriteAnimations(new ArcadeMsPacMan_BagSAM(animContainer));
        closeBag();

        clapperboard = new Clapperboard("3", "JUNIOR");
        clapperboard.pos().set(tilesPx(3), tilesPx(10));
        ClapperboardStateSystem.startFlapAnimation(clapperboard);
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
        final GameSystems sys = game().variant().systems();

        switch (sceneState) {
            case CLAPPERBOARD -> transition(SceneState.DELIVER_JUNIOR)
                .ifPresentOrElse(state -> enterDeliverJuniorState(sys, state), this::updateClapperboardState);

            case DELIVER_JUNIOR -> transition(SceneState.END)
                .ifPresentOrElse(this::changeState, this::updateDeliverJuniorState);

            case END -> game().state().triggerTimeout();

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
        ClapperboardStateSystem.update(clapperboard);
        if (sceneTick == SceneState.CLAPPERBOARD.start() + 60) {
            app().ui().sounds().play(PacManGameSoundID.INTERMISSION_3);
        }
    }

    // State DELIVER_JUNIOR

    private void enterDeliverJuniorState(GameSystems sys, SceneState newState) {
        final MovementSystem motor = sys.motor();
        final WorldNavigationSystem worldNavigator = sys.worldNavigator();
        final ActorSpriteAnimController animSystem = sys.actorSpriteAnimController();
        
        pacMan.pos().set(TS * 3, GROUND_Y - 4);
        pacMan.show();
        worldNavigator.setMoveDir(pacMan, Direction.RIGHT);

        animSystem.select(pacMan, CommonSpriteAnimationID.MR_PAC_MAN_MUNCHING);
        animSystem.stopSelected(pacMan);

        msPacMan.pos().set(TS * 5, GROUND_Y - 4);
        msPacMan.show();
        worldNavigator.setMoveDir(msPacMan, Direction.RIGHT);

        animSystem.select(msPacMan, CommonSpriteAnimationID.PAC_MUNCHING);
        animSystem.stopSelected(msPacMan);

        stork.pos().set(TS * 30, TS * 12);
        stork.show();
        motor.setVelocity(stork, -0.8f, 0);

        animSystem.select(stork, CommonSpriteAnimationID.STORK_FLYING);
        animSystem.playSelected(stork);

        bag.show();
        bag.pos().set(stork.pos().x() - 14, stork.pos().y() + 3);
        motor.setVelocityX(bag, stork.movement().velocityX());
        motor.setAcceleration(bag, 0, 0);
        closeBag();

        stork.setBagReleasedFromBeak(false);
        numBagBounces = 0;

        sceneState = newState;
    }

    private void updateDeliverJuniorState() {
        final MovementSystem motor = game().variant().systems().motor();

        // release bag from beak when stork reaches tile 20
        if (stork.pos().x() <= 20 * WorldMap.TS && !stork.isBagReleasedFromBeak()) {
            motor.setAcceleration(bag, 0, 0.04f); // set y-gravity to let bag fall to ground
            motor.setVelocity(stork, -1, 0); // fly faster without this heavy bag
            stork.setBagReleasedFromBeak(true);
        }

        if (!bag.isOpen()) {
            motor.move(bag);
            if (bag.pos().y() >= GROUND_Y) {
                ++numBagBounces;
                if (numBagBounces < 3) {
                    bag.movement().setVelocity(-0.2f, -1.0f / numBagBounces); // add upwards velocity to bounce
                    bag.pos().setY(GROUND_Y);
                } else {
                    openBag();
                    bag.pos().setY(GROUND_Y);
                    motor.setVelocity(bag, 0, 0);
                    motor.setAcceleration(bag, 0, 0);
                    Logger.info("Delivery of Junior at tick {}", sceneTick);            }
            }
        }

        motor.move(stork);
    }

    private void closeBag() {
        bag.setOpen(false);
        bag.spriteAnim().spriteAnimations().select(CommonSpriteAnimationID.BAG);
    }

    private void openBag() {
        bag.setOpen(true);
        bag.spriteAnim().spriteAnimations().select(CommonSpriteAnimationID.JUNIOR);
    }
}