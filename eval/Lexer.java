class Lexer {
    static final TokenType
        ADD    = new TokenType("+", 3, TokenType.INFIX, false),
        SUB    = new TokenType("-", 3, TokenType.INFIX, false),

        MUL    = new TokenType("*", 4, TokenType.INFIX, false),
        DIV    = new TokenType("/", 4, TokenType.INFIX, false),
        MOD    = new TokenType("%", 4, TokenType.INFIX, false),

        UMIN   = new TokenType("-", 5, TokenType.PREFIX, false),

        POWER  = new TokenType("^", 6, TokenType.INFIX,  false),

        FACT   = new TokenType("!", 7, TokenType.POSTFIX, true),

        CALL   = new TokenType("call", 0, 0, false),
        COMMA  = new TokenType(",",    1, 0, false),

        LPAREN = new TokenType("(",    1, 0, false),
        RPAREN = new TokenType(")",    2, 0, true),

        NUMBER = new TokenType("number", 9, 0, true),
        CONST  = new TokenType("alpha",  9, 0, true),

        END    = new TokenType("end",   0, 0, false),
        ERROR  = new TokenType("error", 0, 0, false);
    
    static final Token
        TOK_ADD = new Token(ADD),
        TOK_SUB = new Token(SUB),

        TOK_MUL = new Token(MUL),
        TOK_DIV = new Token(DIV),
        TOK_MOD = new Token(MOD),

        TOK_UMIN = new Token(UMIN),

        TOK_POWER  = new Token(POWER),
        TOK_FACT   = new Token(FACT),

        TOK_LPAREN = new Token(LPAREN),
        TOK_RPAREN = new Token(RPAREN),
        TOK_COMMA  = new Token(COMMA),

        TOK_END    = new Token(END),
        TOK_ERROR  = new Token(ERROR);

    /*
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
    */

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

    Token nextToken() {
        numberValue = 0;
        nameValue   = "";

        int p  = pos;
        char c = input[p++];
        int begin = pos;
        TokenType retType;

        //skip white space
        while (c == ' ' || c == '\t') {
            c = input[p++];
        }
        pos = p;
        switch (c) {
        case '!': return TOK_FACT;
        case END_MARKER: return TOK_END;
        case '%': return TOK_MOD;
        case '(': return TOK_LPAREN;
        case ')': return TOK_RPAREN;
        case '*': return TOK_TIMES;
        case '+': return TOK_PLUS;
        case ',': return TOK_COMMA;
        case '-': return TOK_MINUS;
        case '/': return TOK_DIV;
        }
        if (c == '^') {
            return TOK_POWER;
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
            try {
                numberValue = Double.parseDouble(String.valueOf(input, begin, p-begin));
                retType = NUMBER;
            } catch (NumberFormatException e) {
                pos = p;
                return TOK_ERROR;
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
            if (c == '(') {
                ++p;
                retType = CALL;
            } else {
                retType = NAME;
            }
        }
        nameValue = String.valueOf(input, begin, p-begin);
        pos = p;
        return new Token(retType, numberValue, nameValue);
    }
    
    public static void main(String[] argv) {
        Lexer lexer = new Lexer(argv[0]);
        Token token;
        do {
            token = lexer.nextToken();
            System.out.println(token.name + " (" + lexer.pos + ", " + lexer.nameValue + ", " + lexer.numberValue + ")");
        } while (token != END);
    }
}
