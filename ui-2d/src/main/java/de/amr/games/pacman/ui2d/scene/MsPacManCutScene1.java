/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.MsPacManIntermission1;
import de.amr.games.pacman.ui2d.rendering.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.MsPacManGameSpriteSheet;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 *
 * @author Armin Reichert
 */
public class MsPacManCutScene1 extends GameScene2D {

    private MsPacManIntermission1 intermission;
    private ClapperboardAnimation clapAnimation;
    private MsPacManGameSpriteSheet ss;

    @Override
    public boolean isCreditVisible() {
        return !context.gameController().hasCredit();
    }

    @Override
    public void init() {
        ss = (MsPacManGameSpriteSheet) context.getSpriteSheet(context.game().variant());
        setScoreVisible(true);
        intermission = new MsPacManIntermission1();
        intermission.msPac.setAnimations(new MsPacManGamePacAnimations(intermission.msPac, ss));
        intermission.pacMan.setAnimations(new MsPacManGamePacAnimations(intermission.pacMan, ss));
        intermission.inky.setAnimations(new MsPacManGameGhostAnimations(intermission.inky, ss));
        intermission.pinky.setAnimations(new MsPacManGameGhostAnimations(intermission.pinky, ss));
        clapAnimation = new ClapperboardAnimation("1", "THEY MEET");
        clapAnimation.start();
        intermission.changeState(MsPacManIntermission1.STATE_FLAP, 120);
    }

    @Override
    public void update() {
        intermission.tick();
        clapAnimation.tick();
    }

    @Override
    public void drawSceneContent() {
        classicRenderer.drawClapperBoard(g, ss, context.theme(), clapAnimation, t(3), t(10));
        classicRenderer.drawPac(g, ss, intermission.msPac);
        classicRenderer.drawPac(g, ss, intermission.pacMan);
        classicRenderer.drawGhost(g, ss, intermission.inky);
        classicRenderer.drawGhost(g, ss, intermission.pinky);
        classicRenderer.drawEntitySprite(g, ss, intermission.heart, ss.heartSprite());
        drawLevelCounter(g);
    }
}