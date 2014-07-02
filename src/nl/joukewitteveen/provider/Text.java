package nl.joukewitteveen.provider;

import java.util.Enumeration;

import nl.joukewitteveen.trainer.*;

public class Text extends Provider {
	private String text;

	public Text(Training parent, BigText.TextRegion region, Enumeration args) {
		super(parent, region, args);
		text = args.hasMoreElements() ? (String) args.nextElement() : null;
	}

	public void run() {
		if(text != null) {
			region.writeText(text, true);
		}
	}
}
