/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.MsPacManIntermission2;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.rendering2d.ClapperboardAnimation;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGamePacAnimations;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 *
 * @author Armin Reichert
 */
public class MsPacManCutScene2 extends GameScene2D {

    private MsPacManIntermission2 intermission;
    private ClapperboardAnimation clapAnimation;

    @Override
    public boolean isCreditVisible() {
        return !context.gameController().hasCredit();
    }

    @Override
    public void init() {
        var ss = classicRenderer.getMsPacManSpriteSheet();
        setScoreVisible(true);
        intermission = new MsPacManIntermission2();
        intermission.msPac.setAnimations(new MsPacManGamePacAnimations(intermission.msPac, ss));
        intermission.pacMan.setAnimations(new MsPacManGamePacAnimations(intermission.pacMan, ss));
        clapAnimation = new ClapperboardAnimation("2", "THE CHASE");
        clapAnimation.start();
        intermission.changeState(MsPacManIntermission2.STATE_FLAP, 120);
    }

    @Override
    public void update() {
        intermission.tick();
        clapAnimation.tick();
    }

    @Override
    public void drawSceneContent() {
        classicRenderer.drawClapperBoard(g, context.theme(), clapAnimation, t(3), t(10));
        classicRenderer.drawPac(g, GameVariant.MS_PACMAN, intermission.msPac);
        classicRenderer.drawPac(g, GameVariant.MS_PACMAN, intermission.pacMan);
        drawLevelCounter(g);
    }
}