package controller;

import common.ApplicationLock;
import common.Log;
import controller.action.ActionBoard;
import controller.net.GameControlReturnDataReceiver;
import controller.net.SPLCoachMessageReceiver;
import controller.net.Sender;
import controller.ui.GCGUI;
import controller.ui.ui.composites.HL_DropIn;
import controller.ui.KeyboardListener;
import controller.ui.setup.StartInput;
import controller.ui.ui.composites.HL_GUI;
import controller.ui.ui.composites.HL_SimGui;
import data.*;
import data.communication.GameControlData;
import data.states.AdvancedData;
import data.states.GamePreparationData;
import data.values.GameTypes;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import javax.swing.*;


/**
 * @author Michel Bartsch
 *
 * The programm starts in this class. The main components are initialised here.
 */
public class GameController {

    /**
     * The version of the GameController. Actually there are no dependencies,
     * but this should be the first thing to be written into the log file.
     */
    public static final String version = "GC2 1.4";

    /**
     * Relative directory of where logs are stored
     */
    private final static String LOG_DIRECTORY = "logs";

    private static final String HELP_TEMPLATE = "Usage: java -jar GameController.jar {options}"
            + "\n  (-h | --help)                   display help"
            + "\n  (-t | --test)                   use test-mode - currently only disabling the delayed switch to playing in SPL"
            + "\n  (-i | --interface) <interface>  set network interface (default is a connected IPv4 interface)"
            + "\n  (-l | --league) %s%sselect league (default is spl)"
            + "\n  (-w | --window)                 select window mode (default is fullscreen)"
            + "\n";
    private static final String COMMAND_INTERFACE = "--interface";
    private static final String COMMAND_INTERFACE_SHORT = "-i";
    private static final String COMMAND_LEAGUE = "--league";
    private static final String COMMAND_LEAGUE_SHORT = "-l";
    private static final String COMMAND_WINDOW = "--window";
    private static final String COMMAND_WINDOW_SHORT = "-w";
    private static final String COMMAND_TEST = "--test";
    private static final String COMMAND_TEST_SHORT = "-t";

    /**
     * The program starts here.
     *
     * @param args This is ignored.
     */
    public static void main(String[] args) {
        // Do not just System.exit(0) on Macs when selecting GameController/Quit
        System.setProperty("apple.eawt.quitStrategy", "CLOSE_ALL_WINDOWS");

        //commands
        String interfaceName = "";
        boolean windowMode = false;
        boolean testMode = false;

        parsing:
        for (int i = 0; i < args.length; i++) {
            if ((args.length > i + 1)
                    && ((args[i].equalsIgnoreCase(COMMAND_INTERFACE_SHORT))
                    || (args[i].equalsIgnoreCase(COMMAND_INTERFACE)))) {
                interfaceName = args[++i];
                continue parsing;
            } else if ((args.length > i + 1)
                    && ((args[i].equalsIgnoreCase(COMMAND_LEAGUE_SHORT))
                    || (args[i].equalsIgnoreCase(COMMAND_LEAGUE)))) {
                i++;
                for (int j = 0; j < Rules.LEAGUES.length; j++) {
                    if (Rules.LEAGUES[j].leagueDirectory.equals(args[i])) {
                        Rules.league = Rules.LEAGUES[j];
                        continue parsing;
                    }
                }
            } else if (args[i].equals(COMMAND_WINDOW_SHORT) || args[i].equals(COMMAND_WINDOW)) {
                windowMode = true;
                continue parsing;
            } else if (args[i].equals(COMMAND_TEST_SHORT) || args[i].equals(COMMAND_TEST)) {
                testMode = true;
                continue parsing;
            }
            String leagues = "";
            for (Rules rules : Rules.LEAGUES) {
                leagues += (leagues.equals("") ? "" : " | ") + rules.leagueDirectory;
            }
            if (leagues.contains("|")) {
                leagues = "(" + leagues + ")";
            }
            System.out.printf(HELP_TEMPLATE, leagues, leagues.length() < 17
                    ? "                ".substring(leagues.length())
                    : "\n                                  ");
            System.exit(0);
        }

        //application-lock
        final ApplicationLock applicationLock = new ApplicationLock("GameController");
        try {
            if (!applicationLock.acquire()) {
                JOptionPane.showMessageDialog(null,
                        "An instance of GameController already exists.",
                        "Multiple instances",
                        JOptionPane.WARNING_MESSAGE);
                System.exit(0);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error while trying to acquire the application lock.",
                    "IOError",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(-1);
        }



        // Network Interface
        InterfaceAddress localAddress = null;
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName(interfaceName);
            if (networkInterface == null || !networkInterface.isUp()) {
                Enumeration<NetworkInterface> nifs = NetworkInterface.getNetworkInterfaces();
                if (interfaceName.isEmpty()) {
                    while (nifs.hasMoreElements()) {
                        NetworkInterface nif = nifs.nextElement();
                        if (!nif.isUp() || nif.isLoopback()) {
                            continue;
                        }
                        for (InterfaceAddress ifAddress : nif.getInterfaceAddresses()) {
                            if (ifAddress.getAddress().isLoopbackAddress()) {
                                // ignore loopback during automatic interface lookup
                                continue;
                            } else if (ifAddress.getAddress() instanceof Inet4Address) {
                                networkInterface = nif;
                                localAddress = ifAddress;
                            }
                        }
                    }
                } else {
                    System.err.printf("The specified interface \"%s\" is not available%n", interfaceName);
                    System.err.print("List of known and up interfaces: ");
                    while (nifs.hasMoreElements()) {
                        NetworkInterface nif = nifs.nextElement();
                        if (nif.isUp()) {
                            System.err.printf("%s (%s)", nif.getName(), nif.getDisplayName());
                            if (nifs.hasMoreElements()) {
                                System.err.print(", ");
                            }
                        }
                    }
                    System.err.println();
                    Log.error("fatal: " + String.format("The specified interface \"%s\" is not available", interfaceName));
                    System.exit(-1);
                }
            } else {
                for (InterfaceAddress ifAddress : networkInterface.getInterfaceAddresses()) {
                    if (ifAddress.getAddress() instanceof Inet4Address) {
                        localAddress = ifAddress;
                    }
                }
                if (localAddress == null) {
                    System.err.printf("The specified interface \"%s\" has no IPv4 address assigned%n", interfaceName);
                    Log.error("fatal: " + String.format("The specified interface \"%s\" has no IPv4 address assigned", interfaceName));
                    System.exit(-1);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error while setting up GameController on interface: " + interfaceName + ".",
                    "Error in network interface",
                    JOptionPane.ERROR_MESSAGE);
            Log.error("fatal: " + e.getMessage());
            System.exit(-1);
        }

        //SPLStandardMessageReceiver
        teamcomm.net.SPLStandardMessageReceiver splStandardMessageReceiver = null;
        try {
            splStandardMessageReceiver = new teamcomm.net.SPLStandardMessageReceiver();
            splStandardMessageReceiver.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error while setting up GameController on port: " + GameControlData.GAMECONTROLLER_RETURNDATA_PORT + ".",
                    "Error on configured port",
                    JOptionPane.ERROR_MESSAGE);
            Log.error("fatal: " + e.getMessage());
            System.exit(-1);
        }

        //collect the start parameters and put them into the first data.
        StartInput input = new StartInput(!windowMode);

        while (!input.finished) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.exit(0);
            }
        }

        // Maybe those two can be merged somehow
        GamePreparationData gpd = input.getGamePreparationData();

        AdvancedData data = new AdvancedData();

        data.team[0].initialize(gpd.getFirstTeam());
        data.team[1].initialize(gpd.getSecondTeam());

        data.kickOffTeam = (byte) gpd.getFirstTeam().getTeamInfo().identifier;
        data.colorChangeAuto = gpd.isAutoColorChange();


        data.gameType = Rules.league.dropInPlayerMode ? GameTypes.DROPIN
                : gpd.isFullTimeGame() ? GameTypes.PLAYOFF: GameTypes.ROUNDROBIN;
        if (testMode) {
            Rules.league.delayedSwitchToPlaying = 0;
        }


        try {
            //sender
            Sender.initialize(localAddress.getBroadcast() == null ? localAddress.getAddress() : localAddress.getBroadcast());
            Sender sender = Sender.getInstance();
            sender.send(data);
            sender.start();

            //event-handler
            EventHandler.getInstance().data = data;

            //receiver
            GameControlReturnDataReceiver.initialize(localAddress.getAddress());
            GameControlReturnDataReceiver receiver = GameControlReturnDataReceiver.getInstance();
            receiver.start();

            if (Rules.league.isCoachAvailable) {
                SPLCoachMessageReceiver spl = SPLCoachMessageReceiver.getInstance();
                spl.start();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error while setting up GameController on port: " + GameControlData.GAMECONTROLLER_RETURNDATA_PORT + ".",
                    "Error on configured port",
                    JOptionPane.ERROR_MESSAGE);
            Log.error("fatal: " + e.getMessage());
            System.exit(-1);
        }

        //log
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-S");

        final File logDir = new File(LOG_DIRECTORY);
        if (!logDir.exists() && !logDir.mkdirs()) {
            Log.init("log_" + df.format(new Date(SystemClock.getInstance().getCurrentTimeMillis())) + ".txt");
        } else {
            final File logFile = new File(logDir,
                    "log_" + df.format(new Date(SystemClock.getInstance().getCurrentTimeMillis())) + ".txt");
            Log.init(logFile.getPath());
        }
        Log.toFile("League = " + Rules.league.leagueName);
        Log.toFile("Game type = " + data.gameType);
        Log.toFile("Auto color change = " + data.colorChangeAuto);
        Log.toFile("Using broadcast address " + (localAddress.getBroadcast() == null ? localAddress.getAddress() : localAddress.getBroadcast()));
        Log.toFile("Listening on address " + (Rules.league.dropBroadcastMessages ? localAddress.getAddress() : "0.0.0.0"));

        //ui
        ActionBoard.init();
        Log.state(data, data.team[0].getName() + " (" + data.team[0].teamColor + ") vs " + data.team[1].getName() + " (" + data.team[1].teamColor + ")");


        boolean isSim = Rules.league.leagueDirectory.equals("hl_sim");

        GCGUI gui;
        if (Rules.league.leagueDirectory.equals("hl_dropin")){
            gui = new HL_DropIn(gpd.getFullScreen(), data, gpd);
        }
        else{
            gui = new HL_GUI(gpd.getFullScreen(), data, gpd);
        }

        new KeyboardListener();
        EventHandler.getInstance().setGUI(gui);
        gui.update(data);

        //input dispose
        input.dispose();

        //clock runs until window is closed
        Clock.getInstance().start();

        // shutdown
        Log.toFile("Shutdown GameController");
        try {
            applicationLock.release();
        } catch (IOException e) {
            Log.error("Error while trying to release the application lock.");
        }
        Sender.getInstance().interrupt();
        GameControlReturnDataReceiver.getInstance().interrupt();
        SPLCoachMessageReceiver.getInstance().interrupt();
        splStandardMessageReceiver.interrupt();
        Thread.interrupted(); // clean interrupted status
        try {
            Sender.getInstance().join();
            GameControlReturnDataReceiver.getInstance().join();
            SPLCoachMessageReceiver.getInstance().join();
        } catch (InterruptedException e) {
            Log.error("Waiting for threads to shutdown was interrupted.");
        }
        try {
            Log.close();
        } catch (IOException e) {
            Log.error("Error while trying to close the log.");
        }
        teamcomm.net.logging.Logger.getInstance().closeLogfile();

        gui.dispose();
        
        // Try to join SPLStandardMessageReceiver
        try {
            splStandardMessageReceiver.join(1000);
        } catch (InterruptedException ex) {
        }

        System.exit(0);
    }
}
