/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 *
 * @author Armin Reichert
 */
public class MsPacManCutScene1 extends GameScene2D {

    private static class MsPacManCutScene1Controller {

        public static final byte STATE_FLAP = 0;
        public static final byte STATE_CHASED_BY_GHOSTS = 1;
        public static final byte STATE_COMING_TOGETHER = 2;
        public static final byte STATE_IN_HEAVEN = 3;

        public static final int UPPER_LANE_Y = TS * 12;
        public static final int MIDDLE_LANE_Y = TS * 18;
        public static final int LOWER_LANE_Y = TS * 24;

        public static final float SPEED_PAC_CHASING = 1.125f;
        public static final float SPEED_PAC_RISING = 0.75f;
        public static final float SPEED_GHOST_AFTER_COLLISION = 0.3f;
        public static final float SPEED_GHOST_CHASING = 1.25f;

        private byte state;
        private final TickTimer stateTimer = new TickTimer("MsPacManIntermission1");

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
                case STATE_CHASED_BY_GHOSTS:
                    updateStateChasedByGhosts(data);
                    break;
                case STATE_COMING_TOGETHER:
                    updateStateComingTogether(data);
                    break;
                case STATE_IN_HEAVEN:
                    if (stateTimer.hasExpired()) {
                        GameController.it().terminateCurrentState();
                        return;
                    }
                    break;
                default:
                    throw new IllegalStateException("Illegal state: " + state);
            }
            stateTimer.tick();
        }

        private void updateStateFlap(Data data) {
            if (stateTimer.atSecond(1)) {
                GameController.it().gameModel(GameVariant.MS_PACMAN).publishGameEvent(GameEventType.INTERMISSION_STARTED);
            } else if (stateTimer.hasExpired()) {
                enterStateChasedByGhosts(data);
            }
        }

        private void enterStateChasedByGhosts(Data data) {
            data.pacMan.setMoveDir(Direction.RIGHT);
            data.pacMan.setPosition(TS * (-2), UPPER_LANE_Y);
            data.pacMan.setSpeed(SPEED_PAC_CHASING);
            data.pacMan.selectAnimation(Pac.ANIM_HUSBAND_MUNCHING);
            data.pacMan.animations().ifPresent(Animations::startSelected);
            data.pacMan.show();

            data.inky.setMoveAndWishDir(Direction.RIGHT);
            data.inky.setPosition(data.pacMan.position().minus(TS * 6, 0));
            data.inky.setSpeed(SPEED_GHOST_CHASING);
            data.inky.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
            data.inky.startAnimation();
            data.inky.show();

            data.msPac.setMoveDir(Direction.LEFT);
            data.msPac.setPosition(TS * 30, LOWER_LANE_Y);
            data.msPac.setSpeed(SPEED_PAC_CHASING);
            data.msPac.selectAnimation(Pac.ANIM_MUNCHING);
            data.msPac.animations().ifPresent(Animations::startSelected);
            data.msPac.show();

            data.pinky.setMoveAndWishDir(Direction.LEFT);
            data.pinky.setPosition(data.msPac.position().plus(TS * 6, 0));
            data.pinky.setSpeed(SPEED_GHOST_CHASING);
            data.pinky.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
            data.pinky.startAnimation();
            data.pinky.show();

            changeState(STATE_CHASED_BY_GHOSTS, TickTimer.INDEFINITE);
        }

        private void updateStateChasedByGhosts(Data data) {
            if (data.inky.posX() > TS * 30) {
                enterStateComingTogether(data);
            } else {
                data.pacMan.move();
                data.msPac.move();
                data.inky.move();
                data.pinky.move();
            }
        }

        private void enterStateComingTogether(Data data) {
            data.msPac.setPosition(TS * (-3), MIDDLE_LANE_Y);
            data.msPac.setMoveDir(Direction.RIGHT);
            data.pinky.setPosition(data.msPac.position().minus(TS * 5, 0));
            data.pinky.setMoveAndWishDir(Direction.RIGHT);
            data.pacMan.setPosition(TS * 31, MIDDLE_LANE_Y);
            data.pacMan.setMoveDir(Direction.LEFT);
            data.inky.setPosition(data.pacMan.position().plus(TS * 5, 0));
            data.inky.setMoveAndWishDir(Direction.LEFT);
            changeState(STATE_COMING_TOGETHER, TickTimer.INDEFINITE);
        }

        private void updateStateComingTogether(Data data) {
            // Pac-Man and Ms. Pac-Man reach end position?
            if (data.pacMan.moveDir() == Direction.UP && data.pacMan.posY() < UPPER_LANE_Y) {
                enterStateInHeaven(data);
            }

            // Pac-Man and Ms. Pac-Man meet?
            else if (data.pacMan.moveDir() == Direction.LEFT && data.pacMan.posX() - data.msPac.posX() < TS * (2)) {
                data.pacMan.setMoveDir(Direction.UP);
                data.pacMan.setSpeed(SPEED_PAC_RISING);
                data.msPac.setMoveDir(Direction.UP);
                data.msPac.setSpeed(SPEED_PAC_RISING);
            }

            // Inky and Pinky collide?
            else if (data.inky.moveDir() == Direction.LEFT && data.inky.posX() - data.pinky.posX() < TS * (2)) {
                data.inky.setMoveAndWishDir(Direction.RIGHT);
                data.inky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
                data.inky.setVelocity(data.inky.velocity().minus(0, 2.0f));
                data.inky.setAcceleration(0, 0.4f);

                data.pinky.setMoveAndWishDir(Direction.LEFT);
                data.pinky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
                data.pinky.setVelocity(data.pinky.velocity().minus(0, 2.0f));
                data.pinky.setAcceleration(0, 0.4f);
            } else {
                data.pacMan.move();
                data.msPac.move();
                data.inky.move();
                data.pinky.move();
                if (data.inky.posY() > MIDDLE_LANE_Y) {
                    data.inky.setPosition(data.inky.posX(), MIDDLE_LANE_Y);
                    data.inky.setAcceleration(Vector2f.ZERO);
                }
                if (data.pinky.posY() > MIDDLE_LANE_Y) {
                    data.pinky.setPosition(data.pinky.posX(), MIDDLE_LANE_Y);
                    data.pinky.setAcceleration(Vector2f.ZERO);
                }
            }
        }

        private void enterStateInHeaven(Data data) {
            data.pacMan.setSpeed(0);
            data.pacMan.setMoveDir(Direction.LEFT);
            data.pacMan.animations().ifPresent(Animations::stopSelected);
            data.pacMan.animations().ifPresent(Animations::resetSelected);

            data.msPac.setSpeed(0);
            data.msPac.setMoveDir(Direction.RIGHT);
            data.msPac.animations().ifPresent(Animations::stopSelected);
            data.msPac.animations().ifPresent(Animations::resetSelected);

            data.inky.setSpeed(0);
            data.inky.hide();

            data.pinky.setSpeed(0);
            data.pinky.hide();

            data.heart.setPosition((data.pacMan.posX() + data.msPac.posX()) / 2, data.pacMan.posY() - TS * (2));
            data.heart.show();

            changeState(STATE_IN_HEAVEN, 3 * 60);
        }
    }

    private static class Data {
        final Pac pacMan = new Pac();
        final Pac msPac = new Pac();
        final Ghost inky = new Ghost(GameModel.CYAN_GHOST, null);
        final Ghost pinky = new Ghost(GameModel.PINK_GHOST, null);
        final Entity heart = new Entity();
    }

    private MsPacManCutScene1Controller sceneController;
    private Data data;
    private ClapperboardAnimation clapAnimation;
    private MsPacManGameSpriteSheet sheet;

    @Override
    public boolean isCreditVisible() {
        return !context.game().hasCredit();
    }

    @Override
    public void init() {
        super.init();
        context.setScoreVisible(true);
        sheet = (MsPacManGameSpriteSheet) context.spriteSheet(context.game().variant());
        data = new Data();
        data.msPac.setAnimations(new MsPacManGamePacAnimations(data.msPac, sheet));
        data.pacMan.setAnimations(new MsPacManGamePacAnimations(data.pacMan, sheet));
        data.inky.setAnimations(new MsPacManGameGhostAnimations(data.inky, sheet));
        data.pinky.setAnimations(new MsPacManGameGhostAnimations(data.pinky, sheet));
        clapAnimation = new ClapperboardAnimation("1", "THEY MEET");
        clapAnimation.start();

        sceneController = new MsPacManCutScene1Controller();
        sceneController.changeState(MsPacManCutScene1Controller.STATE_FLAP, 120);
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
        spriteRenderer.drawGhost(g, data.inky);
        spriteRenderer.drawGhost(g, data.pinky);
        spriteRenderer.drawEntitySprite(g, data.heart, sheet.heartSprite());
        drawLevelCounter(g);
    }
}