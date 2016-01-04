package gov.ornl.csed.cda.datatable;
import java.io.Serializable;
import java.util.ArrayList;

public class Tuple implements Serializable {
	private ArrayList<Double> elements = new ArrayList<>();
	private boolean queryFlag = true;
	private int orderFactor = 0;

	public Tuple() {
	}

    public Tuple (Tuple copyTuple) {
        for (int i = 0; i < copyTuple.getElementCount(); i++) {
            elements.add(copyTuple.getElement(i));
        }
    }

    public Double[] getElementsAsArray() {
		Double elementArray [] = new Double[elements.size()];
        elements.toArray(elementArray);
        return elementArray;
    }

    public void removeAllElements() {
        elements.clear();
    }

	public void removeElement(int index) {
		elements.remove(index);
	}

	public void moveElement(int currentElementIndex, int newElementIndex) {
		if (currentElementIndex == newElementIndex) {
			return;
		}

		double tmp = elements.get(currentElementIndex);
		if (currentElementIndex < newElementIndex) {
			for (int i = currentElementIndex; i < newElementIndex; i++) {
				elements.set(i, elements.get(i + 1));
			}
		} else {
			for (int i = currentElementIndex; i > newElementIndex; i--) {
				elements.set(i, elements.get(i - 1));
			}
		}
		elements.set(newElementIndex, tmp);
	}

	// public void addScatterplotPoint(int x, int y) {
	// scatterplotPoints.add(new Point(x, y));
	// }
	//
	// public Point getScatterplotPoint(int idx) {
	// return scatterplotPoints.get(idx);
	// }
	//
	// public int getScatterplotPointCount() {
	// return scatterplotPoints.size();
	// }
	//
	// public ArrayList<Point> getScatterplotPoints() {
	// return scatterplotPoints;
	// }
	//
	public void setElement(int idx, double value) {
		elements.set(idx, value);
	}

	public void addElement(double value) {
		elements.add(value);
	}

	public boolean equals(Tuple tuple) {
		if (tuple.getElementCount() == this.getElementCount()) {
			for (int i = 0; i < this.getElementCount(); i++) {
				if (tuple.getElement(i) != this.getElement(i)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	public double getElement(int idx) {
		return elements.get(idx);
	}

	public int getElementCount() {
		return elements.size();
	}

	public void setQueryFlag(boolean enabled) {
		queryFlag = enabled;
	}

	public boolean getQueryFlag() {
		return queryFlag;
	}

	public void setOrderFactor(int order) {
		orderFactor = order;
	}

	public int getOrderFactor() {
		return orderFactor;
	}

	//
	// public int getValueYPosition (int index) {
	// return ((Integer)yPositions.get(index)).intValue();
	// }
	//
	// public void addValueYPosition (int position) {
	// yPositions.add(position);
	// }
	//
	// public int getValueXPosition (int index) {
	// return ((Integer)xPositions.get(index)).intValue();
	// }
	//
	// public void addValueXPosition (int position) {
	// //xPositions.ensureCapacity(index+1);
	// xPositions.add(position);
	// }
	//
	// public void clearPositions () {
	// xPositions.clear();
	// yPositions.clear();
	// scatterplotPoints.clear();
	// }
	//
	public int compareTo(Object object) {
		int otherOrderFactor = ((Tuple) object).getOrderFactor();
		if (orderFactor < otherOrderFactor) {
			return 1;
		} else if (orderFactor > otherOrderFactor) {
			return -1;
		}
		return 0;
	}

	// // TODO move outside this function into the PCPanel.
	// public Path2D.Float getPCPolyline() {
	// Path2D.Float path = new Path2D.Float();
	// // path.moveTo(screen_x, screen_y);
	//
	// for (int i = 0; i < xPositions.size(); i++) {
	// int x = ((Number)xPositions.get(i)).intValue();
	// int y = ((Number)yPositions.get(i)).intValue();
	// if (i == 0) {
	// path.moveTo(x, y);
	// } else {
	// path.lineTo(x, y);
	// }
	// }
	// return path;
	// }
}
