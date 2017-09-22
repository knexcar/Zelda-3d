import java.awt.Rectangle;
import java.io.IOException;
import java.io.RandomAccessFile;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.Texture;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Vector3d;

import com.sun.j3d.utils.geometry.Box;
import com.sun.j3d.utils.geometry.Primitive;

public class Floor extends Rectangle {
	Texture color;
	BranchGroup levelGroup;
	//The 3d object that is actually drawn
	//Box drawingObject;
	BranchGroup drawingBranchGroup;
	TransformGroup drawingTransformGroup;
	Transform3D transformObject;
	int type;//0 = normal, 1 = light, 2 = roof
	
	public Floor(BranchGroup levelGroup, int x, int y, int width, int height, Texture color, int type) {
		super(x, y, width, height);
		setPropertiesForBasicWall(levelGroup,x,y,width,height,color,type);
	}
	
	private void setPropertiesForBasicWall(BranchGroup levelGroup, int x, int y, int width, int height, Texture color, int type) {
		this.levelGroup = levelGroup;
		this.type = type;
		//All 3d objects must be in a "Transform Group"
		//the "Transform Group" contains a "transform" and the objecst.
		drawingTransformGroup = new TransformGroup();
		drawingTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
		transformObject = new Transform3D();
		Box drawingObject = null;
		//The visual objects are 1/32 the size.
		//Prepare the color
		this.color = color;
		Appearance realisticColor = new Appearance();
		realisticColor.setColoringAttributes(new ColoringAttributes(new Color3f(1, 1, 1), ColoringAttributes.FASTEST));
		realisticColor.setTexture(color);
		if (type == 0) {
			transformObject.rotX(Math.PI/2);
			transformObject.setTranslation(new Vector3d(x/32.0+width/64.0, -y/32.0-height/64.0, -0.5));
			transformObject.setScale(new Vector3d(width/32.0, height/32.0, 1));
			drawingTransformGroup.setTransform(transformObject);
			//Create the drawing object, add it to the "Transform Group"
			drawingObject = new Box(0.5f, 0, 0.5f, Primitive.GENERATE_NORMALS+Primitive.GENERATE_TEXTURE_COORDS, realisticColor);
		} else if (type == 2) {
			transformObject.rotX(Math.PI/2);
			transformObject.setTranslation(new Vector3d(x/32.0+width/64.0, -y/32.0-height/64.0, 1));
			transformObject.setScale(new Vector3d(width/32.0, height/32.0, 1));
			drawingTransformGroup.setTransform(transformObject);
			//Create the drawing object, add it to the "Transform Group"
			drawingObject = new Box(0.5f, 0.5f, 0.5f, Primitive.GENERATE_NORMALS+Primitive.GENERATE_TEXTURE_COORDS, realisticColor);
		}
		drawingTransformGroup.addChild(drawingObject);
		//put the transform into the listy thing.
		drawingBranchGroup = new BranchGroup();
		drawingBranchGroup.setCapability(BranchGroup.ALLOW_DETACH);
		drawingBranchGroup.addChild(drawingTransformGroup);
		levelGroup.addChild(drawingBranchGroup);
	}
	
	public void saveToBinary(RandomAccessFile file) throws IOException {
		switch (type) {
		case 0:
			file.writeChar('f');
			break;
		case 2:
			file.writeChar('r');
			break;

		default:
			file.writeChar('f');
			break;
		}
		file.writeInt(x);
		file.writeInt(y);
		file.writeInt(width);
		file.writeInt(height);
		file.writeByte(Integer.parseInt(color.getName()));
	}
	
	public Floor(BranchGroup levelGroup, Texture[] textureArray, RandomAccessFile file, char type2) throws IOException {
		x = file.readInt();
		y = file.readInt();
		width = file.readInt();
		height = file.readInt();
		if (type2 == 'f')
		setPropertiesForBasicWall(levelGroup,x,y,width,height,textureArray[file.readByte()],0);
		else if (type2 == 'r')
		setPropertiesForBasicWall(levelGroup,x,y,width,height,textureArray[file.readByte()],2);
	}
	
	public void move(int x, int y) {
		this.x = x; this.y = y;
		transformObject.rotX(Math.PI/2);
		if (type == 0) transformObject.setTranslation(new Vector3d(x/32.0+width/64.0, -y/32.0-height/64.0, -0.5));
		else if (type == 2) transformObject.setTranslation(new Vector3d(x/32.0+width/64.0, -y/32.0-height/64.0, 1));
		transformObject.setScale(new Vector3d(width/32.0, height/32.0, 1));
		drawingTransformGroup.setTransform(transformObject);
	}

	public void unload() {
		drawingBranchGroup.detach();
		drawingBranchGroup.removeAllChildren();
	}
}