/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.PacManIntro;
import de.amr.games.pacman.controller.PacManIntro.State;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.PacManGames2dUI;
import de.amr.games.pacman.ui2d.rendering.PacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.PacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui2d.PacManGames2dUI.*;

/**
 * Intro scene of the PacMan game.
 * <p>
 * The ghosts are presented one after another, then Pac-Man is chased by the ghosts, turns the card and hunts the ghost
 * himself.
 *
 * @author Armin Reichert
 */
public class PacManIntroScene extends GameScene2D {

    private static final char QUOTE = '\"';

    private PacManIntro intro;
    private PacManGameSpriteSheet ss;

    @Override
    public boolean isCreditVisible() {
        return true;
    }

    @Override
    public void init() {
        context.actionHandler().showSignature();
        setScoreVisible(true);
        intro = new PacManIntro();
        ss = (PacManGameSpriteSheet) context.getSpriteSheet(context.game().variant());
        intro.pacMan.setAnimations(new PacManGamePacAnimations(intro.pacMan, ss));
        intro.ghosts().forEach(ghost -> ghost.setAnimations(new PacManGameGhostAnimations(ghost, ss)));
        intro.blinking.reset();
        intro.changeState(State.START);
    }

    @Override
    public void end() {
        context.actionHandler().hideSignature();
        context.actionHandler().stopVoice();
    }

    @Override
    public void update() {
        intro.update();
    }

    @Override
    public void handleKeyboardInput() {
        if (Keyboard.pressed(KEYS_ADD_CREDIT)) {
            context.actionHandler().addCredit();
        } else if (Keyboard.pressed(KEYS_START_GAME)) {
            context.actionHandler().startGame();
        } else if (Keyboard.pressed(KEY_PLAY_CUTSCENES)) {
            context.actionHandler().startCutscenesTest();
        }
    }

    @Override
    public void drawSceneContent() {
        var timer = intro.state().timer();
        drawGallery();
        switch (intro.state()) {
            case SHOWING_POINTS -> drawPoints();
            case CHASING_PAC -> {
                drawPoints();
                drawBlinkingEnergizer();
                drawGuys(flutter(timer.tick()));
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

        int tx = intro.leftTileX;
        if (intro.titleVisible) {
            classicRenderer.drawText(g, "CHARACTER / NICKNAME", context.theme().color("palette.pale"), font, t(tx + 3), t(6));
        }
        Color[] ghostColors = {
            context.theme().color("palette.red"),
            context.theme().color("palette.pink"),
            context.theme().color("palette.cyan"),
            context.theme().color("palette.orange"),
        };
        for (int id = 0; id < 4; ++id) {
            var ghostInfo = intro.ghostInfo[id];
            if (!ghostInfo.pictureVisible) {
                continue;
            }
            int ty = 7 + 3 * id;
            classicRenderer.drawSpriteCenteredOverBox(g, ss, ss.ghostFacingRight(id), t(tx) + 4, t(ty));
            if (ghostInfo.characterVisible) {
                var text = "-" + ghostInfo.character;
                classicRenderer.drawText(g, text, ghostColors[id], font, t(tx + 3), t(ty + 1));
            }
            if (ghostInfo.nicknameVisible) {
                var text = QUOTE + ghostInfo.ghost.name() + QUOTE;
                classicRenderer.drawText(g, text, ghostColors[id], font, t(tx + 14), t(ty + 1));
            }
        }
    }

    private void drawBlinkingEnergizer() {
        if (intro.blinking.isOn()) {
            classicRenderer.drawSpriteScaled(g, ss.source(), ss.getEnergizerSprite(), t(intro.leftTileX),t(20));
        }
    }

    private void drawGhost(Ghost ghost) {
        classicRenderer.drawGhost(g, ss, ghost);
    }

    private void drawGuys(int shakingAmount) {
        if (shakingAmount == 0) {
            intro.ghosts().forEach(this::drawGhost);
        } else {
            drawGhost(intro.ghost(0));
            drawGhost(intro.ghost(3));
            // shaking ghosts effect, not quite as in original game
            g.save();
            g.translate(shakingAmount, 0);
            drawGhost(intro.ghost(1));
            drawGhost(intro.ghost(2));
            g.restore();
        }
        classicRenderer.drawPac(g, ss, intro.pacMan);
    }

    private void drawPoints() {
        var color = context.theme().color("palette.pale");
        var font8 = sceneFont(8);
        var font6 = sceneFont(6);
        int tx = intro.leftTileX + 6;
        int ty = 25;
        g.setFill(Color.rgb(254, 189, 180));
        g.fillRect(s(t(tx) + 4), s(t(ty - 1) + 4), s(2), s(2));
        if (intro.blinking.isOn()) {
            classicRenderer.drawSpriteScaled(g, ss.source(), ss.getEnergizerSprite(), t(tx), t(ty + 1));
        }
        classicRenderer.drawText(g, "10",  color, font8, t(tx + 2), t(ty));
        classicRenderer.drawText(g, "PTS", color, font6, t(tx + 5), t(ty));
        classicRenderer.drawText(g, "50",  color, font8, t(tx + 2), t(ty + 2));
        classicRenderer.drawText(g, "PTS", color, font6, t(tx + 5), t(ty + 2));
    }
}