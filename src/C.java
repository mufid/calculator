public final class C extends MIDlet implements CommandListener {
    private Display display;
    CalcCanvas calcCanvas;
 
    public C() {
        calcCanvas = new CalcCanvas();
    }

    public void commandAction(Command c, Displayable d) {
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
