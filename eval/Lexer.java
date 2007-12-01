class Lexer {
    static final byte
        NONE = 0,
        NUMBER = 1,
        NAME   = 2,
        LPAREN = 3,
        RPAREN = 4,
        PLUS   = 5,
        MINUS  = 6,
        TIMES  = 7,
        DIV    = 8,
        FACT   = 9,
        POWER  = 10,
        MOD    = 11,
        COMMA  = 12,
        END    = 13,
        ERROR  = 14;
    
    static final String[] TOKEN_NAMES = {
        "NONE",
        "NUMBER",
        "NAME",
        "(",
        ")",
        "+",
        "-",
        "*",
        "/",
        "!",
        "^",
        "%",
        ",",
        "END",
        "ERROR"
    };

    static final char END_MARKER = '$';

    char[] input;
    int pos;
    double numberValue;
    String nameValue;

    public Lexer(String str) {
        int len = str.length();
        input = new char[len+1];
        str.getChars(0, len, input, 0);
        input[len] = END_MARKER;
        pos = 0;
    }

    int nextToken() {
        numberValue = 0;
        nameValue   = "";

        int p  = pos;
        char c = input[p++];
        int begin = pos;
        int ret = NONE;

        //skip white space
        while (c == ' ' || c == '\t') {
            c = input[p++];
        }
        pos = p;
        switch (c) {
        case '!': return FACT;
        case END_MARKER: return END;
        case '%': return MOD;
        case '(': return LPAREN;
        case ')': return RPAREN;
        case '*': return TIMES;
        case '+': return PLUS;
        case ',': return COMMA;
        case '-': return MINUS;
        case '/': return DIV;
        }
        if (c == '^') {
            return POWER;
        }

        --p;
        if (('0' <= c && c <= '9') || c == '.') {
            while ('0' <= c && c <= '9') {
                c = input[++p];
            } 
            if (c == '.') {
                c = input[++p];
                while ('0' <= c && c <= '9') {
                    c = input[++p];
                }
                }
            if (c == 'E') {
                c = input[++p];
                if (c == '-') {
                    c = input[++p];
                }
                while ('0' <= c && c <= '9') {
                    c = input[++p];
                }
            }
                /*
                  if (input[end-1] == 'E') {
                  --end;
                }
                */
                
                /*
                if (input[start] == '.' && 
                    (end == start+1  ||
                     (end >= start+2 && input[start+1] == 'E'))) {
                    throw Compiler.error;
                }
                */
            try {
                numberValue = Double.parseDouble(String.valueOf(input, begin, p-begin));
                ret = NUMBER;
            } catch (NumberFormatException e) {
                ret = ERROR;
            }
        } else {
            if (('a' <= c && c <= 'z') ||
                ('A' <= c && c <= 'Z')) {
                do {
                    c = input[++p];
                } while (('a' <= c && c <= 'z') ||
                         ('A' <= c && c <= 'Z') ||
                         ('0' <= c && c <= '9'));
            }
            ret = NAME;
        }
        nameValue = String.valueOf(input, begin, p-begin);
        pos = p;
        return ret;
    }
    
    public static void main(String[] argv) {
        Lexer lexer = new Lexer(argv[0]);
        int token;
        do {
            token = lexer.nextToken();
            System.out.println(Lexer.TOKEN_NAMES[token] + " (" + lexer.pos + ", " + lexer.nameValue + ", " + lexer.numberValue + ")");
        } while (token != END);
    }
}
