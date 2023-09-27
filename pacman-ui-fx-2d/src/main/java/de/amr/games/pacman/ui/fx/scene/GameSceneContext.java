package de.amr.games.pacman.ui.fx.scene;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui.fx.ActionHandler;
import de.amr.games.pacman.ui.fx.SoundHandler;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.scene.media.AudioClip;

/**
 * @author Armin Reichert
 */
public interface GameSceneContext {

  ActionHandler actionHandler();

  Theme theme();

  Spritesheet spritesheet();

  SoundHandler soundHandler();

  default AudioClip clip(String key) {
    return soundHandler().audioClip(game().variant(), key);
  }

  default GameModel game() {
    return GameController.it().game();
  }
}