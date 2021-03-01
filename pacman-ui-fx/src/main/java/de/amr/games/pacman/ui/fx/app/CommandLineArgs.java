package de.amr.games.pacman.ui.fx.app;

import static de.amr.games.pacman.lib.Logging.log;

public class CommandLineArgs {

	double height = 576;
	boolean pacman = false;

	public CommandLineArgs(String[] args) {
		int i = -1;
		while (++i < args.length) {
			if ("-pacman".equals(args[i])) {
				pacman = true;
				continue;
			}
			if ("-mspacman".equals(args[i])) {
				pacman = false;
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