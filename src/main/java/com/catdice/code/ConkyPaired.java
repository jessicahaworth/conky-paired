package com.catdice.code;

import java.io.File;

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
				toggle = !toggle;
				File file = new File(fileLoc);
				try {
					String desktopSession = System.getenv("DESKTOP_SESSION");
					setWallpaper(file, desktopSession);
				} catch (Exception e1) {
					System.err.println("Error! " + e.toString());
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
	public void setWallpaper(File file, String desktopSession) throws Exception {
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
		runtime.exec(script);
	}
}
