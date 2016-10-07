package pressurelab;

import java.awt.Font;
import java.math.BigDecimal;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.joints.DistanceJoint;
import org.jbox2d.dynamics.joints.DistanceJointDef;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PImage;
import shiffman.box2d.Box2DProcessing;

public class Piston {

	int x;
	int y;
	int currentLen;
	int originalLen;
	int n;
	String label;
	boolean display_forces;
	boolean display_k;
	float init_volume = 1;

	PApplet parent;
	Hand hand;
	Box2DProcessing box2d;

//	int piston_img_w = 231;
//	int piston_img_h = 80;

	// BOX2D
	DistanceJointDef djd;
	DistanceJoint dj;

	Anchor anchor;
	PImage piston_img;
	PImage container_img;

	public Piston(int _x, int _y, int _k, int _length, String label, PApplet p, Box2DProcessing box2d,
			ResearchData rData, String image_name) {

		this.x = _x;
		this.y = _y;
		this.n = _k;
		this.originalLen = _length;
		this.label = label;
		this.parent = p;
		this.box2d = box2d;

		this.display_forces = true;
		this.display_k = true;

		this.hand = new Hand(this.x, this.y, true, parent, box2d, rData);
		this.anchor = new Anchor(this.x + this.originalLen, this.y, parent, box2d);

		// Import photo
		this.piston_img = parent.loadImage("piston.png");
		this.container_img = parent.loadImage(image_name);

		// Define the joint
		this.djd = new DistanceJointDef();

		djd.bodyA = this.anchor.body;
		// Body 2 is the Hand's object
		djd.bodyB = this.hand.body;
		// Get the mouse location in world coordinates
		djd.collideConnected = false;
		djd.length = box2d.scalarPixelsToWorld(this.originalLen);

		// Some stuff about how strong and bouncy the piston should be
		// djd.maxForce = (float) (1000.0 * hand.body.m_mass);
		djd.frequencyHz = (float) ((1 / (2 * Math.PI)) * (Math.sqrt(this.n / this.hand.body.m_mass)));
		djd.dampingRatio = 0;

		// Make the joint
		dj = (DistanceJoint) box2d.world.createJoint(djd);
	}

	// TODO: update hand drawing as well.
	public void draw() {
		parent.fill(0);
		parent.text(this.label, this.x + this.originalLen + 80, this.y-50);
		if (dj != null) {
			// We can get the two anchor points
			Vec2 v1 = new Vec2(0, 0);
			dj.getAnchorA(v1);
			Vec2 v2 = new Vec2(0, 0);
			dj.getAnchorB(v2);

			// Convert them to screen coordinates
			v1 = box2d.coordWorldToPixels(v1);
			v2 = box2d.coordWorldToPixels(v2);

			// And draw the container
			parent.imageMode(PConstants.CENTER);
			parent.image(container_img, this.x + this.container_img.width/2 + 120, this.y);
			// int height = (int) (v2.y - v1.y);
			// int width = 30;
			// piston_img.resize(width, height);
			// parent.image(piston_img, v1.x, v1.y);

			// And just draw a line; great for debugging
//			parent.stroke(0);
//			parent.strokeWeight(3);
//			parent.line(v1.x, v1.y, v2.x, v2.y);

			// parent.pushMatrix();
			parent.imageMode(PConstants.CENTER);
			parent.image(piston_img, v2.x + this.piston_img.width/2, this.y, this.piston_img.width, this.piston_img.height);
			// parent.popMatrix();

			// (float) (this.y + (0.5*(v2.y-v1.y)))
			// v2.y-v1.y-(hand.current_hand_img.height/2)

			// Hand-tweaked coordinates
			int dfx = this.hand.x - this.hand.w / 2 - 10;
			int dfy = this.hand.y + this.hand.h + 5;

			// Hand-tweaked coordinates
			int dsx1 = this.anchor.x + 90;
			int dsy1 = this.hand.y + 60;

			Font p1 = parent.getFont();
			PFont p2 = parent.createFont("Verdana", 12);
			
			// Info text location
			int info_x  = this.x + this.originalLen + 170;
			int info_y1 = this.y;
			int info_y2 = this.y + 20;
					

			if (display_k) {
				parent.fill(120);
				parent.pushMatrix();
				parent.textFont(p2);
				BigDecimal d = this.getVolume();
				parent.text("Volume: " + String.format("%.2f", d), info_x, info_y1);
				parent.setFont(p1);
				parent.textSize(18);
				parent.popMatrix();
			}

			if (this.display_forces == true) {
				parent.fill(100);
				parent.pushMatrix();
				parent.textFont(p2);
				parent.text("Pressure: " + String.format("%.2f", this.getPressure()), info_x, info_y2);
				parent.setFont(p1);
				parent.textSize(18);
				parent.popMatrix();
			}
		}

		this.anchor.draw();
		this.hand.draw();
	}

	public BigDecimal getLength() {
		Vec2 v1 = new Vec2(0, 0);
		dj.getAnchorA(v1);
		Vec2 v2 = new Vec2(0, 0);
		dj.getAnchorB(v2);
		
		float rawLen = (v2.sub(v1)).length();
		rawLen *= 5;
		BigDecimal roundLen = new BigDecimal(rawLen).setScale(2, BigDecimal.ROUND_HALF_UP);

		return roundLen;
	}
	
	public BigDecimal getVolume() {
		BigDecimal volume = this.getLength();
		float radius = volume.floatValue() / 6;
		BigDecimal area = new BigDecimal(Math.PI * Math.pow(radius, 2)).setScale(2, BigDecimal.ROUND_HALF_UP);
		volume = volume.multiply(area);
		return volume;
	}

	public void setLength(int len_pixels) {
		dj.setLength(box2d.scalarPixelsToWorld(len_pixels));
	}

	public BigDecimal getPressure() {
//		return (this.n * (dj.getLength() - this.getLength()));
		
		// width of each piston is about 200 cm
		// starting area is 0.12 cm-squared (0.2 * 0.6)
		// dj.getLength() is the starting length (in world units) and does not change
		// this.getLength() is the current length (in world units) and does change based on user interaction

		// pressure = n / v
		// v is given by 0.2 (width of beaker) * current length
		float pressure = this.n / this.getVolume().floatValue();
		BigDecimal pressureRound = new BigDecimal(pressure).setScale(2, BigDecimal.ROUND_HALF_UP);
		return pressureRound;
	}

	public void setX(int x) {
		this.x = x;
		this.hand.setX(x);
		this.anchor.setX(x);
	}

	public void mouseUpdate(int mx, int my, boolean pressed) {
		this.hand.mouseUpdate(mx, my, pressed);
	}

	public void hapkitUpdate(int pos) {
		this.hand.hapkitUpdate(pos);
	}

	public Hand getHand() {
		return this.hand;
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public int getK() {
		return this.n;
	}

	public void setK(int k) {
		this.n = k;
	}

	public String getLabel() {
		return this.label;
	}

	public void displayForce(boolean on) {
		if (on == true) {
			this.display_forces = true;
			System.out.println("Turning on force display under piston.");
		} else if (on == false) {
			this.display_forces = false;
			System.out.println("Turning off force display under piston.");
		}
	}

	public void setLabel(String stringValue) {
		this.label = stringValue;
	}
	
	public void setK2(int value) {
		// TODO Auto-generated method stub
	}

	public void displayK(boolean b) {
		this.display_k = b;
		
	}
}