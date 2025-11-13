/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.actors.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_ActorRenderer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_HUDRenderer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.controller.PacManGamesState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.Pulse;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.action.ArcadeActions;
import de.amr.pacmanfx.ui.action.TestActions;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.rendering.SpriteID.GALLERY_GHOSTS;
import static de.amr.pacmanfx.model.actors.GhostState.EATEN;
import static de.amr.pacmanfx.model.actors.GhostState.FRIGHTENED;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;

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

    private final StateMachine<SceneState, ArcadePacMan_IntroScene> sceneController;

    private ArcadePacMan_SpriteSheet spriteSheet;

    private ArcadePacMan_HUDRenderer hudRenderer;
    private ArcadePacMan_ActorRenderer actorRenderer;

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
        sceneController = new StateMachine<>(SceneState.values(), this);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        super.createRenderers(canvas);

        GameUI_Config uiConfig = ui.currentConfig();

        spriteSheet = (ArcadePacMan_SpriteSheet) uiConfig.spriteSheet();
        sceneRenderer.setImageSmoothing(true);

        hudRenderer       = configureRenderer((ArcadePacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas));
        actorRenderer     = configureRenderer((ArcadePacMan_ActorRenderer) uiConfig.createActorRenderer(canvas));
        debugInfoRenderer = configureRenderer(new BaseDebugInfoRenderer(ui, canvas) {
            @Override
            public void drawDebugInfo() {
                super.drawDebugInfo();
                ctx.fillText("Scene timer %d".formatted(sceneController.state().timer().tickCount()), 0, scaled(5 * TS));
            }
        });
    }

    @Override
    public ArcadePacMan_HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public void doInit() {
        final GameUI_Config uiConfig = ui.currentConfig();

        context().game().hud().creditVisible(true).scoreVisible(true).livesCounterVisible(false).levelCounterVisible(true);

        actionBindings.bind(ArcadeActions.ACTION_INSERT_COIN, ui.actionBindings());
        actionBindings.bind(ArcadeActions.ACTION_START_GAME, ui.actionBindings());
        actionBindings.bind(TestActions.ACTION_CUT_SCENES_TEST, ui.actionBindings());
        actionBindings.bind(TestActions.ACTION_SHORT_LEVEL_TEST, ui.actionBindings());
        actionBindings.bind(TestActions.ACTION_MEDIUM_LEVEL_TEST, ui.actionBindings());

        blinking = new Pulse(10, Pulse.State.ON);

        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());
        pacMan.selectAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);

        ghosts = List.of(
            uiConfig.createAnimatedGhost(RED_GHOST_SHADOW),
            uiConfig.createAnimatedGhost(PINK_GHOST_SPEEDY),
            uiConfig.createAnimatedGhost(CYAN_GHOST_BASHFUL),
            uiConfig.createAnimatedGhost(ORANGE_GHOST_POKEY)
        );

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
    public void drawSceneContent() {
        final GraphicsContext ctx = sceneRenderer.ctx();
        drawGallery(ctx);
        switch (sceneController.state()) {
            case STARTING, PRESENTING_GHOSTS -> {}
            case SHOWING_PELLET_POINTS -> drawPoints(ctx);
            case CHASING_PAC -> {
                drawPoints(ctx);
                drawBlinkingEnergizer(ctx, TS(LEFT_TILE_X), TS(20));
                drawGuys(ctx, true);
                sceneRenderer.fillText(MIDWAY_MFG_CO, ARCADE_PINK, sceneRenderer.arcadeFont8(), TS(4), TS(32));
            }
            case CHASING_GHOSTS, READY_TO_PLAY -> {
                drawPoints(ctx);
                drawGuys(ctx, false);
                sceneRenderer.fillText(MIDWAY_MFG_CO, ARCADE_PINK, sceneRenderer.arcadeFont8(), TS(4), TS(32));
            }
        }
    }

    private void drawGallery(GraphicsContext ctx) {
        ctx.setFont(sceneRenderer.arcadeFont8());
        if (titleVisible) {
            sceneRenderer.fillText("CHARACTER / NICKNAME", ARCADE_WHITE, TS(LEFT_TILE_X + 3), TS(6));
        }
        for (byte p = RED_GHOST_SHADOW; p <= ORANGE_GHOST_POKEY; ++p) {
            if (ghostImageVisible[p]) {
                RectShort sprite = spriteSheet.spriteSequence(GALLERY_GHOSTS)[p];
                sceneRenderer.drawSpriteCentered(TS(LEFT_TILE_X + 1), TS(7.5 + 3 * p), sprite);
            }
            if (ghostCharacterVisible[p]) {
                sceneRenderer.fillText("-" + GHOST_CHARACTERS[p], GHOST_COLORS[p], TS(LEFT_TILE_X + 3), TS(8 + 3 * p));
            }
            if (ghostNicknameVisible[p]) {
                sceneRenderer.fillText(GHOST_NICKNAMES[p], GHOST_COLORS[p], TS(LEFT_TILE_X + 14), TS(8 + 3 * p));
            }
        }
    }

    // TODO make shaking effect look exactly as in original game, find out what's exactly is going on here
    private void drawGuys(GraphicsContext ctx, boolean shaking) {
        long tick = sceneController.state().timer().tickCount();
        int shakingAmount = shaking ? (tick % 5 < 2 ? 0 : -1) : 0;
        if (shakingAmount == 0) {
            ghosts.forEach(ghost -> actorRenderer.drawActor(ghost));
        } else {
            actorRenderer.drawActor(ghosts.get(RED_GHOST_SHADOW));
            actorRenderer.drawActor(ghosts.get(ORANGE_GHOST_POKEY));
            ctx.save();
            ctx.translate(shakingAmount, 0);
            actorRenderer.drawActor(ghosts.get(PINK_GHOST_SPEEDY));
            actorRenderer.drawActor(ghosts.get(CYAN_GHOST_BASHFUL));
            ctx.restore();
        }
        actorRenderer.drawActor(pacMan);
    }

    private void drawPoints(GraphicsContext ctx) {
        ctx.setFill(ARCADE_ROSE);
        // normal pellet
        ctx.fillRect(scaled(TS(LEFT_TILE_X + 6) + 4), scaled(TS(24) + 4), scaled(2), scaled(2));
        sceneRenderer.fillText("10",  ARCADE_WHITE, sceneRenderer.arcadeFont8(), TS(LEFT_TILE_X + 8), TS(25));
        sceneRenderer.fillText("PTS", ARCADE_WHITE, sceneRenderer.arcadeFont6(), TS(LEFT_TILE_X + 11), TS(25));
        // energizer
        drawBlinkingEnergizer(ctx, TS(LEFT_TILE_X + 6), TS(26));
        sceneRenderer.fillText("50",  ARCADE_WHITE, sceneRenderer.arcadeFont8(), TS(LEFT_TILE_X + 8), TS(27));
        sceneRenderer.fillText("PTS", ARCADE_WHITE, sceneRenderer.arcadeFont6(), TS(LEFT_TILE_X + 11), TS(27));
    }

    private void drawBlinkingEnergizer(GraphicsContext ctx, double x, double y) {
        if (blinking.state() == Pulse.State.ON) {
            ctx.save();
            ctx.scale(scaling(), scaling());
            ctx.setFill(ARCADE_ROSE);
            // draw pixelated "circle"
            ctx.fillRect(x + 2, y, 4, 8);
            ctx.fillRect(x, y + 2, 8, 4);
            ctx.fillRect(x + 1, y + 1, 6, 6);
            ctx.restore();
        }
    }

    private enum SceneState implements FsmState<ArcadePacMan_IntroScene> {

        STARTING {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() == 3) {
                    scene.titleVisible = true;
                } else if (timer.atSecond(1)) {
                    scene.sceneController.changeState(PRESENTING_GHOSTS);
                }
            }
        },

        PRESENTING_GHOSTS {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() == 1) {
                    scene.ghostImageVisible[scene.ghostIndex] = true;
                } else if (timer.atSecond(1.0)) {
                    scene.ghostCharacterVisible[scene.ghostIndex] = true;
                } else if (timer.atSecond(1.5)) {
                    scene.ghostNicknameVisible[scene.ghostIndex] = true;
                } else if (timer.atSecond(2.0)) {
                    if (scene.ghostIndex < scene.ghosts.size() - 1) {
                        timer.resetIndefiniteTime();
                    }
                    scene.ghostIndex += 1;
                } else if (timer.atSecond(2.5)) {
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
                if (timer.atSecond(1)) {
                    scene.sceneController.changeState(CHASING_PAC);
                }
            }
        },

        CHASING_PAC {

            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                timer.restartIndefinitely();
                scene.pacMan.setPosition(TS * 36, TS * 20);
                scene.pacMan.setMoveDir(Direction.LEFT);
                scene.pacMan.setSpeed(CHASING_SPEED);
                scene.pacMan.show();
                scene.pacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                scene.ghosts.forEach(ghost -> {
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setPosition(scene.pacMan.x() + 16 * (ghost.personality() + 1), scene.pacMan.y());
                    ghost.setMoveDir(Direction.LEFT);
                    ghost.setWishDir(Direction.LEFT);
                    ghost.setSpeed(CHASING_SPEED);
                    ghost.show();
                    ghost.playAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
                });
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.atSecond(1)) {
                    scene.blinking.start();
                }
                else if (timer.tickCount() == 232) {
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
                } else if (timer.tickCount() == 236) {
                    // Pac-Man moves again a bit
                    scene.pacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                    scene.pacMan.setSpeed(CHASING_SPEED);
                } else if (timer.tickCount() == 240) {
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
                timer.restartIndefinitely();
                scene.ghostKilledTime = timer.tickCount();
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
                    .filter(ghost -> ghost.state() == FRIGHTENED)
                    .filter(ghost -> theGameContext().game().actorsCollide(ghost, scene.pacMan))
                    .findFirst()
                    .ifPresent(victim -> {
                        scene.victims.add(victim);
                        scene.ghostKilledTime = timer.tickCount();
                        scene.pacMan.hide();
                        scene.pacMan.setSpeed(0);
                        scene.ghosts.forEach(ghost -> {
                            ghost.setSpeed(0);
                            ghost.stopAnimation();
                        });
                        victim.setState(EATEN);
                        victim.selectAnimationAt(CommonAnimationID.ANIM_GHOST_NUMBER, scene.victims.size() - 1);
                    });

                // After 50 ticks, Pac-Man and the surviving ghosts get visible again and move on
                if (timer.tickCount() == scene.ghostKilledTime + 50) {
                    scene.pacMan.show();
                    scene.pacMan.setSpeed(CHASING_SPEED);
                    scene.ghosts.forEach(ghost -> {
                        if (ghost.inAnyOfStates(EATEN)) {
                            ghost.hide();
                        } else {
                            ghost.show();
                            ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
                            ghost.playAnimation(CommonAnimationID.ANIM_GHOST_FRIGHTENED);
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
                if (timer.atSecond(0.75)) {
                    scene.ghosts.get(ORANGE_GHOST_POKEY).hide();
                    if (!scene.context().game().canStartNewGame()) {
                        scene.context().gameController().changeGameState(PacManGamesState.STARTING_GAME_OR_LEVEL);
                    }
                } else if (timer.atSecond(5)) {
                    scene.context().gameController().changeGameState(PacManGamesState.SETTING_OPTIONS_FOR_START);
                }
            }
        };

        final TickTimer timer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return timer;
        }
    }
}