package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.TemplateException;
import grisu.frontend.control.clientexceptions.FileTransactionException;
import grisu.frontend.view.swing.files.GridFileSelectionDialog;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.model.FileManager;
import grisu.model.GrisuRegistryManager;
import grisu.model.dto.GridFile;
import grisu.model.job.JobSubmissionObjectImpl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

import com.Ostermiller.util.LineEnds;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
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

			if (documentChanged) {
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

	private StandaloneTextArea textArea;
	protected boolean documentChanged = false;
	private JideSplitButton saveButton;
	private JLabel label_1;

	// private final Validator<String> val = new InputChangedValidator();

	public TextFile(String name, PanelConfig config) throws TemplateException {

		super(name, config);

		if (!displayHelpLabel()) {
			setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("center:max(13dlu;default)"),
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("center:max(35dlu;default):grow"),
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("max(40dlu;default):grow"),
					FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC, }));

			add(getTextArea(), "2, 2, 7, 1, fill, fill");
			add(getComboBox(), "2, 4, 3, 1, fill, default");
			add(getOpenButton(), "6, 4, fill, fill");
			add(getSaveButton(), "8, 4, fill, fill");


		} else {
			setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("center:max(13dlu;default)"),
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("center:max(35dlu;default):grow"),
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("max(40dlu;default):grow"),
					FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC, }));

			add(getTextArea(), "2, 2, 7, 1, fill, fill");
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
			getTextArea().setText("");
			return;
		}

		try {
			final GridFile file = GrisuRegistryManager
					.getDefault(getServiceInterface()).getFileManager()
					.ls(selectedFile);

			loadFile(file);

			addValue("inputFileUrl", selectedFile);

			addHistoryValue(selectedFile);
		} catch (final Exception e) {
			myLogger.error(e);
		}

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

					loadFile(file);

				}
			});
		}
		return openButton;
	}
	
	private void saveAs() {
		// TODO write grid save dialog
		final JFileChooser fc = new JFileChooser();
		final int returnVal = fc.showDialog(TextFile.this,
				"Save as...");

		if (JFileChooser.CANCEL_OPTION == returnVal) {
			return;
		} else {
			String currentUrl = null;
			final File selFile = fc.getSelectedFile();
			currentUrl = selFile.toURI().toString();
			
//			try {
//				FileUtils.forceDelete(FileManager
//						.getFileFromUriOrPath(currentUrl));
//			} catch (final Exception e2) {
//				// doesn't matter
//				myLogger.debug(e2);
//			}
//			try {
//				FileUtils.writeStringToFile(FileManager
//						.getFileFromUriOrPath(currentUrl),
//						getTextArea().getText());
//			} catch (final IOException e1) {
//				myLogger.error(e1);
//			}
//
//			documentChanged = false;

			save(currentUrl);

			getComboBox().addItem(currentUrl);
			getComboBox().setSelectedItem(currentUrl);
			

			return;
		}
	}
	
	private void save(String path) {
		final FileManager fm = GrisuRegistryManager.getDefault(
				getServiceInterface()).getFileManager();
		String text = getTextArea().getText();
		File tempFile = null;
		try {
			InputStream is = new ByteArrayInputStream(text.getBytes());
			tempFile = File.createTempFile("input_file", "grisu");
			
			FileOutputStream fop = new FileOutputStream(tempFile);
			
			LineEnds.convert(is, fop, LineEnds.STYLE_UNIX);
			fop.flush();
			fop.close();
			
		} catch (Exception e3) {
			e3.printStackTrace();
			return;
		}

		if (FileManager.isLocal(path)) {
			try {
				FileUtils.copyFile(tempFile, FileManager
						.getFileFromUriOrPath(path));
				FileUtils.deleteQuietly(tempFile);
			} catch (final IOException e1) {
				myLogger.error(e1);
			}
		} else {

			final File temp = fm.getLocalCacheFile(path);
			try {
				FileUtils.copyFile(tempFile, temp);

				fm.uploadFile(temp, path, true);
				
				FileUtils.deleteQuietly(tempFile);
			} catch (final IOException e1) {
				myLogger.error(e1);
			} catch (final FileTransactionException e2) {
				myLogger.error(e2);
			}

		}
		
		documentChanged = false;
		
	}
	
	private void save() {
		
		String currentUrl = (String) getComboBox()
				.getSelectedItem();

		if (StringUtils.isBlank(currentUrl)) {

			saveAs();
			return;

		} else {
			save(currentUrl);
		}

	}

	private JideSplitButton getSaveButton() {
		if (saveButton == null) {
			saveButton = new JideSplitButton("Save");
			saveButton.setButtonStyle(JideSplitButton.TOOLBOX_STYLE);
			saveButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					myLogger.debug("Saving file...");
					save();

				}
			});
			
	        saveButton.add(new AbstractAction("Save as...") {
	            public void actionPerformed(ActionEvent e) {
	            	myLogger.debug("Saving file as...");
	            	saveAs();
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

	private StandaloneTextArea getTextArea() {

		if (textArea == null) {
			textArea = StandaloneTextArea.createTextArea();

			final Mode mode = new Mode("text");
			mode.setProperty("file", "text.xml");
			ModeProvider.instance.addMode(mode);
			textArea.getBuffer().setMode(mode);
			textArea.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					documentChanged = true;
//					getSaveButton().setEnabled(true);

					// TODO fire validation request
					// getTemplateObject().validateManually();
				}

			});
		}
		return textArea;
	}

	@Override
	protected String getValueAsString() {
		return ((String) getComboBox().getSelectedItem());
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

	}

	public void loadFile(GridFile gfile) {

		final FileManager fm = GrisuRegistryManager.getDefault(
				getServiceInterface()).getFileManager();

		File file;
		try {
			file = fm.downloadFile(gfile.getUrl());
		} catch (final FileTransactionException e1) {
			myLogger.error(e1);
			return;
		}

		String text;
		try {
			text = FileUtils.readFileToString(file);
		} catch (final IOException e) {
			myLogger.error(e);
			return;
		}

		getTextArea().setText(text);

		documentChanged = false;
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

			final Mode mode = new Mode(modeName);
			mode.setProperty("file", mode + ".xml");
			ModeProvider.instance.addMode(mode);
			textArea.getBuffer().setMode(mode);
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
			getTextArea().setText("");
		}

	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {

		if (useHistory()) {
			addValueToHistory();
		}
	}

}
