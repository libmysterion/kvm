package com.mystery.kvm.client;

import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Platform;
import javax.imageio.ImageIO;

public class Tray {

    private Map<KVMClient, PopupMenu> clientMenuMap = new HashMap<>();
    private TrayIcon trayIcon;
    private AutoJoin autojoin;
    private SystemTray systemTray;

    // the tray needs to know which KVMCLIENT is connected...it should only ever be 1
    private KVMClient activeServer;
    private final Object lock = new Object();
    private ClientConfig config;
    private PopupMenu mainPopupMenu;

    private boolean autoConnectOverride = false;
    private final static String AUTO_CONNECT_OFF_HOSTNAME = "Autoconfig > OFF - This cant be a hostname on a netgwork!";

    public Tray(ClientConfig config) {

        this.config = config;

        // ensure awt toolkit is initialized.
        java.awt.Toolkit.getDefaultToolkit();

        // app requires system tray support, just exit if there is no support.
        if (!java.awt.SystemTray.isSupported()) {
            System.out.println("No system tray support, application exiting.");
            Platform.exit();
        }

        systemTray = SystemTray.getSystemTray();
    }

    private void saveAutoConnectConfig() {
        config.setAutoConnectHostName(activeServer.getMonitorInfo().getHostName());
        try {
            config.save();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void uncheckOtherAutoJoinItems(CheckboxMenuItem checkedAuoJoinItem) {
        clientMenuMap.values().stream()
                .map((serverMenuItem) -> (CheckboxMenuItem) serverMenuItem.getItem(1))
                .filter((otherAutoJoinItem) -> (otherAutoJoinItem != checkedAuoJoinItem && otherAutoJoinItem.getState()))
                .forEach((otherAutoJoinItem) -> otherAutoJoinItem.setState(false));
    }

    private void enableAutoJoin(KVMClient client) {
        if (activeServer != client) {
            // in this case the user has connected to a different server manually
            // we should now connect to the selected server
            this.activeServer.disconnectFromServer();
            this.activeServer = client;
            this.activeServer.connectToServer();
            autoConnectOverride = false;
        }
        String autoConnectHostName = this.config.getAutoConnectHostName();
        String hostName = client.getMonitorInfo().getHostName();
        if (!hostName.equals(autoConnectHostName)) {
            this.config.setAutoConnectHostName(client.getMonitorInfo().getHostName());
            try {
                config.save();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void disableAutoJoin(KVMClient client) {
        String autoConnectHostName = config.getAutoConnectHostName();
        String hostName = client.getMonitorInfo().getHostName();
        if (hostName.equals(autoConnectHostName)) {
            config.setAutoConnectHostName(AUTO_CONNECT_OFF_HOSTNAME);
            try {
                config.save();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private MenuItem getConnectItem(PopupMenu activePopup) {
        return activePopup.getItem(0);
    }

    private PopupMenu buildMenuForClient(KVMClient client) {
        PopupMenu menu = new PopupMenu(client.getMonitorInfo().getHostName());
        MenuItem connectItem = new MenuItem("Connect");
        connectItem.setEnabled(activeServer == client);
        connectItem.addActionListener((e) -> {
            synchronized (lock) {
                if (activeServer != client) {

                    PopupMenu prevActivePopup = this.clientMenuMap.get(activeServer);
                    MenuItem activeConnectItem = getConnectItem(prevActivePopup);
                    activeConnectItem.setEnabled(true);
                    // todo set the font to non-bold on prev active guy

                    this.activeServer.disconnectFromServer();
                    this.activeServer = client;
                    this.activeServer.connectToServer();

                    PopupMenu activePopup = this.clientMenuMap.get(activeServer);
                    activeConnectItem = getConnectItem(activePopup);
                    activeConnectItem.setEnabled(false);

                    autoConnectOverride = true;
                }
            }
        });

        CheckboxMenuItem autoJoinItem = new CheckboxMenuItem("Auto-Connect");
        String autoConnectHostName = config.getAutoConnectHostName();

        // check the auto-connect item if the user has set that in the config or we selected it automatically
        // this will still be checked if the user clicks on connect for another server - that is correct
        autoJoinItem.setState(client.getMonitorInfo().getHostName().equals(autoConnectHostName));
        autoJoinItem.addItemListener((e) -> {
            boolean isNowChecked = autoJoinItem.getState();
            if (isNowChecked) {
                uncheckOtherAutoJoinItems(autoJoinItem);
                enableAutoJoin(client);
            } else {
                disableAutoJoin(client);
            }
        });
        menu.add(connectItem);
        menu.add(autoJoinItem);
        return menu;
    }

    public void addClient(KVMClient client) {
        synchronized (lock) {
            String autoConnectHostName = config.getAutoConnectHostName();
            if (autoConnectHostName != null && !autoConnectOverride) {
                // a server has connectd and we are supposed to autoconnect to him and the user has not selected one manually
                if (autoConnectHostName.equals(client.getMonitorInfo().getHostName())) {
                    activeServer = client;
                    activeServer.connectToServer();
                }
            } else {
                // first run or no config file or auto connect not set
                if (activeServer == null) {
                    // first server we found gets persisted
                    // shit...
                    // how do i tell if the user has de-selected all servers for autoconnect cos he wants to do it himself
                    // i need to persist an empty string i guess so it will never match any host and it also wont take this path
                    // lets use a really long ridiculaous and obvious value rather than empty string
                    activeServer = client;
                    saveAutoConnectConfig();
                    activeServer.connectToServer();
                }
            }

            PopupMenu menuForClient = buildMenuForClient(client);
            this.clientMenuMap.put(client, menuForClient);
            if (mainPopupMenu.getItemCount() == 1) {  // if we only have the exit item
                mainPopupMenu.addSeparator();   // then add the separator
            }
            mainPopupMenu.add(menuForClient); // new clients appear at top of menu
        }
    }

    // this is called when server does actual disconnect
    void removeClient(KVMClient client) {
        PopupMenu clientMenu = this.clientMenuMap.get(client);
        this.mainPopupMenu.remove(clientMenu);
        this.clientMenuMap.remove(client);
        if (this.activeServer == client) {
            this.activeServer = null;
        }
        // todo bug here - remove the separator if it is remaining
    }

    // this is called when we click the client in the connections list and click on remove
    void removedByServer(KVMClient client) {
        if (this.activeServer == client) {  // we should not recieve this message otherwise but lets make sure
            PopupMenu clientMenu = this.clientMenuMap.get(client);
            MenuItem connectItem = getConnectItem(clientMenu);
            connectItem.setEnabled(true);
            this.activeServer = null;
        }
    }

    private MenuItem buildExitMenu() {
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(event -> {
            systemTray.remove(trayIcon);
            clientMenuMap.forEach((kvm, menu) -> kvm.close());
            if (autojoin != null) {
                autojoin.stop();
            }
        });
        return exitItem;
    }
//

//                // auto connect "on" indicates that we should auto-connect to a given host if we encounter him
//                // so on first run of the client it will auto detect a bunch of hosts
//                // and we get them in the menu
//                // but we will not connect to any of them
//                // then if the user clicks on auto-connect
//                // we will persist an object which we read on startup
//                // it just contains the hostname to auto connect to
//                // then when a KVM client gets a monitor info
//                // we can compare hostnames
//                // and send a connect message to that server
//                // ok above is option 1 
//                // option 2....
//                // i fear it annoying if there is usually only 1 server
//                // maybe it would be better if we...
//                // auto-connect to the first server we find and put auto-connect on for that server automatically
//                // so we persist the object and all that and check auto connect for the first server
//                // then if the user want to change server he can do that
//                // but the connection process is fully automatic for most users
//                // the counter argument for this is that the user needs to be at the machine to run the client
//                // no....the plan is to install it to run as admnin as a service thing...
//                // so the client should be running on startup
//                // then when the user runs the installer
//                // we probably wont auto-run or anything...dunno...maybe
//                // assume we dont auto-run after install
//                // then ther user will probasbly run the thing themselves
//                // and then the auto-connect on first run makes sense
//                // and if I do manage to run automatically after install
//                // then still it makes sense to auto-connect to the first host we find
//                // 99% of folks will have just 1 server...its tailored for home use
//                // and if you have lots...
//                // then oh no you appear in some random dudes connections list
//                // you wont end up in their monitorSetup
//                // and you need to go to the menu and click on auto-connect on your own server
//                // and that persists so...
//                // OPTION 2 WINS
//                // but if the user ran it just once...
//                // then either way it will auto-connect to the host which is saved
//            });
    public void addAppToTray() {
        try {

            trayIcon = new TrayIcon(ImageIO.read(this.getClass().getResourceAsStream("/icons/monitor-icon-16x16.png")));
            this.mainPopupMenu = new PopupMenu();
            this.mainPopupMenu.add(buildExitMenu());
            trayIcon.setPopupMenu(mainPopupMenu);

            // add the application tray icon to the system tray.
            systemTray.add(trayIcon);

        } catch (java.awt.AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }

    void setAutoJoin(AutoJoin autojoin) {
        this.autojoin = autojoin;
    }

}
