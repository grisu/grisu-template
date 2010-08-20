package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.apache.commons.lang.StringUtils;
import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.frontend.view.swing.files.GrisuFileDialog;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import org.vpac.grisu.model.files.GlazedFile;
import org.vpac.grisu.model.job.JobSubmissionObjectImpl;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class SingleInputFile extends AbstractInputPanel {
	private JComboBox comboBox;
	private JButton button;
	private GrisuFileDialog dialog;

	private String selectedFile = null;

	private DefaultComboBoxModel comboBoxModel;
	private JLabel label;

	public SingleInputFile(String name, PanelConfig config)
			throws TemplateException {

		super(name, config);

		if (!displayHelpLabel()) {
			setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("center:max(35dlu;default):grow"),
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC, }));
			add(getComboBox(), "2, 2, fill, default");
			add(getButton(), "4, 2");
		} else {
			setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("center:max(35dlu;default):grow"),
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC, }));
			add(getComboBox(), "2, 2, fill, default");
			add(getButton(), "4, 2");
			add(getHelpLabel(), "6, 2");
		}

		// config.addValidator(new FileExistsValidator());
	}

	private void fileChanged() {

		if (!isInitFinished()) {
			return;
		}

		if (selectedFile != null) {
			removeValue("inputFileUrl", selectedFile);
		}
		selectedFile = (String) getComboBox().getSelectedItem();

		addValue("inputFileUrl", selectedFile);

		addHistoryValue(selectedFile);

	}

	private JButton getButton() {
		if (button == null) {
			button = new JButton("Browse");
			button.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					if (getServiceInterface() == null) {
						myLogger.error("ServiceInterface not set yet.");
						return;
					}

					GlazedFile file = popupFileDialogAndAskForFile();

					if (file == null) {
						return;
					}

					getComboBox().setSelectedItem(file.getUrl());

				}
			});
		}
		return button;
	}

	private JComboBox getComboBox() {
		if (comboBox == null) {
			comboBoxModel = new DefaultComboBoxModel();
			comboBox = new JComboBox(comboBoxModel);
			comboBox.setPrototypeDisplayValue("xxxxx");
			comboBox.setEditable(true);
			comboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (ItemEvent.SELECTED == e.getStateChange()) {

						if (isInitFinished()) {
							fileChanged();
						}

					}
				}
			});

			comboBox.getEditor().getEditorComponent()
					.addKeyListener(new KeyAdapter() {
						@Override
						public void keyReleased(KeyEvent e) {
							fileChanged();
						}
					});
		}
		return comboBox;
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		Map<String, String> defaultProperties = new HashMap<String, String>();
		defaultProperties.put(TITLE, "Input file");
		defaultProperties.put(HISTORY_ITEMS, "8");

		return defaultProperties;
	}

	// private GrisuFileDialog getFileDialog() {
	//
	// if ( si == null ) {
	// return null;
	// }
	//
	// if ( dialog == null ) {
	// dialog = new GrisuFileDialog(si);
	// dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	//
	// }
	// return dialog;
	// }

	@Override
	protected String getValueAsString() {
		return ((String) getComboBox().getSelectedItem());
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties) {

		getComboBox().removeAllItems();

		String prefills = panelProperties.get(PREFILLS);
		if (StringUtils.isNotBlank(prefills)) {

			for (String value : prefills.split(",")) {
				comboBoxModel.addElement(value);
			}

		}

		if (useHistory()) {
			for (String value : getHistoryValues()) {
				if (comboBoxModel.getIndexOf(value) < 0) {
					comboBoxModel.addElement(value);
				}
			}
		}

	}

	@Override
	void setInitialValue() throws TemplateException {

		if (fillDefaultValueIntoFieldWhenPreparingPanel()) {
			getJobSubmissionObject().addInputFileUrl(getDefaultValue());
			getComboBox().setSelectedItem(getDefaultValue());
		} else {
			getComboBox().addItem("");
			getComboBox().setSelectedItem("");
		}

	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {

		if (useHistory()) {
			addValueToHistory();
		}
	}

}
