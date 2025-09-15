/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.actors.AnimationManager;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_HUD;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_ActorRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_HUDRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_TILES;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationManager.*;

public class TengenMsPacMan_CutScene4 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission.4";

    private static final int LEFT_BORDER = TS;
    private static final int RIGHT_BORDER = TS * (NES_TILES.x() - 2);

    private static final int LOWER_LANE = TS * 21; // TODO not sure

    private TengenMsPacMan_HUDRenderer hudRenderer;
    private TengenMsPacMan_ActorRenderer actorRenderer;

    private Pac pacMan;
    private Pac msPacMan;
    private List<Pac> juniors;
    private List<Integer> juniorCreationTime;
    private Clapperboard clapperboard;


    public TengenMsPacMan_CutScene4(GameUI ui) {
        super(ui);
    }

    @Override
    public void createRenderers(Canvas canvas) {
        super.createRenderers(canvas);

        final GameUI_Config uiConfig = ui.currentConfig();
        hudRenderer       = configureRenderer((TengenMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas));
        actorRenderer     = configureRenderer((TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas));
        debugInfoRenderer = configureRenderer(new DefaultDebugInfoRenderer(ui, canvas));
    }

    @Override
    public TengenMsPacMan_HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    protected void doInit() {
        TengenMsPacMan_HUD hud = (TengenMsPacMan_HUD) context().game().hud();
        hud.all(false);
        hud.showGameOptions(false);

        final GameUI_Config uiConfig = ui.currentConfig();
        final var spriteSheet = (TengenMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        clapperboard = new Clapperboard(spriteSheet, 4, "THE END");
        clapperboard.setPosition(TS(3), TS(10));
        clapperboard.setFont(actorRenderer.arcadeFontTS());
        clapperboard.setVisible(true);
        clapperboard.startAnimation();

        msPacMan = createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations());

        pacMan = createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations());

        juniors = new ArrayList<>();
        juniorCreationTime = new ArrayList<>();

        ui.soundManager().play(MUSIC_ID);
    }

    @Override
    protected void doEnd() {
        ui.soundManager().stop(MUSIC_ID);
    }

    @Override
    public void update() {
        final int tick = (int) context().gameState().timer().tickCount();

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
                pacMan.playAnimation(ANIM_PAC_MAN_MUNCHING);
                pacMan.show();

                msPacMan.setMoveDir(Direction.LEFT);
                msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
                msPacMan.setSpeed(1f);
                msPacMan.playAnimation(ANIM_PAC_MUNCHING);
                msPacMan.show();
            } case 230 -> {
                pacMan.setSpeed(0);
                pacMan.animations().ifPresent(am -> {
                    am.stop();
                    am.reset();
                });
                msPacMan.setSpeed(0);
                msPacMan.animations().ifPresent(am -> {
                    am.stop();
                    am.reset();
                });
            }
            case 400 -> {
                pacMan.playAnimation(ANIM_PAC_MAN_MUNCHING);
                msPacMan.playAnimation(ANIM_PAC_MUNCHING);
            }
            case 520 -> {
                pacMan.selectAnimation(ANIM_PAC_MAN_WAVING_HAND);
                msPacMan.selectAnimation(ANIM_MS_PAC_MAN_WAVING_HAND);
            }
            case 527 -> {
                pacMan.animations().ifPresent(AnimationManager::play);
                msPacMan.animations().ifPresent(AnimationManager::play);
            }
            case 648 -> {
                pacMan.playAnimation(ANIM_PAC_MAN_TURNING_AWAY);
                msPacMan.playAnimation(ANIM_MS_PAC_MAN_TURNING_AWAY);
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
            case 1512 -> context().gameController().changeGameState(GamePlayState.SETTING_OPTIONS_FOR_START);
        }
    }

    private void spawnJunior(int tick) {
        var junior = createPacMan();
        double randomX = 8 * TS + (8 * TS) * Math.random();
        junior.setPosition((float) randomX, sizeInPx().y() - 4 * TS);
        junior.setMoveDir(Direction.UP);
        junior.setSpeed(2);
        junior.setAnimations(ui.currentConfig().createPacAnimations());
        junior.selectAnimation(ANIM_JUNIOR);
        junior.show();
        juniors.add(junior);
        juniorCreationTime.add(tick);

        String id = "audio.intermission.4.junior." + randomInt(1, 3); // 1 or 2
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
    public Vector2i sizeInPx() { return NES_SIZE_PX; }

    @Override
    protected void drawHUD() {
        if (hudRenderer != null) {
            // draw HUD only for non-Arcade map mode
            var game = context().<TengenMsPacMan_GameModel>game();
            if (game.mapCategory() != MapCategory.ARCADE) {
                hudRenderer.drawHUD(context().game(), game.hud(), sizeInPx().minus(0, 2 * TS));
            }
        }
    }

    @Override
    public void drawSceneContent() {
        if (actorRenderer != null) {
            Stream.of(clapperboard, msPacMan, pacMan).forEach(actorRenderer::drawActor);
            juniors.forEach(actor -> actorRenderer.drawActor(actor));
        }
    }
}