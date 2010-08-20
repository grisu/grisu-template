package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.gjt.sp.jedit.Mode;
import org.gjt.sp.jedit.syntax.ModeProvider;
import org.gjt.sp.jedit.textarea.StandaloneTextArea;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.frontend.control.clientexceptions.FileTransactionException;
import org.vpac.grisu.frontend.view.swing.files.GrisuFileDialog;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import org.vpac.grisu.model.FileManager;
import org.vpac.grisu.model.GrisuRegistryManager;
import org.vpac.grisu.model.files.GlazedFile;
import org.vpac.grisu.model.job.JobSubmissionObjectImpl;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class TextFile extends AbstractInputPanel {

	private class InputChangedValidator implements Validator {

		private ServiceInterface si;

		public void setServiceInterface(ServiceInterface si) {
			this.si = si;
		}

		public boolean validate(Problems arg0, String arg1, Object arg2) {

			if (documentChanged) {
				arg0.add("Input file changed. Please save first.");
				return false;
			}
			return true;
		}
	}

	private JComboBox comboBox;
	private JButton button;
	private GrisuFileDialog dialog;

	private String selectedFile = null;

	private DefaultComboBoxModel comboBoxModel;
	private JLabel label;

	private StandaloneTextArea textArea;
	protected boolean documentChanged = false;
	private JButton button_1;
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
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("max(58dlu;default):grow"),
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC, }));

			add(getTextArea(), "2, 2, 7, 1, fill, fill");
			add(getComboBox(), "2,2,2,1, fill, default");
			add(getButton(), "6, 4");
			add(getButton_1(), "8, 4");
		} else {
			setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("center:max(13dlu;default)"),
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("center:max(35dlu;default):grow"),
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("max(58dlu;default):grow"),
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC, }));

			add(getTextArea(), "2, 2, 7, 1, fill, fill");
			add(getComboBox(), "4, 4, fill, default");
			add(getButton(), "6, 4");
			add(getHelpLabel(), "2, 4");
			add(getButton_1(), "8, 4");
		}

		// Validator<String> val2 = Validators.REQUIRE_NON_EMPTY_STRING;
		// config.addValidator(val);
		// config.addValidator(val2);
		// config.addValidator(new FileExistsValidator());
	}

	private StandaloneTextArea getTextArea() {

		if (textArea == null) {
			textArea = StandaloneTextArea.createTextArea();
			Mode mode = new Mode("text");
			mode.setProperty("file", "text.xml");
			ModeProvider.instance.addMode(mode);
			textArea.getBuffer().setMode(mode);
			textArea.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					documentChanged = true;
					getButton_1().setEnabled(true);

					// TODO fire validation request
					// getTemplateObject().validateManually();
				}

			});
		}
		return textArea;
	}

	public void loadFile(GlazedFile gfile) {

		FileManager fm = GrisuRegistryManager.getDefault(getServiceInterface())
				.getFileManager();

		File file;
		try {
			file = fm.downloadFile(gfile.getUrl());
		} catch (FileTransactionException e1) {
			e1.printStackTrace();
			return;
		}

		String text;
		try {
			text = FileUtils.readFileToString(file);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		getTextArea().setText(text);

		documentChanged = false;
		getButton_1().setEnabled(false);

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
			GlazedFile file = GrisuRegistryManager
					.getDefault(getServiceInterface()).getFileManager()
					.createGlazedFileFromUrl(selectedFile);
			loadFile(file);

			addValue("inputFileUrl", selectedFile);

			addHistoryValue(selectedFile);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private JButton getButton() {
		if (button == null) {
			button = new JButton("Open");
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

					getComboBox().addItem(file.getUrl());
					getComboBox().setSelectedItem(file.getUrl());

					loadFile(file);

				}
			});
		}
		return button;
	}

	private JComboBox getComboBox() {
		if (comboBox == null) {
			comboBox = new JComboBox(getComboBoxModel());
			comboBox.setPrototypeDisplayValue("xxxxx");
			comboBox.setEditable(false);
			comboBox.addItemListener(new ItemListener() {
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

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		Map<String, String> defaultProperties = new HashMap<String, String>();
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

	private DefaultComboBoxModel getComboBoxModel() {
		if (comboBoxModel == null) {
			comboBoxModel = new DefaultComboBoxModel();
		}
		return comboBoxModel;
	}

	@Override
	protected void preparePanel(final Map<String, String> panelProperties) {

		getComboBox().removeAllItems();

		String prefills = panelProperties.get(PREFILLS);
		if (StringUtils.isNotBlank(prefills)) {

			for (String value : prefills.split(",")) {
				getComboBoxModel().addElement(value);
			}

		}

		if (useHistory()) {
			for (String value : getHistoryValues()) {
				if (getComboBoxModel().getIndexOf(value) < 0) {
					getComboBoxModel().addElement(value);
				}
			}
		}

		String modeName = panelProperties.get("mode");

		if (StringUtils.isNotBlank(modeName)) {

			Mode mode = new Mode(modeName);
			mode.setProperty("file", mode + ".xml");
			ModeProvider.instance.addMode(mode);
			textArea.getBuffer().setMode(mode);
		}

	}

	@Override
	void setInitialValue() throws TemplateException {

		if (fillDefaultValueIntoFieldWhenPreparingPanel()) {
			try {
				GlazedFile file = GrisuRegistryManager
						.getDefault(getServiceInterface()).getFileManager()
						.createGlazedFileFromUrl(getDefaultValue());
				loadFile(file);
				getJobSubmissionObject().addInputFileUrl(getDefaultValue());
				getComboBox().setSelectedItem(getDefaultValue());
			} catch (Exception e) {
				e.printStackTrace();
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

	private JButton getButton_1() {
		if (button_1 == null) {
			button_1 = new JButton("Save");
			button_1.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					FileManager fm = GrisuRegistryManager.getDefault(
							getServiceInterface()).getFileManager();
					String currentUrl = (String) getComboBox()
							.getSelectedItem();

					if (StringUtils.isBlank(currentUrl)) {

						// TODO write grid save dialog
						JFileChooser fc = new JFileChooser();
						int returnVal = fc.showDialog(TextFile.this,
								"Save as...");

						if (JFileChooser.CANCEL_OPTION == returnVal) {
							return;
						} else {
							File selFile = fc.getSelectedFile();
							currentUrl = selFile.toURI().toString();

							try {
								FileUtils.forceDelete(fm
										.getFileFromUriOrPath(currentUrl));
							} catch (Exception e2) {
								// doesn't matter
								myLogger.debug(e2);
							}
							try {
								FileUtils.writeStringToFile(
										fm.getFileFromUriOrPath(currentUrl),
										getTextArea().getText());
							} catch (IOException e1) {
								e1.printStackTrace();
							}

							documentChanged = false;

							getComboBox().addItem(currentUrl);
							getComboBox().setSelectedItem(currentUrl);

							button_1.setEnabled(false);

							return;
						}

					}

					if (FileManager.isLocal(currentUrl)) {
						try {
							FileUtils.forceDelete(fm
									.getFileFromUriOrPath(currentUrl));
						} catch (Exception e2) {
							// doesn't matter
							myLogger.debug(e2);
						}
						try {
							FileUtils.writeStringToFile(
									fm.getFileFromUriOrPath(currentUrl),
									getTextArea().getText());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} else {

						File temp = fm.getLocalCacheFile(currentUrl);
						try {
							FileUtils.forceDelete(temp);
							FileUtils.writeStringToFile(temp, getTextArea()
									.getText());

							fm.uploadFile(temp, currentUrl, true);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (FileTransactionException e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}

					}

					documentChanged = false;
					button_1.setEnabled(false);

				}
			});
		}
		return button_1;
	}

}
