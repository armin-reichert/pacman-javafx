/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

/**
 * Color value specifications for a map. All values have to be specified in one of these formats:
 * <ul>
 *     <li><code>rgb(red,green,blue)</code> with red, green, blue in 0.255</li>
 *     <li><code>#rrggbb</code> with rr,gg, bb hexadecimal values, # character is optional</li>
 * </ul>
 * @param fill color value used to fill obstacles
 * @param stroke color value used to stroke paths and obstacle  borders
 * @param door ghost house door color
 * @param pellet color of pellets and energizers
 */
public record MapColorScheme(String fill, String stroke, String door, String pellet) {}
