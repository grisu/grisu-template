package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.model.dto.GridFile;
import grisu.model.job.JobDescription;

import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
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

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class MultipleInputFiles extends AbstractInputPanel implements
DropTargetListener {

	private JScrollPane scrollPane;
	private JList list;

	private final DefaultListModel fileModel = new DefaultListModel();
	private JButton button;
	private JButton button_1;

	public MultipleInputFiles(String name, PanelConfig config)
			throws TemplateException {

		super(name, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(32dlu;default):grow"),
				FormSpecs.RELATED_GAP_ROWSPEC,
 FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, }));
		add(getScrollPane(), "2, 2, 5, 1, fill, fill");
		add(getButton_1(), "4, 4, right, default");
		add(getButton(), "6, 4, right, default");
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	@Override
	public void drop(DropTargetDropEvent dtde) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	private JButton getButton() {
		if (button == null) {
			button = new JButton("Add");
			button.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {

					if (getServiceInterface() == null) {
						myLogger.error("ServiceInterface not set yet.");
						return;
					}

					final Set<GridFile> files = popupFileDialogAndAskForFiles();

					if (files == null) {
						return;
					}

					for (final GridFile file : files) {

						final String filename = file.getName();
						for (int i = 0; i < fileModel.getSize(); i++) {
							final GridFile existingFile = (GridFile) fileModel
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

				@Override
				public void actionPerformed(ActionEvent e) {

					final Object[] selFiles = getList_1().getSelectedValues();

					for (final Object file : selFiles) {
						fileModel.removeElement(file);
						removeValue("inputFileUrl", ((GridFile) file).getUrl());
					}

				}
			});
		}
		return button_1;
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		final Map<String, String> defaultProperties = new HashMap<String, String>();
		defaultProperties.put(TITLE, "Input files");

		return defaultProperties;
	}

	private JList getList_1() {
		if (list == null) {
			list = new JList(fileModel);
			list.setLayoutOrientation(JList.VERTICAL_WRAP);
			list.setVisibleRowCount(5);
			list.setDragEnabled(true);
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
		final StringBuffer temp = new StringBuffer();

		for (int i = 0; i < fileModel.getSize(); i++) {
			final GridFile existingFile = (GridFile) fileModel
					.getElementAt(i);
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
	protected void templateRefresh(JobDescription jobObject) {

		fileModel.removeAllElements();

	}
}
