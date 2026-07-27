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
import de.amr.pacmanfx.core.model.component.spriteanim.SpriteAnim;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
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
        msPacMan.assertComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(spriteAnimations));

        pacMan = TengenMsPacMan_ActorFactory.createPacMan();
        pacMan.assertComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(spriteAnimations));

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
        final MovementSystem motor = gameContext.systems().motor;
        final WorldMovementSystem navigator = gameContext.systems().navigator;
        
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final long gameStateTick = gameState().timer().tickCount();

        clapperboard.tick();

        motor.moveAccelerated(pacMan);
        motor.moveAccelerated(msPacMan);
        for (int i = 0; i < juniors.size(); ++i) {
            updateJunior(motor, navigator, gameStateTick, i);
        }

        if (gameStateTick <= TICK_EXPIRES) {
            final short eventTick = (short) gameStateTick;
            switch (eventTick) {
                case 130 -> {
                    pacMan.position().set(LEFT_BORDER, LOWER_LANE);
                    pacMan.visibility().show();

                    navigator.setMoveDir(pacMan, Direction.RIGHT);
                    navigator.setSpeed(pacMan, 1f);

                    pacMan.assertComponent(SpriteAnim.class).animations().select(TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    pacMan.assertComponent(SpriteAnim.class).animations().playSelected();

                    msPacMan.position().set(RIGHT_BORDER, LOWER_LANE);
                    msPacMan.visibility().show();

                    navigator.setMoveDir(msPacMan, Direction.LEFT);
                    navigator.setSpeed(msPacMan, 1f);

                    msPacMan.assertComponent(SpriteAnim.class).animations().select(CommonAnimationID.PAC_MUNCHING);
                    msPacMan.assertComponent(SpriteAnim.class).animations().playSelected();
                }
                case 230 -> {
                    navigator.setSpeed(pacMan, 0);
                    pacMan.assertComponent(SpriteAnim.class).animations().stopSelected();
                    pacMan.assertComponent(SpriteAnim.class).animations().resetSelected();

                    navigator.setSpeed(msPacMan, 0);
                    msPacMan.assertComponent(SpriteAnim.class).animations().stopSelected();
                    msPacMan.assertComponent(SpriteAnim.class).animations().resetSelected();
                }
                case 400 -> {
                    pacMan.assertComponent(SpriteAnim.class).animations().select(TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
                    pacMan.assertComponent(SpriteAnim.class).animations().playSelected();

                    msPacMan.assertComponent(SpriteAnim.class).animations().select(CommonAnimationID.PAC_MUNCHING);
                    msPacMan.assertComponent(SpriteAnim.class).animations().playSelected();
                }
                case 520 -> {
                    pacMan.assertComponent(SpriteAnim.class).animations().select(TengenMsPacMan_AnimationID.MR_PAC_MAN_WAVING_HAND);
                    msPacMan.assertComponent(SpriteAnim.class).animations().select(TengenMsPacMan_AnimationID.MS_PAC_MAN_WAVING_HAND);
                }
                case 527 -> {
                    pacMan.assertComponent(SpriteAnim.class).animations().playSelected();
                    msPacMan.assertComponent(SpriteAnim.class).animations().playSelected();
                }
                case 648 -> {
                    pacMan.assertComponent(SpriteAnim.class).animations().select(TengenMsPacMan_AnimationID.MR_PAC_MAN_TURNING_AWAY);
                    pacMan.assertComponent(SpriteAnim.class).animations().playSelected();
                    msPacMan.assertComponent(SpriteAnim.class).animations().select(TengenMsPacMan_AnimationID.MS_PAC_MAN_TURNING_AWAY);
                    msPacMan.assertComponent(SpriteAnim.class).animations().playSelected();
                }
                case 650 -> {
                    navigator.setSpeed(pacMan, 1.5f); // TODO not sure
                    navigator.setMoveDir(pacMan, Direction.UP);

                    navigator.setSpeed(msPacMan, 1.5f); // TODO not sure
                    navigator.setMoveDir(msPacMan, Direction.UP);
                }
                case 720 -> {
                    pacMan.visibility().hide();
                    msPacMan.visibility().hide();
                }
                case 904, 968, 1032, 1096, 1160, 1224, 1288, 1352 -> spawnJunior(navigator, renderConfig, gameStateTick);
                case 1500 -> optSoundEffects().ifPresent(GameSoundEffects::stopAll);
                case TICK_EXPIRES -> gameContext().flow().enterState(gameContext(), TengenMsPacMan_GameState.GAME_PREPARATION.state());
            }
        }
    }

    private void spawnJunior(WorldMovementSystem navigator, GameVariantRenderConfig renderConfig, long tick) {
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();

        double randomX = 8 * TS + (8 * TS) * Math.random();

        final Pac junior = TengenMsPacMan_ActorFactory.createPacMan();
        junior.position().set((float) randomX, unscaledHeight() - 4 * TS);
        junior.visibility().show();
        navigator.setMoveDir(junior, Direction.UP);
        navigator.setSpeed(junior, 2);

        junior.assertComponent(SpriteAnim.class).setAnimations(renderConfig.createPacAnimations(spriteAnimations));
        junior.assertComponent(SpriteAnim.class).animations().select(TengenMsPacMan_AnimationID.ANIM_JUNIOR);

        juniors.add(junior);
        juniorCreationTimes.add(tick);

        final SoundID soundID = switch (randomInt(1, 3)) {
            case 1 -> TengenMsPacManSoundID.INTERMISSION_4_JUNIOR_1;
            case 2 -> TengenMsPacManSoundID.INTERMISSION_4_JUNIOR_2;
            default -> throw new IllegalArgumentException();
        };
        appContext().ui().sounds().playLoop(soundID);

        Logger.info("Junior spawned at tick {}", tick);
    }

    private void updateJunior(MovementSystem motor, WorldMovementSystem navigator, long tick, int index) {
        Pac junior = juniors.get(index);
        long creationTime = juniorCreationTimes.get(index);
        long lifeTime = tick - creationTime;
        if (lifeTime> 0 && lifeTime % 10 == 0) {
            computeNewMoveDir(navigator, junior);
        }
        motor.moveAccelerated(junior);
        if (junior.position().x > unscaledWidth()) {
            junior.position().setX(0);
        }
        if (junior.position().x < 0) {
            junior.position().setX(unscaledWidth());
        }
    }

    private void computeNewMoveDir(WorldMovementSystem navigator, Pac junior) {
        Direction oldMoveDir = junior.worldMovement().moveDir();
        List<Direction> possibleDirs = new ArrayList<>(List.of(Direction.values()));
        possibleDirs.remove(oldMoveDir.opposite());
        List<Direction> dirsByMinCenterDist = possibleDirs.stream().sorted(
            (d1, d2) -> compareBySmallestDistToSceneCenter(junior, d1, d2)).toList();
        Direction bestDir = dirsByMinCenterDist.getFirst();
        Direction randomDir = possibleDirs.get(randomInt(0, possibleDirs.size()));
        boolean chooseBestDir = randomInt(0, 100) < 40;
        navigator.setMoveDir(junior, chooseBestDir ? bestDir : randomDir);
    }

    private int compareBySmallestDistToSceneCenter(Pac junior, Direction dir1, Direction dir2) {
        Vector2i tile = WorldMovementSystem.computeTile(junior);
        Vector2f pos1 = tile.plus(dir1.vector()).scaled(TS).toVector2f();
        Vector2f pos2 = tile.plus(dir2.vector()).scaled(TS).toVector2f();
        Vector2f center = new Vector2f(0.5f * unscaledWidth(), 0.5f * unscaledHeight());
        return Double.compare(pos1.euclideanDist(center), pos2.euclideanDist(center));
    }
}