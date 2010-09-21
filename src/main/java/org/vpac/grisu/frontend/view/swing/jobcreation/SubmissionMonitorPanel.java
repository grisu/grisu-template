package org.vpac.grisu.frontend.view.swing.jobcreation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.control.exceptions.JobPropertiesException;
import org.vpac.grisu.control.exceptions.JobSubmissionException;
import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.frontend.control.jobMonitoring.RunningJobManager;
import org.vpac.grisu.frontend.model.job.JobObject;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class SubmissionMonitorPanel extends JPanel implements
		PropertyChangeListener {

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
							e.printStackTrace();
						}
					}

				} else if ("Ok".equals(btnText)) {

					if (templateWrapperPanel != null) {

						try {
							templateWrapperPanel.resetTemplate();
						} catch (final TemplateException e) {
							e.printStackTrace();
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
					RunningJobManager.getDefault(si).createJob(job, null);
					// job.createJob();
					job.submitJob();

				} catch (final JobPropertiesException e) {
					final StringBuffer temp = new StringBuffer(
							"Job submission failed: " + e.getLocalizedMessage());
					textArea.append("\n" + temp.toString());

					e.printStackTrace();
				} catch (final JobSubmissionException e) {
					final StringBuffer temp = new StringBuffer(
							"Job submission failed: " + e.getLocalizedMessage());
					textArea.append("\n" + temp.toString());

					e.printStackTrace();
				} catch (final InterruptedException e) {
					final StringBuffer temp = new StringBuffer(
							"Job submission failed: " + e.getLocalizedMessage());
					textArea.append("\n" + temp.toString());

					e.printStackTrace();
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
