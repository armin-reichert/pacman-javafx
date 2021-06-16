package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import java.util.List;

import de.amr.games.pacman.model.common.GameVariant;

class Options {

	double height = 576;
	GameVariant gameVariant = GameVariant.PACMAN;

	public Options(List<String> args) {
		int i = -1;
		while (++i < args.size()) {
			if ("-pacman".equals(args.get(i))) {
				gameVariant = GameVariant.PACMAN;
				continue;
			}
			if ("-mspacman".equals(args.get(i))) {
				gameVariant = GameVariant.MS_PACMAN;
				continue;
			}
			if ("-height".equals(args.get(i))) {
				if (++i == args.size()) {
					log("Error parsing options: missing height value.");
					break;
				}
				try {
					height = Double.parseDouble(args.get(i));
				} catch (NumberFormatException x) {
					log("Error parsing options: '%s' is no legal height value.", args.get(i));
				}
				continue;
			}
			log("Error parsing options: Found garbage '%s'", args.get(i));
		}
	}
}