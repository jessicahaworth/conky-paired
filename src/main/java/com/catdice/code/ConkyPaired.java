package com.catdice.code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * Hello world!
 */
public class ConkyPaired {
    private Log logger = LogFactory.getLog(this.getClass());
    private static String listsLoc = "lists";
    private static String picsLoc = "pics";
    private static String scriptsLoc = "scripts";
    private String mainList = "main.list";
    private String listFilename = listsLoc + "/" + mainList;

    private final Display display = Display.getDefault();
    private final Shell shell = new Shell(display, SWT.TITLE);

    private ManagementShell mShellSingleton;

    private String choice;
    private String newScriptChoice;
    private String newPicChoice;
    private String newPairName;

    List<Pair> pairs = new ArrayList<Pair>();

    private org.eclipse.swt.widgets.List list;

    public static void main(String[] args) {
        new ConkyPaired();
    }

    private ConkyPaired() {
        shell.setText("Conky BG");
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        choice = null;
        newScriptChoice = null;
        newPicChoice = null;
        newPairName = null;

        mShellSingleton = new ManagementShell(display, this);

        populateChoicesList();
        initializeMenu();
        initializeList();
        setUpButtons();

        shell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event event) {
                logger.info("shell handling close event");
                display.dispose();
            }
        });

        shell.pack();
        shell.setSize(200, 400);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    /* initializes the list of pairs in the gui */
    private void initializeList() {
        list = new org.eclipse.swt.widgets.List(shell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        for (Pair p : pairs) {
            list.add(p.getName());
        }
        list.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                handleChoice(event);
            }

            public void widgetDefaultSelected(SelectionEvent event) {
                logger.info("default selection");
                handleChoice(event);
            }

            private void handleChoice(SelectionEvent event) {
                String[] selections = list.getSelection();
                String choiceText = selections[0];
                logger.info("You selected: " + choiceText);
                choice = choiceText;
            }
        });

        list.pack();
    }

    public void refreshList() {
        list.removeAll();
        for (Pair p : pairs) {
            list.add(p.getName());
        }
        list.redraw();
    }

    /* initializes the dropdown menu */
    private void initializeMenu() {
        Menu bar = new Menu(shell, SWT.BAR);
        MenuItem fileItem = new MenuItem(bar, SWT.CASCADE);
        fileItem.setText("&File");
        Menu submenu = new Menu(shell, SWT.DROP_DOWN);
        fileItem.setMenu(submenu);
        MenuItem item = new MenuItem(submenu, SWT.PUSH);
        item.setText("Manage");
        item.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
                logger.info("file -> manage");
            }
        });

        shell.setMenuBar(bar);
    }

    /* adds the Load button to the gui */
    private void setUpButtons() {
        int buttonNum = 0;

        /* the load button */
        Button loadButton = new Button(shell, SWT.PUSH);
        loadButton.setText("Apply");
        loadButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Pair pair = getPair(choice);
                if (pair != null) {
                    changeConkyAndPic(pair.getPicLoc(), pair.getConkyLoc());
                    logger.info("Changed Background to " + pair.getName());
                }
            }
        });
        buttonNum = processButtonLocation(loadButton, buttonNum);

        /* the delete button */
        Button deleteButton = new Button(shell, SWT.PUSH);
        deleteButton.setText("Delete");
        deleteButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Pair pair = getPair(choice);
                if (pair != null) {
                    MessageBox messageBox = new MessageBox(shell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
                    messageBox.setText("About to delete");
                    messageBox.setMessage("Delete the pair \"" + pair.getName() + "\"?");
                    logger.info("Asking to delete " + pair.getName());
                    int buttonID = messageBox.open();
                    switch (buttonID) {
                    case SWT.OK:
                        logger.info("Deleting " + pair.getName());
                        pairs.remove(pair);
                        persistPairs();
                        break;
                    case SWT.CANCEL:
                        logger.info("Aborted deletion of " + pair.getName());
                        break;
                    /* do nothing */
                    }
                }
            }
        });
        buttonNum = processButtonLocation(deleteButton, buttonNum);

        /* the new button */
        Button newButton = new Button(shell, SWT.PUSH);
        newButton.setText("Edit");
        newButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                logger.info("Creating new pair");
                mShellSingleton.makeNewManagementShell();
            }
        });
        buttonNum = processButtonLocation(newButton, buttonNum);

        shell.pack();
    }

    /* rewrites the main file to reflect the pairs in memory */
    public void persistPairs() {
        try {
            /* clear the existing file */
            File mainFile = new File(listFilename);
            FileUtils.forceDelete(mainFile);
            FileUtils.touch(mainFile);

            /* create a new file */
            CSVWriter writer = new CSVWriter(new FileWriter(listFilename), ',');
            for (Pair p : pairs) {
                writer.writeNext(p.getEntries());
            }
            writer.close();
            refreshList();
        } catch (IOException e) {
            logger.error("error persisting pair list");
        }
    }

    /* finds the pair matching the choice in the list of pairs */
    private Pair getPair(String choice) {
        Pair pairRv = null;
        for (Pair p : pairs) {
            if (p.getName().equals(choice)) {
                pairRv = p;
                break;
            }
        }
        return pairRv;
    }

    /* change the desktop background and conky script to the new choice */
    private void changeConkyAndPic(String filePic, String fileConky) {
        String fileLocConky = scriptsLoc + "/" + fileConky;
        String fileLocPic = picsLoc + "/" + filePic;
        File picFile = new File(fileLocPic);
        File conkyFile = new File(fileLocConky);
        try {
            String desktopSession = System.getenv("DESKTOP_SESSION");
            killConkyScripts(conkyFile);
            setWallpaper(picFile, desktopSession);
            runConkyScript(conkyFile);
        } catch (IOException e1) {
            System.err.println("Error! " + e1.toString());
        } catch (InterruptedException e2) {
            System.err.println("Error! " + e2.toString());
        }
    }

    /* adds a button the gui */
    public int processButtonLocation(Button b, int buttonNum) {
        b.setLocation(10, 40 * buttonNum + 10);
        buttonNum++;
        b.pack();
        b.setSize(80, 25);
        return buttonNum;
    }

    /* change the desktop background
     * original gconftool-2 and /desktop/gnome/... */
    public void setWallpaper(File file, String desktopSession) throws IOException, InterruptedException {
        String[] script;
        if (desktopSession.equals("mate")) {
            String s[] = { "mateconftool-2", "-t", "string", "-s", "/desktop/mate/background/picture_filename", file.getAbsolutePath() };
            script = s;
        } else {
            String s[] = { "gconftool-2", "-t", "string", "-s", "/desktop/gnome/background/picture_filename", file.getAbsolutePath() };
            script = s;
        }
        Runtime runtime = Runtime.getRuntime();
        if (file.exists()) {
            runtime.exec(script);
        }
    }

    /* runs a new conky process */
    private void runConkyScript(File file) throws IOException {
        // String[] scriptKillConky = { "/usr/bin/pkill", "'^conky$'" };
        Runtime runtime = Runtime.getRuntime();

        if (file.exists() && file.isFile()) {
            String[] scriptRunConky = { "conky", "-b", "-c", file.getAbsolutePath() };
            runtime.exec(scriptRunConky); // run conky with new conkyrc
        }
    }

    /* kills the conky process */
    private void killConkyScripts(File file) throws IOException {
        Runtime runtime = Runtime.getRuntime();
        String[] scriptFindConky = { "/usr/bin/pgrep", "conky" };
        Process proc = runtime.exec(scriptFindConky); // find running conkys
        InputStream stdin = proc.getInputStream();
        InputStreamReader isr = new InputStreamReader(stdin);
        BufferedReader br = new BufferedReader(isr);
        ArrayList<String> runningConkys = new ArrayList<String>();
        String line = null;
        while ((line = br.readLine()) != null) {
            runningConkys.add(line);
        }
        if (file.exists() && file.isFile()) {
            // kill all other conkys
            for (String process : runningConkys) {
                String[] killConky = { "/bin/kill", process };
                runtime.exec(killConky);
            }
        }
    }

    /* opens the main list and loads the pairs line by line */
    private void populateChoicesList() {
        File picsDir = new File(picsLoc);
        File scriptsDir = new File(scriptsLoc);
        List<String> pics = Arrays.asList(picsDir.list());
        List<String> scripts = Arrays.asList(scriptsDir.list());
        try {
            CSVReader reader;
            reader = new CSVReader(new FileReader(listFilename));
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                /* if the line has the correct number of entries */
                if (nextLine.length == 3) {
                    String name = nextLine[0];
                    String script = nextLine[1];
                    String pic = nextLine[2];
                    /* if the script and pic exist */
                    if (pics.contains(pic) && scripts.contains(script)) {
                        pairs.add(new Pair(name, script, pic));
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            logger.error("failed to load the list");
        }
    }

    public static String getScriptsLoc() {
        return scriptsLoc;
    }

    public static String getPicsLoc() {
        return picsLoc;
    }

    public String getNewScriptChoice() {
        return newScriptChoice;
    }

    public void setNewScriptChoice(String newScriptChoice) {
        this.newScriptChoice = newScriptChoice;
    }

    public String getNewPicChoice() {
        return newPicChoice;
    }

    public void setNewPicChoice(String newPicChoice) {
        this.newPicChoice = newPicChoice;
    }

    public String getNewPairName() {
        return newPairName;
    }

    public void setNewPairName(String newPairName) {
        this.newPairName = newPairName;
    }

    public List<Pair> getPairs() {
        return pairs;
    }

    public void setPairs(List<Pair> pairs) {
        this.pairs = pairs;
    }
}
