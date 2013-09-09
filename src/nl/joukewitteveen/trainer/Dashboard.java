// This MIDlet requires JSRs 75, 118, 135, 139, 179, 226

package nl.joukewitteveen.trainer;

import java.io.*;
import java.util.*;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

import nl.joukewitteveen.util.*;

public class Dashboard extends MIDlet implements DisplayManager, CommandListener {
	// We do not use Command.EXIT to make accidental exits more unlikely
	public static final Command exitCommand = new Command("Exit", Command.SCREEN, 1);
	public static final Command settingsCommand = new Command("Settings", Command.SCREEN, 10);
	public static final Command logCommand = new Command("Log", "Show Application Log", Command.SCREEN, 20);
	private Display display;
	private Stack displayStack = new Stack();
	private BigText canvas;
	private Training training = null;

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

	public Dashboard() {
		display = Display.getDisplay(this);
		AppLog.log("Train2ME " + Settings.Values.version);
		Settings.readValues();
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
			canvas = new BigText(regions);
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
			setDisplay((new SelectTraining(this)).getDisplayable());
		} else {
			setDisplay(canvas);
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

	public Display getDisplay() {
		return display;
	}

	public void setDisplay(Displayable displayable) {
		display.setCurrent((Displayable) displayStack.push(displayable));
	}

	public void previousDisplay() throws EmptyStackException {
		displayStack.pop();
		display.setCurrent((Displayable) displayStack.peek());
	}

	public void commandAction(Command command, Displayable displayable) {
		if(command == settingsCommand) {
			setDisplay((new Settings(this)).getDisplayable());
		} else if(command == logCommand) {
			setDisplay((new AppLog(this)).getDisplayable());
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
	private static final Command intervalsCommand = new Command("New IT", "New Interval Training", Command.SCREEN, 2);

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
		list.addCommand(intervalsCommand);
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
		} else if(command == intervalsCommand) {
			dashboard.setDisplay((new Intervals(dashboard)).getDisplayable());
		} else {
			dashboard.commandAction(command, displayable);
		}
	}
}
