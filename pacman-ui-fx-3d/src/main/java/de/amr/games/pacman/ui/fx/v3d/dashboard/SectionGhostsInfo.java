/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3dUI;

import java.util.function.BiFunction;
import java.util.function.Supplier;

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

		var key = anims.get().currentAnimationName();
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