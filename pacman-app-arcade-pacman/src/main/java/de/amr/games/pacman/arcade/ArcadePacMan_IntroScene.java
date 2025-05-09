/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.model.actors.ActorAnimations.*;
import static de.amr.games.pacman.model.actors.GhostState.EATEN;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.ui.GameAssets.*;
import static de.amr.games.pacman.ui.Globals.*;

/**
 * <p>
 * The ghosts are presented one by one, Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 *
 * @author Armin Reichert
 */
public class ArcadePacMan_IntroScene extends GameScene2D {

    private static final String[] GHOST_NICKNAME = { "\"BLINKY\"", "\"PINKY\"", "\"INKY\"", "\"CLYDE\"" };
    private static final String[] GHOST_CHARACTERS = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
    private static final Color[] GHOST_COLORS = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };
    private static final float CHASE_SPEED = 1.1f;
    private static final float GHOST_FRIGHTENED_SPEED = 0.6f;
    private static final int LEFT_TILE_X = 4;

    private final FiniteStateMachine<SceneState, ArcadePacMan_IntroScene> sceneController;

    private Pulse blinking;
    private Pac pacMan;
    private Ghost[] ghosts;
    private boolean[] ghostImageVisible;
    private boolean[] ghostNicknameVisible;
    private boolean[] ghostCharacterVisible;
    private List<Ghost> victims;
    private boolean titleVisible;
    private int ghostIndex;
    private long ghostKilledTime;

    public ArcadePacMan_IntroScene() {
        sceneController = new FiniteStateMachine<>(SceneState.values()) {
            @Override
            public ArcadePacMan_IntroScene context() {
                return ArcadePacMan_IntroScene.this;
            }
        };
    }

    @Override
    public void bindActions() {
        bindArcadeInsertCoinAction();
        bindArcadeStartGameAction();
        bindStartTestsActions();
    }

    @Override
    public void doInit() {
        ArcadePacMan_SpriteSheet spriteSheet = THE_UI_CONFIGS.current().spriteSheet();
        blinking = new Pulse(10, true);
        pacMan = ArcadePacMan_ActorFactory.createPac();
        pacMan.setAnimations(new ArcadePacMan_PacAnimations(spriteSheet));
        ghosts = new Ghost[] {
            ArcadePacMan_ActorFactory.createRedGhost(),
            ArcadePacMan_ActorFactory.createPinkGhost(),
            ArcadePacMan_ActorFactory.createCyanGhost(),
            ArcadePacMan_ActorFactory.createOrangeGhost()
        };
        for (Ghost ghost : ghosts) {
            ghost.setAnimations(new ArcadePacMan_GhostAnimations(spriteSheet, ghost.id()));
        }
        ghostImageVisible     = new boolean[4];
        ghostNicknameVisible  = new boolean[4];
        ghostCharacterVisible = new boolean[4];
        victims = new ArrayList<>(4);
        titleVisible = false;
        ghostIndex = 0;
        ghostKilledTime = 0;
        game().scoreManager().setScoreVisible(true);
        sceneController.restart(SceneState.STARTING);
    }

    @Override
    protected void doEnd() {
        THE_SOUND.stopVoice();
    }

    @Override
    public void update() {
        sceneController.update();
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_SOUND.playInsertCoinSound();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        final Font font = arcadeFontScaledTS();
        gr.fillCanvas(backgroundColor());
        gr.drawScores(game().scoreManager(), ARCADE_WHITE, font);
        TickTimer timer = sceneController.state().timer();
        drawGallery(font);
        switch (sceneController.state()) {
            case SHOWING_POINTS -> drawPoints();
            case CHASING_PAC -> {
                drawPoints();
                if (blinking.isOn()) {
                    drawEnergizer(tiles_to_px(LEFT_TILE_X), tiles_to_px(20));
                }
                drawGuys(flutter(timer.tickCount()));
                if (gr instanceof ArcadePacMan_GameRenderer r) {
                    r.drawMidwayCopyright(4, 32, ARCADE_PINK, font);
                }
            }
            case CHASING_GHOSTS, READY_TO_PLAY -> {
                drawPoints();
                drawGuys(0);
                if (gr instanceof ArcadePacMan_GameRenderer r) {
                    r.drawMidwayCopyright(4, 32, ARCADE_PINK, font);
                }
            }
        }
        gr.fillTextAtScaledPosition("CREDIT %2d".formatted(THE_COIN_MECHANISM.numCoins()), ARCADE_WHITE, font, 2 * TS, sizeInPx().y() - 2);
        gr.drawLevelCounter(game().levelCounter(), sizeInPx());
    }

    // TODO inspect in MAME what's really going on here
    private int flutter(long time) {
        return time % 5 < 2 ? 0 : -1;
    }

    private void drawGallery(Font font) {
        var spriteSheet = (ArcadePacMan_SpriteSheet) THE_UI_CONFIGS.current().spriteSheet();
        if (titleVisible) {
            gr.fillTextAtScaledPosition("CHARACTER / NICKNAME", ARCADE_WHITE, font, tiles_to_px(LEFT_TILE_X + 3), tiles_to_px(6));
        }
        for (byte id = 0; id < 4; ++id) {
            if (ghostImageVisible[id]) {
                gr.drawSpriteScaledCenteredOverTile(spriteSheet.ghostFacingRight(id),
                    tiles_to_px(LEFT_TILE_X) + HTS, tiles_to_px(7 + 3 * id));
            }
            if (ghostCharacterVisible[id]) {
                gr.fillTextAtScaledPosition("-" + GHOST_CHARACTERS[id], GHOST_COLORS[id], font,
                    tiles_to_px(LEFT_TILE_X + 3), tiles_to_px(8 + 3 * id));
            }
            if (ghostNicknameVisible[id]) {
                gr.fillTextAtScaledPosition(GHOST_NICKNAME[id], GHOST_COLORS[id], font,
                    tiles_to_px(LEFT_TILE_X + 14), tiles_to_px(8 + 3 * id));
            }
        }
    }

    private void drawGuys(int shakingAmount) {
        if (shakingAmount == 0) {
            Stream.of(ghosts).forEach(gr::drawAnimatedActor);
        } else {
            gr.drawAnimatedActor(ghosts[0]);
            gr.drawAnimatedActor(ghosts[3]);
            // shaking ghosts effect, not quite as in original game
            gr.ctx().save();
            gr.ctx().translate(shakingAmount, 0);
            gr.drawAnimatedActor(ghosts[1]);
            gr.drawAnimatedActor(ghosts[2]);
            gr.ctx().restore();
        }
        gr.drawAnimatedActor(pacMan);
    }

    private void drawPoints() {
        Font font8 = arcadeFontScaledTS();
        Font font6 = THE_ASSETS.arcadeFontAtSize(scaled(6));
        int tileX = LEFT_TILE_X + 6;
        int tileY = 25;
        gr.ctx().setFill(ARCADE_ROSE);
        gr.ctx().fillRect(scaled(tiles_to_px(tileX) + 4), scaled(tiles_to_px(tileY - 1) + 4), scaled(2), scaled(2));
        if (blinking.isOn()) {
            drawEnergizer(tiles_to_px(tileX), tiles_to_px(tileY + 1));
        }
        gr.fillTextAtScaledPosition("10",  ARCADE_WHITE, font8, tiles_to_px(tileX + 2), tiles_to_px(tileY));
        gr.fillTextAtScaledPosition("PTS", ARCADE_WHITE, font6, tiles_to_px(tileX + 5), tiles_to_px(tileY));
        gr.fillTextAtScaledPosition("50",  ARCADE_WHITE, font8, tiles_to_px(tileX + 2), tiles_to_px(tileY + 2));
        gr.fillTextAtScaledPosition("PTS", ARCADE_WHITE, font6, tiles_to_px(tileX + 5), tiles_to_px(tileY + 2));
    }

    // draw pixelated "circle"
    private void drawEnergizer(double x, double y) {
        gr.ctx().save();
        gr.ctx().scale(scaling(), scaling());
        gr.ctx().setFill(ARCADE_ROSE);
        gr.ctx().fillRect(x + 2, y, 4, 8);
        gr.ctx().fillRect(x, y + 2, 8, 4);
        gr.ctx().fillRect(x + 1, y + 1, 6, 6);
        gr.ctx().restore();
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
                    if (scene.ghostIndex < scene.ghosts.length - 1) {
                        sceneTimer.resetIndefiniteTime();
                    }
                    scene.ghostIndex += 1;
                } else if (sceneTimer.atSecond(2.5)) {
                    scene.sceneController.changeState(SHOWING_POINTS);
                }
            }
        },

        SHOWING_POINTS {
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
                scene.pacMan.setSpeed(CHASE_SPEED);
                scene.pacMan.show();
                scene.pacMan.selectAnimation(ANIM_PAC_MUNCHING);
                scene.pacMan.startAnimation();
                Stream.of(scene.ghosts).forEach(ghost -> {
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setPosition(scene.pacMan.position().plus(16 * (ghost.id() + 1), 0));
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setSpeed(CHASE_SPEED);
                    ghost.show();
                    ghost.selectAnimation(ANIM_GHOST_NORMAL);
                    ghost.startAnimation();
                });
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (sceneTimer.atSecond(1)) {
                    scene.blinking.start();
                }
                // Pac-Man reaches the energizer at the left and turns
                if (scene.pacMan.posX() <= TS * LEFT_TILE_X) {
                    scene.sceneController.changeState(CHASING_GHOSTS);
                }
                // Ghosts already reverse direction before Pac-Man eats the energizer and turns!
                else if (scene.pacMan.posX() <= TS * LEFT_TILE_X + HTS) {
                    Stream.of(scene.ghosts).forEach(ghost -> {
                        ghost.setState(FRIGHTENED);
                        ghost.selectAnimation(ANIM_GHOST_FRIGHTENED);
                        ghost.setMoveAndWishDir(Direction.RIGHT);
                        ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
                        ghost.move();
                    });
                    scene.pacMan.move();
                } else { // keep moving
                    scene.blinking.tick();
                    scene.pacMan.move();
                    Stream.of(scene.ghosts).forEach(Ghost::move);
                }
            }
        },

        CHASING_GHOSTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                sceneTimer.restartIndefinitely();
                scene.ghostKilledTime = sceneTimer.tickCount();
                scene.pacMan.setMoveDir(Direction.RIGHT);
                scene.pacMan.setSpeed(CHASE_SPEED);
                scene.victims.clear();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (Stream.of(scene.ghosts).allMatch(ghost -> ghost.inState(EATEN))) {
                    scene.pacMan.hide();
                    scene.sceneController.changeState(READY_TO_PLAY);
                    return;
                }

                Stream.of(scene.ghosts)
                    .filter(ghost -> ghost.inState(FRIGHTENED) && ghost.sameTile(scene.pacMan))
                    .findFirst()
                    .ifPresent(victim -> {
                        scene.victims.add(victim);
                        scene.ghostKilledTime = sceneTimer.tickCount();
                        scene.pacMan.hide();
                        scene.pacMan.setSpeed(0);
                        Stream.of(scene.ghosts).forEach(ghost -> {
                            ghost.setSpeed(0);
                            ghost.stopAnimation();
                        });
                        victim.setState(EATEN);
                        victim.selectAnimation(ANIM_GHOST_NUMBER, scene.victims.size() - 1);
                    });

                // After 50 ticks, Pac-Man and the surviving ghosts get visible again and move on
                if (sceneTimer.tickCount() == scene.ghostKilledTime + 50) {
                    scene.pacMan.show();
                    scene.pacMan.setSpeed(CHASE_SPEED);
                    Stream.of(scene.ghosts).forEach(ghost -> {
                        if (ghost.inState(EATEN)) {
                            ghost.hide();
                        } else {
                            ghost.show();
                            ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
                            ghost.startAnimation();
                        }
                    });
                }

                scene.pacMan.move();
                Stream.of(scene.ghosts).forEach(Ghost::move);
                scene.blinking.tick();
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (sceneTimer.atSecond(0.75)) {
                    scene.ghosts[3].hide();
                    if (!game().canStartNewGame()) {
                        THE_GAME_CONTROLLER.changeState(GameState.STARTING_GAME);
                    }
                } else if (sceneTimer.atSecond(5)) {
                    THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
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