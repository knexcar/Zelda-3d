# Zelda-3d
This was a project for my CSE174 class. I created a 3d zelda game using a 3d library. Play the game using the arrow keys, which control "Link" (the green cylinder), and the space bar (which swings the sword).

When the game is started, a generic "box" dungeon level is loaded. Other levels can be loaded by typing their name into the text box at the bottom of the screen. These include:
- "test" - A "test" room with many of the tile types, and a section with different types of walls and doors.
- "test2" - An older version of "test" without the doors, keys, and some of the tiles.
- "test3" - A room with two doors and one key. One door leads to a lot of enemies, while the other leads to the "exit" (or where the exit would be if the game had them).
- "eagleDungeon" - A recreation of the first dungeon from the origional "Legend of Zelda" game.
- "eagleDungeon2" - An older version of "eagleDungeon" without arches over the doors, and less enemies.
The game also includes a level editor which can be used to edit any of the included levels, or to create a new one. Note that two walls/tiles can occupy the same space, so delete the old tiles before adding new ones. 

## Download
The game (compiled for Windows) can be downloaded from [https://drive.google.com/file/d/0B2fztFEIv_baR0J2YXRuOFB0UkE/view?usp=sharing](https://drive.google.com/file/d/0B2fztFEIv_baR0J2YXRuOFB0UkE/view?usp=sharing). Note that the file comes as a .zipp in order to prevent programs like Gmail for freaking out over an executable, so the extension must be changed to .zip before opening.

## Compiling the Code
The game was written in Java, more specifically in the Eclipse IDE. It uses the Java 3D API to display graphics, though it is included in the repository for convinience. The library requires OpenGL 1.2 or higher to run. Note that the "native Library Location" is the "libraryBin" folder. Eclipse should configure this automatically.

## Features
- 3D Graphics
- Explore dungeons
- Fight enemies
- Doors and keys
- Level editor
- Floor, wall, and ceiling tiles allow for interesting dungeon designs.
- Music from The Legend of Zelda