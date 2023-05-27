/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3dUI;

/**
 * @author Armin Reichert
 */
public class SectionGhostsInfo extends Section {

	public SectionGhostsInfo(PacManGames3dUI ui, String title) {
		super(ui, title, Dashboard.MIN_LABEL_WIDTH, Dashboard.TEXT_COLOR, Dashboard.TEXT_FONT, Dashboard.LABEL_FONT);
		addGhostInfo(GameModel.RED_GHOST);
		addEmptyLine();
		addGhostInfo(GameModel.PINK_GHOST);
		addEmptyLine();
		addGhostInfo(GameModel.CYAN_GHOST);
		addEmptyLine();
		addGhostInfo(GameModel.ORANGE_GHOST);
	}

	private void addGhostInfo(byte ghostID) {
		var color = switch (ghostID) {
		case GameModel.RED_GHOST -> "Red";
		case GameModel.PINK_GHOST -> "Pink";
		case GameModel.CYAN_GHOST -> "Cyan";
		case GameModel.ORANGE_GHOST -> "Orange";
		default -> "";
		};
		addInfo(color + " Ghost", ifLevelExists(this::ghostName, ghostID));
		addInfo("State", ifLevelExists(this::ghostState, ghostID));
		addInfo("Killed Index", ifLevelExists(this::ghostKilledIndex, ghostID));
		addInfo("Animation", ifLevelExists(this::ghostAnimation, ghostID));
		addInfo("Movement", ifLevelExists(this::ghostMovement, ghostID));
		addInfo("Tile", ifLevelExists(this::ghostTile, ghostID));
	}

	private Supplier<String> ifLevelExists(BiFunction<GameLevel, Ghost, String> fnGhostInfo, byte ghostID) {
		return () -> {
			var level = game().level().orElse(null);
			return level != null ? fnGhostInfo.apply(level, level.ghost(ghostID)) : InfoText.NO_INFO;
		};
	}

	private String ghostName(GameLevel level, Ghost ghost) {
		return ghost.name();
	}

	private String ghostAnimation(GameLevel level, Ghost ghost) {
		var anims = ghost.animations();
		if (anims.isEmpty()) {
			return InfoText.NO_INFO;
		}
		var key = anims.get().selectedKey();
		var animKeyName = animationKeyName(key);
		var selectedAnim = anims.get().selectedAnimation();
		if (selectedAnim.isPresent()) {
			var running = selectedAnim.get().isRunning();
			return "%s %s".formatted(animKeyName, running ? "" : "stopped");
		} else {
			return InfoText.NO_INFO;
		}
	}

	private String animationKeyName(byte key) {
		return switch (key) {
		case GameModel.AK_GHOST_BLUE -> "AK_GHOST_BLUE";
		case GameModel.AK_GHOST_COLOR -> "AK_GHOST_COLOR";
		case GameModel.AK_GHOST_EYES -> "AK_GHOST_EYES";
		case GameModel.AK_GHOST_FLASHING -> "AK_GHOST_FLASHING";
		case GameModel.AK_GHOST_VALUE -> "AK_GHOST_VALUE";
		case GameModel.AK_MAZE_ENERGIZER_BLINKING -> "AK_MAZE_ENERGIZER_BLINKING";
		case GameModel.AK_MAZE_FLASHING -> "AK_MAZE_FLASHING";
		case GameModel.AK_PAC_DYING -> "AK_PAC_DYING";
		case GameModel.AK_PAC_MUNCHING -> "AK_PAC_MUNCHING";
		case GameModel.AK_PAC_BIG -> "AK_PAC_BIG";
		case GameModel.AK_BLINKY_DAMAGED -> "AK_BLINKY_DAMAGED";
		case GameModel.AK_BLINKY_PATCHED -> "AK_BLINKY_PATCHED";
		case GameModel.AK_BLINKY_NAKED -> "AK_BLINKY_NAKED";
		default -> "unknown key";
		};
	}

	private String ghostTile(GameLevel level, Ghost ghost) {
		return "%s Offset %s".formatted(ghost.tile(), ghost.offset());
	}

	private String ghostState(GameLevel level, Ghost ghost) {
		var stateText = ghost.state().name();
		if (ghost.state() == GhostState.HUNTING_PAC) {
			stateText = level.currentHuntingPhaseName();
		}
		if (ghost.id() == GameModel.RED_GHOST && level.cruiseElroyState() > 0) {
			stateText += " Elroy%d".formatted(level.cruiseElroyState());
		}
		return stateText;
	}

	private String ghostMovement(GameLevel level, Ghost ghost) {
		var speed = ghost.velocity().length();
		return "%.2f px %s (%s)".formatted(speed, ghost.moveDir(), ghost.wishDir());
	}

	private String ghostKilledIndex(GameLevel level, Ghost ghost) {
		return "%d".formatted(ghost.killedIndex());
	}
}