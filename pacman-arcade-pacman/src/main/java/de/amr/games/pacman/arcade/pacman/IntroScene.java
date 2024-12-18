/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.action.GameActions2D;
import de.amr.games.pacman.ui.scene.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.model.actors.Animations.*;
import static de.amr.games.pacman.model.actors.GhostState.EATEN;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;

/**
 * <p>
 * The ghosts are presented one by one, Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 *
 * @author Armin Reichert
 */
public class IntroScene extends GameScene2D {

    static final String[] GHOST_CHARACTERS = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
    static final Color[] GHOST_COLORS = {
        Color.valueOf(Arcade.Palette.RED),
        Color.valueOf(Arcade.Palette.PINK),
        Color.valueOf(Arcade.Palette.CYAN),
        Color.valueOf(Arcade.Palette.ORANGE)
    };
    static final Color PELLET_COLOR = Color.valueOf(Arcade.Palette.ROSE);
    static final float CHASE_SPEED = 1.1f;
    static final float GHOST_FRIGHTENED_SPEED = 0.6f;
    static final int LEFT_TILE_X = 4;

    private final FiniteStateMachine<SceneState, IntroScene> sceneController;

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

    public IntroScene() {
        sceneController = new FiniteStateMachine<>(SceneState.values()) {
            @Override
            public IntroScene context() {
                return IntroScene.this;
            }
        };
    }

    @Override
    public void bindGameActions() {
        bind(GameActions2D.INSERT_COIN, context.arcadeKeys().key(Arcade.Button.COIN));
        bind(GameActions2D.START_GAME, context.arcadeKeys().key(Arcade.Button.START));
        GameActions2D.bindTestActions(this);
    }

    @Override
    public void doInit() {
        PacManGameSpriteSheet spriteSheet = (PacManGameSpriteSheet) context.currentGameConfig().spriteSheet();
        blinking = new Pulse(10, true);
        pacMan = new Pac();
        pacMan.setAnimations(new PacAnimations(spriteSheet));
        ghosts = new Ghost[] { Ghost.blinky(), Ghost.pinky(), Ghost.inky(), Ghost.clyde() };
        for (Ghost ghost : ghosts) {
            ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id()));
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
    protected void doEnd() {
        context.sound().stopVoice();
    }

    @Override
    public void update() {
        sceneController.update();
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        context.sound().playInsertCoinSound();
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        TickTimer timer = sceneController.state().timer();
        drawGallery();
        switch (sceneController.state()) {
            case SHOWING_POINTS -> drawPoints();
            case CHASING_PAC -> {
                drawPoints();
                if (blinking.isOn()) {
                    drawEnergizer(t(LEFT_TILE_X), t(20));
                }
                drawGuys(flutter(timer.tickCount()));
                if (context.gameVariant() == GameVariant.PACMAN) {
                    gr.drawText(PacManGameSpriteSheet.MIDWAY_COPYRIGHT, Color.valueOf(Arcade.Palette.PINK), gr.scaledArcadeFont(TS),  t(4), t(32));
                }
            }
            case CHASING_GHOSTS, READY_TO_PLAY -> {
                drawPoints();
                drawGuys(0);
                if (context.gameVariant() == GameVariant.PACMAN) {
                    gr.drawText(PacManGameSpriteSheet.MIDWAY_COPYRIGHT, Color.valueOf(Arcade.Palette.PINK), gr.scaledArcadeFont(TS),  t(4), t(32));
                }
            }
            default -> {}
        }
        gr.drawText("CREDIT %2d".formatted(context.gameController().coinControl().credit()), Color.valueOf(Arcade.Palette.WHITE), gr.scaledArcadeFont(TS), 2 * TS, size().y() - 2);
        gr.drawLevelCounter(context, size().x() - 4 * TS, size().y() - 2 * TS);
    }

    // TODO inspect in MAME what's really going on here
    private int flutter(long time) {
        return time % 5 < 2 ? 0 : -1;
    }

    private void drawGallery() {
        PacManGameSpriteSheet spriteSheet = (PacManGameSpriteSheet) context.currentGameConfig().spriteSheet();
        Font font = gr.scaledArcadeFont(TS);
        int tx = LEFT_TILE_X;
        if (titleVisible) {
            gr.drawText("CHARACTER / NICKNAME", Color.valueOf(Arcade.Palette.WHITE), font, t(tx + 3), t(6));
        }
        for (byte id = 0; id < 4; ++id) {
            if (!ghostImageVisible[id]) {
                continue;
            }
            int ty = 7 + 3 * id;
            gr.drawSpriteCenteredOverTile(spriteSheet.ghostFacingRight(id), t(tx) + 4, t(ty));
            if (ghostCharacterVisible[id]) {
                String text = "-" + GHOST_CHARACTERS[id];
                gr.drawText(text, GHOST_COLORS[id], font, t(tx + 3), t(ty + 1));
            }
            if (ghostNicknameVisible[id]) {
                String text = '"' + ghosts[id].name().toUpperCase() + '"';
                gr.drawText(text, GHOST_COLORS[id], font, t(tx + 14), t(ty + 1));
            }
        }
    }

    private void drawGuys(int shakingAmount) {
        if (shakingAmount == 0) {
            Stream.of(ghosts).forEach(gr::drawAnimatedEntity);
        } else {
            gr.drawAnimatedEntity(ghosts[0]);
            gr.drawAnimatedEntity(ghosts[3]);
            // shaking ghosts effect, not quite as in original game
            gr.ctx().save();
            gr.ctx().translate(shakingAmount, 0);
            gr.drawAnimatedEntity(ghosts[1]);
            gr.drawAnimatedEntity(ghosts[2]);
            gr.ctx().restore();
        }
        gr.drawAnimatedEntity(pacMan);
    }

    private void drawPoints() {
        var color = Color.valueOf(Arcade.Palette.WHITE);
        var font8 = gr.scaledArcadeFont(8);
        var font6 = gr.scaledArcadeFont(6);
        int tx = LEFT_TILE_X + 6;
        int ty = 25;
        gr.ctx().setFill(PELLET_COLOR);
        gr.ctx().fillRect(scaled(t(tx) + 4), scaled(t(ty - 1) + 4), scaled(2), scaled(2));
        if (blinking.isOn()) {
            drawEnergizer(t(tx), t(ty + 1));
        }
        gr.drawText("10",  color, font8, t(tx + 2), t(ty));
        gr.drawText("PTS", color, font6, t(tx + 5), t(ty));
        gr.drawText("50",  color, font8, t(tx + 2), t(ty + 2));
        gr.drawText("PTS", color, font6, t(tx + 5), t(ty + 2));
    }

    // draw pixelated "circle"
    private void drawEnergizer(double x, double y) {
        gr.ctx().save();
        gr.ctx().scale(scaling(), scaling());
        gr.ctx().setFill(PELLET_COLOR);
        gr.ctx().fillRect(x + 2, y, 4, 8);
        gr.ctx().fillRect(x, y + 2, 8, 4);
        gr.ctx().fillRect(x + 1, y + 1, 6, 6);
        gr.ctx().restore();
    }

    private enum SceneState implements FsmState<IntroScene> {

        STARTING {
            @Override
            public void onUpdate(IntroScene intro) {
                if (timer.tickCount() == 3) {
                    intro.titleVisible = true;
                } else if (timer.atSecond(1)) {
                    intro.sceneController.changeState(PRESENTING_GHOSTS);
                }
            }
        },

        PRESENTING_GHOSTS {
            @Override
            public void onUpdate(IntroScene intro) {
                if (timer.tickCount() == 1) {
                    intro.ghostImageVisible[intro.ghostIndex] = true;
                } else if (timer.atSecond(1.0)) {
                    intro.ghostCharacterVisible[intro.ghostIndex] = true;
                } else if (timer.atSecond(1.5)) {
                    intro.ghostNicknameVisible[intro.ghostIndex] = true;
                } else if (timer.atSecond(2.0)) {
                    if (intro.ghostIndex < intro.ghosts.length - 1) {
                        timer.resetIndefiniteTime();
                    }
                    intro.ghostIndex += 1;
                } else if (timer.atSecond(2.5)) {
                    intro.sceneController.changeState(SHOWING_POINTS);
                }
            }
        },

        SHOWING_POINTS {
            @Override
            public void onEnter(IntroScene intro) {
                intro.blinking.stop();
            }

            @Override
            public void onUpdate(IntroScene intro) {
                if (timer.atSecond(1)) {
                    intro.sceneController.changeState(CHASING_PAC);
                }
            }
        },

        CHASING_PAC {
            @Override
            public void onEnter(IntroScene intro) {
                timer.restartIndefinitely();
                intro.pacMan.setPosition(TS * 36, TS * 20);
                intro.pacMan.setMoveDir(Direction.LEFT);
                intro.pacMan.setSpeed(CHASE_SPEED);
                intro.pacMan.show();
                intro.pacMan.selectAnimation(ANIM_PAC_MUNCHING);
                intro.pacMan.optAnimations().ifPresent(Animations::startCurrentAnimation);
                Stream.of(intro.ghosts).forEach(ghost -> {
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setPosition(intro.pacMan.position().plus(16 * (ghost.id() + 1), 0));
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setSpeed(CHASE_SPEED);
                    ghost.show();
                    ghost.selectAnimation(ANIM_GHOST_NORMAL);
                    ghost.startAnimation();
                });
            }

            @Override
            public void onUpdate(IntroScene intro) {
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
                        ghost.selectAnimation(ANIM_GHOST_FRIGHTENED);
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
            public void onEnter(IntroScene intro) {
                timer.restartIndefinitely();
                intro.ghostKilledTime = timer.tickCount();
                intro.pacMan.setMoveDir(Direction.RIGHT);
                intro.pacMan.setSpeed(CHASE_SPEED);
                intro.victims.clear();
            }

            @Override
            public void onUpdate(IntroScene intro) {
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
                        intro.ghostKilledTime = timer.tickCount();
                        intro.pacMan.hide();
                        intro.pacMan.setSpeed(0);
                        Stream.of(intro.ghosts).forEach(ghost -> {
                            ghost.setSpeed(0);
                            ghost.stopAnimation();
                        });
                        victim.setState(EATEN);
                        victim.selectAnimation(ANIM_GHOST_NUMBER, intro.victims.size() - 1);
                    });

                // After 50 ticks, Pac-Man and the surviving ghosts get visible again and move on
                if (timer.tickCount() == intro.ghostKilledTime + 50) {
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
            public void onUpdate(IntroScene intro) {
                if (timer.atSecond(0.75)) {
                    intro.ghosts[3].hide();
                    if (!intro.context.game().canStartNewGame()) {
                        intro.context.gameController().changeState(GameState.STARTING_GAME);
                    }
                } else if (timer.atSecond(5)) {
                    intro.context.gameController().changeState(GameState.SETTING_OPTIONS);
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