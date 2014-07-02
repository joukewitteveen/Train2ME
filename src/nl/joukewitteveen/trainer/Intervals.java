package nl.joukewitteveen.trainer;

import java.io.PrintStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.*;

import nl.joukewitteveen.util.*;

public class Intervals implements ItemStateListener, CommandListener {
	private static final String[] units = { "s", "min", "h", "m", "km" };
	private static final Command OKCommand = new Command("OK", Command.OK, 0);
	private static final Command CancelCommand = new Command("Cancel", Command.CANCEL, 1);
	private final DisplayManager displayManager;
	private Form editor;

	public Intervals(DisplayManager displayManager) {
		this.displayManager = displayManager;
		editor = new Form("New Interval Training");
	}

	public Displayable getDisplayable() {
		if(editor.size() == 0) {
			editor.append(new TextField("Name", null, 40, TextField.ANY));
			appendEmpty();
			editor.addCommand(OKCommand);
			editor.addCommand(CancelCommand);
			editor.setItemStateListener(this);
			editor.setCommandListener(this);
		}
		return editor;
	}

	private void appendEmpty() {
		editor.append(new TextField("Duration", null, 10, TextField.DECIMAL));
		editor.append(new ChoiceGroup("Unit", Choice.POPUP, units, new Image[]{ null, null, null, null, null }));
	}

	private int indexOf(Item item) {
		for(int i = 0; i < editor.size(); i++) {
			if(editor.get(i) == item) {
				return i;
			}
		}
		return -1;
	}

	public void itemStateChanged(Item item) {
		int index = indexOf(item);
		if(index % 2 == 0 || (index + 2 == editor.size()) == (((TextField) item).size() == 0)) {
			// Not a TextField, or not the addition or deletion of a value
			return;
		}
		if(((TextField) item).size() == 0) {
			editor.delete(index);
			editor.delete(index);
		} else {
			appendEmpty();
		}
	}

	public void commandAction(Command command, Displayable form) {
		if(command == OKCommand) {
			String name = ((TextField) editor.get(0)).getString();
			if(name.length() == 0) {
				displayManager.getDisplay().setCurrent(new Alert("Invalid Specification", "The training needs a name.", null, AlertType.ERROR));
				return;
			}
			PrintStream writer;
			try {
				FileConnection file = (FileConnection) Connector.open(Settings.Values.completePath() + name + ".training");
				if(file.exists()) {
					displayManager.getDisplay().setCurrent(new Alert("Invalid Specification", "A training by this name already exists.", null, AlertType.ERROR));
					return;
				}
				AppLog.log("Creating " + file.getName());
				file.create();
				writer = new PrintStream(file.openOutputStream());
			} catch (Exception e) {
				AppLog.log("> " + e.getMessage());
				return;
			}
			writer.println("7");
			writer.println("120\t90\t70\tCENTER");
			writer.println("235\t110\t15\tRIGHT");
			writer.println("120\t184\t49\tCENTER");
			writer.println("235\t200\t12\tRIGHT");
			writer.println("120\t264\t49\tCENTER");
			writer.println("5\t310\t25\tLEFT");
			writer.println("235\t310\t25\tRIGHT");
			writer.println("Speed\t.km/h");
			writer.println("Text\tkm/h");
			writer.println("Text\t");
			writer.println("Text\t");
			writer.println("Time");
			writer.println("Text\t");
			writer.println("Wait\tPress Continue");
			writer.println("Speed\t.km/h");
			writer.println("Text\tkm/h");
			writer.println("Timer\t.s\t10");
			writer.println("Text\ts");
			writer.println("Time");
			writer.println("Distance\tkm\t0");
			writer.println("Timer\t.h\t0");
			for(int i = 1; i + 3 < editor.size(); i += 2) {
				writer.println("Speed\t.km/h");
				writer.println("Text\tkm/h");
				String duration = ((TextField) editor.get(i)).getString();
				int selected = ((ChoiceGroup) editor.get(i + 1)).getSelectedIndex();
				switch(selected) {
				case 0:  // seconds
				case 1:  // minutes
				case 2:  // hours
					writer.println("Timer\t." + units[selected] + "\t" + duration);
					break;
				case 3:  // metres
				case 4:  // kilometres
					writer.println("Distance\t." + units[selected] + "\t" + duration);
					break;
				}
				writer.println("Text\t" + units[selected]);
				writer.println("Time");
				writer.println("Distance\tkm");
				writer.println("Timer\t.h");
			}
			writer.println("Speed\t.km/h");
			writer.println("Text\tkm/h");
			writer.println("Text\tdone");
			writer.println("Text\t");
			writer.println("Time");
			writer.println("Distance\tkm");
			writer.println("Timer\t.h");
			writer.close();
		}
		displayManager.previousDisplay();
	}
}
