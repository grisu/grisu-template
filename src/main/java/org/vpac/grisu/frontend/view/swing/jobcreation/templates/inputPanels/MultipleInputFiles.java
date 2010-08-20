package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import org.vpac.grisu.model.files.GlazedFile;
import org.vpac.grisu.model.job.JobSubmissionObjectImpl;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MultipleInputFiles extends AbstractInputPanel {

	private JScrollPane scrollPane;
	private JList list;

	private final DefaultListModel fileModel = new DefaultListModel();
	private JButton button;
	private JButton button_1;

	public MultipleInputFiles(String name, PanelConfig config)
			throws TemplateException {

		super(name, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(79dlu;default)"),
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));
		add(getScrollPane(), "2, 2, 5, 1, fill, fill");
		add(getButton_1(), "4, 4, right, default");
		add(getButton(), "6, 4, right, default");
	}

	private JButton getButton() {
		if (button == null) {
			button = new JButton("Add");
			button.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					if (getServiceInterface() == null) {
						myLogger.error("ServiceInterface not set yet.");
						return;
					}

					Set<GlazedFile> files = popupFileDialogAndAskForFiles();

					if (files == null) {
						return;
					}

					for (GlazedFile file : files) {

						String filename = file.getName();
						for (int i = 0; i < fileModel.getSize(); i++) {
							GlazedFile existingFile = (GlazedFile) fileModel
									.getElementAt(i);
							if (existingFile.getName().equals(filename)) {
								fileModel.removeElement(existingFile);
							}
						}

						fileModel.addElement(file);
						addValue("inputFileUrl", file.getUrl());
					}

				}
			});
		}
		return button;
	}

	private JButton getButton_1() {
		if (button_1 == null) {
			button_1 = new JButton("Remove");
			button_1.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					Object[] selFiles = getList_1().getSelectedValues();

					for (Object file : selFiles) {
						removeValue("inputFileUrl",
								((GlazedFile) file).getUrl());
						fileModel.removeElement(file);
					}

				}
			});
		}
		return button_1;
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		Map<String, String> defaultProperties = new HashMap<String, String>();
		defaultProperties.put(TITLE, "Input files");

		return defaultProperties;
	}

	private JList getList_1() {
		if (list == null) {
			list = new JList(fileModel);
			list.setLayoutOrientation(JList.VERTICAL_WRAP);
			list.setVisibleRowCount(5);
		}
		return list;
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getList_1());
		}
		return scrollPane;
	}

	@Override
	protected String getValueAsString() {
		StringBuffer temp = new StringBuffer();

		for (int i = 0; i < fileModel.getSize(); i++) {
			GlazedFile existingFile = (GlazedFile) fileModel.getElementAt(i);
			temp.append(existingFile.getUrl() + ",");
		}

		return temp.toString();

	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {
	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties) {

		fileModel.removeAllElements();

	}

	@Override
	void setInitialValue() throws TemplateException {

	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {

		fileModel.removeAllElements();

	}
}
