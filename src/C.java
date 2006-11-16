import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.IOException;

//#define _STR(x) #x
//#define STR(x) _STR(x)

public final class C extends MIDlet implements CommandListener, Runnable {
    //static final String VERSION = STR(_VERSION_);
    private static final Command 
        //cmdSetup  = new Command("Setup", Command.SCREEN, 1),
        cmdOk    = new Command("Ok",    Command.OK, 1),
        cmdClearHistory = new Command("Clear history", Command.SCREEN, 2),
        cmdClearDefinitions   = new Command("Clear definitions", Command.SCREEN, 3),
        cmdHelp  = new Command("Help",  Command.HELP, 8),
        cmdAbout = new Command("About", Command.HELP, 9),
        cmdExit  = new Command("Exit",  Command.EXIT, 10),
        cmdCancel = new Command("Cancel", Command.CANCEL, 2);

    Alert confirmClearHistory = 
        new Alert("Clear history", 
                  "This will erase the history of past operations. (The user-defined functions and constants are not erased)",
                  null, AlertType.WARNING);
    Alert confirmClearDefinitions =
        new Alert("Clear definitions", 
                  "This will erase the user-defined functions and constants. (The history is not erased)",
                  null, AlertType.WARNING);
        
    static Display display;
    CalcCanvas calcCanvas;
    static final String helpStr = 
"To type an operator (+,-,*,/,^) press the '*' key. " +
"This displays a menu of all the operators, and you choose the one you want by pressing the corresponding key. " +
"E.g. to obtain +, press the '*' key twice. To obtain -, press '*' followed by '0'.\n" +
"When you want to use a function (sin(), cos(), ln()) or a constant (pi, e) press the '#' key to display the functions menu, " +
"and then press more keys to choose from the menu. " +
"In the menu some entries are displayed in a different color - they open a new sub-menu containing related elements. " +
"E.g. to type the decimal dot, press '#' twice. To type 'sin', press '#' and twice '1'.\n" +
"Use UP and DOWN to navigate the history of past expressions, LEFT and RIGHT to navigate inside the expression.\n" +
"You may define new constants and functions: 'a:=2^3' defines a new constant 'a' with the value 8. " +
"'f:=sqrt(x*x+y*y)' defines a new function f(x,y). The functions have up to 3 parameters named x, y, and z.";

    static final String aboutStr = NAME + " v"+VERSION + "\n\u00a9 2006 Mihai Preda\n" + URL;
    Form aboutForm = new Form("About"), helpForm = new Form("Help");
    Thread thread;

    public C() {
        System.out.println(URL);
        //_URL_;
        //System.out.println(STR(http://foo.bar/));
        calcCanvas = new CalcCanvas();
        
        confirmClearHistory.addCommand(cmdOk);
        confirmClearHistory.addCommand(cmdCancel);
        confirmClearHistory.setCommandListener(this);

        confirmClearDefinitions.addCommand(cmdOk);
        confirmClearDefinitions.addCommand(cmdCancel);
        confirmClearDefinitions.setCommandListener(this);     
        
        //addCommand(cmdSetup);
        calcCanvas.addCommand(cmdClearHistory);
        calcCanvas.addCommand(cmdClearDefinitions);
        calcCanvas.addCommand(cmdHelp);
        calcCanvas.addCommand(cmdAbout);
        calcCanvas.addCommand(cmdExit);
        calcCanvas.setCommandListener(this);
        try {
            aboutForm.append(Image.createImage("/a"));
        } catch (IOException e) {
        }
        aboutForm.append(aboutStr);
        aboutForm.addCommand(cmdOk);
        aboutForm.setCommandListener(this);
        helpForm.append(helpStr);
        helpForm.addCommand(cmdOk);
        helpForm.setCommandListener(this);

        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        calcCanvas.threadRun();
    }

    public void commandAction(Command c, Displayable d) {
        //if (c == cmdSetup) {}
        if (c == cmdClearHistory) {
            display.setCurrent(confirmClearHistory);
        } else if (c == cmdClearDefinitions) {
            display.setCurrent(confirmClearDefinitions);
        } else if (c == cmdHelp) {
            display.setCurrent(helpForm);
        } else if (c == cmdAbout) {
            display.setCurrent(aboutForm);
        } else if (c == cmdExit) {
            notifyDestroyed();
        } else if (c == cmdOk) {
            if (d == confirmClearHistory) {
                calcCanvas.clearHistory();
            } else if (d == confirmClearDefinitions) {
                calcCanvas.clearDefinitions();
            }
            display.setCurrent(calcCanvas);
        } else if (c == cmdCancel) {
            display.setCurrent(calcCanvas);
        }
    }

    protected void startApp() {
        display = Display.getDisplay(this);
        display.setCurrent(calcCanvas);
    }

    protected void pauseApp() {
    }

    protected void destroyApp(boolean uncond) {
    }
}
