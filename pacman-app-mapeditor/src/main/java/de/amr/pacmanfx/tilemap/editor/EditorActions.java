package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.tilemap.editor.actions.*;

public interface EditorActions {
    Action_AddBorderWall ADD_BORDER_WALL = new Action_AddBorderWall();
    Action_FloodWithPellets FLOOD_WITH_PELLETS = new Action_FloodWithPellets();
    Action_IdentifyObstacle IDENTIFY_OBSTACLE = new Action_IdentifyObstacle();
    Action_OpenMapFile OPEN_MAP_FILE = new Action_OpenMapFile();
    Action_PlaceArcadeHouse PLACE_ARCADE_HOUSE = new Action_PlaceArcadeHouse();
    Action_SaveMapFile SAVE_MAP_FILE = new Action_SaveMapFile();
    Action_SelectNextMapFile SELECT_NEXT_MAP_FILE = new Action_SelectNextMapFile();
    Action_ShowNewMapDialog SHOW_NEW_MAP_DIALOG = new Action_ShowNewMapDialog();
}
