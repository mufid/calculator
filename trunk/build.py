#!/usr/bin/python

bytecodesStr = '''
RET CONST CALL 
ADD SUB MUL DIV MOD
PI E RND
UMIN POWER FACT
SQRT CBRT
EXP LN LOG10 LOG2
SIN COS TAN ASIN ACOS ATAN
SINH COSH TANH ASINH ACOSH ATANH
ABS FLOOR CEIL SIGN
MIN MAX GCD COMB PERM
LDX LDY LDZ
'''

template = '''
// This file is automatically generated by the build.py script. Do not edit!
public class VM {

public static final byte
%(bytecodes)s;

public static final String[] opcodeName = {
%(names)s
};

public static final byte[] builtinArity = 
{%(arity)s};

}
'''

builtinArity1 = '''
sqrt cbrt
sin cos tan asin acos atan
sinh cosh tanh asinh acosh atanh
exp ln log10 log2
abs floor ceil sign
'''

builtinArity2 = 'min max gcd comb perm'


def genVM():
    bytecodes = [x.lower() for x in bytecodesStr.split()]
    bytecodes += ['bytecode_end']
    str1 = ',\n'.join(['%s = %d' % (name.upper(), id) for (name, id) in zip(bytecodes, xrange(256))])
    str2 = ',\n'.join(['"%s"' % name for name in bytecodes])
    arity = [0]*len(bytecodes)
    for a1 in builtinArity1.split():
        arity[bytecodes.index(a1)] = 1
    for a2 in builtinArity2.split():
        arity[bytecodes.index(a2)] = 2
    arityStr = ', '.join(map(str, arity))

    fo = open('src/VM.java', 'w')
    fo.write(template % dict(bytecodes=str1, names=str2, arity=arityStr))
    fo.close()

genVM()

import mebuild

spec = dict(
cldc    = '1.1',
midp    = '2.0',
name    = 'Javia Calculator',
icon    = 'a',
cls     = 'Calc',
version = '0.2.0',
vendor  = 'Mihai Preda',
infoUrl = 'http://calculator.javia.org/',
extralibs = [],
fileName  = 'calculator'
)

midlet = mebuild.Midlet(**spec)

midlet.build(mebuild.cmdLineOptions())
