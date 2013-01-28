package grisu.frontend.view.swing.jobcreation.templates;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.JobPropertiesException;
import grisu.control.exceptions.JobSubmissionException;
import grisu.control.exceptions.TemplateException;
import grisu.frontend.model.job.JobObject;
import grisu.frontend.view.swing.jobcreation.templates.inputPanels.AbstractInputPanel;
import grisu.model.job.JobDescription;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.netbeans.validation.api.ui.ValidationPanel;

public class TemplateObject {

	static final Logger myLogger = Logger.getLogger(TemplateObject.class
			.getName());

	public static Map<String, String> parseCommandlineTemplate(String template)
			throws TemplateException {

		final Map<String, String> map = new HashMap<String, String>();

		final String[] parts = template.split("\\$");

		for (final String part : parts) {

			if (!part.startsWith("{")) {
				continue;
				// throw new
				// TemplateException("Template format wrong: $ is not followed by {");
			}
			if (!part.contains("}")) {
				throw new TemplateException(
						"Template format wrong: opening { does not have a closing }");
			}

			final String variableName = part.substring(1, part.indexOf("}"));
			map.put(variableName, "");

		}

		return map;

	}

	private String name;

	private JobDescription jobObject;
	private final String commandlineTemplate;
	private final ServiceInterface si;

	private JPanel templatePanel;
	private LinkedHashMap<String, AbstractInputPanel> inputPanels;
	private ValidationPanel validationPanel;

	// private final Map<String, AbstractInputPanel> panels = new
	// HashMap<String, AbstractInputPanel>();
	private final Map<String, String> changedValues;

	private final Map<String, String> fixedValues;

	private JScrollPane scrollPane;

	public TemplateObject(ServiceInterface si, String commandlineTemplate,
			Map<String, String> fixedValues) throws TemplateException {
		this.si = si;
		this.commandlineTemplate = commandlineTemplate;
		this.fixedValues = fixedValues;
		this.changedValues = parseCommandlineTemplate(commandlineTemplate);

		this.jobObject = new JobDescription();

	}

	// public void registerInputPanel(AbstractInputPanel panel) {
	// panels.put(panel.getName(), panel);
	// }

	public LinkedHashMap<String, AbstractInputPanel> getInputPanels() {
		return this.inputPanels;
	}

	public JobDescription getJobSubmissionObject() {
		return this.jobObject;
	}

	public ServiceInterface getServiceInterface() {
		return this.si;
	}

	public String getTemplateName() {
		return name;
	}

	public JPanel getTemplatePanel() {

		return this.templatePanel;
	}

	public ValidationPanel getValidationPanel() {
		return validationPanel;
	}

	public JScrollPane getWrapperScrollPane() {
		if ( scrollPane == null ) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getTemplatePanel());
			scrollPane
			.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		}
		return scrollPane;

	}

	public void reset() throws TemplateException {

		this.jobObject = new JobDescription();

		for (final AbstractInputPanel panel : getInputPanels().values()) {
			panel.initPanel(this, this.jobObject);
		}

		setFixedValues();

		userInput(null, null);
	}

	private void setFixedValues() throws TemplateException {

		for (final String key : fixedValues.keySet()) {

			Object value = fixedValues.get(key);

			final Method[] methods = jobObject.getClass().getMethods();

			for (final Method m : methods) {

				if (m.getName().equals("set" + StringUtils.capitalize(key))) {

					final Class parameterType = m.getParameterTypes()[0];

					if (!parameterType.equals(String.class)) {
						try {
							final Method fromMethod = parameterType.getMethod(
									"valueOf", String.class);

							value = fromMethod.invoke(null, value);
							break;
						} catch (final Exception e) {
							throw new TemplateException(
									"Can't set fixed key/value pair: " + key
									+ "/" + value.toString(), e);
						}
					}
				}

			}

			Method method = null;
			try {

				method = jobObject.getClass().getMethod(
						"set" + StringUtils.capitalize(key), value.getClass());
				method.invoke(jobObject, value);
			} catch (final Exception e) {
				throw new TemplateException("Can't set fixed key/value pair: "
						+ key + "/" + value.toString());
			}

			// jobObject.
		}

	}

	public void setInputPanels(
			LinkedHashMap<String, AbstractInputPanel> inputPanels)
					throws TemplateException {

		this.inputPanels = inputPanels;

		for (final AbstractInputPanel panel : this.inputPanels.values()) {
			panel.initPanel(this, this.jobObject);
		}

		setFixedValues();

	}

	// public void setJobObject(JobDescription job) {
	// this.jobObject = job;
	// }

	public void setTemplateName(String name) {
		this.name = name;
	}

	public void setTemplatePanel(JPanel templatePanel) {

		this.templatePanel = templatePanel;
	}

	public void setValidationPanel(ValidationPanel valP) {
		this.validationPanel = valP;

	}

	public void submitJob() throws JobPropertiesException,
	JobSubmissionException, InterruptedException {

		JobObject job = null;
		job = new JobObject(si, jobObject.getJobDescriptionDocument());

		job.createJob("/ACC");

		job.submitJob();

	}

	public void userInput(String panelName, String newValue) {

		if (newValue == null) {
			newValue = "";
		}

		if ((panelName != null) && (changedValues.get(panelName) == null)) {
			myLogger.debug("Commandline doesn't require value from panel "
					+ panelName);
			return;
		}

		if (panelName != null) {
			changedValues.put(panelName, newValue);
		}
		String newCommandline = commandlineTemplate;
		for (final String key : changedValues.keySet()) {
			newCommandline = newCommandline.replace("${" + key + "}",
					changedValues.get(key));
		}

		jobObject.setCommandline(newCommandline);
	}

	public void validateManually() {
		if ((this.validationPanel != null)
				&& (this.validationPanel.getValidationGroup() != null)) {
			this.validationPanel.getValidationGroup().validateAll();
		}
	}

}
