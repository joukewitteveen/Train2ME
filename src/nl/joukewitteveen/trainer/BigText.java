package nl.joukewitteveen.trainer;

import javax.microedition.lcdui.*;

import nl.joukewitteveen.util.AppLog;

import com.samsung.util.LCDLight;

// JSR 226 is able to do what we want: https://meapplicationdevelopers.java.net/uiLabs/IntroTo226.html
// text-anchor: start, middle, end
// dominant-baseline: text-before-edge, central, text-after-edge

public class BigText extends Canvas {
	private final Display display;
	private boolean activeLight = false;
	final TextRegion[] regions;

	public BigText(Display display, TextRegion[] regions){
		this.display = display;
		this.regions = regions;
		for(int i = 0; i < this.regions.length; i++) {
			this.regions[i].parent = this;
		}
		this.setFullScreenMode(true);
	}

	public void paint(Graphics g) {
		g.setColor(0xABCDEF);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(0x000000);
		for(int i = 0; i < regions.length; i++) {
			if(regions[i].text == null) {
				continue;
			}
			g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD,
					regions[i].height > 32 ? Font.SIZE_LARGE : Font.SIZE_MEDIUM));
			g.drawString(regions[i].text, regions[i].x, regions[i].y, regions[i].anchor);
		}
		if(activeLight) {
			try {
				LCDLight.on(61);
			} catch(Exception e) {
				AppLog.log("Could not set Active Lighting");
			}
		}
	}

	public Display getDisplay() {
		return display;
	}

	public void setActiveLighting(boolean light) {
		activeLight = light;
	}

	public static class TextRegion {
		public static final int TOPLEFT = Graphics.TOP | Graphics.LEFT,
				TOPCENTER = Graphics.TOP | Graphics.HCENTER,
				TOPRIGHT = Graphics.TOP | Graphics.RIGHT,
				// VCENTER is unsupported for text
				/*CENTERLEFT = Graphics.VCENTER | Graphics.LEFT,
                CENTER = Graphics.VCENTER | Graphics.HCENTER,
                CENTERRIGHT = Graphics.VCENTER | Graphics.RIGHT,*/
				BOTTOMLEFT = Graphics.BOTTOM | Graphics.LEFT,
				BOTTOMCENTER = Graphics.BOTTOM | Graphics.VCENTER,
				BOTTOMRIGHT = Graphics.BOTTOM | Graphics.RIGHT;
		private final int x, y, height, anchor;
		private String text = null;
		private BigText parent = null;

		public TextRegion(int x, int y, int height, int anchor) {
			this.x = x;
			this.y = y;
			this.height = height;
			this.anchor = anchor;
		}

		public BigText getParent() {
			return parent;
		}

		public void writeText(String text) {
			writeText(text, true);
		}

		public void writeText(String text, boolean repaint) {
			this.text = text;
			if(repaint) {
				try {
					parent.repaint();  // TODO: specify what region
				} catch(NullPointerException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
