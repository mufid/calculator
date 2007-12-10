// Copyright (c) 2006-2007 Mihai Preda, Carlo Teubner.
// Available under the MIT License (see COPYING).

import javax.microedition.lcdui.*;
import java.io.*;
import javax.microedition.midlet.MIDlet;

import org.javia.lib.*;

public final class Calc extends BasicMIDlet implements CommandListener, Runnable {
    static UnitTest dummy;
    static Calc self;
        
    static Display display;
    CalcCanvas calcCanvas;
    //PlotCanvas plotCanvas;
    static final String helpStr = 
"Press * or # to display a menu, " +
"next press a key (1-9,*0#) to select. " +
"Left menu * contains operators like +-*/; " +
"right menu # contains decimal dot, functions and constants. " +
"E.g. to obtain +, press * twice. " +
"Entries with green corner open sub-menus. " +

"Up and down keys navigate through history.\n\n" +

"Use := to define new functions and constants. " +
"E.g. a:=pi/2 is a constant; f:=sqrt(x^2+y^2) is a function of two parameters x,y. " +
"Functions may have up to 3 parameters x,y,z.\n\n" +

"The variable 'ans' contains the value of the most recent expression. " + 
"'ans' is automatically added in front of an expression that starts with an operator; " +
"e.g. typing '+2' yields 'ans+2'.\n\n" +

"plot(f, xmin, xmax) produces a graph of function f(x) with " +
"x running from xmin to xmax. f can be user-defined (see above) or built-in " +
"(like sin, sqrt) or any expression involving x (like x^2/2), " +
"and xmin,xmax can be numbers or expressions.\n\n" +

"map(f, xmin, xmax, ymin, ymax) produces a two-dimensional map " +
"of the values of a function f(x,y), with x running from xmin to xmax " +
"and y from ymin to ymax. If ymax is omitted, it will be chosen to make " +
"aspect ratio equal to 1.\n\n" +

"par(f, g, tmin, tmax) produces a plot of the points (f(t),g(t)) " +
"with t running from tmin to tmax, i.e., f determines x coordinate " +
"and g determines y coordinate.";


    static final int CMD_OK=1, CMD_HELP=3, CMD_ABOUT=4, CMD_EXIT=5,
        CMD_ANG_RAD  = 6, CMD_ANG_DEG   = 7,
        CMD_ROUND_NO = 8, CMD_ROUND_YES = 9,
        CMD_AXES_NO = 10, CMD_AXES_YES = 11,
        CMD_LABELS_YES = 12, CMD_LABELS_NO = 13,
        CMD_AR1_YES = 14, CMD_AR1_NO = 15;

    static final Command cmdOk
        = new Cmd("OK", CMD_OK, Command.BACK);

    Menu menu;
    Form aboutForm = new Form("About"), helpForm = new Form("Help");
    Thread thread;

    static final int RS_CONFIG = 1;
    static final int RS_CURRENT = 2, RS_HIST_START = 3, RS_MAX_HIST = 32; //32;
    static final int RS_SYMB_START = RS_HIST_START + RS_MAX_HIST;
    static Store rs;
    static CalcConfig cfg;

    private static final int STORE_VERSION = 0;

    public Calc() {
        self = this;
        rs = new Store("calc");
        if (rs.getNumRecords() == 0) {
            rs.write(1, new byte[]{(byte) STORE_VERSION});
        }
        cfg = new CalcConfig(rs, RS_CONFIG);
        display = Display.getDisplay(this);
        calcCanvas = new CalcCanvas();
        //plotCanvas = new PlotCanvas(display, calcCanvas);

        menu = new Menu("Menu", new Cmd[] {
                new Menu("General settings", new Cmd[] {
                        new Menu("Angle unit", new Cmd[] {
                                new Cmd("Radians", CMD_ANG_RAD),
                                new Cmd("Degrees", CMD_ANG_DEG)
                            }, cfg.angleInRadians ? 0 : 1
                            ),
                        new Menu("Rounding", new Cmd[] {
                                new Cmd("Smart rounding", CMD_ROUND_YES),
                                new Cmd("No rounding",    CMD_ROUND_NO),
                            }, cfg.roundingDigits == 1 ? 0 : 1
                            )
                    }),
                new Menu("Plot settings", new Cmd[] {
                        new Menu("Axes", new Cmd[] {
                                new Cmd("Draw axes", CMD_AXES_YES),
                                new Cmd("No axes",   CMD_AXES_NO)
                        }, cfg.axes ? 0 : 1
                        ),
                        new Menu("Labels", new Cmd[] {
                                new Cmd("Draw labels", CMD_LABELS_YES),
                                new Cmd("No labels",   CMD_LABELS_NO)
                        }, cfg.labels ? 0 : 1
                        ),
                        new Menu("Aspect ratio", new Cmd[] {
                                new Cmd("Aspect ratio = 1",       CMD_AR1_YES),
                                new Cmd("Stretch to fill screen", CMD_AR1_NO)
                        }, cfg.aspectRatio1 ? 0 : 1
                        )
                }),
                new Cmd("Help",  CMD_HELP),
                new Cmd("About", CMD_ABOUT),
                new Cmd("Exit",  CMD_EXIT)
            });

        try {
            aboutForm.append(Image.createImage("/a"));
        } catch (IOException e) {
        }
        aboutForm.append("" + 
                         getAppProperty("MIDlet-Name") + " " + 
                         getAppProperty("MIDlet-Version") + "\n");
        aboutForm.append(new StringItem(null, getAppProperty("MIDlet-Info-URL"), Item.HYPERLINK));
        aboutForm.addCommand(cmdOk);
        aboutForm.setCommandListener(this);

        helpForm.append(helpStr);
        helpForm.addCommand(cmdOk);
        helpForm.setCommandListener(this);

        thread = new Thread(this);
        thread.start();

        display.setCurrent(calcCanvas);
        menu.setParent(this, calcCanvas);
    }

    void displayMenu() {
        display.setCurrent(menu.list);
    }

    public void run() {
        calcCanvas.threadRun();
    }

    public void commandAction(Command c, Displayable d) {
        display.setCurrent(calcCanvas);
        switch (((Cmd)c).id()) {
        case CMD_OK:
            break;

        case CMD_ANG_RAD:
            cfg.setAngleInRadians(true);
            calcCanvas.updateResult();
            break;

        case CMD_ANG_DEG:
            cfg.setAngleInRadians(false);
            calcCanvas.updateResult();
            break;

        case CMD_ROUND_YES:
            cfg.setRoundingDigits(1);
            calcCanvas.updateResult();
            break;

        case CMD_ROUND_NO:
            cfg.setRoundingDigits(0);
            calcCanvas.updateResult();
            break;

        case CMD_AXES_YES:
            cfg.setAxes(true);
            break;

        case CMD_AXES_NO:
            cfg.setAxes(false);
            break;

        case CMD_LABELS_YES:
            cfg.setLabels(true);
            break;

        case CMD_LABELS_NO:
            cfg.setLabels(false);
            break;

        case CMD_AR1_YES:
            cfg.setAspectRatio(true);
            break;

        case CMD_AR1_NO:
            cfg.setAspectRatio(false);
            break;

        case CMD_HELP:
            display.setCurrent(helpForm);
            break;
            
        case CMD_ABOUT:
            display.setCurrent(aboutForm);
            break;
            
        case CMD_EXIT:
            exit();
            break;
        }
    }

    protected void onExit() {
        calcCanvas.saveOnExit();
    }
}
