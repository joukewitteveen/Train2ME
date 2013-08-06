// This MIDlet requires JSRs 75, 118, 139, 179, 226

package nl.joukewitteveen.trainer;

import java.io.*;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import nl.joukewitteveen.util.*;

public class Dashboard extends MIDlet implements CommandListener {
	// We do not use Command.EXIT to make accidental exits more unlikely
	public static final Command exitCommand = new Command("Exit", Command.SCREEN, 1);
	public static final Command settingsCommand = new Command("Settings", Command.SCREEN, 1);
	public static final Command logCommand = new Command("Log", "Show Application Log", Command.SCREEN, 2);
	private BigText canvas;
	private Training training = null;
	Display display;

	public Dashboard() {
		display = Display.getDisplay(this);
		AppLog.log("Train2ME " + Settings.Values.version);
		Settings.readValues();
	}

	private static int stringToAnchor(String name) {
		if(name.equals("LEFT")) {
			return BigText.TextRegion.LEFT;
		} else if(name.equals("CENTER")) {
			return BigText.TextRegion.CENTER;
		} else if(name.equals("RIGHT")) {
			return BigText.TextRegion.RIGHT;
		} else {
			return -1;
		}
	}

	public void initializeTraining(InputStream source) {
		try {
			StringUtil.StreamEnumeration fields = new StringUtil.StreamEnumeration(source);
			int numRegions = Integer.parseInt((String) fields.nextElement());
			BigText.TextRegion[] regions = new BigText.TextRegion[numRegions];
			fields.skipToNextRow();
			for(int i = 0; i < numRegions; i++) {
				regions[i] = new BigText.TextRegion(
						Integer.parseInt((String) fields.nextElement()),
						Integer.parseInt((String) fields.nextElement()),
						Integer.parseInt((String) fields.nextElement()),
						stringToAnchor((String) fields.nextElement())
					);
				fields.skipToNextRow();
			}
			canvas = new BigText(display, regions);
			addCommands(canvas);
			canvas.addCommand(Training.pauseCommand);
			canvas.setCommandListener(this);
			training = new Training(regions, fields);
		} catch(Exception e) {
			AppLog.log("Could not parse the training file");
			AppLog.log("> " + e.getMessage());
		}
	}

	public static void addCommands(Displayable displayable) {
		displayable.addCommand(exitCommand);
		displayable.addCommand(settingsCommand);
		displayable.addCommand(logCommand);
	}

	public void startApp() {
		if(training == null) {
			display.setCurrent((new SelectTraining(this)).getDisplayable());
		} else {
			display.setCurrent(canvas);
			training.nextEpoch();
		}
	}

	public void pauseApp() {
	}

	public void destroyApp(boolean unconditional) throws MIDletStateChangeException {
		if(training != null) {
			training.commandAction(exitCommand, null);
		}
	}

	public void commandAction(Command command, Displayable displayable) {
		if(command == settingsCommand) {
			display.setCurrent((new Settings(display)).getDisplayable());
		} else if(command == logCommand) {
			display.setCurrent((new AppLog(display, displayable)).getDisplayable());
		} else if(command == exitCommand) {
			try {
				destroyApp(false);
				notifyDestroyed();
			} catch(MIDletStateChangeException e) {
				AppLog.log("Exiting was prevented");
			}
		} else if(training != null) {
			training.commandAction(command, displayable);
		}
	}
}


class SelectTraining implements CommandListener {
	private Dashboard dashboard;
	private static final Command refreshCommand = new Command("Refresh", Command.SCREEN, 0);

	public SelectTraining(Dashboard dashboard) {
		this.dashboard = dashboard;
	}

	private void refreshList(List list) {
		list.deleteAll();
		try {
			FileConnection directory = (FileConnection) Connector.open(Settings.Values.completePath(), Connector.READ);
			Enumeration listing = directory.list("*.training", false);
			while(listing.hasMoreElements()) {
				list.append((String) listing.nextElement(), null);
			}
		} catch (IOException e) {
			AppLog.log("> " + e.getMessage());
		}
	}

	public Displayable getDisplayable() {
		List list = new List("Select a training", Choice.IMPLICIT);
		refreshList(list);
		Dashboard.addCommands(list);
		list.addCommand(refreshCommand);
		list.setCommandListener(this);
		return (Displayable) list;
	}

	public void commandAction(Command command, Displayable displayable) {
		if(command == List.SELECT_COMMAND) {
			List list = (List) displayable;
			String name = list.getString(list.getSelectedIndex());
			try {
				FileConnection file = (FileConnection) Connector.open(Settings.Values.completePath() + name, Connector.READ);
				dashboard.initializeTraining(file.openInputStream());
				dashboard.startApp();
			} catch(IOException e) {
				AppLog.log("Could not load training");
				AppLog.log("> " + e.getMessage());
			}
		} else if(command == refreshCommand) {
			refreshList((List) displayable);
		} else {
			dashboard.commandAction(command, displayable);
		}
	}
}