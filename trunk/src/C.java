#include "defines.inc"
import javax.microedition.midlet.*;

public final class C extends MIDlet implements CommandListener, Runnable {
    static C self;
    
    private static final Command 
        cmdOk    = new Command("Ok",    Command.BACK, 1),
        cmdBack  = new Command("Back",  Command.BACK, 2);
    
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

    List menuList  = new List("Menu",       List.IMPLICIT, new String[]{
        "Set Angle Unit", 
        "Help", 
        "About", 
        "Exit"
    }, null);

    Form aboutForm = new Form("About"), helpForm = new Form("Help");
    Thread thread;

    static final int RS_CONFIG = 1;
    static final int RS_HIST_START = 3, RS_MAX_HIST = 32; //32;
    static final int RS_SYMB_START = RS_HIST_START + RS_MAX_HIST;
    static RMS rs;
    static boolean angleInRadians = true;
    static Config cfg;

    public C() {
        self = this;
        rs = new RMS("calc");
        cfg = new Config(rs, RS_CONFIG);
        //LOG("config size " + cfg.size());
        if (cfg.size() == 0) {
            cfg.set("angleUnit", "rad");
        }
        angleInRadians = cfg.get("angleUnit").equals("rad");
        
        calcCanvas = new CalcCanvas();

        menuList.addCommand(cmdBack);
        menuList.setCommandListener(this);

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
    }
    
    void displayMenu() {
        menuList.set(0, (angleInRadians? "Degrees" : "Radians") + " angle unit", null);
        display.setCurrent(menuList);
    }

    public void run() {
        calcCanvas.threadRun();
    }

    public void commandAction(Command c, Displayable d) {
        if (c == cmdBack || c == cmdOk) {
            display.setCurrent(calcCanvas);
            return;
        }

        switch (((List) d).getSelectedIndex()) {
        case 0:
            angleInRadians = !angleInRadians;
            display.setCurrent(calcCanvas);
            cfg.set("angleUnit", angleInRadians?"rad":"deg");
            cfg.save();
            break;
        case 1:
            display.setCurrent(helpForm);
            break;
        case 2:
            display.setCurrent(aboutForm);
            break;
        case 3:
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
