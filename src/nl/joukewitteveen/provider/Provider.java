package nl.joukewitteveen.provider;

import java.util.Enumeration;

import nl.joukewitteveen.trainer.*;

public abstract class Provider implements Runnable {
	protected final Training parent;
	protected BigText.TextRegion region;

	public Provider(Training parent, BigText.TextRegion region, Enumeration args) {
		this.parent = parent;
		this.region = region;
	}
}
