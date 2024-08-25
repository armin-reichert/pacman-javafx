/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.fsm.FiniteStateMachine;
import de.amr.games.pacman.lib.fsm.FsmState;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.mspacman.MsPacManGameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.ActionHandler;
import de.amr.games.pacman.ui2d.GameKey;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.AssetMap;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.BitSet;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.model.GameModel.ORANGE_GHOST;

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
                int endPositionY = data.stopY + data.ghostIndex * 16;
                if (ghost.moveDir() == Direction.LEFT) {
                    if (ghost.posX() <= data.stopX) {
                        ghost.setPosX(data.stopX);
                        ghost.setMoveAndWishDir(Direction.UP);
                        data.waitBeforeRising = 2;
                    } else {
                        ghost.move();
                    }
                }
                else if (ghost.moveDir() == Direction.UP) {
                    if (data.waitBeforeRising > 0) {
                        data.waitBeforeRising -= 1;
                    }
                    else if (ghost.posY() <= endPositionY) {
                        ghost.setSpeed(0);
                        ghost.stopAnimation();
                        ghost.resetAnimation();
                        return true;
                    } else {
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
                intro.data.msPacMan.show();
                intro.data.msPacMan.move();
                if (intro.data.msPacMan.posX() <= intro.data.stopMsPacX) {
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
                if (timer.atSecond(2.0) && !gameController().hasCredit()) {
                    gameController().changeState(GameState.READY); // demo level
                } else if (timer.atSecond(5)) {
                    gameController().changeState(GameState.CREDIT);
                }
            }
        };

        final TickTimer timer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return timer;
        }

        GameController gameController() {
            return GameController.it();
        }
    }

    private static class Data {
        final float speed = 1.1f;
        final int stopY = TS * 11 + 1;
        final int stopX = TS * 6 - 4;
        final int stopMsPacX = TS * 15 + 2;
        final Vector2i titlePosition = v2i(TS * 10, TS * 8);
        final TickTimer marqueeTimer = new TickTimer("marquee-timer");
        final int numBulbs = 96;
        final int bulbOnDistance = 16;
        int waitBeforeRising = 0;
        int ghostIndex = 0;
        Pac msPacMan = new Pac();
        Ghost[] ghosts = new Ghost[] {
            new Ghost(RED_GHOST, "Blinky", null),
            new Ghost(PINK_GHOST, "Pinky", null),
            new Ghost(CYAN_GHOST, "Inky", null),
            new Ghost(ORANGE_GHOST, "Sue", null)
        };
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
        data = new Data();

        var sheet = (MsPacManGameSpriteSheet) context.spriteSheet(context.game().variant());
        spriteRenderer.setSpriteSheet(sheet);
        context.setScoreVisible(true);
        clearBlueMazeBug();

        data.msPacMan.setAnimations(new MsPacManGamePacAnimations(data.msPacMan, sheet));
        data.msPacMan.selectAnimation(Pac.ANIM_MUNCHING);
        for (Ghost ghost : data.ghosts) {
            ghost.setAnimations(new MsPacManGameGhostAnimations(ghost, sheet));
            ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
        }

        sceneController.changeState(SceneState.STARTING);
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
    public void handleKeyboardInput(ActionHandler actions) {
        if (GameKey.ADD_CREDIT.pressed()) {
            if (sceneController.state() == SceneState.STARTING) {
                triggerBlueMazeBug();
            }
            actions.addCredit();
        } else if (GameKey.START_GAME.pressed()) {
            actions.startGame();
        } else if (GameKey.CUTSCENES.pressed()) {
            actions.startCutscenesTest();
        }
    }

    @Override
    public void drawSceneContent() {
        AssetMap assets = context.assets();
        Font font8 = sceneFont(8);
        int tx = data.titlePosition.x();
        int ty = data.titlePosition.y();
        int y0 = data.stopY;

        drawMarquee();
        spriteRenderer.drawText(g, "\"MS PAC-MAN\"", assets.color("palette.orange"), font8, tx, ty);
        if (sceneController.state() == SceneState.GHOSTS_MARCHING_IN) {
            Ghost ghost = data.ghosts[data.ghostIndex];
            Color color = switch (ghost.id()) {
                case GameModel.RED_GHOST -> assets.color("palette.red");
                case GameModel.PINK_GHOST -> assets.color("palette.pink");
                case GameModel.CYAN_GHOST -> assets.color("palette.cyan");
                case GameModel.ORANGE_GHOST -> assets.color("palette.orange");
                default -> throw new IllegalStateException("Illegal ghost ID: " + ghost.id());
            };
            if (ghost.id() == GameModel.RED_GHOST) {
                spriteRenderer.drawText(g, "WITH", assets.color("palette.pale"), font8, tx, y0 + t(3));
            }
            String text = ghost.name().toUpperCase();
            double dx = text.length() < 4 ? t(1) : 0;
            spriteRenderer.drawText(g, text, color, font8, tx + t(3) + dx, y0 + t(6));
        } else if (sceneController.state() == SceneState.MS_PACMAN_MARCHING_IN || sceneController.state() == SceneState.READY_TO_PLAY) {
            spriteRenderer.drawText(g, "STARRING", assets.color("palette.pale"), font8, tx, y0 + t(3));
            spriteRenderer.drawText(g, "MS PAC-MAN", assets.color("palette.yellow"), font8, tx, y0 + t(6));
        }
        for (Ghost ghost : data.ghosts) {
            spriteRenderer.drawGhost(g, ghost);
        }
        spriteRenderer.drawPac(g, data.msPacMan);
        drawMsPacManCopyright(t(6), t(28));
        drawLevelCounter(g);
    }

    // TODO This is too cryptic
    private void drawMarquee() {
        double xMin = 60, xMax = 192, yMin = 88, yMax = 148;
        for (int i = 0; i < data.numBulbs; ++i) {
            boolean on = marqueeState().get(i);
            if (i <= 33) { // lower edge left-to-right
                drawLight(xMin + 4 * i, yMax, on);
            } else if (i <= 48) { // right edge bottom-to-top
                drawLight(xMax, 4 * (70 - i), on);
            } else if (i <= 81) { // upper edge right-to-left
                drawLight(4 * (96 - i), yMin, on);
            } else { // left edge top-to-bottom
                drawLight(xMin, 4 * (i - 59), on);
            }
        }
    }

    private void drawLight(double x, double y, boolean on) {
        Color onColor = context.assets().color("palette.pale"), offColor = context.assets().color("palette.red");
        double bulbSize = s(2);
        g.setFill(on ? onColor : offColor);
        g.fillRect(s(x), s(y), bulbSize, bulbSize);
    }

    /**
     * <p>"It is well known that if a credit is inserted at the very beginning of the attract mode,
     * before the red ghost appears under the marquee, the first maze of the game will be colored
     * blue instead of the normal maze color."</p>
     * @see  <a href="http://www.donhodges.com/ms_pacman_bugs.htm">Ms. Pac-Man blue maze bug</a>
     */
    private void triggerBlueMazeBug() {
        var game = (MsPacManGameModel) context.game();
        game.blueMazeBug = true;
        Logger.info("Blue maze bug triggered");
    }

    private void clearBlueMazeBug() {
        var game = (MsPacManGameModel) context.game();
        game.blueMazeBug = false;
        Logger.info("Blue maze bug cleared");
    }

    /**
     * In the Arcade game, 6 of the 96 bulbs are switched-on every frame, shifting every tick. The bulbs in the leftmost
     * column however are switched-off every second frame. Maybe a bug?
     *
     * @return bitset indicating which marquee bulbs are on
     */
    private BitSet marqueeState() {
        var state = new BitSet(data.numBulbs);
        long t = data.marqueeTimer.currentTick();
        for (int b = 0; b < 6; ++b) {
            state.set((int) (b * data.bulbOnDistance + t) % data.numBulbs);
        }
        for (int i = 81; i < data.numBulbs; ++i) {
            if (isOdd(i)) {
                state.clear(i);
            }
        }
        return state;
    }
}