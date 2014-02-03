package com.catdice.code;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public final class ManagementShell {

    final org.eclipse.swt.widgets.List listScript;
    final org.eclipse.swt.widgets.List listPic;
    private Shell managementShell;
    private Composite scriptShell;
    private Composite listShell;
    private Composite picShell;

    public ManagementShell(Display display) {
        managementShell = new Shell(display);
        managementShell.setText("Create new pair");
        managementShell.setLayout(new FillLayout(SWT.VERTICAL));
        managementShell.setSize(400, 400);

        /* load scripts and pics */
        File scriptsDir = new File(ConkyPaired.getScriptsLoc());
        File picsDir = new File(ConkyPaired.getPicsLoc());
        List<String> scripts = Arrays.asList(scriptsDir.list());
        List<String> pics = Arrays.asList(picsDir.list());

        listShell = new Composite(managementShell, SWT.NONE);
        listShell.setLayout(new FillLayout(SWT.HORIZONTAL));

        scriptShell = new Composite(listShell, SWT.NONE);
        scriptShell.setLayout(new FillLayout(SWT.VERTICAL));

        /* initialize list of scripts */
        listScript = new org.eclipse.swt.widgets.List(scriptShell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        for (String script : scripts) {
            listScript.add(script);
        }

        picShell = new Composite(listShell, SWT.NONE);
        picShell.setLayout(new FillLayout(SWT.VERTICAL));
        /* initialize list of pics */
        listPic = new org.eclipse.swt.widgets.List(picShell, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL);
        for (String pic : pics) {
            listPic.add(pic);
        }
    }

    public org.eclipse.swt.widgets.List getListScript() {
        return listScript;
    }

    public org.eclipse.swt.widgets.List getListPic() {
        return listPic;
    }

    public Composite getScriptShell() {
        return scriptShell;
    }

    public Composite getListShell() {
        return listShell;
    }

    public Composite getPicShell() {
        return picShell;
    }

    public Shell getManagementShell() {
        return managementShell;
    }
}
