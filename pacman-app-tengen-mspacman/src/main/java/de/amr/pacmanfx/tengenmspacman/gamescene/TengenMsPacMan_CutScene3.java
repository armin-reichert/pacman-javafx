/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
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
    private Bag flyingBag;

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
        return flyingBag;
    }

    public boolean darkness() {
        return darkness;
    }

    @Override
    public void onActivate() {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();

        // Quit cut scene when "START" button on "joypad" is pressed
        final Joypad joypad = input().joypad();
        actionBindings().bindActionToKeyCombination(appContext().commonActions().gameFlowActions().actionLetGameStateExpire(),
            joypad.keyForButton(JoypadButton.START));

        clapperboard = new Clapperboard(3, "JUNIOR");
        clapperboard.position().set(3 * WorldMap.TS, 10 * WorldMap.TS);
        clapperboard.visibility().show();
        clapperboard.startAnimation();

        msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.animations = renderConfig.createPacAnimations(spriteAnimations);

        pacMan = TengenMsPacMan_ActorFactory.createPacMan();
        pacMan.animations = renderConfig.createPacAnimations(spriteAnimations);

        stork = new Stork(spriteAnimations);
        flyingBag = new Bag(spriteAnimations);

        darkness = false;

        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_3);
    }

    @Override
    public void onDeactivate() {
        appContext().ui().sounds().stop(PacManGameSoundID.INTERMISSION_3);
    }

    @Override
    public void onTick(GameContext gameContext) {
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        final long gameStateTick = gameState().timer().tickCount();

        if (gameStateTick <= TICK_EXPIRES) {
            switch ((int) gameStateTick) {
                case 130 -> {
                    pacMan.position().set(WorldMap.TS * 3, GROUND_Y - 4);
                    pacMan.visibility().show();

                    worldMovementSystem.setMoveDir(pacMan, Direction.RIGHT);
                    worldMovementSystem.setSpeed(pacMan, 0);

                    pacMan.animations.select(TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    pacMan.animations.stopSelected();

                    msPacMan.position().set(WorldMap.TS * 5, GROUND_Y - 4);
                    msPacMan.visibility().show();

                    worldMovementSystem.setMoveDir(msPacMan, Direction.RIGHT);
                    worldMovementSystem.setSpeed(msPacMan, 0);

                    msPacMan.animations.select(CommonAnimationID.PAC_MUNCHING);
                    msPacMan.animations.stopSelected();

                    stork.position().set(RIGHT_BORDER, WorldMap.TS * 7);
                    stork.visibility().show();
                    stork.movement().setVelocity(-0.8f, 0);
                    stork.animations.select(CommonAnimationID.STORK_FLYING);
                    stork.animations.playSelected();
                    stork.setBagReleasedFromBeak(false);
                }
                case 240 -> {
                    // stork releases bag, bag starts falling
                    stork.movement().setVelocity(-1f, 0); // faster, no bag to carry!
                    stork.setBagReleasedFromBeak(true);
                    flyingBag.position().set(stork.position().x - 15, stork.position().y + 8);
                    flyingBag.movement().setVelocity(-0.5f, 0);
                    flyingBag.movement().setAcceleration(0, 0.1f);
                    flyingBag.visibility().show();
                }
                case 320 -> // reaches ground, starts bouncing
                    flyingBag.movement().setVelX(-0.5f);
                case 380 -> {
                    flyingBag.setOpen(true);
                    flyingBag.movement().setVelocity(0, 0);
                    flyingBag.movement().setAcceleration(0, 0);
                }
                case 640 -> darkness = true;
                case TICK_EXPIRES -> gameState().triggerTimeout();
            }
        }

        clapperboard.tick();
        GameContext.SYSTEMS.movementSystem.moveAccelerated(stork);
        if (!flyingBag.isOpen()) {
            GameContext.SYSTEMS.movementSystem.moveAccelerated(flyingBag);
            if (flyingBag.position().y > GROUND_Y) {
                flyingBag.position().setY(GROUND_Y);
                flyingBag.movement().setVelocity(0.9f * flyingBag.movement().velX, -0.3f * flyingBag.movement().velY);
            }
        }
    }
}