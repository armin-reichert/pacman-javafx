package de.amr.games.pacman.ui.fx.scene;

import de.amr.games.pacman.ui.fx.app.ActionHandler;
import de.amr.games.pacman.ui.fx.app.SoundHandler;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import de.amr.games.pacman.ui.fx.util.Theme;

/**
 * @author Armin Reichert
 */
public interface GameSceneContext {

  ActionHandler actionHandler();

  Theme theme();

  Spritesheet spritesheet();

  SoundHandler soundHandler();
}
