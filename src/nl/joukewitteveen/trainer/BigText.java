package nl.joukewitteveen.trainer;

import javax.microedition.lcdui.*;
import javax.microedition.m2g.SVGImage;
import javax.microedition.m2g.ScalableGraphics;

import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGElement;
import org.w3c.dom.svg.SVGSVGElement;

import nl.joukewitteveen.util.AppLog;

import com.samsung.util.LCDLight;


public class BigText extends Canvas {
	private static final String SVGNS = "http://www.w3.org/2000/svg";
	private final Display display;
	private boolean activeLight = false;
	private ScalableGraphics renderer;
	private SVGImage image;

	public BigText(Display display, TextRegion[] regions){
		this.display = display;
		renderer = ScalableGraphics.createInstance();
		image = SVGImage.createEmptyImage(null);
		Document document = image.getDocument();
		SVGSVGElement svg = (SVGSVGElement) document.getDocumentElement();
		for(int i = 0; i < regions.length; i++) {
			regions[i].text = (SVGElement) document.createElementNS(SVGNS, "text");
			regions[i].text.setFloatTrait("x", regions[i].x);
			regions[i].text.setFloatTrait("y", regions[i].y);
			regions[i].text.setFloatTrait("font-size", regions[i].height);
			regions[i].text.setTrait("font-weight", "bold");
			switch(regions[i].anchor) {
			case TextRegion.LEFT:
				regions[i].text.setTrait("text-anchor", "start");
				break;
			case TextRegion.CENTER:
				regions[i].text.setTrait("text-anchor", "middle");
				break;
			case TextRegion.RIGHT:
				regions[i].text.setTrait("text-anchor", "end");
				break;
			}
			svg.appendChild(regions[i].text);
			regions[i].parent = this;
		}
		this.setFullScreenMode(true);
	}

	public void paint(Graphics g) {
		g.setColor(0xFFFFFF);
		g.fillRect(0, 0, getWidth(), getHeight());
		renderer.bindTarget(g);
		image.setViewportWidth(getWidth());
		image.setViewportHeight(getHeight());
		renderer.render(0, 0, image);
		renderer.releaseTarget();
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
		public static final int LEFT = Graphics.LEFT,
				CENTER = Graphics.HCENTER,
				RIGHT = Graphics.RIGHT;
		final int x, y, height, anchor;
		private SVGElement text;
		private BigText parent;

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
			try {
				this.text.setTrait("#text", text);
				if(repaint) {
					parent.repaint();
				}
			} catch(NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
}
