/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
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
        clapperboard.show();
        clapperboard.startAnimation();

        final var factory = TengenMsPacMan_ActorFactory.instance();

        msPacMan = factory.createMsPacMan();
        msPacMan.assertComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        pacMan = factory.createPacMan();
        pacMan.assertComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(spriteAnimations));

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
        final GameSystems sys = gameContext.systems();

        final long gameStateTick = gameState().timer().tickCount();

        if (gameStateTick <= TICK_EXPIRES) {
            switch ((int) gameStateTick) {
                case 130 -> {
                    pacMan.position().set(WorldMap.TS * 3, GROUND_Y - 4);
                    pacMan.show();

                    sys.navigator().setMoveDir(pacMan, Direction.RIGHT);
                    sys.navigator().setSpeed(pacMan, 0);

                    sys.spriteAnim().select(pacMan, TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    sys.spriteAnim().stopSelected(pacMan);

                    msPacMan.position().set(WorldMap.TS * 5, GROUND_Y - 4);
                    msPacMan.show();

                    sys.navigator().setMoveDir(msPacMan, Direction.RIGHT);
                    sys.navigator().setSpeed(msPacMan, 0);

                    sys.spriteAnim().select(msPacMan, CommonAnimationID.PAC_MUNCHING);
                    sys.spriteAnim().stopSelected(msPacMan);

                    stork.position().set(RIGHT_BORDER, WorldMap.TS * 7);
                    stork.show();
                    sys.motor().setVelocity(stork, -0.8f, 0);

                    sys.spriteAnim().select(stork, CommonAnimationID.STORK_FLYING);
                    sys.spriteAnim().playSelected(stork);

                    flyingBag.setOpen(gameContext, false);
                    stork.setBagReleasedFromBeak(false);
                }
                case 240 -> {
                    // stork releases bag, bag starts falling
                    sys.motor().setVelocity(stork, -1f, 0); // faster, no bag to carry!
                    stork.setBagReleasedFromBeak(true);

                    flyingBag.position().set(stork.position().x - 15, stork.position().y + 8);
                    flyingBag.show();
                    sys.motor().setVelocity(flyingBag, -0.5f, 0);
                    sys.motor().setAcceleration(flyingBag, 0, 0.1f);
                }
                case 320 -> // reaches ground, starts bouncing
                    sys.motor().setVelocityX(flyingBag, -0.5f);
                case 380 -> {
                    flyingBag.setOpen(gameContext, true);
                    sys.motor().setVelocity(flyingBag, 0, 0);
                    sys.motor().setAcceleration(flyingBag, 0, 0);
                }
                case 640 -> darkness = true;
                case TICK_EXPIRES -> gameState().triggerTimeout();
            }
        }

        clapperboard.tick();

        sys.motor().moveAccelerated(stork);

        if (!flyingBag.isOpen()) {
            sys.motor().moveAccelerated(flyingBag);
            if (flyingBag.position().y > GROUND_Y) {
                flyingBag.position().setY(GROUND_Y);
                sys.motor().setVelocity(flyingBag,
                    0.9f * flyingBag.movement().velX(),
                    -0.3f * flyingBag.movement().velY()
                );
            }
        }
    }
}