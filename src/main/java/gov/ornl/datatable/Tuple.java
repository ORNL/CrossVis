package gov.ornl.datatable;

import java.io.Serializable;
import java.util.ArrayList;

public class Tuple implements Serializable {
	private ArrayList<Object> elements = new ArrayList<>();
	private boolean queryFlag = true;
	private int orderFactor = 0;

	public Tuple() {

	}

	public Tuple createCopy() {
		Tuple copyTuple = new Tuple();
		for (Object element : elements) {
			copyTuple.addElement(element);
		}
		return copyTuple;
	}

    public Tuple (Tuple copyTuple) {
        for (int i = 0; i < copyTuple.getElementCount(); i++) {
            elements.add(copyTuple.getElement(i));
        }
    }

    public Object[] getElementsAsArray() {
		Object elementArray [] = new Object[elements.size()];
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

		Object tmp = elements.get(currentElementIndex);
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

	public void setElement(int idx, Object value) {
		elements.set(idx, value);
	}

	public void addElement(Object value) {
		elements.add(value);
	}

	public void addElement(int idx, Object value) {
		elements.add(idx, value);
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

	public Object getElement(int idx) {
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

	public int compareTo(Object object) {
		int otherOrderFactor = ((Tuple) object).getOrderFactor();
		if (orderFactor < otherOrderFactor) {
			return 1;
		} else if (orderFactor > otherOrderFactor) {
			return -1;
		}
		return 0;
	}
}
