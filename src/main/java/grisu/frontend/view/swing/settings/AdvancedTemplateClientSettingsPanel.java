package grisu.frontend.view.swing.settings;

import grisu.control.ServiceInterface;
import grisu.frontend.view.swing.ServiceInterfacePanel;
import grisu.jcommons.constants.Constants;
import grisu.jcommons.constants.GridEnvironment;
import grisu.settings.ClientPropertiesManager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class AdvancedTemplateClientSettingsPanel extends JPanel implements
ServiceInterfacePanel {

	public static final String USE_OLD_FILE_MANAGEMENT_PANEL_CONFIG_KEY = "useOldFileManager";

	private JLabel lblClearFilesystemCache;
	private JButton btnClear;

	private ServiceInterface si = null;
	private JLabel lblUseoldSitebased;
	private JCheckBox oldFileManagementCheckBox;
	private JLabel lblMyproxyHost;
	private JTextField textField;
	private JButton btnApply;

	/**
	 * Create the panel.
	 */
	public AdvancedTemplateClientSettingsPanel() {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(69dlu;default)"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
		add(getLblClearFilesystemCache(), "2, 2, 3, 1");
		add(getBtnClear(), "8, 2, right, default");
		add(getLblUseoldSitebased(), "2, 4, 5, 1");
		add(getOldFileManagementCheckBox(), "8, 4, right, default");
		add(getLblMyproxyHost(), "2, 6");
		add(getTextField(), "4, 6, 3, 1, fill, default");
		add(getBtnApply(), "8, 6, right, default");
	}

	private JButton getBtnApply() {
		if (btnApply == null) {
			btnApply = new JButton("Apply");
			btnApply.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					String myProxy = getTextField().getText();
					GridEnvironment.setDefaultMyProxyHost(myProxy);

					btnApply.setEnabled(false);

				}
			});
			btnApply.setEnabled(false);
		}
		return btnApply;
	}

	private JButton getBtnClear() {
		if (btnClear == null) {
			btnClear = new JButton("Clear");
			btnClear.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					if (si == null) {
						return;
					}

					si.setUserProperty(Constants.CLEAR_MOUNTPOINT_CACHE, null);

					JOptionPane
					.showMessageDialog(
							AdvancedTemplateClientSettingsPanel.this,
							"A restart is required. The next startup might take a bit longer than usual.");

				}
			});
			btnClear.setEnabled(false);
			btnClear.setToolTipText("Press this button if you think that you can't see all the filesytems you are supposed to see. Restart required.");
		}
		return btnClear;
	}

	private JLabel getLblClearFilesystemCache() {
		if (lblClearFilesystemCache == null) {
			lblClearFilesystemCache = new JLabel("Clear filesystem cache");

		}
		return lblClearFilesystemCache;
	}

	private JLabel getLblMyproxyHost() {
		if (lblMyproxyHost == null) {
			lblMyproxyHost = new JLabel("MyProxy host");
		}
		return lblMyproxyHost;
	}

	private JLabel getLblUseoldSitebased() {
		if (lblUseoldSitebased == null) {
			lblUseoldSitebased = new JLabel(
					"Use (old) site-based file management panel");
		}
		return lblUseoldSitebased;
	}

	private JCheckBox getOldFileManagementCheckBox() {
		if (oldFileManagementCheckBox == null) {
			oldFileManagementCheckBox = new JCheckBox("");
			String use = ClientPropertiesManager
					.getProperty(USE_OLD_FILE_MANAGEMENT_PANEL_CONFIG_KEY);
			if (StringUtils.equalsIgnoreCase(use, "true")) {
				oldFileManagementCheckBox.setSelected(true);
			}
			oldFileManagementCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {

					if (oldFileManagementCheckBox.isSelected()) {
						useOldFileManagementPanel(true);
					} else {
						useOldFileManagementPanel(false);
					}

					JOptionPane
					.showMessageDialog(
							AdvancedTemplateClientSettingsPanel.this,
							"A restart is required for the changes to take effect.");
				}
			});
		}
		return oldFileManagementCheckBox;
	}

	public JPanel getPanel() {
		return this;
	}

	public String getPanelTitle() {
		return "Advanced settings";
	}

	private JTextField getTextField() {
		if (textField == null) {
			textField = new JTextField();
			textField.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					if (StringUtils.equals(
							GridEnvironment.getDefaultMyProxyServer(),
							textField.getText())) {
						getBtnApply().setEnabled(false);
					} else {
						getBtnApply().setEnabled(true);
					}
				}
			});
			textField.setColumns(10);
		}
		return textField;
	}

	public void setServiceInterface(ServiceInterface si) {

		this.si = si;
		if (si != null) {
			getBtnClear().setEnabled(true);
		} else {
			getBtnClear().setEnabled(false);
		}

	}

	private void useOldFileManagementPanel(boolean use) {

		if (use) {
			ClientPropertiesManager.setProperty(
					USE_OLD_FILE_MANAGEMENT_PANEL_CONFIG_KEY, "true");
		} else {
			ClientPropertiesManager.setProperty(
					USE_OLD_FILE_MANAGEMENT_PANEL_CONFIG_KEY, "false");
		}

	}
}
