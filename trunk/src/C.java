// Copyright (c) 2006-2007, Mihai Preda

#include "defines.inc"
import javax.microedition.midlet.*;

public final class C extends MIDlet implements CommandListener, Runnable {
    static C self;
        
    static Display display;
    CalcCanvas calcCanvas;
    static final String helpStr = 
"Press * or # to display the menu, " +
"next press one more key (1-9,*0#) to select. " +
"The left menu * contains operators like +-*/. " +
"The right menu # contains the decimal dot, functions and constants. " +
"The yellow entries open additional sub-menus. " +
"To obtain +, press twice *. " +
"To obtain the decimal dot, press twice #.\n\n" +

"Use UP and DOWN to navigate the history.\n\n" +

"Use := to define new functions and constants. " +
"E.g. a:=pi/2 is a constant; f:=sqrt(x^2+y^2) is a function with two parameters x,y. " +
"The functions may have up to three parameters named x,y,z.\n\n" +

"The variable 'ans' contains the value of the most recent expression. " + 
"'ans' is automatically added in front of an expression that starts with an operator. " +
"E.g. typing '+2' becomes 'ans+2'.";

    static final int CMD_OK=1, CMD_HELP=3, CMD_ABOUT=4, CMD_EXIT=5,
        CMD_ANG_RAD  = 6, CMD_ANG_DEG   = 7,
        CMD_ROUND_NO = 8, CMD_ROUND_YES = 9;

    static final Command cmdOk
        = new Cmd("Ok", CMD_OK, Command.BACK, 1);

    Menu menu = new Menu("Menu", new Cmd[] {
            new Menu("Setup", new Cmd[] {
                    new Menu("Angle Unit", new Cmd[] {
                            new Cmd("Radians", CMD_ANG_RAD),
                            new Cmd("Degrees", CMD_ANG_DEG)
                        }),
                    new Menu("Rounding", new Cmd[] {
                            new Cmd("Smart Rounding", CMD_ROUND_YES),
                            new Cmd("No Rouding",     CMD_ROUND_NO),
                        })
                }),
            new Cmd("Help",  CMD_HELP),
            new Cmd("About", CMD_ABOUT),
            new Cmd("Exit",  CMD_EXIT)
        });

    Form aboutForm = new Form("About"), helpForm = new Form("Help");
    Thread thread;

    static final int RS_CONFIG = 1;
    static final int RS_HIST_START = 3, RS_MAX_HIST = 32; //32;
    static final int RS_SYMB_START = RS_HIST_START + RS_MAX_HIST;
    static RMS rs;
    static CalcConfig cfg;

    public C() {
        self = this;
        rs = new RMS("calc");
        cfg = new CalcConfig(rs, RS_CONFIG);
        calcCanvas = new CalcCanvas();

        try {
            aboutForm.append(Image.createImage("/a"));
        } catch (IOException e) {
        }
        aboutForm.append(NAME + " " + VERSION + "\n");
        aboutForm.append("\u00a9 Mihai Preda\n" + URL);
        aboutForm.addCommand(cmdOk);
        aboutForm.setCommandListener(this);

        helpForm.append(helpStr);
        helpForm.addCommand(cmdOk);
        helpForm.setCommandListener(this);

        thread = new Thread(this);
        thread.start();

        display = Display.getDisplay(this);
        display.setCurrent(calcCanvas);
        menu.setParent(display, calcCanvas, this);
    }
    
    void displayMenu() {
        display.setCurrent(menu.list);
    }

    public void run() {
        calcCanvas.threadRun();
    }

    public void commandAction(Command c, Displayable d) {
        display.setCurrent(calcCanvas);
        switch (((Cmd)c).id) {
        case CMD_OK:
            break;

        case CMD_ANG_RAD:
            cfg.setAngleInRadians(true);
            break;

        case CMD_ANG_DEG:
            cfg.setAngleInRadians(false);
            break;

        case CMD_ROUND_YES:
            cfg.setRoundingDigits(1);
            break;

        case CMD_ROUND_NO:
            cfg.setRoundingDigits(0);
            break;

        case CMD_HELP:
            display.setCurrent(helpForm);
            break;
            
        case CMD_ABOUT:
            display.setCurrent(aboutForm);
            break;
            
        case CMD_EXIT:
            onExit();
            notifyDestroyed();
            break;
        }
    }

    protected void startApp() {
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean uncond) { //throws MIDletStateChangeException {
        onExit();
    }

    private void onExit() {
        calcCanvas.saveOnExit();
    }
}
