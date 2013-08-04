package nl.joukewitteveen.providers;

import java.util.Enumeration;

import javax.microedition.lcdui.*;
import javax.microedition.location.LocationException;

import nl.joukewitteveen.util.*;
import nl.joukewitteveen.trainer.*;
import nl.joukewitteveen.providers.movement.*;
import nl.joukewitteveen.Provider;

public class Speed extends Provider implements MovementHandler, CommandListener {
	private static final int MS = 0, KT = 1, KMH = 2, MINKM = 3;
	private static final float[] factors = { 1, 3.6f / 1.852f, 3.6f, 6 / 100f };
	private int unit;
	private String label;
	private float previousSpeed = Float.NaN;

	public Speed(Training parent, BigText.TextRegion region, Enumeration args) {
		super(parent, region, args);
		if(args.hasMoreElements()) {
			String unitString = (String) args.nextElement();
			if(unitString.startsWith(".")) {
				unitString = unitString.substring(1);
				label = "";
			} else {
				label = " " + unitString;
			}
			if(unitString.equals("m/s")) {
				unit = MS;
			} else if(unitString.equals("kt")) {
				unit = KT;
			} else if(unitString.equals("km/h")) {
				unit = KMH;
			} else if(unitString.equals("min/km")) {
				unit = MINKM;
			} else {
				AppLog.log("Unsupported unit of speed: " + unitString);
				return;
			}
		} else {
			unit = MS;
			label = " m/s";
		}
		Position.addHandler(this);
		parent.addCommandListener(this);
	}

	public synchronized void movementUpdate(float distance, float speed) {
		if(speed < 0 || speed == previousSpeed) {
			return;
		}
		previousSpeed = speed;
		String value = "--";
		if(!Float.isNaN(speed)) {
			switch(unit) {
			case MS:
			case KT:
			case KMH:
				value = StringUtil.oneDecimal(factors[unit] * speed);
				break;
			case MINKM:
				float pace = 1 / (factors[unit] * speed);
				if(pace <= 60) {
					int subPace = (int) ((pace % 1) * 60 + 0.5f);
					value = ((int) pace) + ":" + StringUtil.twoDigits(subPace);
				} else {
					value = "--:--";
				}
				break;
			}
		}
		region.writeText(value + label);
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
		if(command == Training.nextEpochCommand) {
			Position.removeHandler(this);
			parent.removeCommandListener(this);
		}
	}
}
