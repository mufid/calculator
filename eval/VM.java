// This file is automatically generated by the build.py script. Do not edit!

package org.javia.eval;

class VM {

public static final byte
RESERVED = 0,
CONST = 1,
CALL = 2,
ADD = 3,
SUB = 4,
MUL = 5,
DIV = 6,
MOD = 7,
RND = 8,
UMIN = 9,
POWER = 10,
FACT = 11,
SQRT = 12,
CBRT = 13,
EXP = 14,
LN = 15,
LOG10 = 16,
LOG2 = 17,
SIN = 18,
COS = 19,
TAN = 20,
ASIN = 21,
ACOS = 22,
ATAN = 23,
SINH = 24,
COSH = 25,
TANH = 26,
ASINH = 27,
ACOSH = 28,
ATANH = 29,
ABS = 30,
FLOOR = 31,
CEIL = 32,
SIGN = 33,
MIN = 34,
MAX = 35,
GCD = 36,
COMB = 37,
PERM = 38,
LOAD0 = 39,
LOAD1 = 40,
LOAD2 = 41,
LOAD3 = 42,
LOAD4 = 43,
BYTECODE_END = 44;

public static final String[] opcodeName = {
"reserved",
"const",
"call",
"add",
"sub",
"mul",
"div",
"mod",
"rnd",
"umin",
"power",
"fact",
"sqrt",
"cbrt",
"exp",
"ln",
"log10",
"log2",
"sin",
"cos",
"tan",
"asin",
"acos",
"atan",
"sinh",
"cosh",
"tanh",
"asinh",
"acosh",
"atanh",
"abs",
"floor",
"ceil",
"sign",
"min",
"max",
"gcd",
"comb",
"perm",
"load0",
"load1",
"load2",
"load3",
"load4",
"bytecode_end"
};

public static final byte[] builtinArity = 
{-1, -1, -1, -1, -1, -1, -1, -1, 0, -1, -1, -1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, -1, -1, -1, -1, -1, -1};

}
