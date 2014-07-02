package nl.joukewitteveen.provider;

import java.util.Date;
import java.util.Enumeration;
import java.util.TimerTask;

import javax.microedition.lcdui.*;

import nl.joukewitteveen.trainer.*;
import nl.joukewitteveen.util.*;

public class Timer extends Provider implements CommandListener {
	private static final int UNSUPPORTED = -1, SECONDS = 1, MINUTES = 60, HOURS = 3600;
	private static long start = getTimeS();
	private long pause = 0, stop;
	private int unit;
	private String label;
	private java.util.Timer timer;
	private Watch watch;

	public Timer(Training parent, BigText.TextRegion region, Enumeration args) {
		super(parent, region, args);
		if(args.hasMoreElements()) {
			String unitString = (String) args.nextElement();
			if(unitString.startsWith(".")) {
				unitString = unitString.substring(1);
				label = "";
			} else {
				label = " " + unitString;
			}
			if(unitString.equals("s")) {
				unit = SECONDS;
			} else if(unitString.equals("min")) {
				unit = MINUTES;
			} else if(unitString.equals("h")) {
				unit = HOURS;
			} else {
				AppLog.log("Unsupported unit of time: " + unitString);
				unit = UNSUPPORTED;
				return;
			}
		} else {
			unit = SECONDS;
			label = " s";
		}
		if(args.hasMoreElements()) {
			float arg = Float.parseFloat((String) args.nextElement());
			if(arg == Float.parseFloat("0")) {
				start = getTimeS();
				stop = Long.MIN_VALUE;
			} else {
				stop = getTimeS() + (long) (arg * unit);
			}
		} else {
			stop = Long.MIN_VALUE;
		}
		timer = new java.util.Timer();
		pause = getTimeS();
		parent.addCommandListener(this);
	}

	public static long getTimeS() {
		return (new Date()).getTime() / 1000;
	}

	public void run() {
		if(unit == UNSUPPORTED) {
			return;
		}
		watch = new Watch();
		timer.scheduleAtFixedRate(watch, 0, (unit == HOURS) ? 60000 : 1000);
	}

	public void commandAction(Command command, Displayable displayable) {
		if(command == Training.pauseCommand) {
			watch.cancel();
			pause = getTimeS();
		} else if(command == Training.resumeCommand) {
			start += getTimeS() - pause;
			if(stop != Long.MIN_VALUE) {
				stop += getTimeS() - pause;
			}
			run();
		} else if(command == Training.nextEpochCommand) {
			parent.removeCommandListener(this);
			timer.cancel();
		}
	}

	private class Watch extends TimerTask {
		public synchronized void run() {
			long time;
			if(stop == Long.MIN_VALUE) {
				time = Timer.getTimeS() - start;
			} else {
				time = stop - Timer.getTimeS();
				if(time <= 3) {
					if(time <= 0) {
						ToneUtil.play(ToneUtil.ALARM);
						cancel();
						parent.nextEpoch();
					} else {
						ToneUtil.play(ToneUtil.INFO);
					}
				}
			}
			// At this point, time is in seconds
			switch(unit) {
			case SECONDS:
				region.writeText(time + label);
				break;
			case HOURS:
				time /= 60;
			case MINUTES:
				region.writeText((time / 60) + ":" + StringUtil.twoDigits((int) (time % 60)) + label);
				break;
			}
		}
	};
}
