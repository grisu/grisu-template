package org.vpac.grisu.frontend.view.swing.jobcreation.templates;

import java.awt.BorderLayout;
import java.io.File;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.control.TemplateManager;
import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.model.GrisuRegistryManager;

public class TemplateEditDialog extends JDialog {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			final TemplateEditDialog dialog = new TemplateEditDialog(null, null);
			dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private final JPanel contentPanel = new JPanel();
	private final ServiceInterface si;
	private final TemplateManager tm;
	private final List<String> template;

	/**
	 * Create the dialog.
	 * 
	 * @throws TemplateException
	 */
	public TemplateEditDialog(ServiceInterface si, File templateFile)
			throws TemplateException {
		this.setModal(false);
		this.si = si;
		this.tm = GrisuRegistryManager.getDefault(si).getTemplateManager();

		this.template = this.tm.getLocalTemplate(templateFile);

		setBounds(100, 100, 876, 515);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			final TemplateEditPanel templateEditPanel = new TemplateEditPanel(
					si, templateFile);
			templateEditPanel.setDialog(this);
			contentPanel.add(templateEditPanel, BorderLayout.CENTER);
		}
	}

}
