/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.Pulse;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGameSpriteSheet;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.EATEN;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;

/**
 * Intro scene of the Pac-Man game.
 * <p>
 * The ghosts are presented one by one, Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 *
 * @author Armin Reichert
 */
public class PacManIntroScene extends GameScene2D {

    /**
     * Intro is controlled by a FSM, here come the states.
     */
    private enum SceneState implements FsmState<PacManIntroScene> {

        STARTING {
            @Override
            public void onUpdate(PacManIntroScene intro) {
                if (timer.currentTick() == 3) {
                    intro.data.titleVisible = true;
                } else if (timer.atSecond(1)) {
                    intro.sceneController.changeState(PRESENTING_GHOSTS);
                }
            }
        },

        PRESENTING_GHOSTS {
            @Override
            public void onUpdate(PacManIntroScene intro) {
                if (timer.currentTick() == 1) {
                    intro.data.ghostImageVisible[intro.data.ghostIndex] = true;
                } else if (timer.atSecond(1.0)) {
                    intro.data.ghostCharacterVisible[intro.data.ghostIndex] = true;
                } else if (timer.atSecond(1.5)) {
                    intro.data.ghostNicknameVisible[intro.data.ghostIndex] = true;
                } else if (timer.atSecond(2.0)) {
                    if (intro.data.ghostIndex < intro.data.ghosts.size() - 1) {
                        timer.resetIndefinitely();
                    }
                    intro.data.ghostIndex += 1;
                } else if (timer.atSecond(2.5)) {
                    intro.sceneController.changeState(SHOWING_POINTS);
                }
            }
        },

        SHOWING_POINTS {
            @Override
            public void onEnter(PacManIntroScene intro) {
                intro.data.blinking.stop();
            }

            @Override
            public void onUpdate(PacManIntroScene intro) {
                if (timer.atSecond(1)) {
                    intro.sceneController.changeState(CHASING_PAC);
                }
            }
        },

        CHASING_PAC {
            @Override
            public void onEnter(PacManIntroScene intro) {
                timer.restartIndefinitely();
                intro.data.pacMan.setPosition(TS * 36, TS * 20);
                intro.data.pacMan.setMoveDir(Direction.LEFT);
                intro.data.pacMan.setSpeed(intro.data.chaseSpeed);
                intro.data.pacMan.show();
                intro.data.pacMan.selectAnimation(Pac.ANIM_MUNCHING);
                intro.data.pacMan.animations().ifPresent(Animations::startSelected);
                intro.data.ghosts.forEach(ghost -> {
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setPosition(intro.data.pacMan.position().plus(16 * (ghost.id() + 1), 0));
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setSpeed(intro.data.chaseSpeed);
                    ghost.show();
                    ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
                    ghost.startAnimation();
                });
            }

            @Override
            public void onUpdate(PacManIntroScene intro) {
                if (timer.atSecond(1)) {
                    intro.data.blinking.start();
                }
                // Pac-Man reaches the energizer at the left and turns
                if (intro.data.pacMan.posX() <= TS * intro.data.leftTileX) {
                    intro.sceneController.changeState(CHASING_GHOSTS);
                }
                // Ghosts already reverse direction before Pac-Man eats the energizer and turns!
                else if (intro.data.pacMan.posX() <= TS * intro.data.leftTileX + HTS) {
                    intro.data.ghosts.forEach(ghost -> {
                        ghost.setState(FRIGHTENED);
                        ghost.selectAnimation(Ghost.ANIM_GHOST_FRIGHTENED);
                        ghost.setMoveAndWishDir(Direction.RIGHT);
                        ghost.setSpeed(intro.data.ghostFrightenedSpeed);
                        ghost.move();
                    });
                    intro.data.pacMan.move();
                } else { // keep moving
                    intro.data.blinking.tick();
                    intro.data.pacMan.move();
                    intro.data.ghosts.forEach(Ghost::move);
                }
            }
        },

        CHASING_GHOSTS {
            @Override
            public void onEnter(PacManIntroScene intro) {
                timer.restartIndefinitely();
                intro.data.ghostKilledTime = timer.currentTick();
                intro.data.pacMan.setMoveDir(Direction.RIGHT);
                intro.data.pacMan.setSpeed(intro.data.chaseSpeed);
                intro.data.victims.clear();
            }

            @Override
            public void onUpdate(PacManIntroScene intro) {
                if (intro.data.ghosts.stream().allMatch(ghost -> ghost.inState(EATEN))) {
                    intro.data.pacMan.hide();
                    intro.sceneController.changeState(READY_TO_PLAY);
                    return;
                }

                intro.data.ghosts.stream()
                    .filter(ghost -> ghost.inState(FRIGHTENED) && ghost.sameTile(intro.data.pacMan))
                    .findFirst()
                    .ifPresent(victim -> {
                        intro.data.victims.add(victim);
                        intro.data.ghostKilledTime = timer.currentTick();
                        intro.data.pacMan.hide();
                        intro.data.pacMan.setSpeed(0);
                        intro.data.ghosts.forEach(ghost -> {
                            ghost.setSpeed(0);
                            ghost.stopAnimation();
                        });
                        victim.setState(EATEN);
                        victim.selectAnimation(Ghost.ANIM_GHOST_NUMBER, intro.data.victims.size() - 1);
                    });

                // After 50 ticks, Pac-Man and the surviving ghosts get visible again and move on
                if (timer.currentTick() == intro.data.ghostKilledTime + 50) {
                    intro.data.pacMan.show();
                    intro.data.pacMan.setSpeed(intro.data.chaseSpeed);
                    intro.data.ghosts.forEach(ghost -> {
                        if (ghost.inState(EATEN)) {
                            ghost.hide();
                        } else {
                            ghost.show();
                            ghost.setSpeed(intro.data.ghostFrightenedSpeed);
                            ghost.startAnimation();
                        }
                    });
                }

                intro.data.pacMan.move();
                intro.data.ghosts.forEach(Ghost::move);
                intro.data.blinking.tick();
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(PacManIntroScene intro) {
                if (timer.atSecond(0.75)) {
                    intro.data.ghosts.get(3).hide();
                    if (!intro.context.game().hasCredit()) {
                        intro.context.gameController().changeState(GameState.READY);
                    }
                } else if (timer.atSecond(5)) {
                    intro.context.gameController().changeState(GameState.CREDIT);
                }
            }
        };

        final TickTimer timer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return timer;
        }
    }

    // PacManIntroScene

    private static class Data {
        final float chaseSpeed = 1.1f;
        final float ghostFrightenedSpeed = 0.6f;
        final int leftTileX = 4;
        final Pulse blinking = new Pulse(10, true);
        final Pac pacMan = new Pac();

        // Ghosts
        final List<Ghost> ghosts = List.of(Ghost.red(), Ghost.pink(), Ghost.cyan(), Ghost.orange());
        final String[]    ghostCharacters = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
        final Color[]     ghostColors = { Color.RED, Color.rgb(252, 181, 255), Color.CYAN, Color.rgb(251, 190, 88) };
        final boolean[]   ghostImageVisible = new boolean[4];
        final boolean[]   ghostNicknameVisible = new boolean[4];
        final boolean[]   ghostCharacterVisible = new boolean[4];

        final List<Ghost> victims = new ArrayList<>();
        boolean titleVisible = false;
        int ghostIndex;
        long ghostKilledTime;

        Data() {
            ghosts.get(GameModel.RED_GHOST).setName("Blinky");
            ghosts.get(GameModel.PINK_GHOST).setName("Pinky");
            ghosts.get(GameModel.CYAN_GHOST).setName("Inky");
            ghosts.get(GameModel.ORANGE_GHOST).setName("Clyde");
        }
    }

    private Data data;
    private final FiniteStateMachine<SceneState, PacManIntroScene> sceneController;

    public PacManIntroScene() {
        sceneController = new FiniteStateMachine<>(SceneState.values()) {
            @Override
            public PacManIntroScene context() {
                return PacManIntroScene.this;
            }
        };
    }

    @Override
    public boolean isCreditVisible() {
        return true;
    }

    @Override
    public void init() {
        super.init();
        data = new Data();

        var sheet = (PacManGameSpriteSheet) context.spriteSheet(context.game().variant());
        renderer.spriteRenderer().setSpriteSheet(sheet);
        context.setScoreVisible(true);

        data.pacMan.setAnimations(new PacManGamePacAnimations(data.pacMan, sheet));
        data.ghosts.forEach(ghost -> ghost.setAnimations(new PacManGameGhostAnimations(ghost, sheet)));
        data.blinking.reset();

        sceneController.restart(SceneState.STARTING);
    }

    @Override
    public void end() {
        GameSounds.stopVoice();
    }

    @Override
    public void update() {
        sceneController.update();
    }

    @Override
    public void handleUserInput() {
        if (GameAction.ADD_CREDIT.requested()) {
            context.addCredit();
        } else if (GameAction.START_GAME.requested()) {
            context.startGame();
        } else if (GameAction.CUTSCENES.requested()) {
            context.startCutscenesTest();
        }
    }

    @Override
    public void drawSceneContent() {
        var timer = sceneController.state().timer();
        drawGallery();
        switch (sceneController.state()) {
            case SHOWING_POINTS -> drawPoints();
            case CHASING_PAC -> {
                drawPoints();
                if (data.blinking.isOn()) {
                    drawEnergizer(t(data.leftTileX), t(20));
                }
                drawGuys(flutter(timer.currentTick()));
                drawMidwayCopyright(t(4), t(32));
            }
            case CHASING_GHOSTS, READY_TO_PLAY -> {
                drawPoints();
                drawGuys(0);
                drawMidwayCopyright(t(4), t(32));
            }
            default -> {
            }
        }
        drawLevelCounter(g);
    }

    // TODO inspect in MAME what's really going on here
    private int flutter(long time) {
        return time % 5 < 2 ? 0 : -1;
    }

    private void drawGallery() {
        var font = sceneFont(8);

        int tx = data.leftTileX;
        if (data.titleVisible) {
            renderer.drawText(g, "CHARACTER / NICKNAME", context.assets().color("palette.pale"), font, t(tx + 3), t(6));
        }
        for (byte id = 0; id < 4; ++id) {
            if (!data.ghostImageVisible[id]) {
                continue;
            }
            int ty = 7 + 3 * id;
            renderer.spriteRenderer().drawSpriteCenteredOverBox(g, renderer.spriteRenderer().spriteSheet().ghostFacingRight(id), t(tx) + 4, t(ty));
            if (data.ghostCharacterVisible[id]) {
                var text = "-" + data.ghostCharacters[id];
                renderer.drawText(g, text, data.ghostColors[id], font, t(tx + 3), t(ty + 1));
            }
            if (data.ghostNicknameVisible[id]) {
                var text = '"' + data.ghosts.get(id).name().toUpperCase() + '"';
                renderer.drawText(g, text, data.ghostColors[id], font, t(tx + 14), t(ty + 1));
            }
        }
    }

    private void drawGuys(int shakingAmount) {
        if (shakingAmount == 0) {
            data.ghosts.forEach(ghost -> renderer.drawGhost(g, ghost));
        } else {
            renderer.drawGhost(g, data.ghosts.get(0));
            renderer.drawGhost(g, data.ghosts.get(3));
            // shaking ghosts effect, not quite as in original game
            g.save();
            g.translate(shakingAmount, 0);
            renderer.drawGhost(g, data.ghosts.get(1));
            renderer.drawGhost(g, data.ghosts.get(2));
            g.restore();
        }
        renderer.drawPac(g, data.pacMan);
    }

    private void drawPoints() {
        var color = context.assets().color("palette.pale");
        var font8 = sceneFont(8);
        var font6 = sceneFont(6);
        int tx = data.leftTileX + 6;
        int ty = 25;
        g.setFill(Color.rgb(254, 189, 180));
        g.fillRect(s(t(tx) + 4), s(t(ty - 1) + 4), s(2), s(2));
        if (data.blinking.isOn()) {
            drawEnergizer(TS * tx, TS * (ty+1));
        }
        renderer.drawText(g, "10",  color, font8, t(tx + 2), t(ty));
        renderer.drawText(g, "PTS", color, font6, t(tx + 5), t(ty));
        renderer.drawText(g, "50",  color, font8, t(tx + 2), t(ty + 2));
        renderer.drawText(g, "PTS", color, font6, t(tx + 5), t(ty + 2));
    }

    // draw pixelized "circle"
    private void drawEnergizer(double x, double y) {
        double scaling = scalingPy.get();
        g.save();
        g.scale(scaling, scaling);
        g.setFill(Color.rgb(254, 189, 180));
        g.fillRect(x + 2, y, 4, 8);
        g.fillRect(x, y + 2, 8, 4);
        g.fillRect(x + 1, y + 1, 6, 6);
        g.restore();
    }
}