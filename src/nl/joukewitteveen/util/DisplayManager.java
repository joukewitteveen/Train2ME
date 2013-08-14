package nl.joukewitteveen.util;

import java.util.EmptyStackException;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

public interface DisplayManager {
	public Display getDisplay();
	public void setDisplay(Displayable displayable);
	public void previousDisplay() throws EmptyStackException;
}
