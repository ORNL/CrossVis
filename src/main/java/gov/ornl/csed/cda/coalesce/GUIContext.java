package gov.ornl.csed.cda.coalesce;

import gov.ornl.csed.cda.datatable.*;
import gov.ornl.csed.cda.pcvis.PCPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

public class GUIContext {
	public static final String PROPERTIES = "PROPERTIES";

	private static GUIContext instance;
	private HashMap<String, Object> components = new HashMap<String, Object>();

	public static GUIContext getInstance() {
		if (instance == null) {
			instance = new GUIContext();
		}
		return instance;
	}

	protected GUIContext() {
		initialize();
	}

	protected void initialize() {

	}

	public Object getComponent(String componentName) {
		return components.get(componentName);
	}

	public void registerComponent(String componentName, Object component) {
		components.put(componentName, component);
	}

	public void deregisterComponent(String componentName) {
		components.remove(componentName);
	}

	public GUIProperties getProperties() {
		return (GUIProperties) components.get(PROPERTIES);
	}




}
