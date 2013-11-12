package grisu.frontend.view.swing.jobcreation;

import com.google.common.base.Joiner;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import grisu.control.exceptions.JobPropertiesException;
import grisu.control.exceptions.TemplateException;
import grisu.frontend.model.job.GrisuJob;
import grisu.frontend.view.swing.DefaultFqanChangePanel;
import grisu.frontend.view.swing.jobcreation.templates.TemplateObject;
import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.netbeans.validation.api.ui.ValidationPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

public class TemplateWrapperPanel extends JPanel {

	static final Logger myLogger = Logger.getLogger(TemplateWrapperPanel.class
			.getName());

	public static final String JOB_CREATE_PANEL = "jobCreatePanel";
	public static final String SUBMISSION_LOG_PANEL = "logPanel";

	private final SubmissionMonitorPanel monitorPanel;
	private final JPanel creationPanel;

	private final CardLayout cardLayout = new CardLayout();

	private final TemplateObject template;
	private JButton submitButton;
	private ValidationPanel validationPanel;
	private JLabel label;
	private JLabel label_1;
	private DefaultFqanChangePanel defaultFqanChangePanel = null;

	/**
	 * Create the panel.
	 */
	public TemplateWrapperPanel(TemplateObject template) {

		this.template = template;
		monitorPanel = new SubmissionMonitorPanel(
				this.template.getServiceInterface());
		setLayout(cardLayout);

		creationPanel = new JPanel();

		add(creationPanel, JOB_CREATE_PANEL);
		creationPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("max(29dlu;default):grow"),
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,

				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("bottom:default"),
				FormSpecs.RELATED_GAP_ROWSPEC, }));


		if (template.getTemplatePanel() != null) {
			// creationPanel.add(template.getWrapperScrollPane(),
			// "2, 2, 3, 1, fill, fill");
			creationPanel.add(template.getTemplatePanel(),
					"2, 2, 3, 1, fill, fill");
		}
		if (template.getValidationPanel() != null) {
			creationPanel.add(template.getValidationPanel(),
					"2, 4, 2, 1, fill, fill");
		}

		creationPanel.add(getDefaultFqanChangePanel(), "2, 6, left, center");

		creationPanel.add(getSubmitButton(), "4, 6, right, center");
		add(monitorPanel, SUBMISSION_LOG_PANEL);

		cardLayout.show(this, JOB_CREATE_PANEL);
		monitorPanel.setTemplateWrapperPanel(this);

	}

	private DefaultFqanChangePanel getDefaultFqanChangePanel() {
		if (defaultFqanChangePanel == null) {
			defaultFqanChangePanel = new DefaultFqanChangePanel();
			try {
				defaultFqanChangePanel.setServiceInterface(template
						.getServiceInterface());
			} catch (final InterruptedException e) {
				myLogger.error(e);
			}
		}
		return defaultFqanChangePanel;
	}

	private JButton getSubmitButton() {
		if (submitButton == null) {
			submitButton = new JButton("Submit");
			submitButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {

					try {
						final GrisuJob job = GrisuJob.createJobObject(
								template.getServiceInterface(),
								template.getJobSubmissionObject());

						System.out
						.println("ENV"
								+ Joiner.on(" - ").join(
										job.getEnvironmentVariables()
										.keySet()));

						monitorPanel.startJobSubmission(job);
					} catch (final JobPropertiesException e) {

						ErrorInfo info = new ErrorInfo("Job property error",
								"Can't submit job:\n\n"
										+ e.getLocalizedMessage(), null,
										"Error", e, Level.SEVERE, null);

						JXErrorPane pane = new JXErrorPane();
						pane.setErrorInfo(info);
						// pane.setErrorReporter(new GrisuErrorReporter());

						JXErrorPane.showDialog(
								TemplateWrapperPanel.this.getRootPane(), pane);

						return;
					}

				}
			});
		}
		return submitButton;
	}

	private ValidationPanel getValidationPanel() {
		if (validationPanel == null) {
			validationPanel = new ValidationPanel();
		}
		return validationPanel;
	}

	public void resetTemplate() throws TemplateException {

		template.reset();
	}

	public void switchToJobCreationPanel() {
		cardLayout.show(this, JOB_CREATE_PANEL);
	}

	public void switchToLogPanel() {
		cardLayout.show(this, SUBMISSION_LOG_PANEL);
	}
}
