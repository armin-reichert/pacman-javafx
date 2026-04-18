/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.model.actor.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundID;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.amr.basics.math.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.*;

public class TengenMsPacMan_CutScene4 extends GameScene2D {

    public static final Vector2i SIZE = new Vector2i(NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT);

    public static final int TICK_EXPIRES = 1512;

    private static final int LEFT_BORDER = TS;
    private static final int RIGHT_BORDER = TS * (NES_SCREEN_TILES.x() - 2);

    private static final int LOWER_LANE = TS * 21; // TODO not sure

    private Pac pacMan;
    private Pac msPacMan;
    private List<Pac> juniors;
    private List<Long> juniorCreationTimes;
    private Clapperboard clapperboard;

    public TengenMsPacMan_CutScene4(GameUI ui) {
        super(ui);
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
    public void onSceneStart() {
        final UIConfig uiConfig = ui.currentConfig();

        clapperboard = new Clapperboard(4, "THE END");
        clapperboard.setPosition(TS(3), TS(10));
        clapperboard.setVisible(true);
        clapperboard.startAnimation();

        msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations(ui.spriteAnimationDriver()));

        pacMan = TengenMsPacMan_ActorFactory.createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(ui.spriteAnimationDriver()));

        juniors = new ArrayList<>();
        juniorCreationTimes = new ArrayList<>();

        ui.soundManager().play(SoundID.INTERMISSION_4);
    }

    @Override
    public void onSceneEnd() {
        ui.soundManager().stop(SoundID.INTERMISSION_4);
    }

    @Override
    public void onTick(long tick) {
        final TengenMsPacMan_GameModel game = gameContext().game();
        final State<Game> gameState = game.flow().state();
        final long gameStateTick = gameState.timer().tickCount();

        clapperboard.tick();

        pacMan.move();
        msPacMan.move();
        for (int i = 0; i < juniors.size(); ++i) {
            updateJunior(gameStateTick, i);
        }

        if (gameStateTick <= TICK_EXPIRES) {
            final short eventTick = (short) gameStateTick;
            switch (eventTick) {
                case 130 -> {
                    pacMan.setMoveDir(Direction.RIGHT);
                    pacMan.setPosition(LEFT_BORDER, LOWER_LANE);
                    pacMan.setSpeed(1f);
                    pacMan.selectAnimation(TengenMsPacMan_AnimationID.ANIM_MR_PAC_MAN_MUNCHING);
                    pacMan.playAnimation();
                    pacMan.show();

                    msPacMan.setMoveDir(Direction.LEFT);
                    msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
                    msPacMan.setSpeed(1f);
                    msPacMan.selectAnimation(Pac.AnimationID.PAC_MUNCHING);
                    msPacMan.playAnimation();
                    msPacMan.show();
                }
                case 230 -> {
                    pacMan.setSpeed(0);
                    pacMan.stopAnimation();
                    pacMan.resetAnimation();
                    msPacMan.setSpeed(0);
                    msPacMan.stopAnimation();
                    msPacMan.resetAnimation();
                }
                case 400 -> {
                    pacMan.selectAnimation(TengenMsPacMan_AnimationID.ANIM_MR_PAC_MAN_MUNCHING);
                    pacMan.playAnimation();
                    msPacMan.selectAnimation(Pac.AnimationID.PAC_MUNCHING);
                    msPacMan.playAnimation();
                }
                case 520 -> {
                    pacMan.selectAnimation(TengenMsPacMan_AnimationID.ANIM_MR_PAC_MAN_WAVING_HAND);
                    msPacMan.selectAnimation(TengenMsPacMan_AnimationID.ANIM_MS_PAC_MAN_WAVING_HAND);
                }
                case 527 -> {
                    pacMan.playAnimation();
                    msPacMan.playAnimation();
                }
                case 648 -> {
                    pacMan.selectAnimation(TengenMsPacMan_AnimationID.ANIM_MR_PAC_MAN_TURNING_AWAY);
                    pacMan.playAnimation();
                    msPacMan.selectAnimation(TengenMsPacMan_AnimationID.ANIM_MS_PAC_MAN_TURNING_AWAY);
                    msPacMan.playAnimation();
                }
                case 650 -> {
                    pacMan.setSpeed(1.5f); // TODO not sure
                    pacMan.setMoveDir(Direction.UP);
                    msPacMan.setSpeed(1.5f); // TODO not sure
                    msPacMan.setMoveDir(Direction.UP);
                }
                case 720 -> {
                    pacMan.hide();
                    msPacMan.hide();
                }
                case 904, 968, 1032, 1096, 1160, 1224, 1288, 1352 -> spawnJunior(gameStateTick);
                case 1500 -> soundEffects().ifPresent(GameSoundEffects::stopAll);
                case TICK_EXPIRES -> game.flow().enterState(TengenMsPacMan_GameState.PREPARING_GAME_START);
            }
        }
    }

    private void spawnJunior(long tick) {
        var junior = TengenMsPacMan_ActorFactory.createPacMan();
        double randomX = 8 * TS + (8 * TS) * Math.random();
        junior.setPosition((float) randomX, unscaledSceneSize().y() - 4 * TS);
        junior.setMoveDir(Direction.UP);
        junior.setSpeed(2);
        junior.setAnimations(ui.currentConfig().createPacAnimations(ui.spriteAnimationDriver()));
        junior.selectAnimation(TengenMsPacMan_AnimationID.ANIM_JUNIOR);
        junior.show();
        juniors.add(junior);
        juniorCreationTimes.add(tick);

        String id = SoundID.INTERMISSION_4 + ".junior." + randomInt(1, 3); // 1 or 2
        ui.soundManager().loop(id);

        Logger.info("Junior spawned at tick {}", tick);
    }

    private void updateJunior(long tick, int index) {
        Pac junior = juniors.get(index);
        long creationTime = juniorCreationTimes.get(index);
        long lifeTime = tick - creationTime;
        if (lifeTime> 0 && lifeTime % 10 == 0) {
            computeNewMoveDir(junior);
        }
        junior.move();
        if (junior.x() > unscaledSceneSize().x()) {
            junior.setX(0);
        }
        if (junior.x() < 0) {
            junior.setX(unscaledSceneSize().x());
        }
    }

    private void computeNewMoveDir(Pac junior) {
        Direction oldMoveDir = junior.moveDir();
        List<Direction> possibleDirs = new ArrayList<>(List.of(Direction.values()));
        possibleDirs.remove(oldMoveDir.opposite());
        List<Direction> dirsByMinCenterDist = possibleDirs.stream().sorted(
            (d1, d2) -> compareBySmallestDistToSceneCenter(junior, d1, d2)).toList();
        Direction bestDir = dirsByMinCenterDist.getFirst();
        Direction randomDir = possibleDirs.get(randomInt(0, possibleDirs.size()));
        boolean chooseBestDir = randomInt(0, 100) < 40;
        junior.setMoveDir(chooseBestDir ? bestDir : randomDir);
    }

    private int compareBySmallestDistToSceneCenter(Pac junior, Direction dir1, Direction dir2) {
        Vector2f pos1 = junior.tile().plus(dir1.vector()).scaled(TS).toVector2f();
        Vector2f pos2 = junior.tile().plus(dir2.vector()).scaled(TS).toVector2f();
        Vector2f center = unscaledSceneSize().scaled(0.5);
        return Double.compare(pos1.euclideanDist(center), pos2.euclideanDist(center));
    }

    @Override
    public Vector2i unscaledSceneSize() { return SIZE; }
}