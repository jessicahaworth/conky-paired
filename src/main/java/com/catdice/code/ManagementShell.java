package com.catdice.code;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public final class ManagementShell {
    private Log logger = LogFactory.getLog(this.getClass());
    private final org.eclipse.swt.widgets.List listScript;
    private final org.eclipse.swt.widgets.List listPic;
    private Shell managementShell;
    private Composite scriptShell;
    private Composite listShell;
    private Composite picShell;
    private Button addNewPairButton;
    private final ConkyPaired cp;

    public ManagementShell(Display display, final ConkyPaired cp) {
        this.cp = cp;
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

        Button addNewScriptButton = new Button(scriptShell, SWT.PUSH);
        addNewScriptButton.setText("Add new script");
        listScript.pack();
        addNewScriptButton.pack();
        scriptShell.pack();

        Button addNewPicButton = new Button(picShell, SWT.PUSH);
        addNewPicButton.setText("Add new pic");
        listScript.pack();
        addNewPicButton.pack();
        scriptShell.pack();
        listPic.pack();
        picShell.pack();

        listShell.pack();

        Text text = new Text(managementShell, SWT.BORDER | SWT.SINGLE);
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                Text text = (Text) e.widget;
                logger.info("new pair name is " + text.getText());
                cp.setNewPairName(text.getText().trim());
            }
        });

        addNewPairButton = new Button(managementShell, SWT.PUSH);
        addNewPairButton.setText("Create new pair");

        addNewPicButton.pack();

        setListeners();
    }

    private void setListeners() {
        listScript.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                handleScriptChoice(event);
            }

            public void widgetDefaultSelected(SelectionEvent event) {
                logger.info("default selection");
                handleScriptChoice(event);
            }

            private void handleScriptChoice(SelectionEvent event) {
                String[] selections = listScript.getSelection();
                String choiceText = selections[0];
                logger.info("You selected: " + choiceText);
                cp.setNewScriptChoice(choiceText);
            }
        });

        listPic.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                handlePicChoice(event);
            }

            public void widgetDefaultSelected(SelectionEvent event) {
                logger.info("default selection");
                handlePicChoice(event);
            }

            private void handlePicChoice(SelectionEvent event) {
                String[] selections = listPic.getSelection();
                String choiceText = selections[0];
                logger.info("You selected: " + choiceText);
                cp.setNewPicChoice(choiceText);
            }
        });

        addNewPairButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                if (cp.getNewPairName() != null && cp.getNewPairName().length() > 0) {
                    Pair pair = new Pair(cp.getNewPairName(), cp.getNewScriptChoice(), cp.getNewPicChoice());
                    cp.getPairs().add(pair);
                    cp.persistPairs();
                }
            }
        });
    }

    public Button getAddNewPairButton() {
        return addNewPairButton;
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
