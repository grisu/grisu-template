package grisu.frontend.view.swing.jobcreation;

import grisu.X;
import grisu.control.exceptions.JobPropertiesException;
import grisu.control.exceptions.TemplateException;
import grisu.frontend.model.job.JobObject;
import grisu.frontend.view.swing.DefaultFqanChangePanel;
import grisu.frontend.view.swing.jobcreation.templates.TemplateObject;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.jdesktop.swingx.JXErrorPane;
import org.jdesktop.swingx.error.ErrorInfo;
import org.netbeans.validation.api.ui.ValidationPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

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
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(29dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("bottom:default"),
				FormFactory.RELATED_GAP_ROWSPEC, }));

		if (template.getTemplatePanel() != null) {
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
				public void actionPerformed(ActionEvent arg0) {

					try {
						final JobObject job = JobObject.createJobObject(
								template.getServiceInterface(),
								template.getJobSubmissionObject());

						X.p("SUBMISSION2 single: " + job.isForce_single());
						X.p("SUBMISSION2 mpi: " + job.isForce_mpi());

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
