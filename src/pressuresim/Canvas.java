package pressuresim;


import org.jbox2d.common.Vec2;

import controlP5.Button;
import controlP5.ControlEvent;
import controlP5.ControlP5;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import shiffman.box2d.Box2DProcessing;

public class Canvas extends Component {

	static Box2DProcessing box2d;
	PApplet parent;
	Hapkit hapkit;
	
	double hapkitPos;
	
	Hand hand;
//	Boundary ceiling;
//	Boundary floor;
	
//	PImage wood_plank_img;
//	PImage next_img;
	
//	PImage piston_x;
//	PImage piston_y;
//	PImage piston_x_active;
//	PImage piston_y_active;
	
	Button next, X, Y;
	
//	int pistonx_img_x;
//	int pistonx_img_y;
//	
//	int pistony_img_x;
//	int pistony_img_y;	
	
//	int piston_img_w;
//	int piston_img_h;
	
	int numPistons;
	private Ruler ruler;
	ResearchData rData;
	
	Piston p1, p2, p3;
	PistonCollection pc;
	
	PImage dotted_line_img;
	boolean draw_dotted_line;
	
//	Button delete1;
//    Button delete2;
//	Button delete3;

	static int Y1, Y2, Y3, X_ALL;
	
	public Canvas(Main main, ControlP5 cp5, int _x, int _y, int _w, int _h, Hapkit _hapkit, ResearchData rData) {
		
		super(_x,_y,_w,_h);
		
		Y1 = y+100;
		Y2 = y+250;
		Y3 = y+380;
		X_ALL = this.x+100;
		
		this.hapkit = _hapkit;
		this.numPistons = 3;
		this.rData = rData;
		
//		piston_img_w = 231;
//		piston_img_h = 80;
		
//		pistonx_img_x = this.x+(this.w/4)-(piston_img_w/2);
//		pistonx_img_y = this.y+150;
//		
//		pistony_img_x = this.x+(3*(this.w/4))-(piston_img_w/2);
//		pistony_img_y = this.y+150;	
		
		parent = main; 
		
//		wood_plank_img = parent.loadImage("wood-plank.jpg");
//		next_img = parent.loadImage("arrow-next.png");
//		piston_x = parent.loadImage("pistonx.jpg");
//		piston_y = parent.loadImage("pistony.jpg");
//		piston_x_active = parent.loadImage("pistonx-active.jpg");
//		piston_y_active = parent.loadImage("pistony-active.jpg");
		
		box2d = new Box2DProcessing(parent);
		box2d.createWorld();
		box2d.setScaleFactor(400); // 500 pixels is 1 meter
		box2d.setGravity(0, 0);
		
		// This prevents dynamic bodies from sticking to static ones
		org.jbox2d.common.Settings.velocityThreshold = 0.2f;
		
		p1 = new Piston(this.x+100, Y1, 15, 300, "Piston A", this.parent, box2d, rData, "container_red.png");
		p2 = new Piston(this.x+100, Y2, 35, 300, "Piston B", this.parent, box2d, rData, "container_blue.png");
		p3 = new Piston(this.x+100, Y3, 55, 300, "Piston C", this.parent, box2d, rData, "container_yellow.png");
		
		pc = new PistonCollection(rData, hapkit);
		pc.add(p1);
		pc.add(p2);
		pc.add(p3);
		pc.setActive(p1);
		
		this.draw_dotted_line = true;
		this.dotted_line_img = parent.loadImage("dotted_line.png");
		
		if(rData.isHapkitMode()){
			rData.logEvent(-1, -1, "Initial K value sent to hapkit");
			//hapkit.setKConstant(sc.activePiston.getK());
		}
		
//		floor = new Boundary(this.x + this.w/2, this.h - 20, this.w - 20, 20, parent, box2d);
//		ceiling = new Boundary(this.x+10, this.y+30, this.w - 20, 30, parent, box2d);
		
		// Get ruler spacing from Box2d world.
		// Spacing is determined by the ruler height in pixels (300) divided by the spacing in pixels.
		Vec2 spacing1 = box2d.coordWorldToPixels(new Vec2(0, 0));
		Vec2 spacing2 = box2d.coordWorldToPixels(new Vec2(0, 1));
		float one_meter = spacing2.sub(spacing1).length();
		int spacing = (int) (one_meter/10);
		ruler = new Ruler(parent, cp5, this.x+165, this.y+440, (int) one_meter + 50, 30, spacing);
		
//		delete1 = cp5.addButton("Delete1")
//			     .setValue(0)
//			     .setPosition(x+77,y+25)
//			     .setSize(55,20)
//			     .setCaptionLabel("Delete")
//			     .setId(1);
//		
//		delete2 = cp5.addButton("Delete2")
//			     .setValue(1)
//			     .setPosition(x+220,y+25)
//			     .setSize(55,20)
//			     .setCaptionLabel("Delete")
//			     .setId(1);
//		
//		delete3 = cp5.addButton("Delete3")
//			     .setValue(2)
//			     .setPosition(x+370,y+25)
//			     .setSize(55,20)
//			     .setCaptionLabel("Delete")
//			     .setId(1);
		
		cp5.addListener(this);
		
	}
	
	public void step(){
		this.box2d.step();
		
		// Why is this here?
		if(rData.isHapkitMode()){
		  updatePistonPosition();
		  readHapkitPos();
		}
	}
	
	public void draw(){

		parent.fill(255);
		parent.stroke(0);
		parent.rect(x, y, w, h);
		parent.textSize(18); 
		parent.fill(0);
		
//		parent.pushMatrix();
//		parent.imageMode(PConstants.CORNER);
//		parent.image(wood_plank_img, this.x+10, this.y+50, this.w-20, 30);
//		parent.popMatrix();
		
		pc.draw();
//		floor.draw();
		
		// Draw neutral line
		parent.imageMode(PConstants.CENTER);
		// Hand-tweaked coordinates that put the neutral line at the exact right place (ugly)
		if (this.draw_dotted_line) {
			parent.image(dotted_line_img, this.x + p1.originalLen + 23, Y2-10);
		}
		
		ruler.draw();
		
//		parent.fill(255);
//		parent.stroke(0);
//		parent.strokeWeight(3);	
//		parent.line(this.x+100, Y1, this.x+100, Y3);
	}
	
	private void updatePistonPosition() {
		pc.updateActivePistonPosition(hapkitPos);
	}
	
	public void readHapkitPos() {
		hapkitPos = this.hapkit.getPos();
	}
	
	public void displayForces(boolean on) {
		this.pc.displayForces(on);
	}
	
	public void mousePressed() {
		pc.updateActivePiston(parent.mouseX, parent.mouseY, true, hapkit);
	}
	
	public void mouseReleased() {
		pc.updateActivePiston(parent.mouseX, parent.mouseY, false, hapkit);
	}
	
	public PistonCollection getPistonCollection() {
		return this.pc;
	}

	@Override
	public void controlEvent(ControlEvent event) {
//		System.out.println(event.getValue());
//		if(event.isFrom(delete1)){
//			pc.delete((int)event.getValue());
//		}else if(event.isFrom(delete2)){
//			pc.delete((int)event.getValue());
//		}else if(event.isFrom(delete3)){
//			pc.delete((int)event.getValue());
//		}
	}

	public void displayStiffness(boolean b) {
		this.pc.displayStiffness(b);
	}
	
	public void displayEquilibriumPosition(boolean b) {
		if (b) {
			this.draw_dotted_line = true;
		} else {
			this.draw_dotted_line = false;
		}
	}

}
