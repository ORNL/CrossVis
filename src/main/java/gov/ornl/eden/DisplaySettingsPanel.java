package gov.ornl.eden;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DisplaySettingsPanel extends JPanel implements ChangeListener,
		ActionListener {
	private static final long serialVersionUID = 1L;

	private final Logger log = LoggerFactory.getLogger(PCPanel.class);

	private ArrayList<DisplaySettingsPanelListener> listeners = new ArrayList<DisplaySettingsPanelListener>();

	private ColorIcon selectedDataColorIcon;
	private JButton selectedDataColorButton;
	private JSlider selectedDataAlphaSlider;
	private JSpinner selectedDataAlphaSpinner;
	private SpinnerNumberModel selectedDataAlphaSpinnerModel;

	private ColorIcon unselectedDataColorIcon;
	private JButton unselectedDataColorButton;
	private JSlider unselectedDataAlphaSlider;
	private JSpinner unselectedDataAlphaSpinner;
	private SpinnerNumberModel unselectedDataAlphaSpinnerModel;

	private SpinnerNumberModel titleFontSpinnerModel;

	private JSpinner titleFontSpinner;

	private SpinnerNumberModel secondaryFontSpinnerModel;

	private JSpinner secondaryFontSpinner;

	private SpinnerNumberModel axisWidthSpinnerModel;

	private JSpinner axisWidthSpinner;

	private SpinnerNumberModel correlationBoxHeightSpinnerModel;

	private JSpinner correlationBoxHeightSpinner;

	private SpinnerNumberModel pcpLineSizeSpinnerModel;

	private JSpinner pcpLineSizeSpinner;

	public DisplaySettingsPanel(Color selectedDataColor,
			Color unselectedDataColor) {
		layoutPanel(selectedDataColor, unselectedDataColor);
	}

	public void addPCDisplaySettingsPanelListener(
			DisplaySettingsPanelListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	private void layoutPanel(Color selectedDataColor, Color unselectedDataColor) {
		JPanel mainPanel = new JPanel();
		GridBagConstraints gbc = new GridBagConstraints();
		mainPanel.setLayout(new GridBagLayout());

		// Add selected data color fields
		JLabel label = new JLabel("Selected Data Color: ");
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.LINE_START;

		mainPanel.add(label, gbc);

		selectedDataColorIcon = new ColorIcon(selectedDataColor);
		selectedDataColorButton = new JButton(selectedDataColorIcon);
		selectedDataColorButton.setBackground(Color.white);
		selectedDataColorButton.addActionListener(this);
		gbc.gridx = 1;
		mainPanel.add(selectedDataColorButton, gbc);

		label = new JLabel(" Tranparency: ");
		gbc.gridx = 2;
		mainPanel.add(label, gbc);

		int initialAlphaValue = (int) ((double) (selectedDataColor.getAlpha() / 255.) * 100);
		selectedDataAlphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 100,
				initialAlphaValue);
		selectedDataAlphaSlider.addChangeListener(this);
		gbc.gridx = 3;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(selectedDataAlphaSlider, gbc);

		gbc.gridx = 5;
		gbc.weightx = 1.f;
		gbc.anchor = GridBagConstraints.LINE_START;
		selectedDataAlphaSpinnerModel = new SpinnerNumberModel(
				initialAlphaValue, 0, 100, 1);
		selectedDataAlphaSpinner = new JSpinner(selectedDataAlphaSpinnerModel);
		selectedDataAlphaSpinner.addChangeListener(this);
		mainPanel.add(selectedDataAlphaSpinner, gbc);

		// Add unselected data color fields
		label = new JLabel("Unselected Data Color: ");
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 1;
		gbc.weightx = 0.f;
		gbc.anchor = GridBagConstraints.LINE_START;

		mainPanel.add(label, gbc);

		unselectedDataColorIcon = new ColorIcon(unselectedDataColor);
		unselectedDataColorButton = new JButton(unselectedDataColorIcon);
		unselectedDataColorButton.setBackground(Color.white);
		unselectedDataColorButton.addActionListener(this);
		gbc.gridx = 1;
		mainPanel.add(unselectedDataColorButton, gbc);

		label = new JLabel(" Tranparency: ");
		gbc.gridx = 2;
		mainPanel.add(label, gbc);

		initialAlphaValue = (int) ((double) (unselectedDataColor.getAlpha() / 255.) * 100);
		unselectedDataAlphaSlider = new JSlider(JSlider.HORIZONTAL, 0, 100,
				initialAlphaValue);
		unselectedDataAlphaSlider.addChangeListener(this);
		gbc.gridx = 3;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(unselectedDataAlphaSlider, gbc);

		gbc.gridx = 5;
		gbc.weightx = 1.f;
		gbc.anchor = GridBagConstraints.LINE_START;
		unselectedDataAlphaSpinnerModel = new SpinnerNumberModel(
				initialAlphaValue, 0, 100, 1);
		unselectedDataAlphaSpinner = new JSpinner(
				unselectedDataAlphaSpinnerModel);
		unselectedDataAlphaSpinner.addChangeListener(this);
		mainPanel.add(unselectedDataAlphaSpinner, gbc);

		label = new JLabel("Axis Label Font Size: ");
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 1;
		gbc.weightx = 0.f;
		gbc.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(label, gbc);

		gbc.gridx = 1;
		titleFontSpinnerModel = new SpinnerNumberModel(10, 10, 120, 5);
		titleFontSpinner = new JSpinner(titleFontSpinnerModel);
		titleFontSpinner.addChangeListener(this);
		mainPanel.add(titleFontSpinner, gbc);

		label = new JLabel("Axis Secondary Font Size: ");
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 1;
		gbc.weightx = 0.f;
		gbc.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(label, gbc);

		gbc.gridx = 1;
		secondaryFontSpinnerModel = new SpinnerNumberModel(10, 10, 120, 5);
		secondaryFontSpinner = new JSpinner(secondaryFontSpinnerModel);
		secondaryFontSpinner.addChangeListener(this);
		mainPanel.add(secondaryFontSpinner, gbc);

		label = new JLabel("Axis Width: ");
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.gridwidth = 1;
		gbc.weightx = 0.f;
		gbc.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(label, gbc);

		gbc.gridx = 1;
		axisWidthSpinnerModel = new SpinnerNumberModel(10, 10, 120, 5);
		axisWidthSpinner = new JSpinner(axisWidthSpinnerModel);
		axisWidthSpinner.addChangeListener(this);
		mainPanel.add(axisWidthSpinner, gbc);

		label = new JLabel("Axis Correlation Box Height: ");
		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 1;
		gbc.weightx = 0.f;
		gbc.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(label, gbc);

		gbc.gridx = 1;
		correlationBoxHeightSpinnerModel = new SpinnerNumberModel(10, 10, 120,
				5);
		correlationBoxHeightSpinner = new JSpinner(
				correlationBoxHeightSpinnerModel);
		correlationBoxHeightSpinner.addChangeListener(this);
		mainPanel.add(correlationBoxHeightSpinner, gbc);

		label = new JLabel("Parallel Coordinate Line Size: ");
		gbc.gridx = 0;
		gbc.gridy = 6;
		gbc.gridwidth = 1;
		gbc.weightx = 0.f;
		gbc.anchor = GridBagConstraints.LINE_START;
		mainPanel.add(label, gbc);

		gbc.gridx = 1;
		pcpLineSizeSpinnerModel = new SpinnerNumberModel(2, 1, 30, 1);
		pcpLineSizeSpinner = new JSpinner(pcpLineSizeSpinnerModel);
		pcpLineSizeSpinner.addChangeListener(this);
		mainPanel.add(pcpLineSizeSpinner, gbc);

		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.NORTH);
	}

	private void fireSelectedDataColorChanged() {
		Color color = selectedDataColorIcon.getColor();
		for (DisplaySettingsPanelListener listener : listeners) {
			listener.selectedDataColorChanged(color);
		}
	}

	private void fireUnselectedDataColorChanged() {
		Color color = unselectedDataColorIcon.getColor();
		for (DisplaySettingsPanelListener listener : listeners) {
			listener.unselectedDataColorChanged(color);
		}
	}

	private void notifyListenersSecondaryFontSizeChanged(int fontSize) {
		for (DisplaySettingsPanelListener listener : listeners) {
			listener.secondaryFontSizeChanged(fontSize);
		}
	}

	private void notifyListenersTitleFontSizeChanged(int fontSize) {
		for (DisplaySettingsPanelListener listener : listeners) {
			listener.titleFontSizeChanged(fontSize);
		}
	}

	private void notifyListenersCorrelationBoxHeightChanged(int height) {
		for (DisplaySettingsPanelListener listener : listeners) {
			listener.correlationBoxSizeChanged(height);
		}
	}

	private void notifyListenersAxisWidthChanged(int width) {
		for (DisplaySettingsPanelListener listener : listeners) {
			listener.axisWidthChanged(width);
		}
	}

	private void notifyListenersPCLineSizeChanged(int size) {
		for (DisplaySettingsPanelListener listener : listeners) {
			listener.pcLineSizeChanged(size);
		}
	}

	@Override
	public void stateChanged(ChangeEvent event) {
		if (event.getSource() == selectedDataAlphaSlider) {
			if (!selectedDataAlphaSlider.getValueIsAdjusting()) {
				double alpha = selectedDataAlphaSlider.getValue() / 100.;
				int iAlpha = (int) (255 * alpha);
				Color color = new Color(selectedDataColorIcon.getColor()
						.getRed(), selectedDataColorIcon.getColor().getGreen(),
						selectedDataColorIcon.getColor().getBlue(), iAlpha);
				selectedDataColorIcon.setColor(color);
				repaint();
				fireSelectedDataColorChanged();
			}
			selectedDataAlphaSpinnerModel.setValue(selectedDataAlphaSlider
					.getValue());
		} else if (event.getSource() == selectedDataAlphaSpinner) {
			int spinnerValue = ((Number) selectedDataAlphaSpinnerModel
					.getValue()).intValue();
			double alpha = spinnerValue / 100.;
			int iAlpha = (int) (255 * alpha);
			selectedDataAlphaSlider.setValue(spinnerValue);
		} else if (event.getSource() == unselectedDataAlphaSlider) {
			if (!unselectedDataAlphaSlider.getValueIsAdjusting()) {
				double alpha = unselectedDataAlphaSlider.getValue() / 100.;
				int iAlpha = (int) (255 * alpha);
				Color color = new Color(unselectedDataColorIcon.getColor()
						.getRed(), unselectedDataColorIcon.getColor()
						.getGreen(), unselectedDataColorIcon.getColor()
						.getBlue(), iAlpha);
				unselectedDataColorIcon.setColor(color);
				repaint();
				fireUnselectedDataColorChanged();
			}
			unselectedDataAlphaSpinnerModel.setValue(unselectedDataAlphaSlider
					.getValue());
		} else if (event.getSource() == unselectedDataAlphaSpinner) {
			int spinnerValue = ((Number) unselectedDataAlphaSpinnerModel
					.getValue()).intValue();
			double alpha = spinnerValue / 100.;
			int iAlpha = (int) (255 * alpha);
			unselectedDataAlphaSlider.setValue(spinnerValue);
		} else if (event.getSource() == titleFontSpinner) {
			int fontSize = ((Number) titleFontSpinner.getValue()).intValue();
			notifyListenersTitleFontSizeChanged(fontSize);
		} else if (event.getSource() == secondaryFontSpinner) {
			int fontSize = ((Number) secondaryFontSpinner.getValue())
					.intValue();
			notifyListenersSecondaryFontSizeChanged(fontSize);
		} else if (event.getSource() == axisWidthSpinner) {
			int axisWidth = ((Number) axisWidthSpinner.getValue()).intValue();
			notifyListenersAxisWidthChanged(axisWidth);
		} else if (event.getSource() == correlationBoxHeightSpinner) {
			int height = ((Number) correlationBoxHeightSpinner.getValue())
					.intValue();
			notifyListenersCorrelationBoxHeightChanged(height);
		} else if (event.getSource() == pcpLineSizeSpinner) {
			int pcLineSize = ((Number) pcpLineSizeSpinner.getValue())
					.intValue();
			notifyListenersPCLineSizeChanged(pcLineSize);
		}
		// if (event.getSource() == opacitySlider) {
		// if (!opacitySlider.getValueIsAdjusting()) {
		// double alpha = opacitySlider.getValue()/100.;
		// // pcPanel.setAlphaValue((int)(255 * alpha));
		// int iAlpha = (int)(255*alpha);
		// fireAlphaValueChanged(iAlpha);
		// }
		// opacitySpinnerModel.setValue(opacitySlider.getValue());
		// } else if (event.getSource() == opacitySpinner) {
		// int spinnerValue =
		// ((Number)opacitySpinnerModel.getValue()).intValue();
		// double alpha = spinnerValue/100.;
		// // pcPanel.setAlphaValue((int)(255 * alpha));
		// int iAlpha = (int)(255*alpha);
		// fireAlphaValueChanged(iAlpha);
		// opacitySlider.setValue(spinnerValue);
		// }
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == this.selectedDataColorButton) {
			Color color = selectedDataColorIcon.getColor();
			int alpha = color.getAlpha();
			color = JColorChooser.showDialog(this,
					"Choose Selected Data Color", color);
			if (color != null) {
				color = new Color(color.getRed(), color.getGreen(),
						color.getBlue(), alpha);
				selectedDataColorIcon.setColor(color);
				int newSpinnerValue = (int) ((double) (color.getAlpha() / 255.) * 100);
				selectedDataAlphaSpinnerModel.setValue(newSpinnerValue);
				repaint();
				fireSelectedDataColorChanged();
			}
		} else if (event.getSource() == unselectedDataColorButton) {
			Color color = selectedDataColorIcon.getColor();
			int alpha = color.getAlpha();
			color = JColorChooser.showDialog(this,
					"Choose Unselected Data Color", color);
			if (color != null) {
				color = new Color(color.getRed(), color.getGreen(),
						color.getBlue(), alpha);
				unselectedDataColorIcon.setColor(color);
				int newSpinnerValue = (int) ((double) (color.getAlpha() / 255.) * 100);
				unselectedDataAlphaSpinnerModel.setValue(newSpinnerValue);
				repaint();
				fireUnselectedDataColorChanged();
			}
		}
	}
}
