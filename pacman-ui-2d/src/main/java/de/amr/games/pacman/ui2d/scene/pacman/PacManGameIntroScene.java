/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.EATEN;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.ui2d.GameAssets2D.*;

/**
 * <p>
 * The ghosts are presented one by one, Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 *
 * @author Armin Reichert
 */
public class PacManGameIntroScene extends GameScene2D {

    private static final List<GameAction> ACTIONS = List.of(
        GameAction2D.ADD_CREDIT,
        GameAction2D.START_GAME,
        GameAction2D.TEST_LEVELS_BONI,
        GameAction2D.TEST_LEVELS_TEASERS,
        GameAction2D.TEST_CUT_SCENES);

    static final String[] GHOST_CHARACTERS = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
    static final Color[] GHOST_COLORS = {ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE};
    static final Color PELLET_COLOR = ARCADE_ROSE;
    static final float CHASE_SPEED = 1.1f;
    static final float GHOST_FRIGHTENED_SPEED = 0.6f;
    static final int LEFT_TILE_X = 4;

    private final FiniteStateMachine<SceneState, PacManGameIntroScene> sceneController;

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

    public PacManGameIntroScene() {
        sceneController = new FiniteStateMachine<>(SceneState.values()) {
            @Override
            public PacManGameIntroScene context() {
                return PacManGameIntroScene.this;
            }
        };
    }

    @Override
    public void init() {
        GameSpriteSheet spriteSheet = context.currentGameSceneConfiguration().spriteSheet();
        blinking = new Pulse(10, true);
        pacMan = new Pac();
        pacMan.setAnimations(new PacManGamePacAnimations(spriteSheet));
        ghosts = new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.clyde() };
        for (Ghost ghost : ghosts) {
            ghost.setAnimations(new PacManGameGhostAnimations(spriteSheet, ghost.id()));
        }
        ghostImageVisible     = new boolean[4];
        ghostNicknameVisible  = new boolean[4];
        ghostCharacterVisible = new boolean[4];
        victims = new ArrayList<>(4);
        titleVisible = false;
        ghostIndex = 0;
        ghostKilledTime = 0;
        context.setScoreVisible(true);
        sceneController.restart(SceneState.STARTING);
    }

    @Override
    public void end() {
        context.sounds().stopVoice();
    }

    @Override
    public void update() {
        sceneController.update();
    }

    @Override
    public void handleInput() {
        context.execFirstCalledAction(ACTIONS);
    }

    @Override
    public void drawSceneContent(GameWorldRenderer renderer) {
        TickTimer timer = sceneController.state().timer();
        drawGallery(renderer);
        switch (sceneController.state()) {
            case SHOWING_POINTS -> drawPoints(renderer);
            case CHASING_PAC -> {
                drawPoints(renderer);
                if (blinking.isOn()) {
                    drawEnergizer(renderer, t(LEFT_TILE_X), t(20));
                }
                drawGuys(renderer, flutter(timer.currentTick()));
                if (context.gameVariant() == GameVariant.PACMAN) {
                    renderer.drawText(PacManGameSpriteSheet.MIDWAY_COPYRIGHT, ARCADE_PINK, renderer.scaledArcadeFont(TS),  t(4), t(32));
                }
            }
            case CHASING_GHOSTS, READY_TO_PLAY -> {
                drawPoints(renderer);
                drawGuys(renderer, 0);
                if (context.gameVariant() == GameVariant.PACMAN) {
                    renderer.drawText(PacManGameSpriteSheet.MIDWAY_COPYRIGHT, ARCADE_PINK, renderer.scaledArcadeFont(TS),  t(4), t(32));
                }
            }
            default -> {
            }
        }
        drawCredit(renderer, context.worldSizeTilesOrDefault());
        drawLevelCounter(renderer, context.worldSizeTilesOrDefault());
    }

    // TODO inspect in MAME what's really going on here
    private int flutter(long time) {
        return time % 5 < 2 ? 0 : -1;
    }

    private void drawGallery(GameWorldRenderer renderer) {
        GameSpriteSheet spriteSheet = context.currentGameSceneConfiguration().spriteSheet();
        Font font = renderer.scaledArcadeFont(TS);
        int tx = LEFT_TILE_X;
        if (titleVisible) {
            renderer.drawText("CHARACTER / NICKNAME", ARCADE_PALE, font, t(tx + 3), t(6));
        }
        for (byte id = 0; id < 4; ++id) {
            if (!ghostImageVisible[id]) {
                continue;
            }
            int ty = 7 + 3 * id;
            renderer.drawSpriteCenteredOverBox(spriteSheet.ghostFacingRight(id), t(tx) + 4, t(ty));
            if (ghostCharacterVisible[id]) {
                String text = "-" + GHOST_CHARACTERS[id];
                renderer.drawText(text, GHOST_COLORS[id], font, t(tx + 3), t(ty + 1));
            }
            if (ghostNicknameVisible[id]) {
                String text = '"' + ghosts[id].name().toUpperCase() + '"';
                renderer.drawText(text, GHOST_COLORS[id], font, t(tx + 14), t(ty + 1));
            }
        }
    }

    private void drawGuys(GameWorldRenderer renderer, int shakingAmount) {
        if (shakingAmount == 0) {
            Stream.of(ghosts).forEach(renderer::drawAnimatedEntity);
        } else {
            renderer.drawAnimatedEntity(ghosts[0]);
            renderer.drawAnimatedEntity(ghosts[3]);
            // shaking ghosts effect, not quite as in original game
            renderer.ctx().save();
            renderer.ctx().translate(shakingAmount, 0);
            renderer.drawAnimatedEntity(ghosts[1]);
            renderer.drawAnimatedEntity(ghosts[2]);
            renderer.ctx().restore();
        }
        renderer.drawAnimatedEntity(pacMan);
    }

    private void drawPoints(GameWorldRenderer renderer) {
        var color = ARCADE_PALE;
        var font8 = renderer.scaledArcadeFont(8);
        var font6 = renderer.scaledArcadeFont(6);
        int tx = LEFT_TILE_X + 6;
        int ty = 25;
        renderer.ctx().setFill(PELLET_COLOR);
        renderer.ctx().fillRect(scaled(t(tx) + 4), scaled(t(ty - 1) + 4), scaled(2), scaled(2));
        if (blinking.isOn()) {
            drawEnergizer(renderer, t(tx), t(ty + 1));
        }
        renderer.drawText("10",  color, font8, t(tx + 2), t(ty));
        renderer.drawText("PTS", color, font6, t(tx + 5), t(ty));
        renderer.drawText("50",  color, font8, t(tx + 2), t(ty + 2));
        renderer.drawText("PTS", color, font6, t(tx + 5), t(ty + 2));
    }

    // draw pixelated "circle"
    private void drawEnergizer(GameWorldRenderer renderer, double x, double y) {
        double scaling = scalingPy.get();
        renderer.ctx().save();
        renderer.ctx().scale(scaling, scaling);
        renderer.ctx().setFill(PELLET_COLOR);
        renderer.ctx().fillRect(x + 2, y, 4, 8);
        renderer.ctx().fillRect(x, y + 2, 8, 4);
        renderer.ctx().fillRect(x + 1, y + 1, 6, 6);
        renderer.ctx().restore();
    }

    private enum SceneState implements FsmState<PacManGameIntroScene> {

        STARTING {
            @Override
            public void onUpdate(PacManGameIntroScene intro) {
                if (timer.currentTick() == 3) {
                    intro.titleVisible = true;
                } else if (timer.atSecond(1)) {
                    intro.sceneController.changeState(PRESENTING_GHOSTS);
                }
            }
        },

        PRESENTING_GHOSTS {
            @Override
            public void onUpdate(PacManGameIntroScene intro) {
                if (timer.currentTick() == 1) {
                    intro.ghostImageVisible[intro.ghostIndex] = true;
                } else if (timer.atSecond(1.0)) {
                    intro.ghostCharacterVisible[intro.ghostIndex] = true;
                } else if (timer.atSecond(1.5)) {
                    intro.ghostNicknameVisible[intro.ghostIndex] = true;
                } else if (timer.atSecond(2.0)) {
                    if (intro.ghostIndex < intro.ghosts.length - 1) {
                        timer.resetIndefinitely();
                    }
                    intro.ghostIndex += 1;
                } else if (timer.atSecond(2.5)) {
                    intro.sceneController.changeState(SHOWING_POINTS);
                }
            }
        },

        SHOWING_POINTS {
            @Override
            public void onEnter(PacManGameIntroScene intro) {
                intro.blinking.stop();
            }

            @Override
            public void onUpdate(PacManGameIntroScene intro) {
                if (timer.atSecond(1)) {
                    intro.sceneController.changeState(CHASING_PAC);
                }
            }
        },

        CHASING_PAC {
            @Override
            public void onEnter(PacManGameIntroScene intro) {
                timer.restartIndefinitely();
                intro.pacMan.setPosition(TS * 36, TS * 20);
                intro.pacMan.setMoveDir(Direction.LEFT);
                intro.pacMan.setSpeed(CHASE_SPEED);
                intro.pacMan.show();
                intro.pacMan.selectAnimation(Pac.ANIM_MUNCHING);
                intro.pacMan.animations().ifPresent(Animations::startSelected);
                Stream.of(intro.ghosts).forEach(ghost -> {
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setPosition(intro.pacMan.position().plus(16 * (ghost.id() + 1), 0));
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setSpeed(CHASE_SPEED);
                    ghost.show();
                    ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
                    ghost.startAnimation();
                });
            }

            @Override
            public void onUpdate(PacManGameIntroScene intro) {
                if (timer.atSecond(1)) {
                    intro.blinking.start();
                }
                // Pac-Man reaches the energizer at the left and turns
                if (intro.pacMan.posX() <= TS * LEFT_TILE_X) {
                    intro.sceneController.changeState(CHASING_GHOSTS);
                }
                // Ghosts already reverse direction before Pac-Man eats the energizer and turns!
                else if (intro.pacMan.posX() <= TS * LEFT_TILE_X + HTS) {
                    Stream.of(intro.ghosts).forEach(ghost -> {
                        ghost.setState(FRIGHTENED);
                        ghost.selectAnimation(Ghost.ANIM_GHOST_FRIGHTENED);
                        ghost.setMoveAndWishDir(Direction.RIGHT);
                        ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
                        ghost.move();
                    });
                    intro.pacMan.move();
                } else { // keep moving
                    intro.blinking.tick();
                    intro.pacMan.move();
                    Stream.of(intro.ghosts).forEach(Ghost::move);
                }
            }
        },

        CHASING_GHOSTS {
            @Override
            public void onEnter(PacManGameIntroScene intro) {
                timer.restartIndefinitely();
                intro.ghostKilledTime = timer.currentTick();
                intro.pacMan.setMoveDir(Direction.RIGHT);
                intro.pacMan.setSpeed(CHASE_SPEED);
                intro.victims.clear();
            }

            @Override
            public void onUpdate(PacManGameIntroScene intro) {
                if (Stream.of(intro.ghosts).allMatch(ghost -> ghost.inState(EATEN))) {
                    intro.pacMan.hide();
                    intro.sceneController.changeState(READY_TO_PLAY);
                    return;
                }

                Stream.of(intro.ghosts)
                    .filter(ghost -> ghost.inState(FRIGHTENED) && ghost.sameTile(intro.pacMan))
                    .findFirst()
                    .ifPresent(victim -> {
                        intro.victims.add(victim);
                        intro.ghostKilledTime = timer.currentTick();
                        intro.pacMan.hide();
                        intro.pacMan.setSpeed(0);
                        Stream.of(intro.ghosts).forEach(ghost -> {
                            ghost.setSpeed(0);
                            ghost.stopAnimation();
                        });
                        victim.setState(EATEN);
                        victim.selectAnimation(Ghost.ANIM_GHOST_NUMBER, intro.victims.size() - 1);
                    });

                // After 50 ticks, Pac-Man and the surviving ghosts get visible again and move on
                if (timer.currentTick() == intro.ghostKilledTime + 50) {
                    intro.pacMan.show();
                    intro.pacMan.setSpeed(CHASE_SPEED);
                    Stream.of(intro.ghosts).forEach(ghost -> {
                        if (ghost.inState(EATEN)) {
                            ghost.hide();
                        } else {
                            ghost.show();
                            ghost.setSpeed(GHOST_FRIGHTENED_SPEED);
                            ghost.startAnimation();
                        }
                    });
                }

                intro.pacMan.move();
                Stream.of(intro.ghosts).forEach(Ghost::move);
                intro.blinking.tick();
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(PacManGameIntroScene intro) {
                if (timer.atSecond(0.75)) {
                    intro.ghosts[3].hide();
                    if (!intro.context.game().hasCredit()) {
                        intro.context.gameController().changeState(GameState.READY);
                    }
                } else if (timer.atSecond(5)) {
                    intro.context.gameController().changeState(GameState.STARTING);
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