// Copyright (c) 2007, Carlo Teubner
// Available under the MIT License (see COPYING).

public interface VMConstants {

    // XXX change to byte (saves memory but makes it messier to maintain: use script to generate this file?)
    // (or is it faster when using int? may depend on machine architecture)
    final static int
        LITERAL = 0,

        FIRST_PAR = 100,
        PAR_X = 100,
        PAR_Y = 101,
        PAR_Z = 102,
        LAST_PAR = 102,

        FIRST_VAR = 200,
        VAR_A = 200,
        VAR_B = 201,
        VAR_C = 202,
        VAR_D = 203,
        VAR_M = 204,
        VAR_N = 205,
        VAR_F = 206,
        VAR_G = 207,
        VAR_H = 208,
        LAST_VAR = 208,

        FIRST_VARFUN = 250,
        VARFUN_A = 250,
        VARFUN_B = 251,
        VARFUN_C = 252,
        VARFUN_D = 253,
        VARFUN_M = 254,
        VARFUN_N = 255,
        VARFUN_F = 256,
        VARFUN_G = 257,
        VARFUN_H = 258,
        LAST_VARFUN = 258,
        
        VARFUN_OFFSET = FIRST_VARFUN - FIRST_VAR,

        FIRST_CONST = 300,
        CONST_PI  = 300,
        CONST_E   = 301,
        CONST_ANS = 302,
        CONST_RND = 303,
        LAST_CONST = 303,

        UMINUS = 400,
        FACTORIAL = 401,

        PLUS = 500, MINUS = 501, TIMES = 502, DIVIDE = 503, POWER = 504, MODULO = 505,

        FIRST_FUNCTION = 600,
        
        FIRST_FUNCTION1 = 600,
        SIN  = 600, COS  = 601, TAN  = 602, ASIN  = 603, ACOS  = 604, ATAN  = 605,
        SINH = 606, COSH = 607, TANH = 608, ASINH = 609, ACOSH = 610, ATANH = 611,
        EXP = 612, LOG = 613, LOG10 = 614, LOG2 = 615,
        SQRT = 616, CBRT = 617,
        INT = 618, FRAC = 619, ABS = 620, FLOOR = 621, CEIL = 622, SIGN = 623,
        LAST_FUNCTION1 = 623,

        FIRST_FUNCTION2 = 700,
        MIN = 700, MAX = 701, GCD = 702, COMB = 703, PERM = 704,
        LAST_FUNCTION2 = 704,

        LAST_FUNCTION = 704,

        FIRST_PLOT_COMMAND = 800,
        PLOT = 800,
        MAP = 801,
        PARPLOT = 802,
        LAST_PLOT_COMMAND = 802,

        CUSTOM = 1000;
}