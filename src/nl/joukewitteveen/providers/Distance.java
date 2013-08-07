package nl.joukewitteveen.providers;

import java.util.Enumeration;

import javax.microedition.lcdui.*;
import javax.microedition.location.LocationException;

import nl.joukewitteveen.util.*;
import nl.joukewitteveen.trainer.*;
import nl.joukewitteveen.providers.movement.*;
import nl.joukewitteveen.Provider;

public class Distance extends Provider implements MovementHandler, CommandListener {
	private static final int M = 1, KM = 1000, NM = 1852;
	private static float start = 0;
	private float pause, stop;
	private int unit;
	private String label;
	private float previousDistance = Float.NaN;

	public Distance(Training parent, BigText.TextRegion region, Enumeration args) {
		super(parent, region, args);
		if(args.hasMoreElements()) {
			String unitString = (String) args.nextElement();
			if(unitString.startsWith(".")) {
				unitString = unitString.substring(1);
				label = "";
			} else {
				label = " " + unitString;
			}
			if(unitString.equals("m")) {
				unit = M;
			} else if(unitString.equals("km")) {
				unit = KM;
			} else if(unitString.equals("NM")) {
				unit = NM;
			} else {
				AppLog.log("Unsupported unit of distance: " + unitString);
				return;
			}
		} else {
			unit = M;
			label = " m";
		}
		if(args.hasMoreElements()) {
			float arg = Float.parseFloat((String) args.nextElement());
			if(arg == Float.parseFloat("0")) {
				start = Position.getTotalDistance();
				stop = Float.NaN;
			} else {
				stop = Position.getTotalDistance() + (arg * unit);
			}
		} else {
			stop = Float.NaN;
		}
		Position.addHandler(this);
		pause = 0;
		parent.addCommandListener(this);
	}

	public synchronized void movementUpdate(float distance, float speed) {
		if(Float.isNaN(stop)) {
			distance = (distance - start);
		} else {
			distance = (stop - distance);
			if(distance <= 3 * speed){
				if(distance <= 0) {
					AlertType.ALARM.playSound(region.getParent().getDisplay());
					parent.nextEpoch();
				} else {
					AlertType.INFO.playSound(region.getParent().getDisplay());
				}
			}
		}
		distance /= unit;
		if(distance == previousDistance) {
			return;
		}
		previousDistance = distance;
		region.writeText((Float.isNaN(distance) ? "--" : StringUtil.oneDecimal(distance)) + label);
	}

	public void run() {
		boolean fresh = true;
		region.writeText("-", false);
		try {
			fresh = Position.initialize();
		} catch(LocationException e) {
			region.writeText("NA");
			return;
		}
		if(fresh) {
			parent.addCommandListener(new GPX());
		} else {
			GPX.startSegment();
		}
	}

	public void commandAction(Command command, Displayable displayable) {
		if(command == Training.pauseCommand) {
			Position.removeHandler(this);
			pause = Position.getTotalDistance();
		} else if(command == Training.resumeCommand) {
			start += Position.getTotalDistance() - pause;
			if(stop != Float.NaN) {
				stop += Position.getTotalDistance() - pause;
			}
			Position.addHandler(this);
		} else if(command == Training.nextEpochCommand) {
			Position.removeHandler(this);
			parent.removeCommandListener(this);
		}
	}
}
