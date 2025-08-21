/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_HUDRenderer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createGhost;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createPac;
import static de.amr.pacmanfx.arcade.pacman.rendering.SpriteID.GALLERY_GHOSTS;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.model.actors.GhostState.EATEN;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.ui.CommonGameActions.*;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;

/**
 * The ghosts are presented one by one, Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 */
public class ArcadePacMan_IntroScene extends GameScene2D {

    private static final String MIDWAY_MFG_CO = "© 1980 MIDWAY MFG.CO.";

    private static final String[] GHOST_NICKNAMES  = { "\"BLINKY\"", "\"PINKY\"", "\"INKY\"", "\"CLYDE\"" };
    private static final String[] GHOST_CHARACTERS = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
    private static final Color[]  GHOST_COLORS     = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    private static final float CHASING_SPEED = 1.1f;
    private static final float GHOST_FRIGHTENED_SPEED = 0.6f;
    private static final int LEFT_TILE_X = 4;

    private final StateMachine<SceneState, ArcadePacMan_IntroScene> sceneController = new StateMachine<>(List.of(SceneState.values())) {
        @Override public ArcadePacMan_IntroScene context() { return ArcadePacMan_IntroScene.this; }
    };

    private ArcadePacMan_SpriteSheet spriteSheet;

    private ArcadePacMan_HUDRenderer hudRenderer;
    private GameLevelRenderer gameLevelRenderer;

    private Pulse blinking;
    private Pac pacMan;
    private List<Ghost> ghosts;
    private boolean[] ghostImageVisible;
    private boolean[] ghostNicknameVisible;
    private boolean[] ghostCharacterVisible;
    private List<Ghost> victims;
    private boolean titleVisible;
    private int ghostIndex;
    private long ghostKilledTime;

    public ArcadePacMan_IntroScene(GameUI ui) {
        super(ui);
    }

    @Override
    public void doInit() {
        hudRenderer = (ArcadePacMan_HUDRenderer) ui.currentConfig().createHUDRenderer(canvas);
        gameLevelRenderer = ui.currentConfig().createGameLevelRenderer(canvas);
        debugInfoRenderer = new DefaultDebugInfoRenderer(ui, canvas) {
            @Override
            public void drawDebugInfo() {
                super.drawDebugInfo();
                ctx.fillText("Scene timer %d".formatted(sceneController.state().timer().tickCount()), 0, scaled(5 * TS));
            }
        };
        bindRendererScaling(hudRenderer, gameLevelRenderer, debugInfoRenderer);

        context().game().hudData().credit(true).score(true).livesCounter(false).levelCounter(true);

        actionBindings.assign(ACTION_ARCADE_INSERT_COIN, ui.actionBindings());
        actionBindings.assign(ACTION_ARCADE_START_GAME, ui.actionBindings());
        actionBindings.assign(ACTION_TEST_CUT_SCENES, ui.actionBindings());
        actionBindings.assign(ACTION_TEST_LEVELS_SHORT, ui.actionBindings());
        actionBindings.assign(ACTION_TEST_LEVELS_MEDIUM, ui.actionBindings());

        spriteSheet = (ArcadePacMan_SpriteSheet) ui.currentConfig().spriteSheet();

        blinking = new Pulse(10, true);
        pacMan = createPac();
        pacMan.setAnimations(ui.currentConfig().createPacAnimations(pacMan));
        pacMan.selectAnimation(ANIM_PAC_MUNCHING);
        ghosts = List.of(
            createGhost(RED_GHOST_SHADOW),
            createGhost(PINK_GHOST_SPEEDY),
            createGhost(CYAN_GHOST_BASHFUL),
            createGhost(ORANGE_GHOST_POKEY));
        ghosts.forEach(ghost -> ghost.setAnimations(ui.currentConfig().createGhostAnimations(ghost)));
        ghostImageVisible     = new boolean[4];
        ghostNicknameVisible  = new boolean[4];
        ghostCharacterVisible = new boolean[4];
        victims = new ArrayList<>(4);
        titleVisible = false;
        ghostIndex = 0;
        ghostKilledTime = 0;

        sceneController.restart(SceneState.STARTING);
    }

    @Override
    protected void doEnd() {
        blinking.stop();
    }

    @Override
    public void update() {
        sceneController.update();
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }

    @Override
    public Vector2f sizeInPx() { return ARCADE_MAP_SIZE_IN_PIXELS; }


    @Override
    public void drawHUD() {
        if (hudRenderer != null) {
            hudRenderer.drawHUD(context(), context().game().hudData(), sizeInPx());
        }
    }

    @Override
    public void drawSceneContent() {
        drawGallery();
        switch (sceneController.state()) {
            case STARTING, PRESENTING_GHOSTS -> {}
            case SHOWING_PELLET_POINTS -> drawPoints();
            case CHASING_PAC -> {
                drawPoints();
                drawBlinkingEnergizer(TS(LEFT_TILE_X), TS(20));
                drawGuys(true);
                gameLevelRenderer.fillText(MIDWAY_MFG_CO, ARCADE_PINK, gameLevelRenderer.arcadeFontTS(), TS(4), TS(32));
            }
            case CHASING_GHOSTS, READY_TO_PLAY -> {
                drawPoints();
                drawGuys(false);
                gameLevelRenderer.fillText(MIDWAY_MFG_CO, ARCADE_PINK, gameLevelRenderer.arcadeFontTS(), TS(4), TS(32));
            }
        }
    }

    private void drawGallery() {
        gameLevelRenderer.ctx().setFont(gameLevelRenderer.arcadeFontTS());
        if (titleVisible) {
            gameLevelRenderer.fillText("CHARACTER / NICKNAME", ARCADE_WHITE,
                TS(LEFT_TILE_X + 3), TS(6));
        }
        for (byte personality = RED_GHOST_SHADOW; personality <= ORANGE_GHOST_POKEY; ++personality) {
            if (ghostImageVisible[personality]) {
                gameLevelRenderer.drawSpriteCentered(
                    Vector2f.of(TS(LEFT_TILE_X) + TS, TS(7 + 3 * personality) + HTS),
                    spriteSheet.spriteSequence(GALLERY_GHOSTS)[personality]);
            }
            if (ghostCharacterVisible[personality]) {
                gameLevelRenderer.fillText("-" + GHOST_CHARACTERS[personality], GHOST_COLORS[personality],
                    TS(LEFT_TILE_X + 3), TS(8 + 3 * personality));
            }
            if (ghostNicknameVisible[personality]) {
                gameLevelRenderer.fillText(GHOST_NICKNAMES[personality], GHOST_COLORS[personality],
                    TS(LEFT_TILE_X + 14), TS(8 + 3 * personality));
            }
        }
    }

    // TODO make shaking effect look exactly as in original game, find out what's exactly is going on here
    private void drawGuys(boolean shaking) {
        long tick = sceneController.state().timer().tickCount();
        int shakingAmount = shaking ? (tick % 5 < 2 ? 0 : -1) : 0;
        if (shakingAmount == 0) {
            ghosts.forEach(ghost -> gameLevelRenderer.drawActor(ghost));
        } else {
            gameLevelRenderer.drawActor(ghosts.get(RED_GHOST_SHADOW));
            gameLevelRenderer.drawActor(ghosts.get(ORANGE_GHOST_POKEY));
            gameLevelRenderer.ctx().save();
            gameLevelRenderer.ctx().translate(shakingAmount, 0);
            gameLevelRenderer.drawActor(ghosts.get(PINK_GHOST_SPEEDY));
            gameLevelRenderer.drawActor(ghosts.get(CYAN_GHOST_BASHFUL));
            gameLevelRenderer.ctx().restore();
        }
        gameLevelRenderer.drawActor(pacMan);
    }

    private void drawPoints() {
        gameLevelRenderer.ctx().setFill(ARCADE_ROSE);
        // normal pellet
        gameLevelRenderer.ctx().fillRect(scaled(TS(LEFT_TILE_X + 6) + 4), scaled(TS(24) + 4), scaled(2), scaled(2));
        gameLevelRenderer.fillText("10",  ARCADE_WHITE, gameLevelRenderer.arcadeFontTS(), TS(LEFT_TILE_X + 8), TS(25));
        gameLevelRenderer.fillText("PTS", ARCADE_WHITE, gameLevelRenderer.arcadeFont6(), TS(LEFT_TILE_X + 11), TS(25));
        // energizer
        drawBlinkingEnergizer(TS(LEFT_TILE_X + 6), TS(26));
        gameLevelRenderer.fillText("50",  ARCADE_WHITE, gameLevelRenderer.arcadeFontTS(), TS(LEFT_TILE_X + 8), TS(27));
        gameLevelRenderer.fillText("PTS", ARCADE_WHITE, gameLevelRenderer.arcadeFont6(), TS(LEFT_TILE_X + 11), TS(27));
    }

    private void drawBlinkingEnergizer(double x, double y) {
        if (blinking.isOn()) {
            gameLevelRenderer.ctx().save();
            gameLevelRenderer.ctx().scale(scaling(), scaling());
            gameLevelRenderer.ctx().setFill(ARCADE_ROSE);
            // draw pixelated "circle"
            gameLevelRenderer.ctx().fillRect(x + 2, y, 4, 8);
            gameLevelRenderer.ctx().fillRect(x, y + 2, 8, 4);
            gameLevelRenderer.ctx().fillRect(x + 1, y + 1, 6, 6);
            gameLevelRenderer.ctx().restore();
        }
    }

    private enum SceneState implements FsmState<ArcadePacMan_IntroScene> {

        STARTING {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (sceneTimer.tickCount() == 3) {
                    scene.titleVisible = true;
                } else if (sceneTimer.atSecond(1)) {
                    scene.sceneController.changeState(PRESENTING_GHOSTS);
                }
            }
        },

        PRESENTING_GHOSTS {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (sceneTimer.tickCount() == 1) {
                    scene.ghostImageVisible[scene.ghostIndex] = true;
                } else if (sceneTimer.atSecond(1.0)) {
                    scene.ghostCharacterVisible[scene.ghostIndex] = true;
                } else if (sceneTimer.atSecond(1.5)) {
                    scene.ghostNicknameVisible[scene.ghostIndex] = true;
                } else if (sceneTimer.atSecond(2.0)) {
                    if (scene.ghostIndex < scene.ghosts.size() - 1) {
                        sceneTimer.resetIndefiniteTime();
                    }
                    scene.ghostIndex += 1;
                } else if (sceneTimer.atSecond(2.5)) {
                    scene.sceneController.changeState(SHOWING_PELLET_POINTS);
                }
            }
        },

        SHOWING_PELLET_POINTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                scene.blinking.stop();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (sceneTimer.atSecond(1)) {
                    scene.sceneController.changeState(CHASING_PAC);
                }
            }
        },

        CHASING_PAC {

            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                sceneTimer.restartIndefinitely();
                scene.pacMan.setPosition(TS * 36, TS * 20);
                scene.pacMan.setMoveDir(Direction.LEFT);
                scene.pacMan.setSpeed(CHASING_SPEED);
                scene.pacMan.show();
                scene.pacMan.playAnimation(ANIM_PAC_MUNCHING);
                scene.ghosts.forEach(ghost -> {
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setPosition(scene.pacMan.x() + 16 * (ghost.id().personality() + 1), scene.pacMan.y());
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setSpeed(CHASING_SPEED);
                    ghost.show();
                    ghost.playAnimation(ANIM_GHOST_NORMAL);
                });
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (sceneTimer.atSecond(1)) {
                    scene.blinking.start();
                }
                else if (sceneTimer.tickCount() == 232) {
                    // Pac-Man reaches the energizer at the left and stops
                    scene.pacMan.setSpeed(0);
                    scene.pacMan.stopAnimation();
                    // Ghosts get frightened and reverse direction
                    scene.ghosts.forEach(ghost -> {
                        ghost.setState(FRIGHTENED);
                        ghost.setMoveDir(Direction.RIGHT);
                        ghost.setWishDir(Direction.RIGHT);
                        ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
                    });
                } else if (sceneTimer.tickCount() == 236) {
                    // Pac-Man moves again a bit
                    scene.pacMan.playAnimation(ANIM_PAC_MUNCHING);
                    scene.pacMan.setSpeed(CHASING_SPEED);
                } else if (sceneTimer.tickCount() == 240) {
                    scene.sceneController.changeState(CHASING_GHOSTS);
                }
                scene.blinking.tick();
                scene.pacMan.move();
                scene.ghosts.forEach(Ghost::move);
            }
        },

        CHASING_GHOSTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                sceneTimer.restartIndefinitely();
                scene.ghostKilledTime = sceneTimer.tickCount();
                scene.pacMan.setMoveDir(Direction.RIGHT);
                scene.pacMan.setSpeed(CHASING_SPEED);
                scene.victims.clear();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (scene.ghosts.stream().allMatch(ghost -> ghost.inAnyOfStates(EATEN))) {
                    scene.pacMan.hide();
                    scene.sceneController.changeState(READY_TO_PLAY);
                    return;
                }

                scene.ghosts.stream()
                    .filter(ghost -> ghost.inAnyOfStates(FRIGHTENED) && ghost.sameTilePosition(scene.pacMan))
                    .findFirst()
                    .ifPresent(victim -> {
                        scene.victims.add(victim);
                        scene.ghostKilledTime = sceneTimer.tickCount();
                        scene.pacMan.hide();
                        scene.pacMan.setSpeed(0);
                        scene.ghosts.forEach(ghost -> {
                            ghost.setSpeed(0);
                            ghost.stopAnimation();
                        });
                        victim.setState(EATEN);
                        victim.selectAnimationFrame(ANIM_GHOST_NUMBER, scene.victims.size() - 1);
                    });

                // After 50 ticks, Pac-Man and the surviving ghosts get visible again and move on
                if (sceneTimer.tickCount() == scene.ghostKilledTime + 50) {
                    scene.pacMan.show();
                    scene.pacMan.setSpeed(CHASING_SPEED);
                    scene.ghosts.forEach(ghost -> {
                        if (ghost.inAnyOfStates(EATEN)) {
                            ghost.hide();
                        } else {
                            ghost.show();
                            ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
                            ghost.playAnimation(ANIM_GHOST_FRIGHTENED);
                        }
                    });
                }

                scene.pacMan.move();
                scene.ghosts.forEach(Ghost::move);
                scene.blinking.tick();
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (sceneTimer.atSecond(0.75)) {
                    scene.ghosts.get(ORANGE_GHOST_POKEY).hide();
                    if (!scene.context().game().canStartNewGame()) {
                        scene.context().gameController().changeGameState(GamePlayState.STARTING_GAME);
                    }
                } else if (sceneTimer.atSecond(5)) {
                    scene.context().gameController().changeGameState(GamePlayState.SETTING_OPTIONS_FOR_START);
                }
            }
        };

        final TickTimer sceneTimer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return sceneTimer;
        }
    }
}