/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameController.GameState;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.MsPacMan;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.PacMan;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.math.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_TILES;

public class TengenMsPacMan_CutScene4 extends GameScene2D {

    private static final int LEFT_BORDER = TS;
    private static final int RIGHT_BORDER = TS * (NES_TILES.x() - 2);

    private static final int LOWER_LANE = TS * 21; // TODO not sure

    private Pac pacMan;
    private Pac msPacMan;
    private List<Pac> juniors;
    private List<Integer> juniorCreationTime;
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
    protected void doInit(Game game) {
        game.hud().hide();

        final GameUI_Config uiConfig = ui.currentConfig();
        final var spriteSheet = (TengenMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        clapperboard = new Clapperboard(spriteSheet, 4, "THE END");
        clapperboard.setPosition(TS(3), TS(10));
        clapperboard.setVisible(true);
        clapperboard.startAnimation();

        msPacMan = new MsPacMan();
        msPacMan.setAnimationManager(uiConfig.createPacAnimations());

        pacMan = new PacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());

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

        switch (tick) {
            case 130 -> {
                pacMan.setMoveDir(Direction.RIGHT);
                pacMan.setPosition(LEFT_BORDER, LOWER_LANE);
                pacMan.setSpeed(1f);
                pacMan.playAnimation(TengenMsPacMan_UIConfig.AnimationID.ANIM_PAC_MAN_MUNCHING);
                pacMan.show();

                msPacMan.setMoveDir(Direction.LEFT);
                msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
                msPacMan.setSpeed(1f);
                msPacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                msPacMan.show();
            } case 230 -> {
                pacMan.setSpeed(0);
                pacMan.optAnimationManager().ifPresent(am -> {
                    am.stop();
                    am.reset();
                });
                msPacMan.setSpeed(0);
                msPacMan.optAnimationManager().ifPresent(am -> {
                    am.stop();
                    am.reset();
                });
            }
            case 400 -> {
                pacMan.playAnimation(TengenMsPacMan_UIConfig.AnimationID.ANIM_PAC_MAN_MUNCHING);
                msPacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
            }
            case 520 -> {
                pacMan.selectAnimation(TengenMsPacMan_UIConfig.AnimationID.ANIM_PAC_MAN_WAVING_HAND);
                msPacMan.selectAnimation(TengenMsPacMan_UIConfig.AnimationID.ANIM_MS_PAC_MAN_WAVING_HAND);
            }
            case 527 -> {
                pacMan.optAnimationManager().ifPresent(AnimationManager::play);
                msPacMan.optAnimationManager().ifPresent(AnimationManager::play);
            }
            case 648 -> {
                pacMan.playAnimation(TengenMsPacMan_UIConfig.AnimationID.ANIM_PAC_MAN_TURNING_AWAY);
                msPacMan.playAnimation(TengenMsPacMan_UIConfig.AnimationID.ANIM_MS_PAC_MAN_TURNING_AWAY);
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
            case 1512 -> game.control().enterState(GameState.SETTING_OPTIONS_FOR_START);
        }
    }

    private void spawnJunior(int tick) {
        var junior = new PacMan();
        double randomX = 8 * TS + (8 * TS) * Math.random();
        junior.setPosition((float) randomX, unscaledSize().y() - 4 * TS);
        junior.setMoveDir(Direction.UP);
        junior.setSpeed(2);
        junior.setAnimationManager(ui.currentConfig().createPacAnimations());
        junior.selectAnimation(TengenMsPacMan_UIConfig.AnimationID.ANIM_JUNIOR);
        junior.show();
        juniors.add(junior);
        juniorCreationTime.add(tick);

        String id = SoundID.INTERMISSION_1 + ".junior." + randomInt(1, 3); // 1 or 2
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
    public Vector2i unscaledSize() { return NES_SIZE_PX; }
}