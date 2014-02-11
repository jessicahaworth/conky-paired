package com.catdice.code;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public final class ManagementShell {
    private Log logger = LogFactory.getLog(this.getClass());
    private org.eclipse.swt.widgets.List listScript;
    private org.eclipse.swt.widgets.List listPic;

    private ConkyPaired cp;
    private Display display;

    private Shell managementShell;

    private Composite scriptShell;
    private Composite listShell;
    private Composite picShell;

    private Button addNewPicButton;
    private Button deleteScriptButton;
    private Button deletePicButton;
    private Button addNewScriptButton;
    private Button addNewPairButton;

    private File scriptsDir;
    private File picsDir;

    public ManagementShell(Display display, final ConkyPaired cp) {
        this.cp = cp;
        this.display = display;

        /* load scripts and pics */
        scriptsDir = new File(ConkyPaired.getScriptsLoc());
        picsDir = new File(ConkyPaired.getPicsLoc());
    }

    public void makeNewManagementShell() {
        List<String> scripts = Arrays.asList(scriptsDir.list());
        List<String> pics = Arrays.asList(picsDir.list());

        managementShell = new Shell(display);
        managementShell.setText("Create new pair");
        managementShell.setLayout(new FillLayout(SWT.VERTICAL));
        managementShell.setSize(400, 400);

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

        deleteScriptButton = new Button(scriptShell, SWT.PUSH);
        deleteScriptButton.setText("Delete script");
        deleteScriptButton.pack();

        addNewScriptButton = new Button(scriptShell, SWT.PUSH);
        addNewScriptButton.setText("Add new script");
        addNewScriptButton.pack();

        listScript.pack();
        listPic.pack();
        scriptShell.pack();
        picShell.pack();

        deletePicButton = new Button(picShell, SWT.PUSH);
        deletePicButton.setText("Delete pic");
        deletePicButton.pack();

        addNewPicButton = new Button(picShell, SWT.PUSH);
        addNewPicButton.setText("Add new pic");
        addNewPicButton.pack();

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

        setListeners();

        managementShell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event event) {
                logger.info("managementShell handling close event");
                managementShell.dispose();
            }
        });

        managementShell.open();
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
                    if (pairNameExists(pair)) {
                        MessageBox messageBox = new MessageBox(managementShell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
                        messageBox.setText("About to override");
                        messageBox.setMessage("Override the pair \"" + pair.getName() + "\"?");
                        logger.info("Asking to override " + pair.getName());
                        int buttonID = messageBox.open();
                        switch (buttonID) {
                        case SWT.OK:
                            logger.info("Overriding " + pair.getName());
                            removePairByName(pair);
                            cp.getPairs().add(pair);
                        case SWT.CANCEL:
                            logger.info("Aborted overriding of " + pair.getName());
                            /* do nothing */
                        }
                    } else {
                        cp.getPairs().add(pair);
                    }
                    cp.persistPairs();
                }
            }
        });

        deleteScriptButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String choice = cp.getNewScriptChoice();
                if (choice != null) {
                    MessageBox messageBox = new MessageBox(managementShell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
                    messageBox.setText("About to delete");
                    messageBox.setMessage("Delete the script \"" + choice + "\"?");
                    logger.info("Asking to delete " + choice);
                    int buttonID = messageBox.open();
                    switch (buttonID) {
                    case SWT.OK:
                        String filename = ConkyPaired.getScriptsLoc() + "/" + choice;
                        try {
                            FileUtils.forceDelete(new File(filename));
                            refreshScriptsList();
                            logger.info("deleting " + choice);
                        } catch (IOException e1) {
                            logger.error("failed to delete " + choice);
                        }
                        break;
                    case SWT.CANCEL:
                        /* do nothing */
                        logger.info("Aborted deletion of " + choice);
                        break;
                    }
                }
            }
        });

        deletePicButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                String choice = cp.getNewPicChoice();
                if (choice != null) {
                    MessageBox messageBox = new MessageBox(managementShell, SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
                    messageBox.setText("About to delete");
                    messageBox.setMessage("Delete the pic \"" + choice + "\"?");
                    logger.info("Asking to delete " + choice);
                    int buttonID = messageBox.open();
                    switch (buttonID) {
                    case SWT.OK:
                        String filename = ConkyPaired.getPicsLoc() + "/" + choice;
                        try {
                            FileUtils.forceDelete(new File(filename));
                            refreshPicsList();
                            logger.info("deleting " + choice);
                        } catch (IOException e1) {
                            logger.error("failed to delete " + choice);
                        }
                        break;
                    case SWT.CANCEL:
                        /* do nothing */
                        logger.info("Aborted deletion of " + choice);
                        break;
                    }
                }
            }
        });

        addNewScriptButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(managementShell, SWT.SAVE);
                fileDialog.setText("Add a new script");
                String location = fileDialog.open();
                logger.info("location found: " + location);
                if (location != null) {
                    File sourceFile = new File(location);
                    String destFileName = ConkyPaired.getScriptsLoc() + "/" + sourceFile.getName();
                    File destFile = new File(destFileName);
                    try {
                        FileUtils.copyFile(sourceFile, destFile);
                        refreshScriptsList();
                    } catch (IOException e1) {
                        logger.error("unable to copy over file");
                    }
                }
            }
        });

        addNewPicButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog fileDialog = new FileDialog(managementShell, SWT.SAVE);
                fileDialog.setText("Add a new pic");
                String location = fileDialog.open();
                logger.info("location found: " + location);
                if (location != null) {
                    File sourceFile = new File(location);
                    String destFileName = ConkyPaired.getPicsLoc() + "/" + sourceFile.getName();
                    File destFile = new File(destFileName);
                    try {
                        FileUtils.copyFile(sourceFile, destFile);
                        refreshPicsList();
                    } catch (IOException e1) {
                        logger.error("unable to copy over file");
                    }
                }
            }
        });
    }

    private void removePairByName(Pair pair) {
        Pair pairToRemove = null;
        for (Pair p : cp.getPairs()) {
            if (p.getName().equals(pair.getName())) {
                pairToRemove = p;
                break;
            }
        }
        if (pairToRemove != null) {
            cp.getPairs().remove(pairToRemove);
        }
    }

    private boolean pairNameExists(Pair pair) {
        boolean matches = false;
        for (Pair p : cp.getPairs()) {
            if (p.getName().equals(pair.getName())) {
                matches = true;
                break;
            }
        }
        return matches;
    }

    public void refreshScriptsList() {
        listScript.removeAll();
        List<String> scripts = Arrays.asList(scriptsDir.list());
        for (String s : scripts) {
            listScript.add(s);
        }
        listScript.redraw();
    }

    public void refreshPicsList() {
        listPic.removeAll();
        List<String> scripts = Arrays.asList(picsDir.list());
        for (String s : scripts) {
            listPic.add(s);
        }
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

}
