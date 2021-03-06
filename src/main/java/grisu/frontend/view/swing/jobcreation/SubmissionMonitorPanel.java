package grisu.frontend.view.swing.jobcreation;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;
import grisu.control.ServiceInterface;
import grisu.control.exceptions.JobPropertiesException;
import grisu.control.exceptions.JobSubmissionException;
import grisu.control.exceptions.TemplateException;
import grisu.frontend.control.jobMonitoring.RunningJobManagerManager;
import grisu.frontend.model.job.GrisuJob;
import grisu.model.GrisuRegistryManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SubmissionMonitorPanel extends JPanel implements
PropertyChangeListener {

	static final Logger myLogger = Logger
			.getLogger(SubmissionMonitorPanel.class.getName());

	private GrisuJob job = null;
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
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, }));

		final JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, "2, 2, fill, fill");

		scrollPane.setViewportView(textArea);
		textArea.setEditable(false);
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

		SwingUtilities.invokeLater(new Thread() {
			public void run() {
				if (job == null) {
					textArea.setText("No job associated yet.");
				}
				final StringBuffer temp = new StringBuffer();
				for (final String line : job.getSubmissionLog()) {
					temp.append(line + "\n");
				}
				textArea.setText(temp.toString());
			}
		});
	}

	public void propertyChange(PropertyChangeEvent evt) {

		if ("submissionLog".equals(evt.getPropertyName())) {
			fillTextBox();
		}

	}

	public void setJobObject(GrisuJob job) {
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

	public void startJobSubmission(final GrisuJob job) {

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
                    RunningJobManagerManager.getDefault(si).createJob(job, fqan);
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
