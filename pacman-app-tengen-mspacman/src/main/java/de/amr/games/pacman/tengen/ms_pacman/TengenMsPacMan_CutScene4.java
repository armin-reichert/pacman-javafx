package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.model.actors.ActorAnimations.*;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.games.pacman.ui.Globals.*;

public class TengenMsPacMan_CutScene4 extends GameScene2D {

    static final int LEFT_BORDER = TS;
    static final int RIGHT_BORDER = TS * (NES_TILES.x() - 2);

    static final int CLAP_TILE_X = TS * 3; // TODO not sure
    static final int CLAP_TILE_Y = TS * 10; // TODO not sure

    static final int LOWER_LANE = TS * 21; // TODO not sure

    private Pac pacMan;
    private Pac msPacMan;
    private List<Pac> juniors;
    private List<Integer> juniorCreationTime;

    private MediaPlayer music;
    private ClapperboardAnimation clapAnimation;
    private TengenMsPacMan_SpriteSheet spriteSheet;

    private int t;

    @Override
    protected void doInit() {
        t = -1;

        game().scoreManager().setScoreVisible(false);

        msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        pacMan = TengenMsPacMan_ActorFactory.createPacMan();
        juniors = new ArrayList<>();
        juniorCreationTime = new ArrayList<>();

        spriteSheet = (TengenMsPacMan_SpriteSheet) THE_UI_CONFIGS.current().spriteSheet();
        pacMan.setAnimations(new TengenMsPacMan_PacAnimations(spriteSheet));
        msPacMan.setAnimations(new TengenMsPacMan_PacAnimations(spriteSheet));

        music = THE_SOUND.createSound("intermission.4");
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        t += 1;
        if (t == 0) {
            clapAnimation = new ClapperboardAnimation();
            clapAnimation.start();
            music.play();
        }
        else if (t == 130) {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setPosition(LEFT_BORDER, LOWER_LANE);
            pacMan.setSpeed(1f);
            pacMan.selectAnimation("pacman_munching"); //TODO constant?
            pacMan.startAnimation();
            pacMan.show();

            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
            msPacMan.setSpeed(1f);
            msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
            msPacMan.startAnimation();
            msPacMan.show();
        }
        else if (t == 230) {
            pacMan.setSpeed(0);
            pacMan.stopAnimation();
            pacMan.resetAnimation();
            msPacMan.setSpeed(0);
            msPacMan.stopAnimation();
            msPacMan.resetAnimation();
        }
        else if (t == 400) {
            pacMan.startAnimation();
            msPacMan.startAnimation();
        }
        else if (t == 520) {
            pacMan.selectAnimation(ANIM_MR_PACMAN_WAVING_HAND);
            msPacMan.selectAnimation(ANIM_MS_PACMAN_WAVING_HAND);
        }
        else if (t == 527) {
            pacMan.startAnimation();
            msPacMan.startAnimation();
        }
        else if (t == 648) {
            pacMan.selectAnimation(ANIM_MR_PACMAN_TURNING_AWAY);
            pacMan.startAnimation();
            msPacMan.selectAnimation(ANIM_MS_PACMAN_TURNING_AWAY);
            msPacMan.startAnimation();
        }
        else if (t == 650) {
            pacMan.setSpeed(1.5f); // TODO not sure
            pacMan.setMoveDir(Direction.UP);
            msPacMan.setSpeed(1.5f); // TODO not sure
            msPacMan.setMoveDir(Direction.UP);
        }
        else if (t == 720) {
            pacMan.hide();
            msPacMan.hide();
        }
        else if (isJuniorSpawnTime()) {
            spawnJunior();
        }
        else if (t == 1512) {
            THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
        }

        pacMan.move();
        msPacMan.move();
        for (int i = 0; i < juniors.size(); ++i) {
            updateJunior(i);
        }
        clapAnimation.tick();
    }

    private boolean isJuniorSpawnTime() {
        for (int i = 0; i < 8; ++i) {
            if (t == 904 + 64*i) {
                return true;
            }
        }
        return false;
    }

    private void spawnJunior() {
        String assetNamespace = THE_UI_CONFIGS.current().assetNamespace();
        var junior = TengenMsPacMan_ActorFactory.createPacMan();
        double randomX = 8 * TS + (8 * TS) * Math.random();
        int rnd = Globals.randomInt(1, 3);
        AudioClip clip = THE_ASSETS.get(assetNamespace + ".audio.intermission.4.junior." + rnd);
        junior.setPosition((float) randomX, sizeInPx().y() - 4 * TS);
        junior.setMoveDir(Direction.UP);
        junior.setSpeed(2);
        junior.setAnimations(new TengenMsPacMan_PacAnimations(spriteSheet));
        junior.selectAnimation(ANIM_JUNIOR_PACMAN);
        junior.show();
        juniors.add(junior);
        juniorCreationTime.add(t);
        clip.play();
        Logger.info("Junior spawned at tick {}", t);
    }

    private void updateJunior(int index) {
        Pac junior = juniors.get(index);
        int creationTime = juniorCreationTime.get(index);
        int lifeTime = t - creationTime;
        if (lifeTime> 0 && lifeTime % 10 == 0) {
            computeNewMoveDir(junior);
        }
        junior.move();
        if (junior.posX() > sizeInPx().x()) {
            junior.setPosX(0);
        }
        if (junior.posX() < 0) {
            junior.setPosX(sizeInPx().x());
        }
    }

    private void computeNewMoveDir(Pac junior) {
        Direction oldMoveDir = junior.moveDir();
        List<Direction> possibleDirs = new ArrayList<>(List.of(Direction.values()));
        possibleDirs.remove(oldMoveDir.opposite());
        List<Direction> dirsByMinCenterDist = possibleDirs.stream().sorted((d1, d2) -> bySmallestDistanceToToCenter(junior, d1, d2)).toList();
        Direction bestDir = dirsByMinCenterDist.getFirst();
        Direction randomDir = possibleDirs.get(Globals.randomInt(0, possibleDirs.size()));
        boolean chooseBestDir = Globals.randomInt(0, 100) < 40;
        junior.setMoveDir(chooseBestDir ? bestDir : randomDir);
    }

    private int bySmallestDistanceToToCenter(Pac junior, Direction dir1, Direction dir2) {
        Vector2f pos1 = junior.tile().plus(dir1.vector()).scaled(TS).toVector2f();
        Vector2f pos2 = junior.tile().plus(dir2.vector()).scaled(TS).toVector2f();
        Vector2f center = sizeInPx().scaled(0.5);
        double dist1 = pos1.euclideanDist(center), dist2 = pos2.euclideanDist(center);
        return Double.compare(dist1, dist2);
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    @Override
    protected void drawSceneContent() {
        gr.fillCanvas(backgroundColor());
        gr.drawScores(game().scoreManager(), nesPaletteColor(0x20), arcadeFontScaledTS());
        var r = (TengenMsPacMan_Renderer2D) gr;
        r.drawSceneBorderLines();
        r.drawClapperBoard(clapAnimation, "THE END", 4, CLAP_TILE_X, CLAP_TILE_Y, arcadeFontScaledTS());
        r.drawAnimatedActor(msPacMan);
        r.drawAnimatedActor(pacMan);
        juniors.forEach(r::drawAnimatedActor);
        r.drawLevelCounter(game().levelCounter(), sizeInPx().minus(0, 3*TS));
    }
}
