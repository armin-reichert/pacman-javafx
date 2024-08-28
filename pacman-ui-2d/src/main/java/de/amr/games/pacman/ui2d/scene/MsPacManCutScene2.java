/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.rendering.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 *
 * @author Armin Reichert
 */
public class MsPacManCutScene2 extends GameScene2D {

    private static class Data {
        public static final int UPPER_LANE_Y = TS * 12;
        public static final int MIDDLE_LANE_Y = TS * 18;
        public static final int LOWER_LANE_Y = TS * 24;
        public final Pac pacMan = new Pac();
        public final Pac msPac = new Pac();
    }

    private static class MsPacManCutScene2Controller {

        public static final byte STATE_FLAP = 0;
        public static final byte STATE_CHASING = 1;

        private byte state;
        private final TickTimer stateTimer = new TickTimer("MsPacManIntermission2");

        public void changeState(byte state, long ticks) {
            this.state = state;
            stateTimer.reset(ticks);
            stateTimer.start();
        }

        public void tick(Data data) {
            switch (state) {
                case STATE_FLAP:
                    updateStateFlap(data);
                    break;
                case STATE_CHASING:
                    updateStateChasing(data);
                    break;
                default:
                    throw new IllegalStateException("Illegal state: " + state);

            }
            stateTimer.tick();
        }

        private void updateStateFlap(Data data) {
            if (stateTimer.hasExpired()) {
                GameController.it().gameModel(GameVariant.MS_PACMAN).publishGameEvent(GameEventType.INTERMISSION_STARTED);
                enterStateChasing(data);
            }
        }

        private void enterStateChasing(Data data) {
            data.pacMan.setMoveDir(Direction.RIGHT);
            data.pacMan.selectAnimation(Pac.ANIM_HUSBAND_MUNCHING);
            data.pacMan.animations().ifPresent(Animations::startSelected);
            data.msPac.setMoveDir(Direction.RIGHT);
            data.msPac.selectAnimation(Pac.ANIM_MUNCHING);
            data.msPac.animations().ifPresent(Animations::startSelected);

            changeState(STATE_CHASING, TickTimer.INDEFINITE);
        }

        private void updateStateChasing(Data data) {
            if (stateTimer.atSecond(4.5)) {
                data.pacMan.setPosition(TS * (-2), Data.UPPER_LANE_Y);
                data.pacMan.setMoveDir(Direction.RIGHT);
                data.pacMan.setSpeed(2.0f);
                data.pacMan.show();
                data.msPac.setPosition(TS * (-8), Data.UPPER_LANE_Y);
                data.msPac.setMoveDir(Direction.RIGHT);
                data.msPac.setSpeed(2.0f);
                data.msPac.show();
            } else if (stateTimer.atSecond(9)) {
                data.pacMan.setPosition(TS * 36, Data.LOWER_LANE_Y);
                data.pacMan.setMoveDir(Direction.LEFT);
                data.pacMan.setSpeed(2.0f);
                data.msPac.setPosition(TS * 30, Data.LOWER_LANE_Y);
                data.msPac.setMoveDir(Direction.LEFT);
                data.msPac.setSpeed(2.0f);
            } else if (stateTimer.atSecond(13.5)) {
                data.pacMan.setMoveDir(Direction.RIGHT);
                data.pacMan.setSpeed(2.0f);
                data.msPac.setPosition(TS * (-8), Data.MIDDLE_LANE_Y);
                data.msPac.setMoveDir(Direction.RIGHT);
                data.msPac.setSpeed(2.0f);
                data.pacMan.setPosition(TS * (-2), Data.MIDDLE_LANE_Y);
            } else if (stateTimer.atSecond(17.5)) {
                data.pacMan.setPosition(TS * 42, Data.UPPER_LANE_Y);
                data.pacMan.setMoveDir(Direction.LEFT);
                data.pacMan.setSpeed(4.0f);
                data.msPac.setPosition(TS * 30, Data.UPPER_LANE_Y);
                data.msPac.setMoveDir(Direction.LEFT);
                data.msPac.setSpeed(4.0f);
            } else if (stateTimer.atSecond(18.5)) {
                data.pacMan.setPosition(TS * (-2), Data.LOWER_LANE_Y);
                data.pacMan.setMoveDir(Direction.RIGHT);
                data.pacMan.setSpeed(4.0f);
                data.msPac.setPosition(TS * (-14), Data.LOWER_LANE_Y);
                data.msPac.setMoveDir(Direction.RIGHT);
                data.msPac.setSpeed(4.0f);
            } else if (stateTimer.atSecond(23)) {
                GameController.it().terminateCurrentState();
                return;
            }
            data.pacMan.move();
            data.msPac.move();
        }
    }

    private Data data;
    private MsPacManCutScene2Controller sceneController;
    private ClapperboardAnimation clapAnimation;

    @Override
    public boolean isCreditVisible() {
        return !context.game().hasCredit();
    }

    @Override
    public void init() {
        super.init();
        context.setScoreVisible(true);

        var sheet = (MsPacManGameSpriteSheet) context.spriteSheet(context.game().variant());

        data = new Data();
        data.msPac.setAnimations(new MsPacManGamePacAnimations(data.msPac, sheet));
        data.pacMan.setAnimations(new MsPacManGamePacAnimations(data.pacMan, sheet));
        clapAnimation = new ClapperboardAnimation("2", "THE CHASE");
        clapAnimation.start();

        sceneController = new MsPacManCutScene2Controller();
        sceneController.changeState(MsPacManCutScene2Controller.STATE_FLAP, 120);
    }

    @Override
    public void update() {
        sceneController.tick(data);
        clapAnimation.tick();
    }

    @Override
    public void drawSceneContent() {
        spriteRenderer.drawClapperBoard(g,
            context.assets().font("font.arcade", s(8)),
            context.assets().color("palette.pale"),
            clapAnimation, t(3), t(10));
        spriteRenderer.drawPac(g, data.msPac);
        spriteRenderer.drawPac(g, data.pacMan);
        drawLevelCounter(g);
    }
}