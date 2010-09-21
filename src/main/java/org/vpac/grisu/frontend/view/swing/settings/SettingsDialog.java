package org.vpac.grisu.frontend.view.swing.settings;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.StringUtils;
import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.frontend.control.login.LoginManager;

public class SettingsDialog extends JDialog {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			final SettingsDialog dialog = new SettingsDialog();

			final ServiceInterface si = LoginManager.loginCommandline();
			dialog.setServiceInterface(si);

			dialog.setVisible(true);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private ServiceInterface si;
	private ApplicationSubscribePanel applicationSubscribePanel;

	private final JPanel contentPanel = new JPanel();

	/**
	 * Create the dialog.
	 */
	public SettingsDialog() {
		setTitle("Settings");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 701, 522);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
			contentPanel.add(tabbedPane, BorderLayout.CENTER);
			{

				String environmentVariable = System
						.getProperty("grisu.defaultApplications");
				if (StringUtils.isBlank(environmentVariable)) {
					environmentVariable = System
							.getProperty("grisu.createJobPanels");
					if (StringUtils.isBlank(environmentVariable)) {
						// only add that when no predefined applications
						applicationSubscribePanel = new ApplicationSubscribePanel();
						tabbedPane.addTab("Applications", null,
								applicationSubscribePanel, null);
					}
				}

			}
		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				final JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

	public void setServiceInterface(ServiceInterface si) {
		this.si = si;
		if (applicationSubscribePanel != null) {
			applicationSubscribePanel.setServiceInterface(si);
		}
	}

}
