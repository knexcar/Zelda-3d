import java.awt.Color;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Cylinder;
import com.sun.j3d.utils.geometry.Primitive;

public class Monster extends Rectangle {
	//Texture color;
	BranchGroup levelGroup;
	//The 3d object that is actually drawn
	//Box drawingObject;
	BranchGroup drawingBranchGroup;
	TransformGroup drawingTransformGroup;
	Transform3D transformObject;
	ArrayList<Wall> walls;
	Color color;
	
	int xspeed;
	int yspeed;
	
	public Monster(BranchGroup levelGroup, int x, int y, int xspeed, int yspeed, ArrayList<Wall> walls, Color color) {
		super(x, y, 16, 16);
		this.walls = walls;
		setPropertiesForBasicWall(levelGroup,x,y,xspeed,yspeed, color);
	}
	
	private void setPropertiesForBasicWall(BranchGroup levelGroup, int x, int y, int xspeed, int yspeed, Color color) {
		this.levelGroup = levelGroup;
		this.xspeed = xspeed;
		this.yspeed = yspeed;
		width = 16;
		height = 16;
		//All 3d objects must be in a "Transform Group"
		//the "Transform Group" contains a "transform" and the objecst.
		drawingTransformGroup = new TransformGroup();
		drawingTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformObject = new Transform3D();
		//The visual objects are 1/32 the size.
		transformObject.rotX(Math.PI/2);
		transformObject.setTranslation(new Vector3d(x/32.0+width/64.0, -y/32.0-height/64.0, 0));
		drawingTransformGroup.setTransform(transformObject);
		Appearance realisticColor = new Appearance();
		//realisticColor.setColoringAttributes(new ColoringAttributes(new Color3f(1, 1, 1), ColoringAttributes.FASTEST));
		this.color = color;
		realisticColor.setColoringAttributes(new ColoringAttributes(new Color3f(color), ColoringAttributes.FASTEST));
		//Create the drawing object, add it to the "Transform Group"
		Cylinder drawingObject = new Cylinder(0.25f, 0.5f,Primitive.GENERATE_NORMALS+Primitive.GENERATE_TEXTURE_COORDS, realisticColor);
		drawingTransformGroup.addChild(drawingObject);
		//put the transform into the listy thing.
		drawingBranchGroup = new BranchGroup();
		drawingBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		drawingBranchGroup.addChild(drawingTransformGroup);
		levelGroup.addChild(drawingBranchGroup);
	}
	
	public void saveToBinary(RandomAccessFile file) throws IOException {
		file.writeChar('m');
		file.writeInt(x);
		file.writeInt(y);
		file.writeInt(xspeed);
		file.writeInt(yspeed);
		file.writeInt(color.getRGB());
		//file.writeByte(Integer.parseInt(color.getName()));
	}
	
	public Monster(BranchGroup levelGroup, Texture[] textureArray, RandomAccessFile file, ArrayList<Wall> walls) throws IOException {
		this.walls = walls;
		x = file.readInt();
		y = file.readInt();
		xspeed = file.readInt();
		yspeed = file.readInt();
		color = new Color(file.readInt());
		setPropertiesForBasicWall(levelGroup,x,y,xspeed,yspeed, color);
	}
	
	public void update() {
		//Movement with ollision Checking
		//System.out.println(x+" "+xspeed);
		x += xspeed;
		for (Wall wall : walls) {
			if (wall.intersects(this)) {
				xspeed *= -1;
				break;
			}
		}
		y += yspeed;
		for (Wall wall : walls) {
			if (wall.intersects(this)) {
				yspeed *= -1;
				break;
			}
		}

		//Move the drawing object
		transformObject.rotX(Math.PI/2);
		transformObject.setTranslation(new Vector3d(x/32.0+width/64.0, -y/32.0-height/64.0, 0));
		drawingTransformGroup.setTransform(transformObject);
	}
	
	public void move(int x, int y) {
		this.x = x; this.y = y;
		transformObject.rotX(Math.PI/2);
		transformObject.setTranslation(new Vector3d(x/32.0+width/64.0, -y/32.0-height/64.0, 0));
		drawingTransformGroup.setTransform(transformObject);
	}

	public void unload() {
		drawingBranchGroup.detach();
		drawingBranchGroup.removeAllChildren();
	}
}
