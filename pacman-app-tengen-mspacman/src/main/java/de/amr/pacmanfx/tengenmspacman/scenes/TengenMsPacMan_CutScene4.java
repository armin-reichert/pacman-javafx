/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameState;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundID;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_PIXELS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_TILES;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel.createPacMan;

public class TengenMsPacMan_CutScene4 extends GameScene2D {

    public static final int TICK_EXPIRES = 1512;

    private static final int LEFT_BORDER = TS;
    private static final int RIGHT_BORDER = TS * (NES_SCREEN_TILES.x() - 2);

    private static final int LOWER_LANE = TS * 21; // TODO not sure

    private Pac pacMan;
    private Pac msPacMan;
    private List<Pac> juniors;
    private List<Integer> juniorCreationTime;
    private Clapperboard clapperboard;

    public TengenMsPacMan_CutScene4() {}

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
    protected void doInit(Game game) {
        final UIConfig uiConfig = ui.currentConfig();

        clapperboard = new Clapperboard(4, "THE END");
        clapperboard.setPosition(TS(3), TS(10));
        clapperboard.setVisible(true);
        clapperboard.startAnimation();

        msPacMan = createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations());

        pacMan = createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations());

        juniors = new ArrayList<>();
        juniorCreationTime = new ArrayList<>();

        ui.soundManager().play(SoundID.INTERMISSION_4);
    }

    @Override
    protected void doEnd(Game game) {
        ui.soundManager().stop(SoundID.INTERMISSION_4);
    }

    @Override
    public void update(Game game) {
        final int tick = (int) game.control().state().timer().tickCount();

        clapperboard.tick();

        pacMan.move();
        msPacMan.move();
        for (int i = 0; i < juniors.size(); ++i) {
            updateJunior(tick, i);
        }

        if (tick <= TICK_EXPIRES) {
            final short eventTick = (short) tick;
            switch (eventTick) {
                case 130 -> {
                    pacMan.setMoveDir(Direction.RIGHT);
                    pacMan.setPosition(LEFT_BORDER, LOWER_LANE);
                    pacMan.setSpeed(1f);
                    pacMan.playAnimation(TengenMsPacMan_AnimationID.ANIM_PAC_MAN_MUNCHING);
                    pacMan.show();

                    msPacMan.setMoveDir(Direction.LEFT);
                    msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
                    msPacMan.setSpeed(1f);
                    msPacMan.playAnimation(Pac.AnimationID.PAC_MUNCHING);
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
                    pacMan.playAnimation(TengenMsPacMan_AnimationID.ANIM_PAC_MAN_MUNCHING);
                    msPacMan.playAnimation(Pac.AnimationID.PAC_MUNCHING);
                }
                case 520 -> {
                    pacMan.selectAnimation(TengenMsPacMan_AnimationID.ANIM_PAC_MAN_WAVING_HAND);
                    msPacMan.selectAnimation(TengenMsPacMan_AnimationID.ANIM_MS_PAC_MAN_WAVING_HAND);
                }
                case 527 -> {
                    pacMan.playAnimation();
                    msPacMan.playAnimation();
                }
                case 648 -> {
                    pacMan.playAnimation(TengenMsPacMan_AnimationID.ANIM_PAC_MAN_TURNING_AWAY);
                    msPacMan.playAnimation(TengenMsPacMan_AnimationID.ANIM_MS_PAC_MAN_TURNING_AWAY);
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
                case 904, 968, 1032, 1096, 1160, 1224, 1288, 1352 -> spawnJunior(tick);
                case 1500 -> soundEffects().ifPresent(GameSoundEffects::stopAll);
                case TICK_EXPIRES -> game.control().enterState(TengenMsPacMan_GameState.SETTING_OPTIONS_FOR_START);
            }
        }
    }

    private void spawnJunior(int tick) {
        var junior = createPacMan();
        double randomX = 8 * TS + (8 * TS) * Math.random();
        junior.setPosition((float) randomX, unscaledSize().y() - 4 * TS);
        junior.setMoveDir(Direction.UP);
        junior.setSpeed(2);
        junior.setAnimations(ui.currentConfig().createPacAnimations());
        junior.selectAnimation(TengenMsPacMan_AnimationID.ANIM_JUNIOR);
        junior.show();
        juniors.add(junior);
        juniorCreationTime.add(tick);

        String id = SoundID.INTERMISSION_4 + ".junior." + randomInt(1, 3); // 1 or 2
        ui.soundManager().loop(id);

        Logger.info("Junior spawned at tick {}", tick);
    }

    private void updateJunior(int tick, int index) {
        Pac junior = juniors.get(index);
        int creationTime = juniorCreationTime.get(index);
        int lifeTime = tick - creationTime;
        if (lifeTime> 0 && lifeTime % 10 == 0) {
            computeNewMoveDir(junior);
        }
        junior.move();
        if (junior.x() > unscaledSize().x()) {
            junior.setX(0);
        }
        if (junior.x() < 0) {
            junior.setX(unscaledSize().x());
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
        Vector2f center = unscaledSize().scaled(0.5);
        return Double.compare(pos1.euclideanDist(center), pos2.euclideanDist(center));
    }

    @Override
    public Vector2i unscaledSize() { return NES_SCREEN_PIXELS; }
}