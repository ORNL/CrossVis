package gov.ornl.eden;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public abstract class Renderer extends Thread {
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(this
			.getClass());

	public boolean isRunning = false;
	protected ArrayList<RendererListener> listeners = new ArrayList<RendererListener>();
	protected BufferedImage image;

	protected void fireRendererFinished() {
		for (RendererListener listener : listeners) {
			listener.rendererFinished(this);
		}
	}

	public void addRendererListener(RendererListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	public void removeRendererListener(RendererListener listener) {
		listeners.remove(listener);
	}

	public BufferedImage getRenderedImage() {
		return image;
	}
}
