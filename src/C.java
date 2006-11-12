import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.IOException;

public final class C extends MIDlet implements CommandListener {
    private static final Command 
        //cmdSetup  = new Command("Setup", Command.SCREEN, 1),
        cmdOk    = new Command("Ok",    Command.OK, 1),
        cmdClearHistory = new Command("Erase History", Command.SCREEN, 2),
        cmdHelp  = new Command("Help",  Command.HELP, 8),
        cmdAbout = new Command("About", Command.HELP, 9),
        cmdExit  = new Command("Exit",  Command.EXIT, 10);

    private Display display;
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

    static final String aboutStr = "JaviaCalc v0.1.0\n\u00a9 2006 Mihai Preda\nhttp://javia.org/calc/";
    Form aboutForm = new Form("About"), helpForm = new Form("Help");
    
    public C() {
        calcCanvas = new CalcCanvas();
        //addCommand(cmdSetup);
        calcCanvas.addCommand(cmdClearHistory);
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
    }

    public void commandAction(Command c, Displayable d) {
        //if (c == cmdSetup) {}
        if (c == cmdClearHistory) {
            calcCanvas.clearHistory();
        } else if (c == cmdHelp) {
            display.setCurrent(helpForm);
        } else if (c == cmdAbout) {
            display.setCurrent(aboutForm);
        } else if (c == cmdExit) {
            notifyDestroyed();
        } else if (c == cmdOk) {
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
