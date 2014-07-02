package nl.joukewitteveen.provider;

import java.util.Enumeration;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import nl.joukewitteveen.trainer.BigText.TextRegion;
import nl.joukewitteveen.trainer.Training;

public class Wait extends Text implements CommandListener {
	private static Command continueCommand = new Command("Continue", Command.SCREEN, -10);

	public Wait(Training parent, TextRegion region, Enumeration args) {
		super(parent, region, args);
		parent.addCommandListener(this);
		region.getParent().removeCommand(Training.pauseCommand);
		region.getParent().addCommand(continueCommand);
	}

	public void run() {
		super.run();
	}

	public void commandAction(Command command, Displayable displayable) {
		if(command == continueCommand || command == Training.nextEpochCommand) {
			region.getParent().removeCommand(continueCommand);
			region.getParent().addCommand(Training.pauseCommand);
			parent.removeCommandListener(this);
			if(command == continueCommand) {
				parent.nextEpoch();
			}
		}
	}
}
