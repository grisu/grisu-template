package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.JobnameHelpers;
import grisu.control.ServiceInterface;
import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.files.GridFileSelectionDialog;
import grisu.frontend.view.swing.files.GridFileTextEditPanel;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.model.GrisuRegistryManager;
import grisu.model.dto.GridFile;
import grisu.model.job.JobDescription;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import com.jidesoft.swing.JideButton;
import com.jidesoft.swing.JideSplitButton;

public class TextFile extends AbstractInputPanel {

	private class InputChangedValidator implements Validator {

		private ServiceInterface si;

		public void setServiceInterface(ServiceInterface si) {
			this.si = si;
		}

		@Override
		public boolean validate(Problems arg0, String arg1, Object arg2) {

			if (getGrisuTextArea().isDocumentUnsaved()) {
				arg0.add("Input file changed. Please save first.");
				return false;
			}
			return true;
		}
	}

	private JComboBox comboBox;
	private JideButton openButton;
	private GridFileSelectionDialog dialog;

	private String selectedFile = null;

	private DefaultComboBoxModel comboBoxModel;
	private JLabel label;

//	protected boolean documentChanged = false;
	private JideSplitButton saveButton;
	private JLabel label_1;
	
	private GridFileTextEditPanel grisuTextArea = null;

	// private final Validator<String> val = new InputChangedValidator();

	public TextFile(String name, PanelConfig config) throws TemplateException {

		super(name, config);

		if (!displayHelpLabel()) {
			setLayout(new FormLayout(new ColumnSpec[] {
					FormSpecs.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("center:max(13dlu;default)"),
					FormSpecs.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("center:max(35dlu;default):grow"),
					FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
					FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
					FormSpecs.RELATED_GAP_COLSPEC, }, new RowSpec[] {
					FormSpecs.RELATED_GAP_ROWSPEC,
					RowSpec.decode("max(40dlu;default):grow"),
					FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
					FormSpecs.RELATED_GAP_ROWSPEC, }));

			add(getGrisuTextArea(), "2, 2, 7, 1, fill, fill");
			add(getComboBox(), "2, 4, 3, 1, fill, default");
			add(getOpenButton(), "6, 4, fill, fill");
			add(getSaveButton(), "8, 4, fill, fill");


		} else {
			setLayout(new FormLayout(new ColumnSpec[] {
					FormSpecs.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("center:max(13dlu;default)"),
					FormSpecs.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("center:max(35dlu;default):grow"),
					FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
					FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
					FormSpecs.RELATED_GAP_COLSPEC, }, new RowSpec[] {
					FormSpecs.RELATED_GAP_ROWSPEC,
					RowSpec.decode("max(40dlu;default):grow"),
					FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
					FormSpecs.RELATED_GAP_ROWSPEC, }));

			add(getGrisuTextArea(), "2, 2, 7, 1, fill, fill");
			add(getComboBox(), "4, 4, fill, default");
			add(getOpenButton(), "6, 4, fill, fill");
			add(getHelpLabel(), "2, 4");
			add(getSaveButton(), "8, 4, fill, fill");
		}

		// Validator<String> val2 = Validators.REQUIRE_NON_EMPTY_STRING;
		// config.addValidator(val);
		// config.addValidator(val2);
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

		if (StringUtils.isBlank(selectedFile)) {
			getGrisuTextArea().setText("");
			return;
		}

		try {
			final GridFile file = GrisuRegistryManager
					.getDefault(getServiceInterface()).getFileManager()
					.ls(selectedFile);

			loadFile(file);

			addValue("inputFileUrl", selectedFile);

			// setting jobname if configured in widget config
			String jobnameCreate = getPanelProperty(SingleInputFile.SET_JOBNAME);
			if ( "true".equalsIgnoreCase(jobnameCreate) || "count".equalsIgnoreCase(jobnameCreate) ) {
				
				String jobname = FilenameUtils.getBaseName(selectedFile);
				final String sugJobname = getUserEnvironmentManager()
						.calculateUniqueJobname(jobname);

				try {
					setValue("jobname", sugJobname);
				} catch (TemplateException e) {
					myLogger.debug("Can't set jobname:"+e.getLocalizedMessage());
				}
			} else if ( "timestamp".equalsIgnoreCase(jobnameCreate) ) {
				String jobname = FilenameUtils.getBaseName(selectedFile);
				final String sugJobname = JobnameHelpers.calculateTimestampedJobname(jobname, JobnameHelpers.short_format);

				try {
					setValue("jobname", sugJobname);
				} catch (TemplateException e) {
					myLogger.debug("Can't set jobname:"+e.getLocalizedMessage());
				}
			}
			

			addHistoryValue(selectedFile);
		} catch (final Exception e) {
			myLogger.error(e);
		}

	}
	
	private GridFileTextEditPanel getGrisuTextArea() {
		if ( grisuTextArea == null ) {
			grisuTextArea = new GridFileTextEditPanel(templateName);
		}
		return grisuTextArea;
	}

	private JideButton getOpenButton() {
		if (openButton == null) {
			openButton = new JideButton("Open");
			openButton.setButtonStyle(JideSplitButton.TOOLBOX_STYLE);
			openButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (getServiceInterface() == null) {
						myLogger.error("ServiceInterface not set yet.");
						return;
					}

					final GridFile file = popupFileDialogAndAskForFile();

					if (file == null) {
						return;
					}

					getComboBox().addItem(file.getUrl());
					getComboBox().setSelectedItem(file.getUrl());

//					loadFile(file);

				}
			});
		}
		return openButton;
	}
	
	public GridFileSelectionDialog getFileDialog() {
		return getFileDialog(templateName);
	}

	private JideSplitButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JideSplitButton("Save");
			saveButton.setButtonStyle(JideSplitButton.TOOLBOX_STYLE);
			saveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					myLogger.debug("Saving file...");
					getGrisuTextArea().save();

				}
			});
			
	        saveButton.add(new AbstractAction("Save as...") {
	            public void actionPerformed(ActionEvent e) {
	            	myLogger.debug("Saving file as...");
	            	File file = getGrisuTextArea().saveAs();
	            	if ( file != null ) {
	            		getComboBox().addItem(file.getAbsolutePath());
	            		getComboBox().setSelectedItem(file.getAbsolutePath());
	            	}
	            }
	        });
			
	        saveButton.getPopupMenu().addPopupMenuListener(new PopupMenuListener() {
	            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	                myLogger.debug("menu is clicked");
	            }

	            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
	            }

	            public void popupMenuCanceled(PopupMenuEvent e) {
	            }
	        });
		}
		return saveButton;
	}

	private JComboBox getComboBox() {
		if (comboBox == null) {
			comboBox = new JComboBox(getComboBoxModel());
			comboBox.setPrototypeDisplayValue("xxxxx");
			comboBox.setEditable(false);
			comboBox.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent e) {
					if (ItemEvent.SELECTED == e.getStateChange()) {

						if (isInitFinished()) {
							fileChanged();
						}

					}
				}
			});

			// comboBox.getEditor().getEditorComponent().addKeyListener(
			// new KeyAdapter() {
			// @Override
			// public void keyReleased(KeyEvent e) {
			// fileChanged();
			// }
			// });
		}
		return comboBox;
	}

	private DefaultComboBoxModel getComboBoxModel() {
		if (comboBoxModel == null) {
			comboBoxModel = new DefaultComboBoxModel();
		}
		return comboBoxModel;
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		final Map<String, String> defaultProperties = new HashMap<String, String>();
		defaultProperties.put(TITLE, "Input file");
		defaultProperties.put(HISTORY_ITEMS, "8");
		defaultProperties.put("mode", "text");
		defaultProperties.put(FILL_WITH_DEFAULT_VALUE, "false");

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
	public JComboBox getJComboBox() {
		return getComboBox();
	}



	@Override
	protected String getValueAsString() {
		return ((String) getComboBox().getSelectedItem());
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

	}
	
	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		String prop = arg0.getPropertyName();
		
		if ( "documentUnsaved".equals(prop) ) {
			if ( (Boolean)(arg0.getNewValue()) ) {
				myLogger.debug("Text unsaved now.");
			} else {
				myLogger.debug("Text saved.");
			}
		}
	}
	
	public void loadFile(GridFile gfile) {

		getGrisuTextArea().setFile(gfile);

//		getSaveButton().setEnabled(false);

	}

	@Override
	protected void preparePanel(final Map<String, String> panelProperties) {

		getComboBox().removeAllItems();

		final String prefills = panelProperties.get(PREFILLS);
		if (StringUtils.isNotBlank(prefills)) {

			for (final String value : prefills.split(",")) {
				getComboBoxModel().addElement(value);
			}

		}

		if (useHistory()) {
			for (final String value : getHistoryValues()) {
				if (getComboBoxModel().getIndexOf(value) < 0) {
					getComboBoxModel().addElement(value);
				}
			}
		}

		final String modeName = panelProperties.get("mode");

		if (StringUtils.isNotBlank(modeName)) {
			
			getGrisuTextArea().setTextMode(modeName);
		}

	}

	@Override
	void setInitialValue() throws TemplateException {

		if (fillDefaultValueIntoFieldWhenPreparingPanel()) {
			try {
				if (StringUtils.isNotBlank(getDefaultValue())) {
					final GridFile file = GrisuRegistryManager
							.getDefault(getServiceInterface()).getFileManager()
							.ls(getDefaultValue());

					loadFile(file);
					getJobSubmissionObject().addInputFileUrl(getDefaultValue());
					getComboBox().setSelectedItem(getDefaultValue());
				}
			} catch (final Exception e) {
				myLogger.error(e);
			}
		} else {
			getComboBox().addItem("");
			getComboBox().setSelectedItem("");
			getGrisuTextArea().setText("");
		}

	}
	
	@Override
	public void setServiceInterface(ServiceInterface si) {
		super.setServiceInterface(si);
		getGrisuTextArea().setServiceInterface(si);
	}

	@Override
	protected void templateRefresh(JobDescription jobObject) {

		if (useHistory()) {
			addValueToHistory();
		}
	}

}
