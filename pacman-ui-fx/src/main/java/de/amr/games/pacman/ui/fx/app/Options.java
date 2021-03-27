package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.model.common.GameVariant;

class Options {

	double height = 576;
	GameVariant gameVariant = GameVariant.PACMAN;

	public Options(String[] args) {
		int i = -1;
		while (++i < args.length) {
			if ("-pacman".equals(args[i])) {
				gameVariant = GameVariant.PACMAN;
				continue;
			}
			if ("-mspacman".equals(args[i])) {
				gameVariant = GameVariant.MS_PACMAN;
				continue;
			}
			if ("-height".equals(args[i])) {
				if (++i == args.length) {
					log("Error parsing options: missing height value.");
					break;
				}
				try {
					height = Double.parseDouble(args[i]);
				} catch (NumberFormatException x) {
					log("Error parsing options: '%s' is no legal height value.", args[i]);
				}
				continue;
			}
			log("Error parsing options: Found garbage '%s'", args[i]);
		}
	}
}