import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.PointLight;
import javax.media.j3d.SpotLight;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.View;
import javax.sound.sampled.Clip;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;

public class Player extends Rectangle {
	public BranchGroup levelGroup;
	
	//The actual object
	Cylinder drawingObject;
	BranchGroup branchGroup;
	TransformGroup drawingTransformGroup;
	Transform3D transformObject;
	
	//Move the camera
	TransformGroup cameraTransformGroup;
	Transform3D cameraTramsform;
	double rotationAngle = 0.5;
	double cameraDis = 11;
	
	//Lighting
	SpotLight light;
	
	int xspeed;
	int yspeed;
	int maxSpeed = 3;
	int direction;
	ArrayList<Wall> walls;
	ArrayList<Monster> monsters;
	Clip[] soundEffects;
	int keys;

	private Sword sword;
	private int swordTimer;
	
	public Player(BranchGroup levelGroup, TransformGroup cameraTransformGroup, ArrayList<Wall> walls, ArrayList<Monster> monsters, Clip[] soundEffects, int x, int y, int lightRange) {
		super(x, y, 16, 16);
		this.levelGroup = levelGroup;
		this.walls = walls;
		this.monsters = monsters;
		this.soundEffects = soundEffects;
		sword = null;
		swordTimer = 0;
		direction = 3;
		keys = 0;
		//Set up the drawing object
		branchGroup = new BranchGroup();
		drawingTransformGroup = new TransformGroup();
		drawingTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformObject = new Transform3D();
		transformObject.rotX(Math.PI/2);
		transformObject.setTranslation(new Vector3d(x/32.0+width/64.0, -y/32.0-height/64.0, 0));
		drawingTransformGroup.setTransform(transformObject);
		//Create the drawing object, add it to the "Transform Group"
		//drawingObject = new ColorCube(0.25f);
		Appearance realisticColor = new Appearance();
		realisticColor.setColoringAttributes(new ColoringAttributes(0, 0.75f, 0, ColoringAttributes.FASTEST));
		drawingObject = new Cylinder(0.25f, 0.5f,Primitive.GENERATE_NORMALS+Primitive.GENERATE_TEXTURE_COORDS, realisticColor);
		drawingTransformGroup.addChild(drawingObject);
		
		/*light = new SpotLight(new Color3f(0.5f, 0.5f, 0.5f), new Point3f(), new Point3f(), new Vector3f(), 1, 1);
		light.setEnable(true);
		light.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), lightRange/32));
		light.setCapability(SpotLight.ALLOW_POSITION_WRITE);
		light.setPosition(x, y, 0);
		//branchGroup.addChild(light);*/
		branchGroup.addChild(drawingTransformGroup);
		
		//put the transform into the thingy.
		levelGroup.addChild(branchGroup);
		//levelGroup.addChild(light);
		
		//Set up the camera.
		this.cameraTransformGroup = cameraTransformGroup;
		cameraTramsform = new Transform3D();
		cameraTramsform.rotX(rotationAngle);
		cameraTramsform.setTranslation(new Vector3d((x+8)/32.0,-cameraDis*Math.sin(rotationAngle)-(y+8)/32.0,cameraDis*Math.cos(rotationAngle)));
		cameraTransformGroup.setTransform(cameraTramsform);
		
		xspeed = 0;
		yspeed = 0;
	}
	
	public void update() {
		//Movement with collision Checking
		x += xspeed;
		if (doThatCollisionChecking()) x -= xspeed;
		y += yspeed;
		if (doThatCollisionChecking()) y -= yspeed;

		//Move the drawing object
		transformObject.rotX(Math.PI/2);
		transformObject.setTranslation(new Vector3d(x/32.0+width/64.0, -y/32.0-height/64.0, 0));
		drawingTransformGroup.setTransform(transformObject);
		
		//Move the camera
		cameraTramsform.rotX(rotationAngle);
		cameraTramsform.setTranslation(new Vector3d((x+8)/32.0,-cameraDis*Math.sin(rotationAngle)-(y+8)/32.0,cameraDis*Math.cos(rotationAngle)));
		cameraTransformGroup.setTransform(cameraTramsform);
		
		//Move the light
		//light.setPosition(x, y, 0);
		
		//Sword stuff
		if (sword != null) {
			swordTimer--;
			sword.move(x, y);
			for (Monster monster : monsters) {
				if (sword.intersects(monster)) {
					monster.unload();
					monsters.remove(monster);
					soundEffects[4].setFramePosition(0);
					soundEffects[4].start();
					break;
				}
			}
			if (swordTimer == 0) {
				sword.unload();
				sword = null;
			}
		}
	}
	
	private boolean doThatCollisionChecking() {
		for (Wall wall : walls) {
			if (wall.intersects(this)) {
				if (wall.type <= 0 || (wall.type == 1 && keys == 0)) {
					return true;
				} else if (wall.type == 30) {
					keys++;
					wall.unload();
					walls.remove(wall);
					soundEffects[1].setFramePosition(0);
					soundEffects[1].start();
					return false;
				} else if ((wall.type == 1 && keys > 0)) {
					keys--;
					wall.unload();
					walls.remove(wall);
					soundEffects[2].setFramePosition(0);
					soundEffects[2].start();
					return false;
				}
			}
		}
		return false;
	}
	
	public void respondToKeyPress(int keyCode) {
		switch (keyCode) {
		case KeyEvent.VK_LEFT: xspeed = -maxSpeed; direction = 2; break;
		case KeyEvent.VK_RIGHT: xspeed = maxSpeed; direction = 0; break;
		case KeyEvent.VK_UP: yspeed = -maxSpeed; direction = 1; break;
		case KeyEvent.VK_DOWN: yspeed = maxSpeed; direction = 3; break;
		case KeyEvent.VK_SPACE: if (sword == null) {
			sword = new Sword(levelGroup, direction, x, y); soundEffects[3].setFramePosition(0); soundEffects[3].start(); swordTimer = 10;
			} break;
		}
	}
	
	public void respondToKeyRelease(int keyCode) {
		switch (keyCode) {
		case KeyEvent.VK_LEFT: if (xspeed == -maxSpeed) xspeed = 0; if (direction == 2) reCalcDirection(); break;
		case KeyEvent.VK_RIGHT: if (xspeed == maxSpeed) xspeed = 0; if (direction == 0) reCalcDirection(); break;
		case KeyEvent.VK_UP: if (yspeed == -maxSpeed) yspeed = 0; if (direction == 1) reCalcDirection(); break;
		case KeyEvent.VK_DOWN: if (yspeed == maxSpeed) yspeed = 0; if (direction == 3) reCalcDirection(); break;
		}
	}
	
	public void reCalcDirection() {
		if (xspeed == -maxSpeed) direction = 2; 
		else if (xspeed == maxSpeed) direction = 0; 
		else if (yspeed == -maxSpeed) direction = 1; 
		else if (yspeed == maxSpeed) direction = 3;
	}
	
	public void move(int x, int y) {
		this.x = x; this.y = y;
		transformObject.rotX(Math.PI/2);
		transformObject.setTranslation(new Vector3d(x/32.0+width/64.0, -y/32.0-height/64.0, 0));
		//transformObject.setScale(new Vector3d(width/32.0, height/32.0, 1));
		drawingTransformGroup.setTransform(transformObject);
		//light.setPosition(x, y, 0);
	}
	
	
	public class Sword extends Rectangle {
		//The actual object
		Box drawingObject;
		BranchGroup branchGroup;
		TransformGroup drawingTransformGroup;
		Transform3D transformObject;
		
		int xoffset, yoffset;
		int timer;
		
		public Sword(BranchGroup levelGroup, int direction , int x, int y) {
			super(x, y, 16, 16);
			int s0 = 16, s1 = 4;
			if (direction == 0) {
				width = s0; height = s1;
				xoffset = 16; yoffset = 8-s1/2;
			} else if (direction == 1) {
				width = s1; height = s0;
				xoffset = 8-s1/2; yoffset = -s0;
			} else if (direction == 2) {
				width = s0; height = s1;
				xoffset = -s0; yoffset = 8-s1/2;
			} else if (direction == 3) {
				width = s1; height = s0;
				xoffset = 8-s1/2; yoffset = 16;
			}
			this.x = x+xoffset;
			this.y = y+yoffset;
			//Set up the drawing object
			branchGroup = new BranchGroup();
			branchGroup.setCapability(BranchGroup.ALLOW_DETACH);
			drawingTransformGroup = new TransformGroup();
			drawingTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
			transformObject = new Transform3D();
			transformObject.setTranslation(new Vector3d(this.x/32.0+width/64.0, -this.y/32.0-height/64.0, 0));
			transformObject.setScale(new Vector3d(width/32.0, height/32.0, 1));
			drawingTransformGroup.setTransform(transformObject);
			//Create the drawing object, add it to the "Transform Group"
			//drawingObject = new ColorCube(0.25f);
			Appearance realisticColor = new Appearance();
			realisticColor.setColoringAttributes(new ColoringAttributes(0.25f, 0.25f, 0, ColoringAttributes.FASTEST));
			drawingObject = new Box(0.5f, 0.5f, 0.1f, Primitive.GENERATE_NORMALS+Primitive.GENERATE_TEXTURE_COORDS, realisticColor);
			drawingTransformGroup.addChild(drawingObject);
			branchGroup.addChild(drawingTransformGroup);
			
			//put the transform into the thingy.
			levelGroup.addChild(branchGroup);
		}
		
		public void move(int x, int y) {
			this.x = x+xoffset; this.y = y+yoffset;
			transformObject.setTranslation(new Vector3d(this.x/32.0+width/64.0, -this.y/32.0-height/64.0, 0));
			transformObject.setScale(new Vector3d(width/32.0, height/32.0, 1));
			drawingTransformGroup.setTransform(transformObject);
		}

		public void unload() {
			branchGroup.detach();
			branchGroup.removeAllChildren();
		}
	}
}
