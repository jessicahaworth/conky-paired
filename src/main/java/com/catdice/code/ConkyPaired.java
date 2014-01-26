package com.catdice.code;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Hello world!
 * 
 */
public class ConkyPaired {
    private Log logger = LogFactory.getLog(this.getClass());
    boolean toggle = false;

    public static void main(String[] args) {
        List<Pair> choices = new ArrayList<Pair>();
        choices.add(new Pair("dragon", "scripts/1_conkyrc", "pics/1.jpg"));
        choices.add(new Pair("girl", "scripts/2_conkyrc", "pics/2.jpg"));
        new ConkyPaired(choices);
    }

    private ConkyPaired(List<Pair> choices) {
        Display display = new Display();
        Shell shell = new Shell(display);

        int buttonNum = 0;

        for (final Pair pair : choices) {
            Button b = new Button(shell, SWT.PUSH);
            b.setText(pair.getName());
            b.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    logger.info("Changed Background to " + pair.getName());
                    changeConkyAndPic(pair.getPicLoc(), pair.getConkyLoc());
                }
            });
            b.setLocation(0, 40 * buttonNum);
            b.pack();
            buttonNum++;
        }

        shell.pack();
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
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
}
