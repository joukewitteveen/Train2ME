package nl.joukewitteveen.providers;

import java.util.Enumeration;
import java.util.TimerTask;

import javax.microedition.lcdui.*;

import nl.joukewitteveen.Provider;
import nl.joukewitteveen.trainer.*;
import nl.joukewitteveen.util.*;

public class Metronome extends Provider implements CommandListener {
	private float bpm;
	private java.util.Timer timer;
	private TimerTask tick;

	public Metronome(Training parent, BigText.TextRegion region, Enumeration args) {
		super(parent, region, args);
		if(args.hasMoreElements()) {
			bpm = Float.parseFloat((String) args.nextElement());
		} else {
			bpm = 60;
		}
		timer = new java.util.Timer();
		parent.addCommandListener(this);
	}

	public void run() {
		region.writeText(StringUtil.oneDecimal(bpm));
		tick = new TimerTask() {
			public void run() {
				ToneUtil.play(ToneUtil.INFO);
			}
		};
		timer.scheduleAtFixedRate(tick, 0, (long) (60000 / bpm));
	}

	public void commandAction(Command command, Displayable displayable) {
		if(command == Training.pauseCommand) {
			tick.cancel();
		} else if(command == Training.resumeCommand) {
			run();
		} else if(command == Training.nextEpochCommand) {
			parent.removeCommandListener(this);
			timer.cancel();
		}
	}
}
