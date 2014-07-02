package nl.joukewitteveen.util;

import java.util.Enumeration;

import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.*;
import javax.microedition.rms.*;

public class Settings implements CommandListener {
	private static final String recordStoreName = "Train2ME";
	private DisplayManager displayManager;
	private static final Form menu = new Form("Settings", new Item[]{
			new ChoiceGroup("Storage", Choice.POPUP),
			new TextField("Path", "", 40, TextField.ANY),
			new ChoiceGroup(null, Choice.MULTIPLE, new String[]{ "Vibrate" }, new Image[]{ null }),
			new ChoiceGroup(null, Choice.MULTIPLE, new String[]{ "Active Lighting" }, new Image[]{ null })
		});
	private static final Command OKCommand = new Command("OK", Command.OK, 0);
	private static final Command CancelCommand = new Command("Cancel", Command.CANCEL, 1);
	public static class Values {
		public static final String version = "0.5";
		public static String root;
		public static String path;
		public static boolean vibrate;
		public static boolean activeLighting;
		public static String completePath() {
			return "file:///" + root + path;
		}
	}

	public static void readValues() {
		try {
			RecordStore recordStore = RecordStore.openRecordStore(recordStoreName, false);
			if(!(recordStore.getNumRecords() == 4 && recordStore.getNextRecordID() == 5)) {
				AppLog.log("Malformed RecordStore, deleting");
				recordStore.closeRecordStore();
				RecordStore.deleteRecordStore(recordStoreName);
				throw new RecordStoreNotFoundException();
			}
			Values.root = StringUtil.nullIsEmpty(recordStore.getRecord(1));
			Values.path = StringUtil.nullIsEmpty(recordStore.getRecord(2));
			Values.vibrate = (recordStore.getRecord(3)[0] == 0) ? false : true;
			Values.activeLighting = (recordStore.getRecord(4)[0] == 0) ? false : true;
			recordStore.closeRecordStore();
		} catch (Exception e) {
			AppLog.log("Could not open RecordStore, using defaults");
			Enumeration roots = FileSystemRegistry.listRoots();
			if(roots.hasMoreElements()) {
				Values.root = (String) roots.nextElement();
			} else {
				Values.root = "/";
			}
			Values.path = "Train2ME/";
			Values.vibrate = false;
			Values.activeLighting = false;
		}
	}

	public Settings(DisplayManager displayManager) {
		this.displayManager = displayManager;
	}

	public Displayable getDisplayable() {
		ChoiceGroup storage = (ChoiceGroup) menu.get(0);
		Enumeration roots = FileSystemRegistry.listRoots();
		storage.deleteAll();
		if(Values.root != null) {
			storage.append(Values.root, null);
			storage.setSelectedIndex(0, true);
		}
		while(roots.hasMoreElements()) {
			String root = (String) roots.nextElement();
			if(!root.equals(Values.root)) {
				storage.append(root, null);
			}
		}
		if(Values.path != null) {
			((TextField) menu.get(1)).setString(Values.path);
		}
		((ChoiceGroup) menu.get(2)).setSelectedIndex(0, Values.vibrate);
		((ChoiceGroup) menu.get(3)).setSelectedIndex(0, Values.activeLighting);
		menu.addCommand(OKCommand);
		menu.addCommand(CancelCommand);
		menu.setCommandListener(this);
		return menu;
	}

	public void commandAction(Command command, Displayable form) {
		if(command == OKCommand) {
			try {
				try {
					RecordStore.deleteRecordStore(recordStoreName);
				} catch(RecordStoreNotFoundException e){
					AppLog.log("No previous RecordStore");
				}
				RecordStore recordStore = RecordStore.openRecordStore(recordStoreName, true);
				ChoiceGroup storage = (ChoiceGroup) menu.get(0);
				Values.root = storage.getString(storage.getSelectedIndex());
				recordStore.addRecord(Values.root.getBytes(), 0, Values.root.length());
				Values.path = ((TextField) menu.get(1)).getString();
				recordStore.addRecord(Values.path.getBytes(), 0, Values.path.length());
				Values.vibrate = ((ChoiceGroup) menu.get(2)).isSelected(0);
				recordStore.addRecord(new byte[]{(byte) (Values.vibrate ? 1 : 0)}, 0, 1);
				Values.activeLighting = ((ChoiceGroup) menu.get(3)).isSelected(0);
				recordStore.addRecord(new byte[]{(byte) (Values.activeLighting ? 1 : 0)}, 0, 1);
				recordStore.closeRecordStore();
			} catch(Exception e) {
				AppLog.log("Could not write the RecordStore");
				AppLog.log("> " + e.getMessage());
			}
		}
		form.removeCommand(OKCommand);
		form.removeCommand(CancelCommand);
		displayManager.previousDisplay();
	}
}
