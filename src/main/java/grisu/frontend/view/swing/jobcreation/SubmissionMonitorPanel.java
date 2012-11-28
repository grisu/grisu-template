package grisu.frontend.view.swing.jobcreation;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.JobPropertiesException;
import grisu.control.exceptions.JobSubmissionException;
import grisu.control.exceptions.TemplateException;
import grisu.frontend.control.jobMonitoring.RunningJobManager;
import grisu.frontend.model.job.JobObject;
import grisu.model.GrisuRegistryManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class SubmissionMonitorPanel extends JPanel implements
PropertyChangeListener {

	static final Logger myLogger = Logger
			.getLogger(SubmissionMonitorPanel.class.getName());

	private JobObject job = null;
	private final JTextArea textArea = new JTextArea();
	private final JButton cancelButton = new JButton("Cancel");

	private TemplateWrapperPanel templateWrapperPanel;

	private Thread submissionThread = null;

	private final ServiceInterface si;

	/**
	 * Create the panel.
	 */
	public SubmissionMonitorPanel(ServiceInterface si) {
		this.si = si;
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));

		final JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, "2, 2, fill, fill");

		scrollPane.setViewportView(textArea);

		cancelButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				final String btnText = cancelButton.getText();

				if ("Cancel".equals(btnText)) {

					if (submissionThread != null) {

						try {
							submissionThread.interrupt();
						} catch (final Exception e) {
							myLogger.error(e);
						}
					}

				} else if ("Ok".equals(btnText)) {

					if (templateWrapperPanel != null) {

						try {
							templateWrapperPanel.resetTemplate();
						} catch (final TemplateException e) {
							myLogger.error(e);
						}

						templateWrapperPanel.switchToJobCreationPanel();
					}

				}

			}
		});
		add(cancelButton, "2, 4, right, default");

	}

	public void fillTextBox() {
		if (job == null) {
			textArea.setText("No job associated yet.");
		}
		final StringBuffer temp = new StringBuffer();
		for (final String line : job.getSubmissionLog()) {
			temp.append(line + "\n");
		}
		textArea.setText(temp.toString());
	}

	public void propertyChange(PropertyChangeEvent evt) {

		if ("submissionLog".equals(evt.getPropertyName())) {
			fillTextBox();
		}

	}

	public void setJobObject(JobObject job) {
		if (this.job != null) {
			this.job.removePropertyChangeListener(this);
		}
		this.job = job;
		this.job.addPropertyChangeListener(this);
		cancelButton.setText("Cancel");
		fillTextBox();

	}

	public void setTemplateWrapperPanel(TemplateWrapperPanel panel) {
		this.templateWrapperPanel = panel;
	}

	public void startJobSubmission(final JobObject job) {

		submissionThread = new Thread() {

			@Override
			public void run() {

				try {

					SwingUtilities.invokeLater(new Thread() {
						@Override
						public void run() {

							templateWrapperPanel.switchToLogPanel();

						}
					});

					setJobObject(job);
					final String fqan = GrisuRegistryManager.getDefault(si)
					.getUserEnvironmentManager().getCurrentFqan();
					RunningJobManager.getDefault(si).createJob(job, fqan);
					// job.createJob();
					job.submitJob();

				} catch (final JobPropertiesException e) {
					final StringBuffer temp = new StringBuffer(
							"Job submission failed: " + e.getLocalizedMessage());
					textArea.append("\n" + temp.toString());

					myLogger.error(e);
				} catch (final JobSubmissionException e) {
					final StringBuffer temp = new StringBuffer(
							"Job submission failed: " + e.getLocalizedMessage());
					textArea.append("\n" + temp.toString());

					myLogger.error(e);
				} catch (final InterruptedException e) {
					final StringBuffer temp = new StringBuffer(
							"Job submission failed: " + e.getLocalizedMessage());
					textArea.append("\n" + temp.toString());

					myLogger.error(e);
				} catch (Exception e) {
					final StringBuffer temp = new StringBuffer(
							"Job submission failed: " + e.getLocalizedMessage());
					textArea.append("\n" + temp.toString());
					myLogger.error(e);
				} finally {
					SwingUtilities.invokeLater(new Thread() {
						@Override
						public void run() {
							cancelButton.setText("Ok");
						}
					});
				}
			}
		};
		submissionThread.start();
	}
}
