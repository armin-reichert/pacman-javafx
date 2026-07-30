/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.GameEntity;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import java.util.List;

import static de.amr.basics.spriteanim.SpriteAnimationAccess.singleSpriteAnimation;
import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 */
public class ArcadeMsPacMan_CutScene1 extends AbstractGameScene2D {

    static final int UPPER_Y  = WorldMap.TS * 12;
    static final int MIDDLE_Y = WorldMap.TS * 18;
    static final int LOWER_Y  = WorldMap.TS * 24;

    static final float SPEED_PAC_CHASING = 1.125f;
    static final float SPEED_GHOST_CHASING = 1.25f;
    static final float SPEED_RISING = 0.75f;
    static final float SPEED_GHOST_AFTER_COLLISION = 0.3f;

    // Public for access by renderer
    public Pac pacMan;
    public Pac msPacMan;
    public Ghost inky;
    public Ghost pinky;
    public GameEntity heart;
    public Clapperboard clapperboard;

    public ArcadeMsPacMan_CutScene1(GameAppContext appContext) {
        super(appContext);
    }

    @Override
    public void onActivate() {
        initScene();
        setState(SceneState.CLAPPERBOARD, 120);
    }

    @Override
    public void onTick(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        switch (sceneState) {
            case CLAPPERBOARD -> updateStateClapperboard(sys);
            case CHASED_BY_GHOSTS -> updateStateChasedByGhosts(sys);
            case COMING_TOGETHER -> updateStateComingTogether(sys);
            case IN_HEAVEN -> updateStateInHeaven();
            default -> throw new IllegalStateException("Illegal scene state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    private void initScene() {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();
        final var spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();

        final var factory = new ArcadeMsPacMan_ActorFactory();

        pacMan = factory.createPacMan();
        pacMan.requireComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        msPacMan = factory.createMsPacMan();
        msPacMan.requireComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        inky = renderConfig.createAnimatedGhost(gameContext(), spriteAnimations, GhostPersonality.CYAN_GHOST_BASHFUL);

        pinky = renderConfig.createAnimatedGhost(gameContext(), spriteAnimations, GhostPersonality.PINK_GHOST_SPEEDY);

        heart = new GameEntity();
        heart.setComponent(SpriteAnim.class, new SpriteAnim());
        heart.requireComponent(SpriteAnim.class).setAnimations(singleSpriteAnimation(spriteSheet.findSprite(SpriteID.HEART)));

        clapperboard = new Clapperboard("1", "THEY MEET");
        clapperboard.position().set(tilesPx(3), tilesPx(10));
        clapperboard.startAnimation();
    }

    // Scene controller state machine

    private enum SceneState {CLAPPERBOARD, CHASED_BY_GHOSTS, COMING_TOGETHER, IN_HEAVEN}

    private SceneState sceneState;
    private final TickTimer sceneTimer = new TickTimer("Timer-MsPacMan_CutScene1");

    private void setState(SceneState state, long ticks) {
        sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard(GameSystems sys) {
        clapperboard.tick();
        if (sceneTimer.atSecond(1)) {
            appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_1);
        } else if (sceneTimer.hasExpired()) {
            enterStateChasedByGhosts(sys);
        }
    }

    private void enterStateChasedByGhosts(GameSystems sys) {
        pacMan.position().set(WorldMap.TS * (-2), UPPER_Y);
        pacMan.show();

        sys.navigator().setMoveDir(pacMan, Direction.RIGHT);
        sys.navigator().setSpeed(pacMan, SPEED_PAC_CHASING);

        sys.spriteAnim().select(pacMan, CommonAnimationID.MR_PAC_MAN_MUNCHING);
        sys.spriteAnim().playSelected(pacMan);

        inky.position().set(pacMan.position().x - 6 * WorldMap.TS, pacMan.position().y);
        inky.show();

        sys.navigator().setSpeed(inky, SPEED_GHOST_CHASING);
        sys.navigator().setMoveDir(inky, Direction.RIGHT);
        sys.navigator().setWishDir(inky, Direction.RIGHT);

        sys.spriteAnim().select(inky, CommonAnimationID.GHOST_NORMAL);
        sys.spriteAnim().playSelected(inky);

        msPacMan.position().set(WorldMap.TS * 30, LOWER_Y);
        msPacMan.show();

        sys.navigator().setMoveDir(msPacMan, Direction.LEFT);
        sys.navigator().setSpeed(msPacMan, SPEED_PAC_CHASING);

        sys.spriteAnim().select(msPacMan, CommonAnimationID.PAC_MUNCHING);
        sys.spriteAnim().playSelected(msPacMan);

        pinky.position().set(msPacMan.position().x + 6 * WorldMap.TS, msPacMan.position().y);
        pinky.show();

        sys.navigator().setMoveDir(pinky, Direction.LEFT);
        sys.navigator().setWishDir(pinky, Direction.LEFT);
        sys.navigator().setSpeed(pinky, SPEED_GHOST_CHASING);

        sys.spriteAnim().select(pinky, CommonAnimationID.GHOST_NORMAL);
        sys.spriteAnim().playSelected(pinky);

        setState(SceneState.CHASED_BY_GHOSTS, TickTimer.INDEFINITE);
    }

    private void updateStateChasedByGhosts(GameSystems sys) {
        if (inky.position().x > WorldMap.TS * 30) {
            enterStateComingTogether(sys);
        }
        else {
            List.of(pacMan, msPacMan, inky, pinky).forEach(sys.motor()::moveAccelerated);
        }
    }

    private void enterStateComingTogether(GameSystems sys) {
        msPacMan.position().set(WorldMap.TS * (-3), MIDDLE_Y);
        sys.navigator().setMoveDir(msPacMan, Direction.RIGHT);

        pinky.position().set(msPacMan.position().x - 5 * WorldMap.TS, msPacMan.position().y);
        sys.navigator().setMoveDir(pinky, Direction.RIGHT);
        sys.navigator().setWishDir(pinky, Direction.RIGHT);

        pacMan.position().set(WorldMap.TS * 31, MIDDLE_Y);
        sys.navigator().setMoveDir(pacMan, Direction.LEFT);

        inky.position().set(pacMan.position().x + 5 * WorldMap.TS, pacMan.position().y);
        sys.navigator().setMoveDir(inky, Direction.LEFT);
        sys.navigator().setWishDir(inky, Direction.LEFT);

        setState(SceneState.COMING_TOGETHER, TickTimer.INDEFINITE);
    }

    private void updateStateComingTogether(GameSystems sys) {
        // Pac-Man and Ms. Pac-Man reach end position?
        if (pacMan.worldNavigation().moveDir() == Direction.UP && pacMan.position().y < UPPER_Y) {
            enterStateInHeaven(sys);
        }

        // Pac-Man and Ms. Pac-Man meet?
        else if (pacMan.worldNavigation().moveDir() == Direction.LEFT && pacMan.position().x - msPacMan.position().x < WorldMap.TS * 2) {
            sys.navigator().setMoveDir(pacMan, Direction.UP);
            sys.navigator().setSpeed(pacMan, SPEED_RISING);
            sys.navigator().setMoveDir(msPacMan, Direction.UP);
            sys.navigator().setSpeed(msPacMan, SPEED_RISING);
        }

        // Inky and Pinky collide?
        else if (inky.worldNavigation().moveDir() == Direction.LEFT && inky.position().x - pinky.position().x < WorldMap.TS * 2) {
            sys.navigator().setMoveDir(inky, Direction.RIGHT);
            sys.navigator().setWishDir(inky, Direction.RIGHT);
            sys.navigator().setSpeed(inky, SPEED_GHOST_AFTER_COLLISION);

            sys.motor().setVelocityY(inky, inky.movement().velY() - 2.0f);
            sys.motor().setAcceleration(inky, 0, 0.4f);

            sys.navigator().setMoveDir(pinky, Direction.LEFT);
            sys.navigator().setWishDir(pinky, Direction.LEFT);
            sys.navigator().setSpeed(pinky, SPEED_GHOST_AFTER_COLLISION);

            sys.motor().setVelocityY(pinky, pinky.movement().velY() - 2.0f);
            sys.motor().setAcceleration(pinky, 0, 0.4f);
        }

        else {
            List.of(pacMan, msPacMan, inky, pinky).forEach(sys.motor()::moveAccelerated);

            // Collision with ground?
            if (inky.position().y > MIDDLE_Y) {
                inky.position().setY(MIDDLE_Y);
                inky.movement().setAcceleration(0, 0);
            }
            if (pinky.position().y > MIDDLE_Y) {
                pinky.position().setY(MIDDLE_Y);
                pinky.movement().setAcceleration(0, 0);
            }
        }
    }

    private void enterStateInHeaven(GameSystems sys) {
        sys.navigator().setSpeed(pacMan, 0);
        sys.navigator().setMoveDir(pacMan, Direction.LEFT);

        sys.spriteAnim().stopSelected(pacMan);
        sys.spriteAnim().resetSelected(pacMan);

        sys.navigator().setSpeed(msPacMan, 0);
        sys.navigator().setMoveDir(msPacMan, Direction.RIGHT);

        sys.spriteAnim().stopSelected(msPacMan);
        sys.spriteAnim().resetSelected(msPacMan);

        inky.hide();
        pinky.hide();

        heart.position().set((pacMan.position().x + msPacMan.position().x) * 0.5f, pacMan.position().y - WorldMap.TS * 2);
        heart.show();

        setState(SceneState.IN_HEAVEN, 3L * GameConstants.SIMULATION_FPS);
    }

    private void updateStateInHeaven() {
        if (sceneTimer.hasExpired()) {
            gameState().triggerTimeout();
        }
    }
}