/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.mspacman.MsPacManGameModel;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.BitSet;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.*;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 *
 * @author Armin Reichert
 */
public class MsPacManIntroScene extends GameScene2D {

    private enum SceneState implements FsmState<MsPacManIntroScene> {

        STARTING {
            @Override
            public void onEnter(MsPacManIntroScene intro) {
                intro.data.marqueeTimer.restartIndefinitely();
                intro.data.msPacMan.setPosition(TS * 31, TS * 20);
                intro.data.msPacMan.setMoveDir(Direction.LEFT);
                intro.data.msPacMan.setSpeed(intro.data.speed);
                intro.data.msPacMan.setVisible(true);
                intro.data.msPacMan.selectAnimation(Pac.ANIM_MUNCHING);
                intro.data.msPacMan.animations().ifPresent(Animations::startSelected);
                for (Ghost ghost : intro.data.ghosts) {
                    ghost.setPosition(TS * 33.5f, TS * 20);
                    ghost.setMoveAndWishDir(Direction.LEFT);
                    ghost.setSpeed(intro.data.speed);
                    ghost.setState(GhostState.HUNTING_PAC);
                    ghost.setVisible(true);
                    ghost.startAnimation();
                }
                intro.data.ghostIndex = 0;
            }

            @Override
            public void onUpdate(MsPacManIntroScene intro) {
                intro.data.marqueeTimer.tick();
                if (timer.atSecond(1)) {
                    intro.sceneController.changeState(GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {

            @Override
            public void onUpdate(MsPacManIntroScene intro) {
                intro.data.marqueeTimer.tick();
                boolean reachedEndPosition = letGhostMarchIn(intro.data);
                if (reachedEndPosition) {
                    if (intro.data.ghostIndex == 3) {
                        intro.sceneController.changeState(MS_PACMAN_MARCHING_IN);
                    } else {
                        ++intro.data.ghostIndex;
                    }
                }
            }

            boolean letGhostMarchIn(Data data) {
                Ghost ghost = data.ghosts[data.ghostIndex];
                if (ghost.moveDir() == Direction.LEFT) {
                    if (ghost.posX() <= data.stopXGhost) {
                        ghost.setPosX(data.stopXGhost);
                        ghost.setMoveAndWishDir(Direction.UP);
                        data.waitBeforeRising = 2;
                    } else {
                        ghost.move();
                    }
                }
                else if (ghost.moveDir() == Direction.UP) {
                    int endPositionY = data.topY + data.ghostIndex * 16;
                    if (data.waitBeforeRising > 0) {
                        data.waitBeforeRising--;
                    }
                    else if (ghost.posY() <= endPositionY) {
                        ghost.setSpeed(0);
                        ghost.stopAnimation();
                        ghost.resetAnimation();
                        return true;
                    }
                    else {
                        ghost.move();
                    }
                }
                return false;
            }
        },

        MS_PACMAN_MARCHING_IN {

            @Override
            public void onUpdate(MsPacManIntroScene intro) {
                intro.data.marqueeTimer.tick();
                intro.data.msPacMan.move();
                if (intro.data.msPacMan.posX() <= intro.data.stopXMsPacMan) {
                    intro.data.msPacMan.setSpeed(0);
                    intro.data.msPacMan.animations().ifPresent(Animations::resetSelected);
                    intro.sceneController.changeState(READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {

            @Override
            public void onUpdate(MsPacManIntroScene intro) {
                intro.data.marqueeTimer.tick();
                if (timer.atSecond(2.0) && !intro.context.game().hasCredit()) {
                    intro.context.gameController().changeState(GameState.READY); // demo level
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

    private static class Data {
        final float speed = 1.1f;
        final int topY = TS * 11 + 1;
        final int stopXGhost = TS * 6 - 4;
        final int stopXMsPacMan = TS * 15 + 2;
        final Vector2i titlePosition = v2i(TS * 10, TS * 8);
        final Pac msPacMan = new Pac();
        final Ghost[] ghosts = { Ghost.red(), Ghost.pink(), Ghost.cyan(), Ghost.orange() };
        // Marquee
        final TickTimer marqueeTimer = new TickTimer("marquee-timer");
        final int numBulbs = 96;
        final int distanceBetweenActiveBulbs = 16;
        // Mutable state
        int ghostIndex;
        int waitBeforeRising;

        Data() {
            ghosts[RED_GHOST].setName("Blinky");
            ghosts[PINK_GHOST].setName("Pinky");
            ghosts[CYAN_GHOST].setName("Inky");
            ghosts[ORANGE_GHOST].setName("Sue");
        }
    }

    private final FiniteStateMachine<SceneState, MsPacManIntroScene> sceneController;
    private Data data;

    public MsPacManIntroScene() {
        sceneController = new FiniteStateMachine<>(SceneState.values()) {
            @Override
            public MsPacManIntroScene context() {
                return MsPacManIntroScene.this;
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
        context.setScoreVisible(true);

        var sheet = (MsPacManGameSpriteSheet) context.spriteSheet(context.game().variant());
        renderer.spriteRenderer().setSpriteSheet(sheet);
        clearBlueMazeBug();

        data = new Data();
        data.msPacMan.setAnimations(new MsPacManGamePacAnimations(data.msPacMan, sheet));
        data.msPacMan.selectAnimation(Pac.ANIM_MUNCHING);
        for (Ghost ghost : data.ghosts) {
            ghost.setAnimations(new MsPacManGameGhostAnimations(ghost, sheet));
            ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
        }
        sceneController.restart(SceneState.STARTING);
    }

    /**
     * 6 of the 96 bulbs are switched on per frame, shifting counter-clockwise every tick.
     * The bulbs on the left border however are switched off every second frame. Bug in original game?
     *
     * @return bit set indicating which bulbs are switched on
     */
    private BitSet computeMarqueeState(long tick) {
        var state = new BitSet(data.numBulbs);
        for (int b = 0; b < 6; ++b) {
            state.set((b * data.distanceBetweenActiveBulbs + (int) tick) % data.numBulbs);
        }
        // Simulate bug on left border
        for (int i = 81; i < data.numBulbs; i += 2) {
            state.clear(i);
        }
        return state;
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
            if (sceneController.state() == SceneState.STARTING) {
                triggerBlueMazeBug();
            }
            context.addCredit();
        } else if (GameAction.START_GAME.requested()) {
            context.startGame();
        } else if (GameAction.CUTSCENES.requested()) {
            context.startCutscenesTest();
        }
    }

    @Override
    public void drawSceneContent() {
        MsPacManGameWorldRenderer msPacManGameWorldRenderer = (MsPacManGameWorldRenderer) renderer;
        AssetStorage assets = context.assets();
        Font font8 = sceneFont(8); // depends on current scaling!
        BitSet marqueeState = computeMarqueeState(data.marqueeTimer.currentTick());
        drawMarquee(marqueeState);
        renderer.drawText(g, "\"MS PAC-MAN\"", assets.color("palette.orange"), font8, data.titlePosition.x(), data.titlePosition.y());
        if (sceneController.state() == SceneState.GHOSTS_MARCHING_IN) {
            if (data.ghostIndex == GameModel.RED_GHOST) {
                renderer.drawText(g, "WITH", assets.color("palette.pale"), font8, data.titlePosition.x(), data.topY + t(3));
            }
            String ghostName = data.ghosts[data.ghostIndex].name().toUpperCase();
            Color color = switch (data.ghostIndex) {
                case GameModel.RED_GHOST -> assets.color("palette.red");
                case GameModel.PINK_GHOST -> assets.color("palette.pink");
                case GameModel.CYAN_GHOST -> assets.color("palette.cyan");
                case GameModel.ORANGE_GHOST -> assets.color("palette.orange");
                default -> throw new IllegalStateException("Illegal ghost index: " + data.ghostIndex);
            };
            double dx = ghostName.length() < 4 ? t(1) : 0;
            renderer.drawText(g, ghostName, color, font8, data.titlePosition.x() + t(3) + dx, data.topY + t(6));
        } else if (sceneController.state() == SceneState.MS_PACMAN_MARCHING_IN || sceneController.state() == SceneState.READY_TO_PLAY) {
            renderer.drawText(g, "STARRING", assets.color("palette.pale"), font8, data.titlePosition.x(), data.topY + t(3));
            renderer.drawText(g, "MS PAC-MAN", assets.color("palette.yellow"), font8, data.titlePosition.x(), data.topY + t(6));
        }
        for (Ghost ghost : data.ghosts) {
            renderer.drawGhost(g, ghost);
        }
        renderer.drawPac(g, data.msPacMan);
        msPacManGameWorldRenderer.drawMsPacManMidwayCopyright(g,
            context.assets().get("ms_pacman.logo.midway"),
            t(6), t(28), context.assets().color("palette.red"), sceneFont(TS));
    }

    // TODO This is too cryptic
    private void drawMarquee(BitSet marqueeState) {
        double xMin = 60, xMax = 192, yMin = 88, yMax = 148;
        for (int i = 0; i < data.numBulbs; ++i) {
            boolean on = marqueeState.get(i);
            if (i <= 33) { // lower edge left-to-right
                drawBulb(xMin + 4 * i, yMax, on);
            } else if (i <= 48) { // right edge bottom-to-top
                drawBulb(xMax, 4 * (70 - i), on);
            } else if (i <= 81) { // upper edge right-to-left
                drawBulb(4 * (96 - i), yMin, on);
            } else { // left edge top-to-bottom
                drawBulb(xMin, 4 * (i - 59), on);
            }
        }
    }

    private void drawBulb(double x, double y, boolean on) {
        g.setFill(on ? context.assets().color("palette.pale") : context.assets().color("palette.red"));
        g.fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }

    /**
     * <p>"It is well known that if a credit is inserted at the very beginning of the attract mode,
     * before the red ghost appears under the marquee, the first maze of the game will be colored
     * blue instead of the normal maze color."</p>
     * @see  <a href="http://www.donhodges.com/ms_pacman_bugs.htm">Ms. Pac-Man blue maze bug</a>
     */
    private void triggerBlueMazeBug() {
        if (context.game() instanceof MsPacManGameModel msPacManGame) {
            msPacManGame.blueMazeBug = true;
            Logger.info("Blue maze bug triggered");
        }
    }

    private void clearBlueMazeBug() {
        if (context.game() instanceof MsPacManGameModel msPacManGame) {
            msPacManGame.blueMazeBug = false;
        }
    }
}