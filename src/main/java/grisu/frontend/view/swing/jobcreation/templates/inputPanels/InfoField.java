package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.model.job.JobSubmissionObjectImpl;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

public class InfoField extends AbstractInputPanel {
	private JScrollPane scrollPane;
	private JTextArea textArea;

	public InfoField(String templateName, PanelConfig config)
	throws TemplateException {
		super(templateName, config);
		setLayout(new BorderLayout(0, 0));
		add(getScrollPane(), BorderLayout.CENTER);

	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		final Map<String, String> defaultProperties = new HashMap<String, String>();

		return defaultProperties;
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getTextArea());
		}
		return scrollPane;
	}

	private JTextArea getTextArea() {
		if (textArea == null) {
			textArea = new JTextArea();
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);
			textArea.setBackground(UIManager.getColor("Label.background"));
			textArea.setEditable(false);
		}
		return textArea;
	}

	@Override
	protected String getValueAsString() {
		return "";
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {
	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties)
	throws TemplateException {

		final String text = getDefaultValue();
		getTextArea().setText(text);

	}

	@Override
	void setInitialValue() throws TemplateException {

	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {

	}
}
