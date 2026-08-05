/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.SpriteAnimSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.ActorAnimationID;
import de.amr.pacmanfx.core.entities.clapperboard.Clapperboard;
import de.amr.pacmanfx.core.entities.pac.Pac;
import de.amr.pacmanfx.core.entities.stork.Stork;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.core.entities.bag.Bag;
import de.amr.pacmanfx.tengenmspacman.entities.bag.BagAnimationSystem;
import de.amr.pacmanfx.tengenmspacman.entities.bag.BagSAM;
import de.amr.pacmanfx.tengenmspacman.entities.clapperboard.TengenMsPacMan_ClapperboardStateSystem;
import de.amr.pacmanfx.tengenmspacman.entities.stork.TengenMsPacMan_StorkSAM;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.NES_SCREEN_WIDTH;

/**
 * Intermission scene 3: "Junior".
 *
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 */
public class TengenMsPacMan_CutScene3 extends AbstractGameScene2D {

    public static final int TICK_CLAP = 2;
    public static final int TICK_EXPIRES = 660;

    private static final int GROUND_Y = WorldMap.TS * 24;
    private static final int RIGHT_BORDER = WorldMap.TS * 30;

    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;
    private Stork stork;
    private Bag bag;

    private boolean darkness;

    public TengenMsPacMan_CutScene3(GameAppContext app) {
        super(app);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    public Clapperboard clapperboard() {
        return clapperboard;
    }

    public Pac pacMan() {
        return pacMan;
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    public Stork stork() {
        return stork;
    }

    public Bag flyingBag() {
        return bag;
    }

    public boolean darkness() {
        return darkness;
    }

    @Override
    public void onActivate() {
        // Quit cut scene when "START" button on "joypad" is pressed
        final GameAction quitAction = appContext().commonActions().gameFlowActions().actionLetGameStateExpire();
        actionBindings().bindActionToKeyCombination(quitAction, input().joypad().keyForButton(JoypadButton.START));

        createActors();
        darkness = false;
    }
    
    private void createActors() {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer animationContainer = appContext().ui().sprites().animations();

        clapperboard = new Clapperboard("3", "JUNIOR");
        clapperboard.pos().set(3 * WorldMap.TS, 10 * WorldMap.TS);

        final var factory = TengenMsPacMan_ActorFactory.instance();

        msPacMan = factory.createMsPacMan();
        msPacMan.spriteAnim().setAnimations(renderConfig.createPacAnimations(animationContainer));

        pacMan = factory.createPacMan();
        pacMan.spriteAnim().setAnimations(renderConfig.createPacAnimations(animationContainer));

        stork = new Stork();
        stork.spriteAnim().setAnimations(new TengenMsPacMan_StorkSAM(animationContainer));

        bag = new Bag();
        bag.spriteAnim().setAnimations(new BagSAM(animationContainer));
    }
    
    private void playMusic() {
        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_3);
    }
    
    private void stopMusic() {
        appContext().ui().sounds().stop(PacManGameSoundID.INTERMISSION_3);
    }

    @Override
    public void onDeactivate() {
        stopMusic();
    }

    @Override
    public void onTick(GameContext gameContext) {
        final long tick = gameState().timer().tickCount();

        if (tick == TICK_CLAP) {
            clapperboard.show();
            TengenMsPacMan_ClapperboardStateSystem.startFlapAnimation(clapperboard);
            playMusic();
        }
        else if (tick == TICK_EXPIRES) {
            gameState().triggerTimeout();
            return;
        }

        TengenMsPacMan_ClapperboardStateSystem.update(clapperboard);
        playCutScene(gameContext, tick);
        BagAnimationSystem.update(bag);
    }
    
    private void letActorsMove(MovementSystem motor) {
        motor.move(stork);
        if (!bag.isOpen()) {
            motor.move(bag);
            if (bag.pos().y() > GROUND_Y) {
                bag.pos().setY(GROUND_Y);
                motor.setVelocity(bag,
                    0.9f * bag.movement().velocityX(),
                    -0.3f * bag.movement().velocityY()
                );
            }
        }
    }
    
    private void playCutScene(GameContext game, long tick) {
        final MovementSystem motor = game.systems().motor();
        final WorldNavigationSystem navigator = game.systems().worldNavigator();
        final SpriteAnimSystem animSystem = game.systems().spriteAnim();

        letActorsMove(motor);
        
        if (tick == 130) {
            pacMan.show();
            pacMan.pos().set(WorldMap.TS * 3, GROUND_Y - 4);

            navigator.setMoveDir(pacMan, Direction.RIGHT);
            navigator.setSpeed(pacMan, 0);

            animSystem.select(pacMan, TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
            animSystem.stopSelected(pacMan);

            msPacMan.pos().set(WorldMap.TS * 5, GROUND_Y - 4);
            msPacMan.show();

            navigator.setMoveDir(msPacMan, Direction.RIGHT);
            navigator.setSpeed(msPacMan, 0);

            animSystem.select(msPacMan, ActorAnimationID.PAC_MUNCHING);
            animSystem.stopSelected(msPacMan);

            stork.show();
            stork.pos().set(RIGHT_BORDER, WorldMap.TS * 7);
            motor.setVelocity(stork, -0.8f, 0);

            animSystem.select(stork, ActorAnimationID.STORK_FLYING);
            animSystem.playSelected(stork);

            bag.setOpen(false);
            stork.setBagReleasedFromBeak(false);
        }
        else if (tick == 240) {
            // stork releases bag, bag starts falling
            motor.setVelocity(stork, -1f, 0); // faster, no bag to carry!
            stork.setBagReleasedFromBeak(true);

            bag.show();
            bag.pos().set(stork.pos().x() - 15, stork.pos().y() + 8);
            motor.setVelocity(bag, -0.5f, 0);
            motor.setAcceleration(bag, 0, 0.1f);
        }
        else if (tick == 320) {
            // reaches ground, starts bouncing
            motor.setVelocityX(bag, -0.5f);
        }
        else if (tick == 380) {
            bag.setOpen(true);
            motor.setVelocity(bag, 0, 0);
            motor.setAcceleration(bag, 0, 0);
        }
        else if (tick == 640) {
            darkness = true;
        }
    }
}