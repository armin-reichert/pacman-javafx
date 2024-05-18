/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntro;
import de.amr.games.pacman.controller.MsPacManIntro.State;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.fx.PacManGames2dUI;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.util.Keyboard;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 *
 * @author Armin Reichert
 */
public class MsPacManIntroScene extends GameScene2D {

    private MsPacManIntro intro;

    @Override
    public boolean isCreditVisible() {
        return true;
    }

    @Override
    public void init() {
        setScoreVisible(true);
        intro = new MsPacManIntro();
        var ss = context.<MsPacManGameSpriteSheet>spriteSheet();
        intro.msPacMan.setAnimations(new MsPacManGamePacAnimations(intro.msPacMan, ss));
        intro.msPacMan.selectAnimation(Pac.ANIM_MUNCHING);
        for (var ghost : intro.ghosts) {
            ghost.setAnimations(new MsPacManGameGhostAnimations(ghost, ss));
            ghost.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
        }
        intro.changeState(State.START);
    }

    @Override
    public void update() {
        intro.update();
    }

    @Override
    public void handleKeyboardInput() {
        if (Keyboard.pressed(PacManGames2dUI.KEYS_ADD_CREDIT)) {
            context.actionHandler().addCredit();
        } else if (Keyboard.pressed(PacManGames2dUI.KEYS_START_GAME)) {
            context.actionHandler().startGame();
        } else if (Keyboard.pressed(PacManGames2dUI.KEY_SELECT_VARIANT)) {
            context.actionHandler().switchGameVariant();
        } else if (Keyboard.pressed(PacManGames2dUI.KEY_PLAY_CUTSCENES)) {
            context.actionHandler().startCutscenesTest();
        }
    }

    @Override
    public void drawSceneContent() {
        var theme = context.theme();
        var font8 = sceneFont(8);
        var tx = intro.titlePosition.x();
        var ty = intro.titlePosition.y();
        var y0 = intro.stopY;
        drawMarquee();
        classicRenderer.drawText(g, "\"MS PAC-MAN\"", context.theme().color("palette.orange"), font8, tx, ty);
        if (intro.state() == State.GHOSTS_MARCHING_IN) {
            var ghost = intro.ghosts[intro.ghostIndex];
            var color = switch (ghost.id()) {
                case GameModel.RED_GHOST -> theme.color("palette.red");
                case GameModel.PINK_GHOST -> theme.color("palette.pink");
                case GameModel.CYAN_GHOST -> theme.color("palette.cyan");
                case GameModel.ORANGE_GHOST -> theme.color("palette.orange");
                default -> throw new IllegalStateException("Unexpected value: " + ghost.id());
            };
            if (ghost.id() == GameModel.RED_GHOST) {
                classicRenderer.drawText(g, "WITH", context.theme().color("palette.pale"), font8, tx, y0 + t(3));
            }
            var text = ghost.name().toUpperCase();
            var dx = text.length() < 4 ? t(1) : 0;
            classicRenderer.drawText(g, text, color, font8, tx + t(3) + dx, y0 + t(6));
        } else if (intro.state() == State.MS_PACMAN_MARCHING_IN || intro.state() == State.READY_TO_PLAY) {
            classicRenderer.drawText(g, "STARRING", context.theme().color("palette.pale"), font8, tx, y0 + t(3));
            classicRenderer.drawText(g, "MS PAC-MAN", context.theme().color("palette.yellow"), font8, tx, y0 + t(6));
        }
        for (var ghost : intro.ghosts) {
            classicRenderer.drawGhost(g, GameVariant.MS_PACMAN, ghost);
        }
        classicRenderer.drawPac(g, GameVariant.MS_PACMAN, intro.msPacMan);
        drawMsPacManCopyright(t(6), t(28));
        classicRenderer.drawLevelCounter(g, GameVariant.MS_PACMAN, context.game().levelCounter());
    }

    private void drawMarquee() {
        var on = intro.marqueeState();
        for (int i = 0; i < intro.numBulbs; ++i) {
            g.setFill(on.get(i)
                ? context.theme().color("palette.pale")
                : context.theme().color("palette.red"));
            if (i <= 33) {
                g.fillRect(s(60 + 4 * i), s(148), s(2), s(2));
            } else if (i <= 48) {
                g.fillRect(s(192), s(280 - 4 * i), s(2), s(2));
            } else if (i <= 81) {
                g.fillRect(s(384 - 4 * i), s(88), s(2), s(2));
            } else {
                g.fillRect(s(60), s(4 * i - 236), s(2), s(2));
            }
        }
    }
}