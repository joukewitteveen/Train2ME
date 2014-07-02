package nl.joukewitteveen.trainer;

import java.util.NoSuchElementException;
import java.util.Vector;

import javax.microedition.lcdui.*;

import nl.joukewitteveen.provider.*;
import nl.joukewitteveen.util.*;

public class Training implements CommandListener {
	public static final Command pauseCommand = new Command("Pause", Command.STOP, -1);
	public static final Command resumeCommand = new Command("Resume", Command.STOP, -1);
	public static final Command nextEpochCommand = new Command("nextEpoch", Command.STOP, 0);
	private Vector listeners = new Vector();
	private final BigText.TextRegion[] regions;
	private Thread[] fields;
	private StringUtil.StreamEnumeration specification;

	public Training(BigText.TextRegion[] regions, StringUtil.StreamEnumeration specification) {
		this.regions = regions;
		this.fields = new Thread[regions.length];
		this.specification = specification;
	}

	public synchronized void nextEpoch() {
		commandAction(nextEpochCommand, null);
		for(int i = 0; i < fields.length; i++) {
			if(fields[i] != null) {
				fields[i].interrupt();
			}
		}
		Provider[] providers = new Provider[regions.length];
		try {
			String provider;
			for(int i = 0; i < regions.length; i++) {
				provider = (String) specification.nextElement();
				if(provider.equals("Distance")) {
					providers[i] = new Distance(this, regions[i], specification);
				} else if(provider.equals("Metronome")) {
					providers[i] = new Metronome(this, regions[i], specification);
				} else if(provider.equals("Speed")) {
					providers[i] = new Speed(this, regions[i], specification);
				} else if(provider.equals("Text")) {
					providers[i] = new Text(this, regions[i], specification);
				} else if(provider.equals("Time")) {
					providers[i] = new Time(this, regions[i], specification);
				} else if(provider.equals("Timer")) {
					providers[i] = new Timer(this, regions[i], specification);
				} else if(provider.equals("Wait")) {
					providers[i] = new Wait(this, regions[i], specification);
				} else if(provider.startsWith("//") || !specification.hasMoreElements()) {
					i--;
				} else {
					AppLog.log("Unsupported provider: " + provider);
				}
				specification.skipToNextRow();
			}
		} catch(NoSuchElementException e) {
			AppLog.log("Incomplete training specification");
			commandAction(null, null);
			return;
		}
		for(int i = 0; i < regions.length; i++) {
			fields[i] = new Thread(providers[i]);
			fields[i].start();
		}
	}

	public void addCommandListener(CommandListener listener) {
		listeners.addElement(listener);
	}

	public boolean removeCommandListener(CommandListener listener) {
		return listeners.removeElement(listener);
	}

	public void commandAction(Command command, Displayable displayable) {
		CommandListener[] persistentListeners = new CommandListener[listeners.size()];
		listeners.copyInto(persistentListeners);
		for(int i = 0; i < persistentListeners.length; i++) {
			persistentListeners[i].commandAction(command, displayable);
		}
		if(command == pauseCommand) {
			ToneUtil.setBlocked(true);
			displayable.removeCommand(pauseCommand);
			displayable.addCommand(resumeCommand);
		} else if(command == resumeCommand) {
			ToneUtil.setBlocked(false);
			displayable.removeCommand(resumeCommand);
			displayable.addCommand(pauseCommand);
		}
	}
}
