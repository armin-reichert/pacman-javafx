/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.gamescene;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.systems.MovementSystem;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacManSoundID;
import de.amr.pacmanfx.tengenmspacman.flow.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;
import de.amr.pacmanfx.ui.sound.SoundID;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.core.model.world.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameVariantConfig.*;

public class TengenMsPacMan_CutScene4 extends AbstractGameScene2D {

    public static final int TICK_EXPIRES = 1512;

    private static final int LEFT_BORDER = TS;
    private static final int RIGHT_BORDER = TS * (NES_SCREEN_TILES.x() - 2);

    private static final int LOWER_LANE = TS * 21; // TODO not sure

    private Pac pacMan;
    private Pac msPacMan;
    private List<Pac> juniors;
    private List<Long> juniorCreationTimes;
    private Clapperboard clapperboard;

    public TengenMsPacMan_CutScene4(GameAppContext appContext) {
        super(appContext);
        unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
    }

    public Pac pacMan() {
        return pacMan;
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    public List<Pac> juniors() {
        return Collections.unmodifiableList(juniors);
    }

    public Clapperboard clapperboard() {
        return clapperboard;
    }

    @Override
    public void onActivate() {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();

        clapperboard = new Clapperboard(4, "THE END");
        clapperboard.position().set(tilesPx(3), tilesPx(10));
        clapperboard.visibility().show();
        clapperboard.startAnimation();

        msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.animations = renderConfig.createPacAnimations(spriteAnimations);

        pacMan = TengenMsPacMan_ActorFactory.createPacMan();
        pacMan.animations = renderConfig.createPacAnimations(spriteAnimations);

        juniors = new ArrayList<>();
        juniorCreationTimes = new ArrayList<>();

        appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_4);
    }

    @Override
    public void onDeactivate() {
        appContext().ui().sounds().stop(PacManGameSoundID.INTERMISSION_4);
    }

    @Override
    public void onTick(GameContext gameContext) {
        final MovementSystem movementSystem = gameContext.systems().movementSystem;
        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;
        
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final long gameStateTick = gameState().timer().tickCount();

        clapperboard.tick();

        movementSystem.moveAccelerated(pacMan);
        movementSystem.moveAccelerated(msPacMan);
        for (int i = 0; i < juniors.size(); ++i) {
            updateJunior(movementSystem, worldMovementSystem, gameStateTick, i);
        }

        if (gameStateTick <= TICK_EXPIRES) {
            final short eventTick = (short) gameStateTick;
            switch (eventTick) {
                case 130 -> {
                    pacMan.position().set(LEFT_BORDER, LOWER_LANE);
                    pacMan.visibility().show();

                    worldMovementSystem.setMoveDir(pacMan, Direction.RIGHT);
                    worldMovementSystem.setSpeed(pacMan, 1f);

                    pacMan.animations.select(TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    pacMan.animations.playSelected();

                    msPacMan.position().set(RIGHT_BORDER, LOWER_LANE);
                    msPacMan.visibility().show();

                    worldMovementSystem.setMoveDir(msPacMan, Direction.LEFT);
                    worldMovementSystem.setSpeed(msPacMan, 1f);

                    msPacMan.animations.select(CommonAnimationID.PAC_MUNCHING);
                    msPacMan.animations.playSelected();
                }
                case 230 -> {
                    worldMovementSystem.setSpeed(pacMan, 0);
                    pacMan.animations.stopSelected();
                    pacMan.animations.resetSelected();

                    worldMovementSystem.setSpeed(msPacMan, 0);
                    msPacMan.animations.stopSelected();
                    msPacMan.animations.resetSelected();
                }
                case 400 -> {
                    pacMan.animations.select(TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    pacMan.animations.playSelected();

                    msPacMan.animations.select(CommonAnimationID.PAC_MUNCHING);
                    msPacMan.animations.playSelected();
                }
                case 520 -> {
                    pacMan.animations.select(TengenMsPacMan_AnimationID.MR_PAC_MAN_WAVING_HAND);
                    msPacMan.animations.select(TengenMsPacMan_AnimationID.MS_PAC_MAN_WAVING_HAND);
                }
                case 527 -> {
                    pacMan.animations.playSelected();
                    msPacMan.animations.playSelected();
                }
                case 648 -> {
                    pacMan.animations.select(TengenMsPacMan_AnimationID.MR_PAC_MAN_TURNING_AWAY);
                    pacMan.animations.playSelected();
                    msPacMan.animations.select(TengenMsPacMan_AnimationID.MS_PAC_MAN_TURNING_AWAY);
                    msPacMan.animations.playSelected();
                }
                case 650 -> {
                    worldMovementSystem.setSpeed(pacMan, 1.5f); // TODO not sure
                    worldMovementSystem.setMoveDir(pacMan, Direction.UP);

                    worldMovementSystem.setSpeed(msPacMan, 1.5f); // TODO not sure
                    worldMovementSystem.setMoveDir(msPacMan, Direction.UP);
                }
                case 720 -> {
                    pacMan.visibility().hide();
                    msPacMan.visibility().hide();
                }
                case 904, 968, 1032, 1096, 1160, 1224, 1288, 1352 -> spawnJunior(worldMovementSystem, renderConfig, gameStateTick);
                case 1500 -> optSoundEffects().ifPresent(GameSoundEffects::stopAll);
                case TICK_EXPIRES -> gameContext().flow().enterState(gameContext(), TengenMsPacMan_GameState.GAME_PREPARATION.state());
            }
        }
    }

    private void spawnJunior(WorldMovementSystem worldMovementSystem, GameVariantRenderConfig renderConfig, long tick) {
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();

        double randomX = 8 * TS + (8 * TS) * Math.random();

        final Pac junior = TengenMsPacMan_ActorFactory.createPacMan();
        junior.position().set((float) randomX, unscaledHeight() - 4 * TS);
        junior.visibility().show();
        worldMovementSystem.setMoveDir(junior, Direction.UP);
        worldMovementSystem.setSpeed(junior, 2);

        junior.animations = renderConfig.createPacAnimations(spriteAnimations);
        junior.animations.select(TengenMsPacMan_AnimationID.ANIM_JUNIOR);

        juniors.add(junior);
        juniorCreationTimes.add(tick);

        SoundID soundID = switch (randomInt(1, 3)) {
            case 1 -> TengenMsPacManSoundID.INTERMISSION_4_JUNIOR_1;
            case 2 -> TengenMsPacManSoundID.INTERMISSION_4_JUNIOR_2;
            default -> throw new IllegalArgumentException();
        };
        appContext().ui().sounds().playLoop(soundID);

        Logger.info("Junior spawned at tick {}", tick);
    }

    private void updateJunior(MovementSystem movementSystem, WorldMovementSystem worldMovementSystem, long tick, int index) {
        Pac junior = juniors.get(index);
        long creationTime = juniorCreationTimes.get(index);
        long lifeTime = tick - creationTime;
        if (lifeTime> 0 && lifeTime % 10 == 0) {
            computeNewMoveDir(worldMovementSystem, junior);
        }
        movementSystem.moveAccelerated(junior);
        if (junior.position().x > unscaledWidth()) {
            junior.position().setX(0);
        }
        if (junior.position().x < 0) {
            junior.position().setX(unscaledWidth());
        }
    }

    private void computeNewMoveDir(WorldMovementSystem worldMovementSystem, Pac junior) {
        Direction oldMoveDir = junior.worldMovement().moveDir();
        List<Direction> possibleDirs = new ArrayList<>(List.of(Direction.values()));
        possibleDirs.remove(oldMoveDir.opposite());
        List<Direction> dirsByMinCenterDist = possibleDirs.stream().sorted(
            (d1, d2) -> compareBySmallestDistToSceneCenter(junior, d1, d2)).toList();
        Direction bestDir = dirsByMinCenterDist.getFirst();
        Direction randomDir = possibleDirs.get(randomInt(0, possibleDirs.size()));
        boolean chooseBestDir = randomInt(0, 100) < 40;
        worldMovementSystem.setMoveDir(junior, chooseBestDir ? bestDir : randomDir);
    }

    private int compareBySmallestDistToSceneCenter(Pac junior, Direction dir1, Direction dir2) {
        Vector2i tile = junior.tile();
        Vector2f pos1 = tile.plus(dir1.vector()).scaled(TS).toVector2f();
        Vector2f pos2 = tile.plus(dir2.vector()).scaled(TS).toVector2f();
        Vector2f center = new Vector2f(0.5f * unscaledWidth(), 0.5f * unscaledHeight());
        return Double.compare(pos1.euclideanDist(center), pos2.euclideanDist(center));
    }
}