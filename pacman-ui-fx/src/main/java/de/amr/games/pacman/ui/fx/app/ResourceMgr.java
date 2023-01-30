/*
MIT License

Copyright (c) 2022 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package de.amr.games.pacman.ui.fx.app;

import java.net.URL;
import java.util.List;
import java.util.MissingResourceException;

/**
 * @author Armin Reichert
 */
public class ResourceMgr {

	private static final String RESOURCE_ROOT_DIR = "/de/amr/games/pacman/ui/fx/";
	public static final List<String> FLOOR_TEXTURES = List.of("none", "penrose-tiling.jpg", "escher-texture.jpg");
	public static final String VOICE_HELP = "sound/common/press-key.mp3";
	public static final String VOICE_AUTOPILOT_OFF = "sound/common/autopilot-off.mp3";
	public static final String VOICE_AUTOPILOT_ON = "sound/common/autopilot-on.mp3";
	public static final String VOICE_IMMUNITY_OFF = "sound/common/immunity-off.mp3";
	public static final String VOICE_IMMUNITY_ON = "sound/common/immunity-on.mp3";

	/**
	 * @param relativePath relative path starting from resource root directory
	 * @return full path to resource including path to resource root directory
	 */
	public static String toFullPath(String relativePath) {
		return RESOURCE_ROOT_DIR + relativePath;
	}

	/**
	 * @param relativePath relative path starting from resource root directory
	 * @return URL of resource addressed by this path
	 */
	public static URL urlFromRelPath(String relativePath) {
		return url(toFullPath(relativePath));
	}

	/**
	 * @param fullPath full path to resource including path to resource root directory
	 * @return URL of resource addressed by this path
	 */
	public static URL url(String fullPath) {
		var url = ResourceMgr.class.getResource(fullPath);
		if (url == null) {
			throw new MissingResourceException("Missing resource, path=" + fullPath, "", fullPath);
		}
		return url;
	}
}