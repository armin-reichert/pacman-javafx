/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.model.actors.ArcadeMsPacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.simulation.FrameContext;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.tengenmspacman.config.TengenMsPacManGameVariant.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.config.TengenMsPacManGameVariant.NES_SCREEN_WIDTH;

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
        final GameVariantConfig gameVariantConfig = appContext().variants().currentVariant().config();
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();

        // Quit cut scene when "START" button on "joypad" is pressed
        final Joypad joypad = input().joypad();
        actionBindings().bindActionToKeyCombination(appContext().commonActions().gameFlowActions().actionLetGameStateExpire(),
            joypad.keyForButton(JoypadButton.START));

        clapperboard = new Clapperboard(3, "JUNIOR");
        clapperboard.setPosition(3 * WorldMap.TS, 10 * WorldMap.TS);
        clapperboard.show();
        clapperboard.startAnimation();

        msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAnimations(gameVariantConfig.createPacAnimations(spriteAnimations));

        pacMan = TengenMsPacMan_ActorFactory.createPacMan();
        pacMan.setAnimations(gameVariantConfig.createPacAnimations(spriteAnimations));

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
    public void onTick(FrameContext frame) {
        final long gameStateTick = gameState().timer().tickCount();
        if (gameStateTick <= TICK_EXPIRES) {
            switch ((int) gameStateTick) {
                case 130 -> {
                    pacMan.setMoveDir(Direction.RIGHT);
                    pacMan.setPosition(WorldMap.TS * 3, GROUND_Y - 4);
                    pacMan.setSpeed(0);
                    pacMan.animations().select(TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    pacMan.animations().stopSelected();
                    pacMan.show();

                    msPacMan.setMoveDir(Direction.RIGHT);
                    msPacMan.setPosition(WorldMap.TS * 5, GROUND_Y - 4);
                    msPacMan.setSpeed(0);
                    msPacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
                    msPacMan.animations().stopSelected();
                    msPacMan.show();

                    stork.setPosition(RIGHT_BORDER, WorldMap.TS * 7);
                    stork.setVelocity(-0.8f, 0);
                    stork.setBagReleasedFromBeak(false);
                    stork.animations().select(ArcadeMsPacMan_AnimationID.STORK_FLYING);
                    stork.animations().playSelected();
                    stork.show();
                }
                case 240 -> {
                    // stork releases bag, bag starts falling
                    stork.setVelocity(-1f, 0); // faster, no bag to carry!
                    stork.setBagReleasedFromBeak(true);
                    flyingBag.setPosition(stork.x() - 15, stork.y() + 8);
                    flyingBag.setVelocity(-0.5f, 0);
                    flyingBag.setAcceleration(0, 0.1f);
                    flyingBag.show();
                }
                case 320 -> // reaches ground, starts bouncing
                    flyingBag.setVelX(-0.5f);
                case 380 -> {
                    flyingBag.setOpen(true);
                    flyingBag.setVelocity(0, 0);
                    flyingBag.setAcceleration(0, 0);
                }
                case 640 -> darkness = true;
                case TICK_EXPIRES -> gameState().triggerTimeout();
            }
        }

        clapperboard.tick();
        stork.move();
        if (!flyingBag.isOpen()) {
            flyingBag.move();
            if (flyingBag.y() > GROUND_Y) {
                flyingBag.setY(GROUND_Y);
                flyingBag.setVelocity(0.9f * flyingBag.velX(), -0.3f * flyingBag.velY());
            }
        }
    }
}