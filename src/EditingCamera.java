import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;

public class EditingCamera extends Rectangle {
	public BranchGroup levelGroup;
	
	//Move the camera
	TransformGroup cameraTransformGroup;
	Transform3D cameraTramsform;
	double rotationAngle = 0;
	double cameraDistance = 24;
	
	int xspeed;
	int yspeed;
	int maxSpeed = 5;
	ArrayList<Wall> walls;
	
	public EditingCamera(BranchGroup levelGroup, TransformGroup cameraTransformGroup, int x, int y) {
		super(x, y, 16, 16);
		this.levelGroup = levelGroup;
		
		//Set up the camera.
		this.cameraTransformGroup = cameraTransformGroup;
		cameraTramsform = new Transform3D();
		cameraTramsform.rotX(rotationAngle);
		cameraTramsform.setTranslation(new Vector3d((x+8)/32.0,-cameraDistance*Math.sin(rotationAngle)-(y+8)/32.0,cameraDistance*Math.cos(rotationAngle)));
		cameraTransformGroup.setTransform(cameraTramsform);
		
		xspeed = 0;
		yspeed = 0;
	}
	
	public void update() {
		//Movement without collision Checking
		x += xspeed;
		y += yspeed;
		
		//Move the camera
		cameraTramsform.rotX(rotationAngle);
		cameraTramsform.setTranslation(new Vector3d((x+8)/32.0,-cameraDistance*Math.sin(rotationAngle)-(y+8)/32.0,cameraDistance*Math.cos(rotationAngle)));
		cameraTransformGroup.setTransform(cameraTramsform);
	}
	
	public void respondToKeyPress(int keyCode) {
		switch (keyCode) {
		case KeyEvent.VK_LEFT: xspeed = -maxSpeed; break;
		case KeyEvent.VK_RIGHT: xspeed = maxSpeed; break;
		case KeyEvent.VK_UP: yspeed = -maxSpeed; break;
		case KeyEvent.VK_DOWN: yspeed = maxSpeed; break;
		}
	}
	
	public void respondToKeyRelease(int keyCode) {
		switch (keyCode) {
		case KeyEvent.VK_LEFT: if (xspeed == -maxSpeed) xspeed = 0; break;
		case KeyEvent.VK_RIGHT: if (xspeed == maxSpeed) xspeed = 0; break;
		case KeyEvent.VK_UP: if (yspeed == -maxSpeed) yspeed = 0; break;
		case KeyEvent.VK_DOWN: if (yspeed == maxSpeed) yspeed = 0; break;
		}
	}
}
