package com.catdice.code;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
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
		new ConkyPaired();
	}

	private ConkyPaired() {
		Display display = new Display();
		Shell shell = new Shell(display);
		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("Change Background");
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				System.out.println("Changed Background?");
				String fileLoc = "pics/1.jpg";
				if (toggle) {
					fileLoc = "pics/2.jpg";
				}

				File picFile = new File(fileLoc);

				fileLoc = "scripts/1_conkyrc";
				if (toggle) {
					fileLoc = "scripts/2_conkyrc";
				}

				File conkyFile = new File(fileLoc);
				toggle = !toggle;
				try {
					String desktopSession = System.getenv("DESKTOP_SESSION");
					setWallpaper(picFile, desktopSession);
					runConkyScript(conkyFile);
				} catch (IOException ex) {
					System.err.println("Error! " + ex.toString());
				}
			}

		});
		shell.setDefaultButton(ok);
		shell.setLayout(new RowLayout());
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	// original gconftool-2 and /desktop/gnome/...
	public void setWallpaper(File file, String desktopSession)
			throws IOException {
		String[] script;
		if (desktopSession.equals("mate")) {
			String s[] = { "mateconftool-2", "-t", "string", "-s",
					"/desktop/mate/background/picture_filename",
					file.getAbsolutePath() };
			script = s;
		} else {
			String s[] = { "gconftool-2", "-t", "string", "-s",
					"/desktop/gnome/background/picture_filename",
					file.getAbsolutePath() };
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

		String[] scriptKillConky = { "/usr/bin/pkill", "conky" };
		String[] scriptFindConky = { "/usr/bin/pgrep", "conky" };

		Process proc = runtime.exec(scriptFindConky); // find running instance
														// of conky, if
		InputStream stdin = proc.getInputStream();
		InputStreamReader isr = new InputStreamReader(stdin);
		BufferedReader br = new BufferedReader(isr);
		String line = null;
		ArrayList<String> runningConkys = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			runningConkys.add(line);
		}

		for (String process : runningConkys) {
			String[] killConky = { "/bin/kill", process };
			runtime.exec(killConky);
		}

		if (file.exists() && file.isFile()) {
			String[] scriptRunConky = { "conky", "-c", file.getAbsolutePath() };
			// runtime.exec(scriptKillConky); // kill running instance of conky
			runtime.exec(scriptRunConky); // run conky with new conkyrc
		}

	}
}
