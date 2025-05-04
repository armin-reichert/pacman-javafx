/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Actor;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.media.MediaPlayer;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_SpriteSheet.HEART_SPRITE;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.model.actors.ActorAnimations.*;
import static de.amr.games.pacman.ui.GameAssets.ARCADE_WHITE;
import static de.amr.games.pacman.ui.Globals.THE_SOUND;
import static de.amr.games.pacman.ui.Globals.THE_UI_CONFIGS;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 *
 * @author Armin Reichert
 */
public class ArcadeMsPacMan_CutScene1 extends GameScene2D {

    private static final int UPPER_LANE_Y  = TS * 12;
    private static final int MIDDLE_LANE_Y = TS * 18;
    private static final int LOWER_LANE_Y  = TS * 24;

    private static final float SPEED_PAC_CHASING = 1.125f;
    private static final float SPEED_PAC_RISING = 0.75f;
    private static final float SPEED_GHOST_AFTER_COLLISION = 0.3f;
    private static final float SPEED_GHOST_CHASING = 1.25f;

    private Pac pacMan;
    private Pac msPacMan;
    private Ghost inky;
    private Ghost pinky;
    private Actor heart;
    private MediaPlayer music;
    private ClapperboardAnimation clapperboardAnimation;

    @Override
    public void doInit() {
        game().scoreVisibleProperty().set(true);

        pacMan = new Pac();
        msPacMan = new Pac();
        inky = new Ghost(CYAN_GHOST_ID, "Inky");
        pinky = new Ghost(PINK_GHOST_ID, "Pinky");
        heart = new Actor();

        ArcadeMsPacMan_SpriteSheet spriteSheet = THE_UI_CONFIGS.current().spriteSheet();
        msPacMan.setAnimations(new ArcadeMsPacMan_PacAnimations(spriteSheet));
        pacMan.setAnimations(new ArcadeMsPacMan_PacAnimations(spriteSheet));
        inky.setAnimations(new ArcadeMsPacMan_GhostAnimations(spriteSheet, inky.id()));
        pinky.setAnimations(new ArcadeMsPacMan_GhostAnimations(spriteSheet, pinky.id()));

        clapperboardAnimation = new ClapperboardAnimation("1", "THEY MEET");
        clapperboardAnimation.start();
        music = THE_SOUND.createSound("intermission.1");

        setState(STATE_CLAPPERBOARD, 120);
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        updateSceneState();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        gr.setScaling(scaling());
        gr.drawScores(game(), ARCADE_WHITE, arcadeFontScaledTS());
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) { // could also be VectorGraphicsRenderer!
            r.drawClapperBoard(clapperboardAnimation, tiles_to_px(3), tiles_to_px(10), arcadeFontScaledTS());
        }
        gr.drawAnimatedActor(msPacMan);
        gr.drawAnimatedActor(pacMan);
        gr.drawAnimatedActor(inky);
        gr.drawAnimatedActor(pinky);
        gr.drawActorSprite(heart, HEART_SPRITE);
        gr.drawLevelCounter(game().levelCounter(), sizeInPx());
    }

    // Scene controller state machine

    private static final byte STATE_CLAPPERBOARD = 0;
    private static final byte STATE_CHASED_BY_GHOSTS = 1;
    private static final byte STATE_COMING_TOGETHER = 2;
    private static final byte STATE_IN_HEAVEN = 3;

    private byte state;
    private final TickTimer stateTimer = new TickTimer("MsPacManCutScene1");

    private void setState(byte state, long ticks) {
        this.state = state;
        stateTimer.reset(ticks);
        stateTimer.start();
    }

    private void updateSceneState() {
        switch (state) {
            case STATE_CLAPPERBOARD -> updateStateClapperboard();
            case STATE_CHASED_BY_GHOSTS -> updateStateChasedByGhosts();
            case STATE_COMING_TOGETHER -> updateStateComingTogether();
            case STATE_IN_HEAVEN -> updateStateInHeaven();
            default -> throw new IllegalStateException("Illegal state: " + state);
        }
        stateTimer.doTick();
    }

    private void updateStateClapperboard() {
        clapperboardAnimation.tick();
        if (stateTimer.atSecond(1)) {
            music.play();
        } else if (stateTimer.hasExpired()) {
            enterStateChasedByGhosts();
        }
    }

    private void enterStateChasedByGhosts() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(TS * (-2), UPPER_LANE_Y);
        pacMan.setSpeed(SPEED_PAC_CHASING);
        pacMan.selectAnimation(ANIM_MR_PACMAN_MUNCHING);
        pacMan.startAnimation();
        pacMan.show();

        inky.setMoveAndWishDir(Direction.RIGHT);
        inky.setPosition(pacMan.position().minus(TS * 6, 0));
        inky.setSpeed(SPEED_GHOST_CHASING);
        inky.selectAnimation(ANIM_GHOST_NORMAL);
        inky.startAnimation();
        inky.show();

        msPacMan.setMoveDir(Direction.LEFT);
        msPacMan.setPosition(TS * 30, LOWER_LANE_Y);
        msPacMan.setSpeed(SPEED_PAC_CHASING);
        msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
        msPacMan.startAnimation();
        msPacMan.show();

        pinky.setMoveAndWishDir(Direction.LEFT);
        pinky.setPosition(msPacMan.position().plus(TS * 6, 0));
        pinky.setSpeed(SPEED_GHOST_CHASING);
        pinky.selectAnimation(ANIM_GHOST_NORMAL);
        pinky.startAnimation();
        pinky.show();

        setState(STATE_CHASED_BY_GHOSTS, TickTimer.INDEFINITE);
    }

    private void updateStateChasedByGhosts() {
        if (inky.posX() > TS * 30) {
            enterStateComingTogether();
        }
        else {
            pacMan.move();
            msPacMan.move();
            inky.move();
            pinky.move();
        }
    }

    private void enterStateComingTogether() {
        msPacMan.setPosition(TS * (-3), MIDDLE_LANE_Y);
        msPacMan.setMoveDir(Direction.RIGHT);

        pinky.setPosition(msPacMan.position().minus(TS * 5, 0));
        pinky.setMoveAndWishDir(Direction.RIGHT);

        pacMan.setPosition(TS * 31, MIDDLE_LANE_Y);
        pacMan.setMoveDir(Direction.LEFT);

        inky.setPosition(pacMan.position().plus(TS * 5, 0));
        inky.setMoveAndWishDir(Direction.LEFT);

        setState(STATE_COMING_TOGETHER, TickTimer.INDEFINITE);
    }

    private void updateStateComingTogether() {
        // Pac-Man and Ms. Pac-Man reach end position?
        if (pacMan.moveDir() == Direction.UP && pacMan.posY() < UPPER_LANE_Y) {
            enterStateInHeaven();
        }

        // Pac-Man and Ms. Pac-Man meet?
        else if (pacMan.moveDir() == Direction.LEFT && pacMan.posX() - msPacMan.posX() < TS * (2)) {
            pacMan.setMoveDir(Direction.UP);
            pacMan.setSpeed(SPEED_PAC_RISING);
            msPacMan.setMoveDir(Direction.UP);
            msPacMan.setSpeed(SPEED_PAC_RISING);
        }

        // Inky and Pinky collide?
        else if (inky.moveDir() == Direction.LEFT && inky.posX() - pinky.posX() < TS * (2)) {
            inky.setMoveAndWishDir(Direction.RIGHT);
            inky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            inky.setVelocity(inky.velocity().minus(0, 2.0f));
            inky.setAcceleration(0, 0.4f);

            pinky.setMoveAndWishDir(Direction.LEFT);
            pinky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            pinky.setVelocity(pinky.velocity().minus(0, 2.0f));
            pinky.setAcceleration(0, 0.4f);
        }
        else {
            pacMan.move();
            msPacMan.move();
            inky.move();
            pinky.move();
            if (inky.posY() > MIDDLE_LANE_Y) {
                inky.setPosition(inky.posX(), MIDDLE_LANE_Y);
                inky.setAcceleration(Vector2f.ZERO);
            }
            if (pinky.posY() > MIDDLE_LANE_Y) {
                pinky.setPosition(pinky.posX(), MIDDLE_LANE_Y);
                pinky.setAcceleration(Vector2f.ZERO);
            }
        }
    }

    private void enterStateInHeaven() {
        pacMan.setSpeed(0);
        pacMan.setMoveDir(Direction.LEFT);
        pacMan.stopAnimation();
        pacMan.resetAnimation();

        msPacMan.setSpeed(0);
        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.stopAnimation();
        msPacMan.resetAnimation();

        inky.setSpeed(0);
        inky.hide();

        pinky.setSpeed(0);
        pinky.hide();

        heart.setPosition((pacMan.posX() + msPacMan.posX()) / 2, pacMan.posY() - TS * (2));
        heart.show();

        setState(STATE_IN_HEAVEN, 3 * 60);
    }

    private void updateStateInHeaven() {
        if (stateTimer.hasExpired()) {
            THE_GAME_CONTROLLER.letCurrentStateExpire();
        }
    }
}