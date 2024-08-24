/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.PacManIntroController;
import de.amr.games.pacman.controller.PacManIntroController.State;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui2d.ActionHandler;
import de.amr.games.pacman.ui2d.GameKey;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.rendering.PacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.PacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.PacManGameSpriteSheet;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.t;

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

    private PacManIntroController introController;

    @Override
    public boolean isCreditVisible() {
        return true;
    }

    @Override
    public void init() {
        super.init();
        context.setScoreVisible(true);
        introController = new PacManIntroController();
        spriteRenderer.setSpriteSheet(context.spriteSheet(context.game().variant()));
        introController.pacMan.setAnimations(new PacManGamePacAnimations(introController.pacMan, (PacManGameSpriteSheet) spriteRenderer.spriteSheet()));
        introController.ghosts().forEach(ghost -> ghost.setAnimations(new PacManGameGhostAnimations(ghost, (PacManGameSpriteSheet) spriteRenderer.spriteSheet())));
        introController.blinking.reset();
        introController.changeState(State.START);
    }

    @Override
    public void end() {
        GameSounds.stopVoice();
    }

    @Override
    public void update() {
        introController.update();
    }

    @Override
    public void handleKeyboardInput(ActionHandler actions) {
        if (GameKey.ADD_CREDIT.pressed()) {
            actions.addCredit();
        } else if (GameKey.START_GAME.pressed()) {
            actions.startGame();
        } else if (GameKey.CUTSCENES.pressed()) {
            actions.startCutscenesTest();
        }
    }

    @Override
    public void drawSceneContent() {
        var timer = introController.state().timer();
        drawGallery();
        switch (introController.state()) {
            case SHOWING_POINTS -> drawPoints();
            case CHASING_PAC -> {
                drawPoints();
                drawBlinkingEnergizer();
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

        int tx = introController.leftTileX;
        if (introController.titleVisible) {
            spriteRenderer.drawText(g, "CHARACTER / NICKNAME", context.assets().color("palette.pale"), font, t(tx + 3), t(6));
        }
        Color[] ghostColors = {
            context.assets().color("palette.red"),
            context.assets().color("palette.pink"),
            context.assets().color("palette.cyan"),
            context.assets().color("palette.orange"),
        };
        for (byte id = 0; id < 4; ++id) {
            var ghostInfo = introController.ghostInfo[id];
            if (!ghostInfo.pictureVisible) {
                continue;
            }
            int ty = 7 + 3 * id;
            spriteRenderer.drawSpriteCenteredOverBox(g, spriteRenderer.spriteSheet().ghostFacingRight(id), t(tx) + 4, t(ty));
            if (ghostInfo.characterVisible) {
                var text = "-" + ghostInfo.character;
                spriteRenderer.drawText(g, text, ghostColors[id], font, t(tx + 3), t(ty + 1));
            }
            if (ghostInfo.nicknameVisible) {
                var text = QUOTE + ghostInfo.ghost.name() + QUOTE;
                spriteRenderer.drawText(g, text, ghostColors[id], font, t(tx + 14), t(ty + 1));
            }
        }
    }

    private void drawBlinkingEnergizer() {
        if (introController.blinking.isOn()) {
            spriteRenderer.drawSpriteScaled(g, spriteRenderer.spriteSheet().getEnergizerSprite(),t(introController.leftTileX),t(20));
        }
    }

    private void drawGhost(Ghost ghost) {
        spriteRenderer.drawGhost(g, ghost);
    }

    private void drawGuys(int shakingAmount) {
        if (shakingAmount == 0) {
            introController.ghosts().forEach(this::drawGhost);
        } else {
            drawGhost(introController.ghost(0));
            drawGhost(introController.ghost(3));
            // shaking ghosts effect, not quite as in original game
            g.save();
            g.translate(shakingAmount, 0);
            drawGhost(introController.ghost(1));
            drawGhost(introController.ghost(2));
            g.restore();
        }
        spriteRenderer.drawPac(g, introController.pacMan);
    }

    private void drawPoints() {
        var color = context.assets().color("palette.pale");
        var font8 = sceneFont(8);
        var font6 = sceneFont(6);
        int tx = introController.leftTileX + 6;
        int ty = 25;
        g.setFill(Color.rgb(254, 189, 180));
        g.fillRect(s(t(tx) + 4), s(t(ty - 1) + 4), s(2), s(2));
        if (introController.blinking.isOn()) {
            spriteRenderer.drawSpriteScaled(g, spriteRenderer.spriteSheet().getEnergizerSprite(),
                t(tx), t(ty + 1));
        }
        spriteRenderer.drawText(g, "10",  color, font8, t(tx + 2), t(ty));
        spriteRenderer.drawText(g, "PTS", color, font6, t(tx + 5), t(ty));
        spriteRenderer.drawText(g, "50",  color, font8, t(tx + 2), t(ty + 2));
        spriteRenderer.drawText(g, "PTS", color, font6, t(tx + 5), t(ty + 2));
    }
}