import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.IOException;

//#define _STR(x) #x
//#define STR(x) _STR(x)

public final class C extends MIDlet implements CommandListener, Runnable {
    //static final String VERSION = STR(_VERSION_);
    private static final Command 
        //cmdSetup  = new Command("Setup", Command.SCREEN, 1),
        cmdKeyDelete = new Command("Delete", Command.BACK, 1),
        //cmdClearHistory = new Command("Clear history", Command.SCREEN, 2),
        //cmdClearDefinitions   = new Command("Clear definitions", Command.SCREEN, 3),
        cmdHelp  = new Command("Help",  Command.HELP, 8),
        cmdAbout = new Command("About", Command.HELP, 9),
        cmdExit  = new Command("Exit",  Command.EXIT, 10),

        cmdOk    = new Command("Ok",    Command.OK, 1),
        cmdYes   = new Command("Yes", Command.OK, 1),
        cmdCancel = new Command("Cancel", Command.CANCEL, 2);

    /*
    Alert confirmClearHistory = 
        new Alert("Clear history", 
                  "Do you want to erase the history of past operations? (The user-defined functions are preserved)",
                  null, AlertType.WARNING);
    */
    /*
    Alert confirmClearDefinitions =
        new Alert("Clear definitions", 
                  "Do you want to erase the user-defined functions and constants?",
                  null, AlertType.WARNING);
    */
        
    static Display display;
    CalcCanvas calcCanvas;
    static final String helpStr = 
"Press * or # to display the menu, " +
"next press one more key (1-9,*0#) to select. " +
"The left menu * contains the decimal dot, functions and constants. " +
"The right menu # contains operators like +,-,*,/. " +
"The entries marked in blue open additional sub-menus. " +
"E.g. to obtain the decimal dot, press twice *; " +
"to obtain +, press twice #.\n\n" +

"Use UP and DOWN to navigate the history.\n\n" +

"Use := to define new functions and constants. " +
"E.g. a:=pi/2 is a constant; f:=sqrt(x^2+y^2) is a function with two parameters x,y. " +
"The functions may have up to three parameters named x,y,z.\n\n" +

"The variable 'ans' contains the value of the most recent expression. " + 
"'ans' is automatically added in front of an expression that starts with an operator. " +
"E.g. typing '+2' becomes 'ans+2'.";

    Form aboutForm = new Form("About"), helpForm = new Form("Help");
    Thread thread;

    public C() {
        System.out.println(URL);
        //_URL_;
        //System.out.println(STR(http://foo.bar/));
        calcCanvas = new CalcCanvas();

        /*
        confirmClearHistory.addCommand(cmdYes);
        confirmClearHistory.addCommand(cmdCancel);
        confirmClearHistory.setCommandListener(this);
        */
        
        /*
        confirmClearDefinitions.addCommand(cmdYes);
        confirmClearDefinitions.addCommand(cmdCancel);
        confirmClearDefinitions.setCommandListener(this);     
        */
        
        //addCommand(cmdSetup);
        calcCanvas.addCommand(cmdKeyDelete);
        //calcCanvas.addCommand(cmdClearHistory);
        //calcCanvas.addCommand(cmdClearDefinitions);
        calcCanvas.addCommand(cmdHelp);
        calcCanvas.addCommand(cmdAbout);
        calcCanvas.addCommand(cmdExit);
        calcCanvas.setCommandListener(this);
        try {
            aboutForm.append(Image.createImage("/a"));
        } catch (IOException e) {
        }
        //static final String aboutStr = NAME + " v"+VERSION + "\n\u00a9 2006 Mihai Preda\n" + URL;
        aboutForm.append(NAME + " " + VERSION + "\n");
        aboutForm.append("\u00a9 Mihai Preda\n" + URL);
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
        /*
        else if (c == cmdClearHistory) {
            display.setCurrent(confirmClearHistory);
        } else if (c == cmdClearDefinitions) {
            display.setCurrent(confirmClearDefinitions);
        } 
        */

        /*
        else if (c == cmdYes) {
            if (d == confirmClearHistory) {
                calcCanvas.clearHistory();
            } else if (d == confirmClearDefinitions) {
            calcCanvas.clearDefinitions();
            }
            
            display.setCurrent(calcCanvas);
        }
        */


        if (c == cmdKeyDelete) {
            calcCanvas.keyPressed(CalcCanvas.KEY_CLEAR);
        } else if (c == cmdHelp) {
            display.setCurrent(helpForm);
        } else if (c == cmdAbout) {
            display.setCurrent(aboutForm);
        } else if (c == cmdExit) {
            notifyDestroyed();
        } else { //cmdCancel, cmdOk
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
        calcCanvas.saveOnExit();
    }
}
