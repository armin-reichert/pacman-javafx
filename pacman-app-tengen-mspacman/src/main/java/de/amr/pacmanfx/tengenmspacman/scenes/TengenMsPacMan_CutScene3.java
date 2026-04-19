/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.basics.fsm.State;
import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengenmspacman.model.actor.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.sound.SoundID;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 3: "Junior".
 *
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 */
public class TengenMsPacMan_CutScene3 extends GameScene2D {

    public static final Vector2i SIZE = new Vector2i(NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT);

    public static final int TICK_EXPIRES = 660;

    private static final int GROUND_Y = TS * 24;
    private static final int RIGHT_BORDER = TS * 30;

    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;
    private Stork stork;
    private Bag flyingBag;

    private boolean darkness;

    public TengenMsPacMan_CutScene3(GameUI ui) {
        super(ui);
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
    public void onSceneStart() {
        final UIConfig uiConfig = ui.currentConfig();

        actionBindings.add(
            ACTION_LET_GAME_STATE_EXPIRE,
            Input.instance().joypad.keyForButton(JoypadButton.START)
        );

        clapperboard = new Clapperboard(3, "JUNIOR");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.show();
        clapperboard.startAnimation();

        msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations());

        pacMan = TengenMsPacMan_ActorFactory.createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations());

        stork = new Stork();
        flyingBag = new Bag();

        darkness = false;

        ui.soundManager().play(SoundID.INTERMISSION_3);
    }

    @Override
    public void onSceneEnd() {
        ui.soundManager().stop(SoundID.INTERMISSION_3);
    }

    @Override
    public void onTick(long tick) {
        final State<Game> gameState = gameContext().game().flow().state();
        final long gameStateTick = gameState.timer().tickCount();
        if (gameStateTick <= TICK_EXPIRES) {
            switch ((int) gameStateTick) {
                case 130 -> {
                    pacMan.setMoveDir(Direction.RIGHT);
                    pacMan.setPosition(TS * 3, GROUND_Y - 4);
                    pacMan.setSpeed(0);
                    pacMan.selectAnimation(TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    pacMan.animations().stopSelectedAnimation();
                    pacMan.show();

                    msPacMan.setMoveDir(Direction.RIGHT);
                    msPacMan.setPosition(TS * 5, GROUND_Y - 4);
                    msPacMan.setSpeed(0);
                    msPacMan.selectAnimation(ArcadePacMan_AnimationID.PAC_MUNCHING);
                    msPacMan.animations().stopSelectedAnimation();
                    msPacMan.show();

                    stork.setPosition(RIGHT_BORDER, TS * 7);
                    stork.setVelocity(-0.8f, 0);
                    stork.setBagReleasedFromBeak(false);
                    stork.selectAnimation(Stork.AnimationID.FLYING);
                    stork.playAnimation();
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
                    flyingBag.setVelocity(-0.5f, flyingBag.velocity().y());
                case 380 -> {
                    flyingBag.setOpen(true);
                    flyingBag.setVelocity(Vector2f.ZERO);
                    flyingBag.setAcceleration(Vector2f.ZERO);
                }
                case 640 -> darkness = true;
                case TICK_EXPIRES -> gameState.expire();
            }
        }

        clapperboard.tick();
        stork.move();
        if (!flyingBag.isOpen()) {
            flyingBag.move();
            Vector2f velocity = flyingBag.velocity();
            if (flyingBag.y() > GROUND_Y) {
                flyingBag.setY(GROUND_Y);
                flyingBag.setVelocity(0.9f * velocity.x(), -0.3f * velocity.y());
            }
        }
    }

    @Override
    public Vector2i unscaledSceneSize() { return SIZE; }
}