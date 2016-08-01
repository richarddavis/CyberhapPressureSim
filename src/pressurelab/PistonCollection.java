package pressurelab;


import java.lang.reflect.Constructor;
import java.util.ArrayList;

import processing.core.PApplet;
import shiffman.box2d.Box2DProcessing;

public class PistonCollection {
	
	ArrayList<Piston> pistons;
	Piston activePiston; 
	ResearchData rData;
	Hapkit hapkit;
	
	public PistonCollection(ResearchData rData, Hapkit _hapkit){
		this.pistons = new ArrayList<Piston>();
		this.rData = rData;
		this.hapkit = _hapkit;
	}
	
	public boolean add(Piston p){
		return pistons.add(p);
	}
	
	public float getActiveForce() {
		return this.activePiston.getForce();
	}
	
	public void displayForces(boolean display_on) {
		for (Piston p : pistons) {
			p.displayForce(display_on);
		}
	}
	
	public void displayStiffness(boolean b) {
		for (Piston p : pistons) {
			p.displayK(b);
		}
	}
	
	public void draw() {
		for (Piston p : pistons) {
			if(p != null){
				p.draw();
			}
		}
	}
	
	public void setActive(Piston p){
		if(activePiston == null){
			activePiston = p;
			activePiston.getHand().swapIcon();
		}else{
			activePiston.getHand().swapIcon();
			p.hand.swapIcon();
			activePiston = p;
		}
		if(rData.getInputMode() == ResearchData.HAPKIT_MODE){
			System.out.println("Setting Hapkit k-constant to:");
			System.out.println(p.getK());
			this.hapkit.setKConstant(p.getK());
			// MAKES ALL OTHER SPRING ACT NORMALLY AGAIN:
			destroyOldHapkitJoints();
		}
	}
	
	public void updateActivePiston(int mx, int my, boolean pressed, Hapkit hapkit) {
		for (Piston p : pistons) {
			if (p != null && p.getHand().contains(mx, my)) {
				this.setActive(p);
				rData.logEvent(p.getK(), -1, "SWITCHING BETWEEN PISTONS");
				break;
			}
		}
		
		if(rData.getInputMode() == ResearchData.MOUSE_MODE){
			this.activePiston.mouseUpdate(mx, my, pressed);
		}else{
			// Why was the following line included?
			//this.activeSpring.hapkitUpdate(my);
		}
	}
	
	private void destroyOldHapkitJoints() {
		for (Piston p : pistons) {
			if(p != null && !p.equals(activePiston)){
				p.hand.destroy();
			}
		}
	}

	public void updateActivePistonPosition(double hapkitPos) {
		int currentPos = (int) this.activePiston.getX()+this.activePiston.originalLen+10;
		int newPos = (int) (currentPos + hapkitPos);
		//System.out.println(hapkitPos);
		this.activePiston.hapkitUpdate(newPos);	
	}

	public void delete(int value) {
		pistons.remove(value);
		pistons.add(value, null);
	}

	public void add(int x_i, Piston p) {
		pistons.add(x_i, p);
	}

}
