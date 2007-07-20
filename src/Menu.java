#include "defines.inc"

class IdCommand extends Command {
    IdCommand(String label, int id) {
        this(label, id, Command.SCREEN, 1);
    }

    IdCommand(String label, int id, int commandType, int priority) {
        super(label, commandType, priority);
        this.id = id;
    }

    int getId() {
        return id;
    }

    int id;
}

class Menu extends Command implements CommandListener {
    static final Command kCmdBack = new Command("Back", Command.BACK, 2);

    Menu(String label, Command children[]) {
        super(label, Command.SCREEN, 1);
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

    void setParent(Display display, Displayable parent) {
        this.display = display;
        this.parent  = parent;
        this.rootListener = rootListener;
        int nChildren = children.length;
        for (int i = 0; i < nChildren; ++i) {
            if (children[i] instanceof Menu) {
                ((Menu)children[i]).setParent(display, list);
            }
        }
    }

    void setCommandListener(CommandListener listener) {
        rootListener = listener;
    }

    public void commandAction(Command cmd, Displayable cmdSource) {
        Command parentCmd = cmd;
        if (cmdSource == list) {
            LOG("from self list");
            if (cmd.getCommandType() == Command.BACK) {
                display.setCurrent(parent);
                return;
            }
            int index = ((List)cmdSource).getSelectedIndex();
            Command child = children[index];
            if (child instanceof Menu) {
                display.setCurrent(((Menu)child).list);
                return;
            } else {
                parentCmd = child;
            }
        }
        CommandListener upper = 
            (rootListener == null) ? (CommandListener)parent : rootListener;
        upper.commandAction(parentCmd, list);
    }

    Command[] children;
    List list;
    Displayable parent;
    Display display;
    CommandListener rootListener;
}

