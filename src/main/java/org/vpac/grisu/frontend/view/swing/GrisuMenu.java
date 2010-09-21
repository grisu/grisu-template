package org.vpac.grisu.frontend.view.swing;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.frontend.view.swing.settings.SettingsDialog;

public class GrisuMenu extends JMenuBar {

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					final GrisuMenu frame = new GrisuMenu();
					frame.setVisible(true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private final SettingsDialog dialog = new SettingsDialog();

	private ServiceInterface si;

	private JMenu fileMenu;
	private JMenu toolsMenu;

	private JMenuItem settingsItem;
	private JMenuItem exitItem;

	/**
	 * Create the frame.
	 */
	public GrisuMenu() {

		add(getFileMenu());
		add(getToolsMenu());
	}

	private JMenuItem getExitItem() {
		if (exitItem == null) {
			exitItem = new JMenuItem("Exit");
			exitItem.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {
					System.exit(0);
				}
			});
		}
		return exitItem;
	}

	private JMenu getFileMenu() {
		if (fileMenu == null) {
			fileMenu = new JMenu("File");
			fileMenu.add(getExitItem());
		}
		return fileMenu;
	}

	private JMenuItem getSettingsItem() {
		if (settingsItem == null) {
			settingsItem = new JMenuItem("Settings");
			settingsItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {

					dialog.setVisible(true);

				}
			});
		}
		return settingsItem;
	}

	private JMenu getToolsMenu() {
		if (toolsMenu == null) {
			toolsMenu = new JMenu("Tools");
			toolsMenu.add(getSettingsItem());
		}
		return toolsMenu;
	}

	public void setServiceInterface(ServiceInterface si) {
		this.si = si;
		dialog.setServiceInterface(si);
	}
}
