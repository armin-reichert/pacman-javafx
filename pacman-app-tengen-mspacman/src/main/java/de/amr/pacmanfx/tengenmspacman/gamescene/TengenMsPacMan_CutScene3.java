/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.entities.ActorAnimationID;
import de.amr.pacmanfx.core.model.entities.pac.Pac;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.entities.*;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.input.Joypad;
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

    public static final int TICK_EXPIRES = 660;

    private static final int GROUND_Y = WorldMap.TS * 24;
    private static final int RIGHT_BORDER = WorldMap.TS * 30;

    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;
    private Stork stork;
    private Bag bag;

    private boolean darkness;

    public TengenMsPacMan_CutScene3(GameAppContext appContext) {
        super(appContext);
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
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer animationContainer = appContext().ui().sprites().animations();

        // Quit cut scene when "START" button on "joypad" is pressed
        final Joypad joypad = input().joypad();
        actionBindings().bindActionToKeyCombination(appContext().commonActions().gameFlowActions().actionLetGameStateExpire(),
            joypad.keyForButton(JoypadButton.START));

        clapperboard = new Clapperboard(3, "JUNIOR");
        clapperboard.pos().set(3 * WorldMap.TS, 10 * WorldMap.TS);
        clapperboard.show();
        ClapperboardStateSystem.startFlapAnimation(clapperboard);

        final var factory = TengenMsPacMan_ActorFactory.instance();

        msPacMan = factory.createMsPacMan();
        msPacMan.spriteAnimation().setAnimations(renderConfig.createPacAnimations(animationContainer));

        pacMan = factory.createPacMan();
        pacMan.spriteAnimation().setAnimations(renderConfig.createPacAnimations(animationContainer));

        stork = new Stork();
        stork.spriteAnim().setAnimations(new StorkSpriteAnimationMap(animationContainer));

        bag = new Bag();
        bag.spriteAnim().setAnimations(new BagAnimationSpriteMap(animationContainer));

        darkness = false;

        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_3);
    }

    @Override
    public void onDeactivate() {
        appContext().ui().sounds().stop(PacManGameSoundID.INTERMISSION_3);
    }

    @Override
    public void onTick(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        final long gameStateTick = gameState().timer().tickCount();

        if (gameStateTick <= TICK_EXPIRES) {
            switch ((int) gameStateTick) {
                case 130 -> {
                    pacMan.show();
                    pacMan.pos().set(WorldMap.TS * 3, GROUND_Y - 4);

                    sys.worldNavigator().setMoveDir(pacMan, Direction.RIGHT);
                    sys.worldNavigator().setSpeed(pacMan, 0);

                    sys.spriteAnim().select(pacMan, TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    sys.spriteAnim().stopSelected(pacMan);

                    msPacMan.pos().set(WorldMap.TS * 5, GROUND_Y - 4);
                    msPacMan.show();

                    sys.worldNavigator().setMoveDir(msPacMan, Direction.RIGHT);
                    sys.worldNavigator().setSpeed(msPacMan, 0);

                    sys.spriteAnim().select(msPacMan, ActorAnimationID.PAC_MUNCHING);
                    sys.spriteAnim().stopSelected(msPacMan);

                    stork.show();
                    stork.pos().set(RIGHT_BORDER, WorldMap.TS * 7);
                    sys.motor().setVelocity(stork, -0.8f, 0);

                    sys.spriteAnim().select(stork, ActorAnimationID.STORK_FLYING);
                    sys.spriteAnim().playSelected(stork);

                    bag.setOpen(false);
                    stork.setBagReleasedFromBeak(false);
                }
                case 240 -> {
                    // stork releases bag, bag starts falling
                    sys.motor().setVelocity(stork, -1f, 0); // faster, no bag to carry!
                    stork.setBagReleasedFromBeak(true);

                    bag.show();
                    bag.pos().set(stork.pos().x() - 15, stork.pos().y() + 8);
                    sys.motor().setVelocity(bag, -0.5f, 0);
                    sys.motor().setAcceleration(bag, 0, 0.1f);
                }
                case 320 -> // reaches ground, starts bouncing
                    sys.motor().setVelocityX(bag, -0.5f);
                case 380 -> {
                    bag.setOpen(true);
                    sys.motor().setVelocity(bag, 0, 0);
                    sys.motor().setAcceleration(bag, 0, 0);
                }
                case 640 -> darkness = true;
                case TICK_EXPIRES -> gameState().triggerTimeout();
            }
        }

        ClapperboardStateSystem.update(clapperboard);

        sys.motor().move(stork);

        if (!bag.isOpen()) {
            sys.motor().move(bag);
            if (bag.pos().y() > GROUND_Y) {
                bag.pos().setY(GROUND_Y);
                sys.motor().setVelocity(bag,
                    0.9f * bag.movement().velocityX(),
                    -0.3f * bag.movement().velocityY()
                );
            }
        }

        BagAnimationSystem.update(bag);
    }
}