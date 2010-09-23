package org.vpac.grisu.frontend.view.swing;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.vpac.grisu.GrisuVersion;
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
	private JMenu helpMenu;

	private JMenuItem settingsItem;
	private JMenuItem exitItem;
	private JMenuItem versionItem;

	/**
	 * Create the frame.
	 */
	public GrisuMenu() {

		add(getFileMenu());
		add(getToolsMenu());
		setHelpMenu(getGrisuHelpMenu());
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

	private JMenu getGrisuHelpMenu() {
		if (helpMenu == null) {
			helpMenu = new JMenu("Help");
			helpMenu.add(getVersionItem());
		}
		return helpMenu;
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

	private JMenuItem getVersionItem() {
		if (versionItem == null) {
			versionItem = new JMenuItem("Version");
			versionItem.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {

					String grisuCommonsVersion = GrisuVersion
							.get("grisu-commons");
					System.out.println(grisuCommonsVersion);

				}
			});
		}
		return versionItem;
	}

	public void setServiceInterface(ServiceInterface si) {
		this.si = si;
		dialog.setServiceInterface(si);
	}
	
	  public void setHelpMenu(JMenu menu) {
		    if (helpMenu != null) {
		      remove(helpMenu);
		    }
		    helpMenu = menu;
		    super.add(helpMenu);
		  }

		  public JMenu add(JMenu menu) {
		    if (helpMenu != null) {
		      return (JMenu) add(menu, getComponentCount() - 1);
		    }
		    else {
		      return super.add(menu);
		    }
		  }

		  public JMenu getHelpMenu() {
		    return helpMenu;
		  }

		  public void remove(JMenu menu) {
		    if (menu == helpMenu) {
		      helpMenu = null;
		    }
		    super.remove(menu);
		  }

		  public void removeAll() {
		    super.removeAll();
		    helpMenu = null;
		  }

}
