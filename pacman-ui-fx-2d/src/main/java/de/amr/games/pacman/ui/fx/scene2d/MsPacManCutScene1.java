/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission1;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.rendering2d.ClapperboardAnimation;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGamePacAnimations;

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

    @Override
    public boolean isCreditVisible() {
        return !context.gameController().hasCredit();
    }

    @Override
    public void init() {
        var ss = classicRenderer.getMsPacManSpriteSheet();
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
        var ss = classicRenderer.getMsPacManSpriteSheet();
        classicRenderer.drawClapperBoard(g, context.theme(), clapAnimation, t(3), t(10));
        classicRenderer.drawPac(g, GameVariant.MS_PACMAN, intermission.msPac);
        classicRenderer.drawPac(g, GameVariant.MS_PACMAN, intermission.pacMan);
        classicRenderer.drawGhost(g, GameVariant.MS_PACMAN, intermission.inky);
        classicRenderer.drawGhost(g, GameVariant.MS_PACMAN, intermission.pinky);
        classicRenderer.drawEntitySprite(g, ss, intermission.heart, ss.heartSprite());
        double x = t(24);
        double y = t(34);
        classicRenderer.drawLevelCounter(g, GameVariant.MS_PACMAN, context.game().levelCounter(), x, y);
    }
}