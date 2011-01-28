package grisu.frontend.view.swing.jobcreation.templates;

import grisu.backend.info.InformationManagerManager;
import grisu.backend.model.job.gt4.GT4Submitter;
import grisu.backend.model.job.gt5.GT5Submitter;
import grisu.control.ServiceInterface;
import grisu.control.exceptions.JobPropertiesException;
import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.login.LoginPanel;
import grisu.model.GrisuRegistryManager;
import grisu.settings.ServerPropertiesManager;
import grisu.utils.SeveralXMLHelpers;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.apache.commons.io.FilenameUtils;

import au.org.arcs.jcommons.interfaces.InformationManager;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class TemplateEditPanel extends JPanel implements
PropertyChangeListener, ActionListener {

	// // //////////////////////////////////////////////// inner class
	// OpenAction
	// class OpenAction extends AbstractAction {
	// // ============================================= constructor
	// public OpenAction() {
	// super("Open...");
	// putValue(MNEMONIC_KEY, new Integer('O'));
	// }
	//
	// // ========================================= actionPerformed
	// public void actionPerformed(ActionEvent e) {
	// int retval = _fileChooser.showOpenDialog(TemplateEditPanel.this);
	// if (retval == JFileChooser.APPROVE_OPTION) {
	// File f = _fileChooser.getSelectedFile();
	// currentFile = f;
	// try {
	// FileReader reader = new FileReader(f);
	// textArea.read(reader, ""); // Use TextComponent read
	// TemplateEditPanel.this.actionPerformed(null);
	// } catch (IOException ioex) {
	// System.out.println(e);
	// System.exit(1);
	// }
	// }
	// }
	// }

	// ////////////////////////////////////////////////// inner class SaveAction
	// class SaveAction extends AbstractAction {
	// // ============================================= constructor
	// SaveAction() {
	// super("Save...");
	// putValue(MNEMONIC_KEY, new Integer('S'));
	// }
	//
	// // ========================================= actionPerformed
	// public void actionPerformed(ActionEvent e) {
	// int retval = _fileChooser.showSaveDialog(TemplateEditPanel.this);
	// if (retval == JFileChooser.APPROVE_OPTION) {
	// File f = _fileChooser.getSelectedFile();
	// try {
	// FileWriter writer = new FileWriter(f);
	// textArea.write(writer); // Use TextComponent write
	//
	// if (si != null) {
	// GrisuRegistryManager.getDefault(si)
	// .getTemplateManager().addLocalTemplate(
	// currentFile);
	// }
	// } catch (IOException ioex) {
	// JOptionPane.showMessageDialog(TemplateEditPanel.this, ioex);
	// System.exit(1);
	// }
	// }
	// }
	// }
	private static InformationManager informationManager = null;

	private static InformationManager getInformationManager() {
		if (informationManager == null) {
			try {
				informationManager = InformationManagerManager
				.getInformationManager(ServerPropertiesManager
						.getInformationManagerConf());
			} catch (final Exception e) {
				return null;
			}
		}
		return informationManager;
	}

	public static String getStackTrace(Throwable t) {
		final StringWriter stringWritter = new StringWriter();
		final PrintWriter printWritter = new PrintWriter(stringWritter, true);
		t.printStackTrace(printWritter);
		printWritter.flush();
		stringWritter.flush();

		return stringWritter.toString();
	}

	protected final File currentFile;
	protected JDialog optionalDialog;

	// private final Action _openAction = new OpenAction();

	// private final Action _saveAction = new SaveAction();

	private final JFileChooser _fileChooser = new JFileChooser();
	private JSplitPane splitPane;
	private JPanel panel;
	private JPanel panel_1;
	private JScrollPane scrollPane;
	private JTextArea textArea;

	private JButton button;
	private final ServiceInterface si;
	private JPanel errorPanel;
	private JScrollPane scrollPane_1;

	private JTextArea textArea_1;

	private JPanel currentTemplatePanel = null;
	private JButton button_1;
	private JTextArea jsdlTextArea;

	private TemplateObject template;
	private JTabbedPane tabbedPane;
	private JScrollPane scrollPane_3;
	private JScrollPane scrollPane_2;
	private JTextArea gt4TextArea;
	private JScrollPane scrollPane_4;
	private JTextArea gt5TextArea;

	private LoginPanel lp;

	/**
	 * Create the frame.
	 * 
	 * @throws TemplateException
	 */
	public TemplateEditPanel(ServiceInterface si, File currentFile)
	throws TemplateException {

		this.si = si;

		this.currentFile = currentFile;

		setBounds(100, 100, 800, 600);

		try {
			setLayout(new BorderLayout(0, 0));
			add(getSplitPane(), BorderLayout.CENTER);

		} catch (final Exception e) {
			throw new RuntimeException(e);
		}

		try {
			final FileReader reader = new FileReader(currentFile);
			textArea.read(reader, ""); // Use TextComponent read
			TemplateEditPanel.this.actionPerformed(null);
		} catch (final IOException ioex) {
			throw new TemplateException("Could not open template "
					+ currentFile.toString());
		}

	}

	public void actionPerformed(ActionEvent arg0) {

		// save backup file
		File tempF = new File(System.getProperty("java.io.tmpdir"),
				currentFile.getName());
		saveCurrentTextToFile(tempF);

		if (currentTemplatePanel != null) {
			getCardPanel().remove(currentTemplatePanel);
		}

		getErrorTextArea().setText("");
		getJsdlTextArea().setText("");
		final CardLayout cl = (CardLayout) (getCardPanel().getLayout());
		cl.show(getCardPanel(), "error");

		final List<String> lines = new LinkedList(Arrays.asList(getTextArea()
				.getText().split("\n")));

		try {

			if ((template != null)
					&& (template.getJobSubmissionObject() != null)) {
				template.getJobSubmissionObject().removePropertyChangeListener(
						TemplateEditPanel.this);
			}
			String templateFilename = null;
			if (currentFile != null) {
				templateFilename = FilenameUtils.getBaseName(currentFile
						.toString());
			}
			template = createTemplatePanel(templateFilename, lines);
			template.getJobSubmissionObject().addPropertyChangeListener(
					TemplateEditPanel.this);

			final JPanel tempPanel = new JPanel();
			tempPanel.setLayout(new BorderLayout());
			if (template.getTemplatePanel() != null) {
				tempPanel.add(template.getTemplatePanel(), BorderLayout.CENTER);

			}
			if (template.getValidationPanel() != null) {
				tempPanel
				.add(template.getValidationPanel(), BorderLayout.SOUTH);
			}

			currentTemplatePanel = tempPanel;

			setJobDescriptions();

			getCardPanel().add(currentTemplatePanel, "currentTemplate");
			cl.show(getCardPanel(), "currentTemplate");

		} catch (final TemplateException e) {

			final StringBuffer temp = new StringBuffer(
					"Error when building template: " + e.getLocalizedMessage()
					+ "\n\n");
			temp.append(getStackTrace(e));
			getErrorTextArea().setText(temp.toString());
			getErrorTextArea().setCaretPosition(0);
			cl.show(getCardPanel(), "error");
		}

	}

	public TemplateObject createTemplatePanel(String templateFileName,
			List<String> lines) throws TemplateException {
		return TemplateHelpers.parseAndCreateTemplatePanel(si,
				templateFileName, lines);
	}

	private JButton getButton() {
		if (button == null) {
			button = new JButton("Apply");
			button.addActionListener(this);
		}
		return button;
	}

	private JButton getButton_1() {
		if (button_1 == null) {
			button_1 = new JButton("Save");
			button_1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					File f = currentFile;
					if (f == null) {
						final int retval = _fileChooser
						.showSaveDialog(TemplateEditPanel.this);
						if (retval == JFileChooser.APPROVE_OPTION) {
							f = _fileChooser.getSelectedFile();
						} else {
							return;
						}
					}
					saveCurrentTextToFile(f);

					if (si != null) {
						GrisuRegistryManager.getDefault(si)
						.getTemplateManager()
						.addLocalTemplate(currentFile);
					}

					if (optionalDialog != null) {
						optionalDialog.dispose();
					}

				}
			});
		}
		return button_1;
	}

	private JPanel getCardPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new CardLayout(0, 0));
			panel.add(getErrorPanel(), "error");
		}
		return panel;
	}

	private JPanel getErrorPanel() {
		if (errorPanel == null) {
			errorPanel = new JPanel();
			errorPanel.setLayout(new BorderLayout(0, 0));
			errorPanel.add(getScrollPane_1(), BorderLayout.CENTER);
		}
		return errorPanel;
	}

	private JTextArea getErrorTextArea() {
		if (textArea_1 == null) {
			textArea_1 = new JTextArea();
			textArea_1.setEditable(false);
		}
		return textArea_1;
	}

	private JTextArea getGt4TextArea() {
		if (gt4TextArea == null) {
			gt4TextArea = new JTextArea();
			gt4TextArea.setEditable(false);
		}
		return gt4TextArea;
	}

	private JTextArea getGt5TextArea() {
		if (gt5TextArea == null) {
			gt5TextArea = new JTextArea();
			gt5TextArea.setEditable(false);
		}
		return gt5TextArea;
	}

	private JTextArea getJsdlTextArea() {
		if (jsdlTextArea == null) {
			jsdlTextArea = new JTextArea();
			jsdlTextArea.setEditable(false);
		}
		return jsdlTextArea;
	}

	private JPanel getPanel_1() {
		if (panel_1 == null) {
			panel_1 = new JPanel();
			panel_1.setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("max(18dlu;default):grow"),
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("default:grow"),
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("max(97dlu;default):grow"),
					FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("96dlu"),
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC, }));
			panel_1.add(getScrollPane(), "2, 2, 7, 1, fill, fill");
			panel_1.add(getTabbedPane(), "2, 4, 7, 1, fill, fill");
			panel_1.add(getButton(), "6, 6, right, default");
			panel_1.add(getButton_1(), "8, 6, right, default");
		}
		return panel_1;
	}

	public JPanel getRootPanel() {
		return this;
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getTextArea());
		}
		return scrollPane;
	}

	private JScrollPane getScrollPane_1() {
		if (scrollPane_1 == null) {
			scrollPane_1 = new JScrollPane();
			scrollPane_1.setViewportView(getErrorTextArea());
		}
		return scrollPane_1;
	}

	private JScrollPane getScrollPane_2() {
		if (scrollPane_2 == null) {
			scrollPane_2 = new JScrollPane();
			scrollPane_2.setViewportView(getGt4TextArea());
		}
		return scrollPane_2;
	}

	private JScrollPane getScrollPane_3() {
		if (scrollPane_3 == null) {
			scrollPane_3 = new JScrollPane();
			scrollPane_3.setViewportView(getJsdlTextArea());
		}
		return scrollPane_3;
	}

	private JScrollPane getScrollPane_4() {
		if (scrollPane_4 == null) {
			scrollPane_4 = new JScrollPane();
			scrollPane_4.setViewportView(getGt5TextArea());
		}
		return scrollPane_4;
	}

	private JSplitPane getSplitPane() {
		if (splitPane == null) {
			splitPane = new JSplitPane();
			splitPane.setLeftComponent(getCardPanel());
			splitPane.setRightComponent(getPanel_1());
			splitPane.setDividerLocation(400);
		}
		return splitPane;
	}

	private JTabbedPane getTabbedPane() {
		if (tabbedPane == null) {
			tabbedPane = new JTabbedPane(SwingConstants.TOP);
			tabbedPane.addTab("Jsdl", null, getScrollPane_3(), null);
			tabbedPane.addTab("GT4 RSL", null, getScrollPane_2(), null);
			tabbedPane.addTab("GT5 RSL", null, getScrollPane_4(), null);
		}
		return tabbedPane;
	}

	private JTextArea getTextArea() {
		if (textArea == null) {
			textArea = new JTextArea();
		}
		return textArea;
	}

	public void propertyChange(PropertyChangeEvent arg0) {

		setJobDescriptions();

	}

	private void saveCurrentTextToFile(File file) {
		try {
			final FileWriter writer = new FileWriter(file);
			textArea.write(writer); // Use TextComponent write

		} catch (final Exception ioex) {
			JOptionPane.showMessageDialog(TemplateEditPanel.this, ioex);
			// System.exit(1);
		}
	}

	public void setDialog(JDialog templateEditDialog) {

		this.optionalDialog = templateEditDialog;

	}

	private void setJobDescriptions() {
		if ((template != null) && (template.getJobSubmissionObject() != null)) {

			String jsdl;
			try {
				jsdl = template.getJobSubmissionObject()
				.getJobDescriptionDocumentAsString();
				getJsdlTextArea().setText(jsdl);
				getJsdlTextArea().setCaretPosition(0);
			} catch (final JobPropertiesException e) {
				final StringBuffer temp = new StringBuffer(
						"Can't calculate jsdl right now: "
						+ e.getLocalizedMessage() + "\n\n");
				temp.append(getStackTrace(e));
				getJsdlTextArea().setText(temp.toString());
				getJsdlTextArea().setCaretPosition(0);
				getGt4TextArea().setText(temp.toString());
				getGt4TextArea().setCaretPosition(0);
				getGt5TextArea().setText(temp.toString());
				getGt5TextArea().setCaretPosition(0);
				return;
			}

			if (getInformationManager() == null) {
				getGt4TextArea()
				.setText(
						"Can't calculate rsl because local-backend.jar not in classpath.");
				getGt5TextArea()
				.setText(
						"Can't calculate rsl because local-backend.jar not in classpath.");
				return;
			}

			try {
				final String gt4rsl = GT4Submitter
				.createJobSubmissionDescription(
						getInformationManager(),
						SeveralXMLHelpers.fromString(jsdl));
				getGt4TextArea().setText(gt4rsl);
			} catch (final Exception e) {
				// e.printStackTrace();
			}

			try {
				final String gt5rsl = GT5Submitter
				.createJobSubmissionDescription(
						getInformationManager(),
						SeveralXMLHelpers.fromString(jsdl));
				getGt5TextArea().setText(gt5rsl);
			} catch (final Exception e) {
				// e.printStackTrace();
			}

		}
	}

	public void setLoginPanel(LoginPanel lp) {

		this.lp = lp;
	}

}
