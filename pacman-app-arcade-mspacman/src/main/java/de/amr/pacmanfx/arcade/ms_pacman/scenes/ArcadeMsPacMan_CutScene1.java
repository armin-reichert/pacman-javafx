/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_ActorRenderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_HUDRenderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.animation.SingleSpriteActor;

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.*;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_PacAnimationManager.PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 */
public class ArcadeMsPacMan_CutScene1 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission.1";

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

    private ArcadeMsPacMan_HUDRenderer hudRenderer;
    private ArcadeMsPacMan_ActorRenderer actorSpriteRenderer;

    private SingleSpriteActor heart;
    private Clapperboard clapperboard;

    public ArcadeMsPacMan_CutScene1(GameUI ui) {
        super(ui);
    }
    
    @Override
    public void doInit() {
        final GameUI_Config uiConfig = ui.currentConfig();
        final var spriteSheet = (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        hudRenderer = (ArcadeMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas);
        actorSpriteRenderer = (ArcadeMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas);

        bindRendererProperties(hudRenderer, actorSpriteRenderer);

        context().game().hudData().score(true).levelCounter(true).livesCounter(false);

        pacMan = createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(pacMan));

        msPacMan = createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations(msPacMan));

        inky = createGhost(CYAN_GHOST_BASHFUL);
        inky.setAnimations(uiConfig.createGhostAnimations(inky));

        pinky = createGhost(PINK_GHOST_SPEEDY);
        pinky.setAnimations(uiConfig.createGhostAnimations(pinky));

        heart = new SingleSpriteActor(spriteSheet.sprite(SpriteID.HEART));

        clapperboard = new Clapperboard("1", "THEY MEET");
        clapperboard.setPosition(TS(3), TS(10));
        clapperboard.setFont(actorSpriteRenderer.arcadeFontTS());
        clapperboard.startAnimation();

        setState(STATE_CLAPPERBOARD, 120);
    }

    @Override
    protected void doEnd() {
        ui.soundManager().stop(MUSIC_ID);
    }

    @Override
    public void update() {
        switch (sceneState) {
            case STATE_CLAPPERBOARD -> updateStateClapperboard();
            case STATE_CHASED_BY_GHOSTS -> updateStateChasedByGhosts();
            case STATE_COMING_TOGETHER -> updateStateComingTogether();
            case STATE_IN_HEAVEN -> updateStateInHeaven();
            default -> throw new IllegalStateException("Illegal scene state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawHUD() {
        if (hudRenderer != null) {
            hudRenderer.drawHUD(context(), context().game().hudData(), sizeInPx());
        }
    }

    @Override
    public void drawSceneContent() {
        if (actorSpriteRenderer != null) {
            Stream.of(clapperboard, msPacMan, pacMan, inky, pinky, heart).forEach(actorSpriteRenderer::drawActor);
        }
    }

    // Scene controller state machine

    private static final byte STATE_CLAPPERBOARD = 0;
    private static final byte STATE_CHASED_BY_GHOSTS = 1;
    private static final byte STATE_COMING_TOGETHER = 2;
    private static final byte STATE_IN_HEAVEN = 3;

    private byte sceneState;
    private final TickTimer sceneTimer = new TickTimer("MsPacMan_CutScene1");

    private void setState(byte state, long ticks) {
        sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard() {
        clapperboard.tick();
        if (sceneTimer.atSecond(1)) {
            ui.soundManager().play(MUSIC_ID);
        } else if (sceneTimer.hasExpired()) {
            enterStateChasedByGhosts();
        }
    }

    private void enterStateChasedByGhosts() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(TS * (-2), UPPER_LANE_Y);
        pacMan.setSpeed(SPEED_PAC_CHASING);
        pacMan.playAnimation(PAC_MAN_MUNCHING);
        pacMan.show();

        inky.setMoveDir(Direction.RIGHT);
        inky.setWishDir(Direction.RIGHT);
        inky.setPosition(pacMan.x() - 6 * TS, pacMan.y());
        inky.setSpeed(SPEED_GHOST_CHASING);
        inky.playAnimation(ANIM_GHOST_NORMAL);
        inky.show();

        msPacMan.setMoveDir(Direction.LEFT);
        msPacMan.setPosition(TS * 30, LOWER_LANE_Y);
        msPacMan.setSpeed(SPEED_PAC_CHASING);
        msPacMan.playAnimation(ANIM_PAC_MUNCHING);
        msPacMan.show();

        pinky.setMoveDir(Direction.LEFT);
        pinky.setWishDir(Direction.LEFT);
        pinky.setPosition(msPacMan.x() + 6 * TS, msPacMan.y());
        pinky.setSpeed(SPEED_GHOST_CHASING);
        pinky.playAnimation(ANIM_GHOST_NORMAL);
        pinky.show();

        setState(STATE_CHASED_BY_GHOSTS, TickTimer.INDEFINITE);
    }

    private void updateStateChasedByGhosts() {
        if (inky.x() > TS * 30) {
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

        pinky.setPosition(msPacMan.x() - 5 * TS, msPacMan.y());
        pinky.setMoveDir(Direction.RIGHT);
        pinky.setWishDir(Direction.RIGHT);

        pacMan.setPosition(TS * 31, MIDDLE_LANE_Y);
        pacMan.setMoveDir(Direction.LEFT);

        inky.setPosition(pacMan.x() + 5 * TS, pacMan.y());
        inky.setMoveDir(Direction.LEFT);
        inky.setWishDir(Direction.LEFT);

        setState(STATE_COMING_TOGETHER, TickTimer.INDEFINITE);
    }

    private void updateStateComingTogether() {
        // Pac-Man and Ms. Pac-Man reach end position?
        if (pacMan.moveDir() == Direction.UP && pacMan.y() < UPPER_LANE_Y) {
            enterStateInHeaven();
        }

        // Pac-Man and Ms. Pac-Man meet?
        else if (pacMan.moveDir() == Direction.LEFT && pacMan.x() - msPacMan.x() < TS * 2) {
            pacMan.setMoveDir(Direction.UP);
            pacMan.setSpeed(SPEED_PAC_RISING);
            msPacMan.setMoveDir(Direction.UP);
            msPacMan.setSpeed(SPEED_PAC_RISING);
        }

        // Inky and Pinky collide?
        else if (inky.moveDir() == Direction.LEFT && inky.x() - pinky.x() < TS * 2) {
            inky.setMoveDir(Direction.RIGHT);
            inky.setWishDir(Direction.RIGHT);
            inky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            inky.setVelocity(inky.velocity().minus(0, 2.0f));
            inky.setAcceleration(0, 0.4f);

            pinky.setMoveDir(Direction.LEFT);
            pinky.setWishDir(Direction.LEFT);
            pinky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            pinky.setVelocity(pinky.velocity().minus(0, 2.0f));
            pinky.setAcceleration(0, 0.4f);
        }

        else {
            pacMan.move();
            msPacMan.move();
            inky.move();
            pinky.move();

            // Collision with ground?
            if (inky.y() > MIDDLE_LANE_Y) {
                inky.setY(MIDDLE_LANE_Y);
                inky.setAcceleration(Vector2f.ZERO);
            }
            if (pinky.y() > MIDDLE_LANE_Y) {
                pinky.setY(MIDDLE_LANE_Y);
                pinky.setAcceleration(Vector2f.ZERO);
            }
        }
    }

    private void enterStateInHeaven() {
        pacMan.setSpeed(0);
        pacMan.setMoveDir(Direction.LEFT);
        pacMan.animations().ifPresent(am -> {
            am.stop();
            am.reset();
        });

        msPacMan.setSpeed(0);
        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.animations().ifPresent(am -> {
            am.stop();
            am.reset();
        });

        inky.hide();
        pinky.hide();

        heart.setPosition((pacMan.x() + msPacMan.x()) * 0.5f, pacMan.y() - TS * 2);
        heart.show();

        setState(STATE_IN_HEAVEN, 3 * NUM_TICKS_PER_SEC);
    }

    private void updateStateInHeaven() {
        if (sceneTimer.hasExpired()) {
            context().gameController().letCurrentGameStateExpire();
        }
    }
}