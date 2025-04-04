/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

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
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.GameAction;
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
import static de.amr.games.pacman.ui.Globals.THE_UI;

/**
 * <p>
 * The ghosts are presented one by one, Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 *
 * @author Armin Reichert
 */
public class IntroScene extends GameScene2D {

    static final String[] GHOST_CHARACTERS = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
    static final Color[] GHOST_COLORS = {
        Color.web(Arcade.Palette.RED),
        Color.web(Arcade.Palette.PINK),
        Color.web(Arcade.Palette.CYAN),
        Color.web(Arcade.Palette.ORANGE)
    };
    static final Color PELLET_COLOR = Color.web(Arcade.Palette.ROSE);
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
        bind(GameAction.INSERT_COIN, THE_UI.keyboard().currentArcadeKeyBinding().key(Arcade.Button.COIN));
        bind(GameAction.START_GAME, THE_UI.keyboard().currentArcadeKeyBinding().key(Arcade.Button.START));
        bindTestsStartingActions();
    }

    @Override
    public void doInit() {
        var spriteSheet = (ArcadePacMan_SpriteSheet) THE_UI.configurations().current().spriteSheet();
        blinking = new Pulse(10, true);
        pacMan = new Pac();
        pacMan.setAnimations(new PacAnimations(spriteSheet));
        ghosts = new Ghost[] {
            ArcadePacMan_GameModel.blinky(),
            ArcadePacMan_GameModel.pinky(),
            ArcadePacMan_GameModel.inky(),
            ArcadePacMan_GameModel.clyde()
        };
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
        game().setScoreVisible(true);
        sceneController.restart(SceneState.STARTING);
    }

    @Override
    protected void doEnd() {
        THE_UI.sound().stopVoice();
    }

    @Override
    public void update() {
        sceneController.update();
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_UI.sound().playInsertCoinSound();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        Font font = THE_UI.assets().scaledArcadeFont(scaled(TS));
        gr.setScaling(scaling());
        gr.fillCanvas(backgroundColor());
        if (game().isScoreVisible()) {
            gr.drawScores(game().scoreManager(), Color.web(Arcade.Palette.WHITE), font);
        }
        TickTimer timer = sceneController.state().timer();
        drawGallery(font);
        switch (sceneController.state()) {
            case SHOWING_POINTS -> drawPoints();
            case CHASING_PAC -> {
                drawPoints();
                if (blinking.isOn()) {
                    drawEnergizer(tiles2Px(LEFT_TILE_X), tiles2Px(20));
                }
                drawGuys(flutter(timer.tickCount()));
                if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.PACMAN)) {
                    gr.drawText(ArcadePacMan_SpriteSheet.MIDWAY_COPYRIGHT, Color.web(Arcade.Palette.PINK), font, tiles2Px(4), tiles2Px(32));
                }
            }
            case CHASING_GHOSTS, READY_TO_PLAY -> {
                drawPoints();
                drawGuys(0);
                if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.PACMAN)) {
                    gr.drawText(ArcadePacMan_SpriteSheet.MIDWAY_COPYRIGHT, Color.web(Arcade.Palette.PINK), font, tiles2Px(4), tiles2Px(32));
                }
            }
            default -> {}
        }
        gr.drawText("CREDIT %2d".formatted(THE_COIN_STORE.numCoins()), Color.web(Arcade.Palette.WHITE), font, 2 * TS, sizeInPx().y() - 2);
        gr.drawLevelCounter(game().levelCounter(), sizeInPx().x() - 4 * TS, sizeInPx().y() - 2 * TS);
    }

    // TODO inspect in MAME what's really going on here
    private int flutter(long time) {
        return time % 5 < 2 ? 0 : -1;
    }

    private void drawGallery(Font font) {
        var spriteSheet = (ArcadePacMan_SpriteSheet) THE_UI.configurations().current().spriteSheet();
        if (titleVisible) {
            gr.drawText("CHARACTER / NICKNAME", Color.web(Arcade.Palette.WHITE), font, tiles2Px(LEFT_TILE_X + 3), tiles2Px(6));
        }
        for (byte id = 0; id < 4; ++id) {
            int tileY = 7 + 3 * id;
            if (ghostImageVisible[id]) {
                gr.drawSpriteCenteredOverTile(spriteSheet.ghostFacingRight(id), tiles2Px(LEFT_TILE_X) + 4, tiles2Px(tileY));
            }
            if (ghostCharacterVisible[id]) {
                String text = "-" + GHOST_CHARACTERS[id];
                gr.drawText(text, GHOST_COLORS[id], font, tiles2Px(LEFT_TILE_X + 3), tiles2Px(tileY + 1));
            }
            if (ghostNicknameVisible[id]) {
                String text = '"' + ghosts[id].name().toUpperCase() + '"';
                gr.drawText(text, GHOST_COLORS[id], font, tiles2Px(LEFT_TILE_X + 14), tiles2Px(tileY + 1));
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
        var color = Color.web(Arcade.Palette.WHITE);
        Font font8 = THE_UI.assets().scaledArcadeFont(scaled(8));
        Font font6 = THE_UI.assets().scaledArcadeFont(scaled(6));
        int tileX = LEFT_TILE_X + 6;
        int tileY = 25;
        gr.ctx().setFill(PELLET_COLOR);
        gr.ctx().fillRect(scaled(tiles2Px(tileX) + 4), scaled(tiles2Px(tileY - 1) + 4), scaled(2), scaled(2));
        if (blinking.isOn()) {
            drawEnergizer(tiles2Px(tileX), tiles2Px(tileY + 1));
        }
        gr.drawText("10",  color, font8, tiles2Px(tileX + 2), tiles2Px(tileY));
        gr.drawText("PTS", color, font6, tiles2Px(tileX + 5), tiles2Px(tileY));
        gr.drawText("50",  color, font8, tiles2Px(tileX + 2), tiles2Px(tileY + 2));
        gr.drawText("PTS", color, font6, tiles2Px(tileX + 5), tiles2Px(tileY + 2));
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
                intro.pacMan.startAnimation();
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
                    if (!THE_GAME_CONTROLLER.game().canStartNewGame()) {
                        THE_GAME_CONTROLLER.changeState(GameState.STARTING_GAME);
                    }
                } else if (timer.atSecond(5)) {
                    THE_GAME_CONTROLLER.changeState(GameState.SETTING_OPTIONS);
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