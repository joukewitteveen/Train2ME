package nl.joukewitteveen.util;

import javax.microedition.lcdui.*;

public class AppLog implements CommandListener {
	private static final int lines = 25;
	private static int head = 0;
	private static String[] messages = new String[lines];
	private final Display display;
	private final Displayable nextDisplayable;

	public static void log(String message) {
		System.out.println(message);
		messages[head] = message;
		if(++head >= lines)
			head -= lines;
	}

	public AppLog(Display display, Displayable nextDisplayable) {
		this.display = display;
		this.nextDisplayable = nextDisplayable;
	}

	public Displayable getDisplayable() {
		TextBox text = new TextBox("Logged events", null, 40 * lines, TextField.UNEDITABLE);
		String message;
		for(int i = 1; i <= lines; i++) {
			message = messages[(lines + head - i) % lines];
			if(message == null)
				continue;
			try {
				text.insert(message + "\n", -1);
			} catch(IllegalArgumentException e) {
				break;
			}
		}
		text.addCommand(new Command("Back", Command.BACK, 0));
		text.setCommandListener(this);
		return (Displayable) text;
	}

	public void commandAction(Command command, Displayable text) {
		display.setCurrent(nextDisplayable);
	}
}
