package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationMap;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.randomInt;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_TILES;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationMap.*;

public class TengenMsPacMan_CutScene4 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission.4";

    private static final int LEFT_BORDER = TS;
    private static final int RIGHT_BORDER = TS * (NES_TILES.x() - 2);

    private static final int LOWER_LANE = TS * 21; // TODO not sure

    private Pac pacMan;
    private Pac msPacMan;
    private List<Pac> juniors;
    private List<Integer> juniorCreationTime;

    private Clapperboard clapperboard;

    private int t;

    public TengenMsPacMan_CutScene4(GameUI ui) {
        super(ui);
    }
    
    @Override
    protected void doInit() {
        t = -1;

        gameContext().theGame().theHUD().showScore(false);
        gameContext().theGame().theHUD().showLevelCounter(true);
        gameContext().theGame().theHUD().showLivesCounter(false);

        var spriteSheet = (TengenMsPacMan_SpriteSheet) ui.theConfiguration().spriteSheet();
        clapperboard = new Clapperboard(spriteSheet, 4, "THE END");
        clapperboard.setPosition(3*TS, 10*TS);
        clapperboard.setFont(scaledArcadeFont8());
        msPacMan = createMsPacMan(null);
        pacMan = createPacMan(null);
        msPacMan.setAnimations(ui.theConfiguration().createPacAnimations(msPacMan));
        pacMan  .setAnimations(ui.theConfiguration().createPacAnimations(pacMan));
        juniors = new ArrayList<>();
        juniorCreationTime = new ArrayList<>();
    }

    @Override
    protected void doEnd() {
        ui.theSound().stop(MUSIC_ID);
    }

    @Override
    public void update() {
        t += 1;
        if (t == 0) {
            clapperboard.setVisible(true);
            clapperboard.startAnimation();
            ui.theSound().play(MUSIC_ID);
        }
        else if (t == 130) {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setPosition(LEFT_BORDER, LOWER_LANE);
            pacMan.setSpeed(1f);
            pacMan.playAnimation(ANIM_PAC_MAN_MUNCHING);
            pacMan.show();

            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
            msPacMan.setSpeed(1f);
            msPacMan.playAnimation(ANIM_PAC_MUNCHING);
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
            pacMan.playAnimation(ANIM_PAC_MAN_MUNCHING);
            msPacMan.playAnimation(ANIM_PAC_MUNCHING);
        }
        else if (t == 520) {
            pacMan.selectAnimation(ANIM_PAC_MAN_WAVING_HAND);
            msPacMan.selectAnimation(ANIM_MS_PAC_MAN_WAVING_HAND);
        }
        else if (t == 527) {
            pacMan.playAnimation();
            msPacMan.playAnimation();
        }
        else if (t == 648) {
            pacMan.playAnimation(ANIM_PAC_MAN_TURNING_AWAY);
            msPacMan.playAnimation(ANIM_MS_PAC_MAN_TURNING_AWAY);
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
            gameContext().theGameController().changeGameState(GameState.SETTING_OPTIONS_FOR_START);
        }

        pacMan.move();
        msPacMan.move();
        for (int i = 0; i < juniors.size(); ++i) {
            updateJunior(i);
        }
        clapperboard.tick();
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
        var junior = createPacMan(null);
        double randomX = 8 * TS + (8 * TS) * Math.random();
        junior.setPosition((float) randomX, sizeInPx().y() - 4 * TS);
        junior.setMoveDir(Direction.UP);
        junior.setSpeed(2);
        junior.setAnimations(ui.theConfiguration().createPacAnimations(junior));
        junior.selectAnimation(TengenMsPacMan_PacAnimationMap.ANIM_JUNIOR);
        junior.show();
        juniors.add(junior);
        juniorCreationTime.add(t);

        String id = "audio.intermission.4.junior." + randomInt(1, 3); // 1 or 2
        ui.theSound().loop(id);

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
        if (junior.x() > sizeInPx().x()) {
            junior.setX(0);
        }
        if (junior.x() < 0) {
            junior.setX(sizeInPx().x());
        }
    }

    private void computeNewMoveDir(Pac junior) {
        Direction oldMoveDir = junior.moveDir();
        List<Direction> possibleDirs = new ArrayList<>(List.of(Direction.values()));
        possibleDirs.remove(oldMoveDir.opposite());
        List<Direction> dirsByMinCenterDist = possibleDirs.stream().sorted((d1, d2) -> bySmallestDistanceToToCenter(junior, d1, d2)).toList();
        Direction bestDir = dirsByMinCenterDist.getFirst();
        Direction randomDir = possibleDirs.get(randomInt(0, possibleDirs.size()));
        boolean chooseBestDir = randomInt(0, 100) < 40;
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
    public Vector2f sizeInPx() { return NES_SIZE_PX; }

    @SuppressWarnings("unchecked")
    @Override
    public TengenMsPacMan_GameRenderer renderer() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void draw() {
        gameRenderer.setScaling(scaling());
        clear();
        drawSceneContent();
        if (debugInfoVisibleProperty.get()) {
            drawDebugInfo();
        }
        var game = gameContext().<TengenMsPacMan_GameModel>theGame();
        if (game.mapCategory() != MapCategory.ARCADE) {
            gameRenderer.drawHUD(gameContext(), game.theHUD(), sizeInPx().minus(0, 2 * TS), ui.theGameClock().tickCount());
        }
    }

    @Override
    public void drawSceneContent() {
        renderer().drawVerticalSceneBorders();
        renderer().drawActors(List.of(clapperboard, msPacMan, pacMan));
        renderer().drawActors(juniors);
    }
}