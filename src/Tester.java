import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.Texture;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.Timer;

import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;


public class Tester extends JPanel {
	private static final long serialVersionUID = 1L;
	//3D variables
	public SimpleUniverse world;
	//public BranchGroup mainGroup;
	public BranchGroup levelGroup;
	JFrame window;
	Canvas3D canvas3d;

	//Resource Variables
	Texture[] wallTextures;
	Clip[] music;
	Clip[] soundEffects;

	//Actual game variables
	Player player;
	EditingCamera editor;
	ArrayList<Wall> walls;
	ArrayList<Floor> floors;
	ArrayList<Monster> monsters;
	Timer gameUpdateTimer;
	
	
	/**Variable to determine what type of items are being placed.
	 * -2 = Placing the player
	 * -1 = Delete mode.
	 * 1 = Placing a wall.
	 * 2 = Placing a floor or ceiling.
	 * 3 = Placing a monster.
	 * 30 = Placing a key.
	 */
	int placeMode = -1;
	static final int PLACE_MODE_PLAYER = -2;
	static final int PLACE_MODE_DELETE = -1;
	static final int PLACE_MODE_NONE = 0;
	static final int PLACE_MODE_WALL = 1;
	static final int PLACE_MODE_FLOOR = 2;
	static final int PLACE_MODE_MONSTER = 3;
	static final int PLACE_MODE_CEILING = 4;
	static final int PLACE_MODE_KEY = 30;
	
	Wall wallToPlace;
	Floor floorToPlace;
	Monster monsterToPlace;
	boolean editMode;
	int musicSelected;
	int cursorX, cursorY;

	//UI Variables
	Panel editPanel;
	private JTextField textField;
	JComboBox<String> comboBox;
	private SpringLayout springLayout;


	public static void main(String[] args) {
		//System.loadLibrary("libraryBin/j3dcore-ogl.dll");
		new Tester();
	}

	
	/**Constructor. Called only when the game is started. Deals with 
	 * creating the window, loading essential resources, and setting
	 * up everything else that needs to be set up.
	 */
	private Tester() {
		//Window Stuff
		{
			window = new JFrame("Zelda");
			window.setBounds(100, 100, 640, 580);
			window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			window.setAlwaysOnTop(true);
			window.setVisible(true);
			window.getContentPane().add(this);
		}
		springLayout = new SpringLayout();
		setLayout(springLayout);
		{
			//prepare the group of all objects in the level.
			levelGroup = new BranchGroup();
			levelGroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
			levelGroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
			levelGroup.setCapability(BranchGroup.ALLOW_DETACH);
			createButtons();
		}

		{//Load textures
			wallTextures = new Texture[10];
			for (int i = 0; i < wallTextures.length; i++) {
				wallTextures[i] = new TextureLoader("texture"+i+".png", new Container()).getTexture();
				wallTextures[i].setName(""+i);
				wallTextures[i].setMagFilter(Texture.BASE_LEVEL_POINT);
			}
		}
		
		{//Load Music
			music = new Clip[3];
			for (int i = 0; i < music.length; i++) {
				try {
					AudioInputStream ais = 
							AudioSystem.getAudioInputStream(new File("music"+i+".wav"));
					music[i] = AudioSystem.getClip();
					music[i].open(ais);
				} catch(Exception e) { System.out.println(e);}
			}
			soundEffects = new Clip[5];
			for (int i = 0; i < soundEffects.length; i++) {
				try {
					AudioInputStream ais = 
							AudioSystem.getAudioInputStream(new File("effect"+i+".wav"));
					soundEffects[i] = AudioSystem.getClip();
					soundEffects[i].open(ais);
				} catch(Exception e) { System.out.println(e);}
			}
		}

		//levelGroup.addChild(new ColorCube(0.3));
		editMode = false;
		walls = new ArrayList<Wall>();
		floors = new ArrayList<Floor>();
		monsters = new ArrayList<Monster>();
		createSampleRoom();
		setEditMode(false);
		repaint();

		//Update everything
		gameUpdateTimer = new Timer(20, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {update();}
		});
		gameUpdateTimer.start();
		this.revalidate();
		repaint();
		window.repaint();
	}

	
	
	
	/**Simple update loop. Defers to either the editor or
	 * the player and monsters for more advanced logic.
	 */
	private void update() {
		if (editMode) {
			editor.update();
		} else {
			player.update();
			for (Monster monster : monsters) {
				monster.update();
			}
		}
		//window.repaint();
	}

	
	
	
	
	/**Private method to create all the buttons for the UI.
	 * 
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createButtons() {
		textField = new JTextField("test");
		springLayout.putConstraint(SpringLayout.WEST, textField, 10, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, textField, 255, SpringLayout.WEST, this);
		add(textField);
		textField.setColumns(10);

		JButton btnLoadPlay = new JButton("Load");
		springLayout.putConstraint(SpringLayout.SOUTH, textField, -7, SpringLayout.NORTH, btnLoadPlay);
		springLayout.putConstraint(SpringLayout.NORTH, btnLoadPlay, -23, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.WEST, btnLoadPlay, 89, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, btnLoadPlay, 0, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, btnLoadPlay, 166, SpringLayout.WEST, this);
		btnLoadPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!loadLevel(textField.getText())) {
					setEditMode(false);
				}
			}
		});
		add(btnLoadPlay);

		JButton btnEditMode = new JButton("Edit");
		springLayout.putConstraint(SpringLayout.NORTH, btnEditMode, -23, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.WEST, btnEditMode, 178, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, btnEditMode, 0, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, btnEditMode, 255, SpringLayout.WEST, this);
		btnEditMode.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!loadLevel(textField.getText())) {
					setEditMode(true);
				}
			}
		});
		add(btnEditMode);

		editPanel = new Panel();
		springLayout.putConstraint(SpringLayout.NORTH, editPanel, -56, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.WEST, editPanel, 261, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.SOUTH, editPanel, 0, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.EAST, editPanel, 624, SpringLayout.WEST, this);
		add(editPanel);
		editPanel.setLayout(null);
		editPanel.setVisible(false);

		{
			/*JButton btnPlaceItem = new JButton("Place Item");
			btnPlaceItem.setBounds(156, 0, 112, 23);
			editPanel.add(btnPlaceItem);
			btnPlaceItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setItemToPlace();
				}
			});*/

			comboBox = new JComboBox();
			comboBox.setModel(new DefaultComboBoxModel(new String[] {"Delete Mode", "Player",
					"Wall Brick", "Wall Block", "Wall Rock", "Wall Tile", "Wall Statue 1", "Wall Statue 2", "Water",
					"Floor Dirt", "Floor Tile", "Monster Horiz", "Monster Vert", "Monster Fast Horiz", "Monster Fast Vert",
					"Ceiling Brick", "Ceiling Block", "Ceiling Rock", "Ceiling Tile", 
					"Door", "Door Horiz", "Door Vert", "Key"}));
			comboBox.setBounds(0, 1, 146, 20);
			editPanel.add(comboBox);
			comboBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					setItemToPlace();
				}
			});
		}

		JButton btnSave = new JButton("Save");
		btnSave.setBounds(0, 32, 87, 23);
		editPanel.add(btnSave);
		btnSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!saveLevel(textField.getText())) {
					setEditMode(true);
				}
			}
		});

		JButton btnSaveAndPlay = new JButton("Save and Play");
		btnSaveAndPlay.setBounds(97, 32, 133, 23);
		editPanel.add(btnSaveAndPlay);
		btnSaveAndPlay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!saveLevel(textField.getText())) {
					setEditMode(false);
				}
			}
		});
		
		JComboBox musicSelect = new JComboBox();
		musicSelect.setModel(new DefaultComboBoxModel(new String[] {"No Music", "Hyrule Castle", "Dungeon", "Dungeon NES"}));
		musicSelect.setBounds(240, 33, 113, 20);
		editPanel.add(musicSelect);
		musicSelect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selection = ((JComboBox) e.getSource()).getModel().getSelectedItem().toString();
				if (selection.equals("No Music")) {
					musicSelected = 0;
				} else if (selection.equals("Hyrule Castle")) {
					musicSelected = 1;
				} else if (selection.equals("Dungeon")) {
					musicSelected = 2;
				} else if (selection.equals("Dungeon NES")) {
					musicSelected = 3;
				}
			}
		});
		
				//Create a 3d canvas.
				canvas3d = new Canvas3D(SimpleUniverse.getPreferredConfiguration());
				springLayout.putConstraint(SpringLayout.NORTH, textField, 11, SpringLayout.SOUTH, canvas3d);
				springLayout.putConstraint(SpringLayout.NORTH, canvas3d, 0, SpringLayout.NORTH, this);
				springLayout.putConstraint(SpringLayout.WEST, canvas3d, 0, SpringLayout.WEST, this);
				springLayout.putConstraint(SpringLayout.SOUTH, canvas3d, -60, SpringLayout.SOUTH, this);
				springLayout.putConstraint(SpringLayout.EAST, canvas3d, 0, SpringLayout.EAST, this);
				world = new SimpleUniverse(canvas3d);
				this.add(canvas3d);
				
				
						//Handle the player/editor movement
						canvas3d.addKeyListener(new KeyListener() {
							@Override
							public void keyTyped(KeyEvent arg0) {}
							@Override
							public void keyReleased(KeyEvent arg0) {
								if (editMode) editor.respondToKeyRelease(arg0.getKeyCode());
								else player.respondToKeyRelease(arg0.getKeyCode());
							}
							@Override
							public void keyPressed(KeyEvent arg0) {
								if (editMode) editor.respondToKeyPress(arg0.getKeyCode());
								else player.respondToKeyPress(arg0.getKeyCode());
							}
						});
						
								//Handle the editor placing
								canvas3d.addMouseMotionListener(new MouseMotionListener() {
									public void mouseMoved(MouseEvent e) {moveItemToPlace(e);}
									public void mouseDragged(MouseEvent e) {
										if (placeMode == PLACE_MODE_DELETE) {
											placeItemToPlace(e);
										} else {
											if (moveItemToPlace(e)) {
												placeItemToPlace(e);
											}
										}
									}
								});
								
										canvas3d.addMouseListener(new MouseListener() {
											public void mouseReleased(MouseEvent e) {}
											public void mousePressed(MouseEvent e) {
												//placeItemToPlace(e);
											}
											public void mouseExited(MouseEvent e) {}
											public void mouseEntered(MouseEvent e) {}
											public void mouseClicked(MouseEvent e) {
												placeItemToPlace(e);
											}
										});

		JButton btnNew = new JButton("New");
		springLayout.putConstraint(SpringLayout.NORTH, btnNew, -23, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.SOUTH, btnNew, 0, SpringLayout.SOUTH, this);
		springLayout.putConstraint(SpringLayout.WEST, btnNew, 12, SpringLayout.WEST, this);
		springLayout.putConstraint(SpringLayout.EAST, btnNew, 77, SpringLayout.WEST, this);
		add(btnNew);
		btnNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				createSampleRoom();
				textField.setText("new");
				setEditMode(true);
			}
		});

	}

	
	
	/**Creates a basic room, with 4 walls and a floor. Useful to create a
	 * "default" level when the game is loaded, or a "blank slate" is desired.
	 */
	private void createSampleRoom() {
		walls.clear();
		floors.clear();
		monsters.clear();
		levelGroup.detach();
		levelGroup.removeAllChildren();
		player = new Player(levelGroup, world.getViewingPlatform().getViewPlatformTransform(),walls,monsters,soundEffects, 32*7+8, 32*4+8, 64);
		for (int i=0; i<=32*13; i += 32) {
			for (int j = 0; j <= 32*8; j += 32) {
				if (i == 0 || i == 32*13 || j == 0 || j == 32*8) {
					Wall wall = new Wall(levelGroup,i,j,32,32,wallTextures[1],0);
					walls.add(wall);
				} else {
					Floor floor = new Floor(levelGroup,i,j,32,32,wallTextures[4], 0);
					floors.add(floor);
				}
			}
		}
		System.out.println(floors.size());
		world.addBranchGraph(levelGroup);
		musicSelected = 1;
	}

	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
	}
	
	
	
	
	

	/**Set whether the game is in "level editing" mode or "Playing Mode"
	 * @param editOrNot Whether or not the game should be in edit mode.
	 */
	public void setEditMode(boolean editOrNot) {
		for (int i = 0; i < music.length; i++) {
			if (music[i] != null) {
				music[i].stop();
			}
		}
		canvas3d.requestFocus();
		//Prevents error like text boxes showing up underneath the game screen.
		this.revalidate();
		
		if (editOrNot) {
			editor = new EditingCamera(levelGroup, world.getViewingPlatform().getViewPlatformTransform(), 64, 64);
			setItemToPlace();
			editPanel.setVisible(true);
		} else {
			cancelItemToPlace();
			editPanel.setVisible(false);
			if (musicSelected >= 1) {
				if (music[musicSelected-1] != null) {
					music[musicSelected-1].setFramePosition(0);
					music[musicSelected-1].loop(Clip.LOOP_CONTINUOUSLY);
				}
			}
		}
		editMode = editOrNot;
	}

	
	
	
	/**Sets which item will be placed next based on the selection of the text box.
	 * 
	 */
	public void setItemToPlace() {
		//Cancel the previous item immediately to prevent ghosts
		cancelItemToPlace();
		String selectedItem = comboBox.getModel().getSelectedItem().toString();
		//"Delete Mode", "Player", "Wall Brick", "Wall Block", "Wall Rock", "Wall Tile", "Floor Dirt", "Floor Tile"
		String firstPartOfItem = selectedItem.split(" ")[0];
		if (firstPartOfItem.equals("Wall")) {
			placeMode = PLACE_MODE_WALL;
			int textureIndex = 0;
			if (selectedItem.equals("Wall Brick")) textureIndex = 1;
			else if (selectedItem.equals("Wall Block")) textureIndex = 0;
			else if (selectedItem.equals("Wall Dirt")) textureIndex = 2;
			else if (selectedItem.equals("Wall Rock")) textureIndex = 3;
			else if (selectedItem.equals("Wall Tile")) textureIndex = 4;
			else if (selectedItem.equals("Wall Statue 1")) textureIndex = 7;
			else if (selectedItem.equals("Wall Statue 2")) textureIndex = 8;
			else if (selectedItem.equals("Wall Water")) textureIndex = 9;
			wallToPlace = new Wall(levelGroup, 0, 0, 32, 32, wallTextures[textureIndex],0);
		} else if (firstPartOfItem.equals("Door")) {
			placeMode = PLACE_MODE_WALL;
			if (selectedItem.equals("Door")) wallToPlace = new Wall(levelGroup, 0, 0, 32, 32, wallTextures[5],1);
			else if (selectedItem.equals("Door Horiz")) wallToPlace = new Wall(levelGroup, 0, 0, 64, 32, wallTextures[6],1);
			else if (selectedItem.equals("Door Vert")) wallToPlace = new Wall(levelGroup, 0, 0, 32, 64, wallTextures[6],1);
		} else if (firstPartOfItem.equals("Water")) {
			placeMode = PLACE_MODE_WALL;
			wallToPlace = new Wall(levelGroup, 0, 0, 32, 32, wallTextures[9],-1);
		} else if (firstPartOfItem.equals("Key")) {
			placeMode = PLACE_MODE_KEY;
			wallToPlace = new Wall(levelGroup, 0, 0, 16, 16, wallTextures[0],30);
		} else if (firstPartOfItem.equals("Delete")) {
			placeMode = PLACE_MODE_DELETE;
		} else if (firstPartOfItem.equals("Player")) {
			placeMode = PLACE_MODE_PLAYER;
		} else if (firstPartOfItem.equals("Floor")) {
			placeMode = PLACE_MODE_FLOOR;
			int textureIndex = 0;
			if (selectedItem.equals("Floor Brick")) textureIndex = 1;
			else if (selectedItem.equals("Floor Block")) textureIndex = 0;
			else if (selectedItem.equals("Floor Dirt")) textureIndex = 2;
			else if (selectedItem.equals("Floor Rock")) textureIndex = 3;
			else if (selectedItem.equals("Floor Tile")) textureIndex = 4;
			floorToPlace = new Floor(levelGroup, 0, 0, 32, 32, wallTextures[textureIndex], 0);
		} else if (firstPartOfItem.equals("Ceiling")) {
			placeMode = PLACE_MODE_CEILING;
			int textureIndex = 0;
			if (selectedItem.equals("Ceiling Brick")) textureIndex = 1;
			else if (selectedItem.equals("Ceiling Block")) textureIndex = 0;
			else if (selectedItem.equals("Ceiling Dirt")) textureIndex = 2;
			else if (selectedItem.equals("Ceiling Rock")) textureIndex = 3;
			else if (selectedItem.equals("Ceiling Tile")) textureIndex = 4;
			floorToPlace = new Floor(levelGroup, 0, 0, 32, 32, wallTextures[textureIndex], 2);
		} else if (firstPartOfItem.equals("Monster")) {
			placeMode = PLACE_MODE_MONSTER;
			int xspeed = 0;
			int yspeed = 0;
			Color color = null;
			if (selectedItem.equals("Monster Horiz")) {xspeed = 1; color = new Color(128, 32, 32);}
			else if (selectedItem.equals("Monster Vert")) {yspeed = 1; color = new Color(128, 32, 32);}
			else if (selectedItem.equals("Monster Fast Horiz")) {xspeed = 3; color = new Color(32, 32, 128);}
			else if (selectedItem.equals("Monster Fast Vert")) {yspeed = 3; color = new Color(32, 32, 128);}
			monsterToPlace = new Monster(levelGroup, 0, 0, xspeed, yspeed, walls, color);
		}
		canvas3d.requestFocus();
	}

	
	
	
	/**Moves whatever item is currently being placed. Used when the mouse is moved
	 * and it now hovers over another grid square.
	 * @param e The MouseEvent to get mouse coordinates from.
	 * @return Whether the cursor has actually moved enough to warrant moving the item.
	 */
	public boolean moveItemToPlace(MouseEvent e) {
		if (editMode) {
			int cursorXt = getCursorX(e),
					//cursorYt = (int) (Math.floor(((e.getY()*480/canvas3d.getHeight()-240)*640/canvas3d.getWidth()+editor.y)/32f)*32);
					cursorYt = getCursorY(e);
			if (cursorX == cursorXt && cursorY == cursorYt) return false;//To stop it from doing too much
			cursorX = cursorXt; cursorY = cursorYt;
			switch (placeMode) {
			case PLACE_MODE_WALL: if (wallToPlace != null) wallToPlace.move(cursorXt, cursorYt); break;
			case PLACE_MODE_FLOOR: if (floorToPlace != null) floorToPlace.move(cursorXt, cursorYt); break;
			case PLACE_MODE_CEILING: if (floorToPlace != null) floorToPlace.move(cursorXt, cursorYt); break;
			case PLACE_MODE_MONSTER: if (monsterToPlace != null) monsterToPlace.move(cursorXt+8, cursorYt+8); break;
			case PLACE_MODE_KEY: if (wallToPlace != null) wallToPlace.move(cursorXt+8, cursorYt+8); break;
			}
		}
		return true;
	}
	
	
	int getCursorX(MouseEvent e) {
		return (int) (Math.floor((e.getX()*640/canvas3d.getWidth()+editor.x-320)/32f)*32);
	}
	int getCursorY(MouseEvent e) {
		return (int) (Math.floor(((e.getY()*640/canvas3d.getWidth())+editor.y-640f/canvas3d.getWidth()*canvas3d.getHeight()/2)/32f)*32);
	}

	
	
	
	
	
	
	/**Places the item that was going to be placed, at the location of the mouse cursor.
	 * @param e The MouseEvent to get mouse coordinates from.
	 */
	public void placeItemToPlace(MouseEvent e) {
		if (editMode) {
			if (placeMode == PLACE_MODE_WALL) {
				//Place a wall
				deleteItems(e, DELETE_WALLS + DELETE_FLOORS + DELETE_MONSTERS);
				walls.add(wallToPlace);
				wallToPlace = null;
				placeMode = PLACE_MODE_NONE;
				//Prepare another wall
				setItemToPlace();
			} else 	if (placeMode == PLACE_MODE_KEY) {
				//Place a ley (which is saved as a wall)
				deleteItems(e, DELETE_WALLS + DELETE_MONSTERS);
				walls.add(wallToPlace);
				wallToPlace = null;
				placeMode = PLACE_MODE_NONE;
				//Prepare another wall
				setItemToPlace();
			} else if (placeMode == PLACE_MODE_FLOOR) {
				//Place a floor
				deleteItems(e, DELETE_WALLS + DELETE_FLOORS);
				floors.add(floorToPlace);
				floorToPlace = null;
				placeMode = PLACE_MODE_NONE;
				//Prepare another floor
				setItemToPlace();
			} else if (placeMode == PLACE_MODE_CEILING) {
				//Place a floor
				deleteItems(e, DELETE_CEILINGS);
				floors.add(floorToPlace);
				floorToPlace = null;
				placeMode = PLACE_MODE_NONE;
				//Prepare another ceiling
				setItemToPlace();
			} else if (placeMode == PLACE_MODE_MONSTER) {
				//Place a monster
				deleteItems(e, DELETE_WALLS + DELETE_MONSTERS);
				monsters.add(monsterToPlace);
				monsterToPlace = null;
				placeMode = PLACE_MODE_NONE;
				//Prepare another monster
				setItemToPlace();
			} else if (placeMode == PLACE_MODE_DELETE) {
				//Delete Things
				deleteItems(e, DELETE_WALLS + DELETE_FLOORS + DELETE_CEILINGS + DELETE_MONSTERS);
			} else if (placeMode == PLACE_MODE_PLAYER) {
				//Move the player
				player.move(cursorX+8, cursorY+8);
			}
		}
	}
	
	
	
	
	
	
	static final int DELETE_WALLS = 1;
	static final int DELETE_FLOORS = 2;
	static final int DELETE_CEILINGS = 4;
	static final int DELETE_MONSTERS = 8;
	
	/**Deletes an item under the mouse cursor.
	 * @param e The MouseEvent to get the position of the mouse cursor from.
	 * @param bitMaskOfWhatToDelete Determines whether to delete walls, floors, ceilings, and/or monsters.
	 */
	public void deleteItems(MouseEvent e, int bitMaskOfWhatToDelete) {
		if ((bitMaskOfWhatToDelete & DELETE_WALLS) > 0) {
			for (Wall wall : walls) {
				if (wall.intersects(getCursorX(e), getCursorY(e), 2, 2)) {
					wall.unload();
					walls.remove(wall);
					break;
				}
			}
		}
		if ((bitMaskOfWhatToDelete & DELETE_FLOORS) > 0) {
			for (Floor floor : floors) {
				if (floor.intersects(getCursorX(e), getCursorY(e), 2, 2) && floor.type == Floor.TYPE_NORMAL) {
					floor.unload();
					floors.remove(floor);
					break;
				}
			}
		}
		if ((bitMaskOfWhatToDelete & DELETE_CEILINGS) > 0) {
			for (Floor floor : floors) {
				if (floor.intersects(getCursorX(e), getCursorY(e), 2, 2) && floor.type == Floor.TYPE_CEILING) {
					floor.unload();
					floors.remove(floor);
					break;
				}
			}
		}
		if ((bitMaskOfWhatToDelete & DELETE_MONSTERS) > 0) {
			for (Monster monster : monsters) {
				if (monster.intersects(getCursorX(e)+16, getCursorY(e)+16, 2, 2)) {
					monster.unload();
					monsters.remove(monster);
					break;
				}
			}
		}
	}

	
	
	
	
	/**Call this when no longer placing a particular item, so that
	 * the item is unloaded properly (and doesn't leave behind any ghosts),
	 * and so everything is cleaned up.
	 */
	public void cancelItemToPlace() {
		if (editMode) {
			if (wallToPlace != null) {
				wallToPlace.unload(); wallToPlace = null;}
			if (floorToPlace != null) {
				floorToPlace.unload(); floorToPlace = null;
			}
			if (monsterToPlace != null) {
				monsterToPlace.unload(); monsterToPlace = null;
			}
			//placeMode = 0;
		}
	}

	
	
	/**Loads a level from a file.
	 * @param fileName The name of the level to load, without an extension.
	 * @return
	 */
	public boolean loadLevel(String fileName) {
		levelGroup.detach();
		levelGroup.removeAllChildren();
		boolean error = false;
		if (fileName.isEmpty()) {
			error = true;
			return error;
		}
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(fileName+".bin", "r");
			walls.clear();
			floors.clear();
			monsters.clear();
			player = new Player(levelGroup, world.getViewingPlatform().getViewPlatformTransform(), walls,monsters,soundEffects, file.readInt(), file.readInt(), 64);
			file.readInt();//In case a Z value is added.
			musicSelected = file.readInt();
			file.seek(256);
			while (file.getFilePointer() < file.length()) {
				char type = (char) file.readChar();
				if (type == 'w') {
					walls.add(new Wall(levelGroup, wallTextures, file,'w'));
				} else if (type == 'd') {
					walls.add(new Wall(levelGroup, wallTextures, file,'d'));
				} else if (type == 'k') {
					walls.add(new Wall(levelGroup, wallTextures, file,'k'));
				} else if (type == 'W') {
					walls.add(new Wall(levelGroup, wallTextures, file,'W'));
				} else if (type == 'f') {
					floors.add(new Floor(levelGroup, wallTextures, file, 'f'));
				} else if (type == 'r') {
					floors.add(new Floor(levelGroup, wallTextures, file, 'r'));
				} else if (type == 'm') {
					monsters.add(new Monster(levelGroup, wallTextures, file, walls));
				}
			}
		} catch (IOException e) {
			error = true;
			System.out.println(e.toString());
		} finally {
			try {file.close();} catch (Exception e) {}
		}
		world.addBranchGraph(levelGroup);
		return error;
	}

	
	
	/**Save a level to a file.
	 * @param fileName The name of the level to save, without an extension.
	 * @return Whether or not there was an error.
	 */
	public boolean saveLevel(String fileName) {
		boolean error = false;
		if (fileName.isEmpty()) {
			error = true;
			return error;
		}
		RandomAccessFile file = null;
		try {
			file = new RandomAccessFile(fileName+".bin", "rw");
			file.setLength(2);
			file.writeInt(player.x);
			file.writeInt(player.y);
			file.writeInt(0);//In case a z value is added
			file.writeInt(musicSelected);
			file.seek(256);
			for (Wall wall : walls) {
				wall.saveToBinary(file);
			}
			for (Floor floor : floors) {
				floor.saveToBinary(file);
			}
			for (Monster monster : monsters) {
				monster.saveToBinary(file);
			}
		} catch (IOException e) {
			error = true;
			System.out.println(e.toString());
		} finally {
			try {file.close();} catch (Exception e) {}
		}
		return error;
	}
}
