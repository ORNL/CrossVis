package gov.ornl.csed.cda.coalesce;

import java.awt.Color;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Utilities {
	private static final Logger log = LoggerFactory.getLogger(Utilities.class);

	public static Color getColorForCorrelationCoefficient(double corrcoef,
			double threshold) {
		Color c;
		if (Double.isNaN(corrcoef)) {
			return null;
		}

		if (corrcoef > 0.f) {
			float norm = 1.f - (float) Math.abs(corrcoef);
			Color c0 = new Color(211, 37, 37); // high pos. corr.
			Color c1 = new Color(240, 240, 240); // low pos. corr.

			if (Math.abs(corrcoef) > threshold) {
				c = c0;
			} else {
				int r = c0.getRed()
						+ (int) (norm * (c1.getRed() - c0.getRed()));
				int green = c0.getGreen()
						+ (int) (norm * (c1.getGreen() - c0.getGreen()));
				int b = c0.getBlue()
						+ (int) (norm * (c1.getBlue() - c0.getBlue()));
				c = new Color(r, green, b);
			}
		} else {
			float norm = 1.f - (float) Math.abs(corrcoef);
			Color c0 = new Color(44, 110, 211/* 177 */); // high neg. corr.
			Color c1 = new Color(240, 240, 240);// low neg. corr.

			if (Math.abs(corrcoef) > threshold) {
				c = c0;
			} else {
				int r = c0.getRed()
						+ (int) (norm * (c1.getRed() - c0.getRed()));
				int green = c0.getGreen()
						+ (int) (norm * (c1.getGreen() - c0.getGreen()));
				int b = c0.getBlue()
						+ (int) (norm * (c1.getBlue() - c0.getBlue()));
				c = new Color(r, green, b);
			}
		}
		return c;
	}
}
