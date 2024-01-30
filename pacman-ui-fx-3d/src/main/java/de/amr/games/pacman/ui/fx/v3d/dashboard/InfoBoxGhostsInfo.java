/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.fx.util.Theme;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author Armin Reichert
 */
public class InfoBoxGhostsInfo extends InfoBox {

	public InfoBoxGhostsInfo(Theme theme, String title) {
		super(theme, title);
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
		addInfo(color + " Ghost", ifLevelExists(this::ghostNameAndState, ghostID));
		//addInfo("Killed Index", ifLevelExists(this::ghostKilledIndex, ghostID));
		addInfo("Animation", ifLevelExists(this::ghostAnimation, ghostID));
		addInfo("Movement", ifLevelExists(this::ghostMovement, ghostID));
		addInfo("Tile", ifLevelExists(this::ghostTile, ghostID));
	}

	private Supplier<String> ifLevelExists(BiFunction<GameLevel, Ghost, String> fnGhostInfo, byte ghostID) {
		return () -> {
			var level = sceneContext.game().level().orElse(null);
			return level != null ? fnGhostInfo.apply(level, level.ghost(ghostID)) : InfoText.NO_INFO;
		};
	}

	private String ghostNameAndState(GameLevel level, Ghost ghost) {
		var ghostName = ghost.name();
		if (ghost.id() == GameModel.RED_GHOST && level.cruiseElroyState() > 0) {
			ghostName= "Elroy" + level.cruiseElroyState();
		}
		return String.format("%s (%s)", ghostName, ghostState(level, ghost));
	}

	private String ghostAnimation(GameLevel level, Ghost ghost) {
		if (ghost.animations().isEmpty()) {
			return InfoText.NO_INFO;
		}
		var key = ghost.animations().get().currentAnimationName();
		return key != null ? key : InfoText.NO_INFO;
	}

	private String ghostTile(GameLevel level, Ghost ghost) {
		return "%s Offset %s".formatted(ghost.tile(), ghost.offset());
	}

	private String ghostState(GameLevel level, Ghost ghost) {
		var stateText = ghost.state().name();
		if (ghost.state() == GhostState.HUNTING_PAC) {
			stateText = level.currentHuntingPhaseName();
		}
		return stateText;
	}

	private String ghostMovement(GameLevel level, Ghost ghost) {
		var speed = ghost.velocity().length();
		return "%.2f px %s (%s)".formatted(speed, ghost.moveDir(), ghost.wishDir());
	}
}