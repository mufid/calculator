import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;

class Cmd extends Command {
    Cmd(String label, int id) {
        this(label, id, Command.SCREEN, 1);
    }

    Cmd(String label, int id, int commandType, int priority) {
        super(label, commandType, priority);
        this.id = id;
    }

    int getId() {
        return id;
    }

    int id;
}

class Menu extends Cmd implements CommandListener {
    static final Command kCmdBack = new Command("Back", Command.BACK, 2);

    Menu(String label, Command children[]) {
        super(label, -1);
        this.children = children;
        this.parent   = null;
        this.display  = null;
        this.rootListener = null;
        int nChildren = children.length;
        String childLabels[] = new String[nChildren];
        for (int i = 0; i < nChildren; ++i) {
            childLabels[i] = children[i].getLabel();
        }
        this.list = new List(label, List.IMPLICIT, childLabels, null);
        list.addCommand(kCmdBack);
        list.setCommandListener(this);
    }

    void setParent(Display display, Displayable parent, CommandListener root) {
        this.display = display;
        this.parent  = parent;
        this.rootListener = root;
        int nChildren = children.length;
        for (int i = 0; i < nChildren; ++i) {
            if (children[i] instanceof Menu) {
                ((Menu)children[i]).setParent(display, list, root);
            }
        }
    }

    public void commandAction(Command cmd, Displayable cmdSource) {
        if (cmd.getCommandType() == Command.BACK) {
            display.setCurrent(parent);
            return;
        }
        int index = ((List)cmdSource).getSelectedIndex();
        Command child = children[index];
        if (child instanceof Menu) {
            display.setCurrent(((Menu)child).list);
        } else {
            rootListener.commandAction(child, list);
        }
    }

    Command[] children;
    List list;
    Displayable parent;
    Display display;
    CommandListener rootListener;
}

