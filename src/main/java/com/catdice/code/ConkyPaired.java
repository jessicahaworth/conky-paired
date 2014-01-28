package com.catdice.code;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
import org.eclipse.swt.widgets.Shell;

/**
 * Hello world!
 */
public class ConkyPaired {
    private Log logger = LogFactory.getLog(this.getClass());
    private String picsLoc = "pics";
    private String scriptsLoc = "scripts";

    private final Display display = new Display();
    private final Shell shell = new Shell(display, SWT.TITLE);
    private final Shell managementShell = new Shell(display);
    private String choice = "";
    List<Pair> pairs = new ArrayList<Pair>();

    public static void main(String[] args) {
        new ConkyPaired();
    }

    private ConkyPaired() {
        shell.setText("Conky BG");
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        choice = null;

        populateChoicesList();
        initializeMenu();
        initializeList();
        setUpButton();

        shell.pack();
        shell.setSize(180, 200);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }

    private void initializeList() {
        final org.eclipse.swt.widgets.List list = new org.eclipse.swt.widgets.List(shell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
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
    }

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
                logger.info("manage");
                managementShell.open();
            }
        });

        shell.setMenuBar(bar);
    }

    private void setUpButton() {
        int buttonNum = 0;
        /* the load button */
        Button loadButton = new Button(shell, SWT.PUSH);
        loadButton.setText("Load");
        loadButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Pair pair = getPair(choice);
                changeConkyAndPic(pair.getPicLoc(), pair.getConkyLoc());
                logger.info("Changed Background to " + pair.getName());
            }
        });
        buttonNum = processButtonLocation(loadButton, buttonNum);
        shell.pack();
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

    private void changeConkyAndPic(String fileLocPic, String fileLocConky) {
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

    public int processButtonLocation(Button b, int buttonNum) {
        b.setLocation(10, 40 * buttonNum + 10);
        buttonNum++;
        b.pack();
        b.setSize(80, 25);
        return buttonNum;
    }

    // original gconftool-2 and /desktop/gnome/...
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

    private void runConkyScript(File file) throws IOException {
        // String[] scriptKillConky = { "/usr/bin/pkill", "'^conky$'" };
        Runtime runtime = Runtime.getRuntime();

        if (file.exists() && file.isFile()) {
            String[] scriptRunConky = { "conky", "-b", "-c", file.getAbsolutePath() };
            runtime.exec(scriptRunConky); // run conky with new conkyrc
        }
    }

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

    private void populateChoicesList() {
        File picsDir = new File(picsLoc);
        File scriptsDir = new File(scriptsLoc);
        List<String> pics = Arrays.asList(picsDir.list());
        List<String> scripts = Arrays.asList(scriptsDir.list());
        HashMap<Integer, String> numToScript = new HashMap<Integer, String>();
        HashMap<Integer, String> numToPic = new HashMap<Integer, String>();

        for (String scriptName : scripts) {
            /* extract a number from the file name */
            Integer scriptNumber = Integer.parseInt(scriptName.replaceAll("[\\D]", ""));
            if (scriptNumber != null) {
                numToScript.put(scriptNumber, scriptName);
            }
        }

        for (String picName : pics) {
            /* extract a number from the file name */
            Integer picNumber = Integer.parseInt(picName.replaceAll("[\\D]", ""));
            if (picNumber != null) {
                numToPic.put(picNumber, picName);
            }
        }

        Set<Integer> scriptInts = numToScript.keySet();

        for (Integer scriptInt : scriptInts) {
            if (numToPic.containsKey(scriptInt)) {

            }
        }

        pairs.add(new Pair("dragon", "scripts/1_conkyrc", "pics/1.jpg"));
        pairs.add(new Pair("girl", "scripts/2_conkyrc", "pics/2.jpg"));
    }
}
