class MoreMath {
    //parts derived from FDLIBM 5.3

    //#define HI(x) (int)(Double.doubleToLongBits(x) >>> 32)
    //#define LO(x) (int)(Double.doubleToLongBits(x) & 0x00000000ffffffffL)

    private static final int HI(double x) {
        return (int)(Double.doubleToLongBits(x) >> 32);
    }
  
    private static final int LO(double x) {
        return (int)Double.doubleToLongBits(x);
    }

    private static final long set(int newHiPart, int newLowPart) {
        return ((((long) newHiPart) << 32) | newLowPart);
    }

    /*
    //following two methods from MicroDouble 1.0
    private static long setLO(long d, int newLowPart) {
        return ((d & 0xFFFFFFFF00000000L) | newLowPart);
    }

    private static long setHI(long d, int newHiPart) {
      return ((d & 0x00000000FFFFFFFFL) | (((long) newHiPart) << 32));
    }
    */

    static final double
        E      = 2.7182818284590452354,
        PI     = 3.14159265358979323846,
        LOG2E  = 1.4426950408889634074,
        LOG10E = 0.43429448190325182765,
        LN2    = 0.69314718055994530942,
        LN10   = 2.30258509299404568402,
        SQRT2  = 1.41421356237309504880;

    private static final double
        zero = 0.0,
        one  = 1.0,
        two  = 2.0,
        half = .5,

        //exp()
        huge     = 1.0e300,
        twom1000= 9.33263618503218878990e-302,     /* 2**-1000=0x01700000,0*/
        o_threshold =  7.09782712893383973096e+02,
        u_threshold = -7.45133219101941108420e+02,
        //invln2 = 1.44269504088896338700,
        P1   =  1.66666666666666019037e-01,
        P2   = -2.77777777770155933842e-03,
        P3   =  6.61375632143793436117e-05,
        P4   = -1.65339022054652515390e-06,
        P5   =  4.13813679705723846039e-08,

        ln2_hi  =  6.93147180369123816490e-01,
        ln2_lo  =  1.90821492927058770002e-10,
        halF[]  = {0.5, -0.5},
        ln2HI[] = {ln2_hi, -ln2_hi},
        ln2LO[] = {ln2_lo, -ln2_lo},

        //log()
        two54 = 1.80143985094819840000e+16,
        Lg1 = 6.666666666666735130e-01,
        Lg2 = 3.999999999940941908e-01,
        Lg3 = 2.857142874366239149e-01,
        Lg4 = 2.222219843214978396e-01,
        Lg5 = 1.818357216161805012e-01,
        Lg6 = 1.531383769920937332e-01,
        Lg7 = 1.479819860511658591e-01;

    
    static final double exp(double x) {
        int hx = HI(x);
        int xsb = hx >>> 31;
        hx &= 0x7fffffff;
        
        if (hx >= 0x40862E42) {			/* if |x|>=709.78... */
            if(hx >= 0x7ff00000) {
                if(((hx & 0xfffff) | LO(x)) != 0) { 
                    return x+x; 		/* NaN */
                } else {
                    return (xsb == 0) ? x : 0.0;	/* exp(+-inf)={inf,0} */
                }
            }
            if (x > o_threshold) { return huge*huge; /* overflow */ }
            if (x < u_threshold) { return twom1000*twom1000; /* underflow */ }
        }

        double hi = 0, lo = 0, c, t, y;
        int k = 0;
        if (hx > 0x3fd62e42) {		/* if  |x| > 0.5 ln2 */ 
            if(hx < 0x3FF0A2B2) {	/* and |x| < 1.5 ln2 */
                hi = x - ln2HI[xsb]; 
                lo = ln2LO[xsb]; 
                k = 1 - xsb - xsb;
            } else {
                k  = (int)(LOG2E * x + halF[xsb]);
                t  = k;
                hi = x - t * ln2HI[0];	/* t*ln2HI is exact here */
                lo = t*ln2LO[0];
            }
            x  = hi - lo;
        } else if (hx < 0x3e300000)  {	/* when |x|<2**-28 */
            if(huge + x > one) return one + x;/* trigger inexact */
        } else {
            k = 0;
        }

        /* x is now in primary range */
        t  = x * x;
        c  = x - t * (P1 + t * (P2 + t * (P3 +t * (P4 + t * P5))));
        if (k==0) {
            return one-((x*c)/(c-2.0)-x); 
        } else { 
            y = one-((lo-(x*c)/(2.0-c))-hi);
        }
        long ybits = Double.doubleToLongBits(y);
        if(k >= -1021) {
            ybits += ((long)k) << 52;
            //HI(y) += (k<<20); /* add k to y's exponent */
            return Double.longBitsToDouble(ybits);
        } else {
            ybits += ((long)(k + 1000)) << 52;
            //HI(y) += ((k+1000)<<20);/* add k to y's exponent */
            return Double.longBitsToDouble(ybits) * twom1000;
        }
    }

    static final double log(double x) {
        double hfsq, f, s, z, R, w, t1, t2, dk;
        int k, i, j;
        //unsigned lx;

        int hx = HI(x);
        int lx = LO(x);

        k=0;
        if (hx < 0x00100000) {			/* x < 2**-1022  */
            if (((hx & 0x7fffffff) | lx) == 0) { 
                return -two54/zero;		/* log(+-0)=-inf */
            }
            if (hx < 0) {
                return (x-x) / zero;	/* log(-#) = NaN */
            }
            k -= 54; 
            x *= two54; /* subnormal number, scale up x */
            hx = HI(x);		/* high word of x */
        } 
        if (hx >= 0x7ff00000) {
            return x + x;
        }
        k  += (hx >> 20) - 1023;

        //System.out.println("k= " + k);

        hx &= 0x000fffff;
        i = (hx + 0x95f64) & 0x100000;
        //System.out.println("hx= " + hx + "; i= " + i);
        long bits = (((long)(hx | (i ^ 0x3ff00000))) << 32) | 
            (Double.doubleToLongBits(x) & 0xffffffffL);
        x = Double.longBitsToDouble(bits);
        //HI(x) = hx | (i ^ 0x3ff00000);	/* normalize x or x/2 */
        //System.out.println("x= " + x);
        k += (i >> 20);
        f = x - 1.0;
        if ((0x000fffff & (2 + hx)) < 3) {	/* |f| < 2**-20 */
            if(f==zero) {
                if(k==0) {
                    return zero;
                } else {
                    dk=(double)k;
                    return dk*ln2_hi+dk*ln2_lo;
                }
            }
            R = f * f * (0.5 - 0.33333333333333333 * f);
            if (k==0) { 
                return f-R;
            } else {
                dk=(double)k;
                return dk*ln2_hi-((R-dk*ln2_lo)-f);
            }
        }
        s = f/(2.0+f); 
        dk = (double)k;
        z = s*s;
        i = hx-0x6147a;
        w = z*z;
        j = 0x6b851-hx;
        t1= w * (Lg2+w*(Lg4+w*Lg6)); 
        t2= z * (Lg1+w*(Lg3+w*(Lg5+w*Lg7))); 
        i |= j;
        R = t2 + t1;
        if (i > 0) {
            hfsq=0.5*f*f;
            if (k==0) { 
                return f-(hfsq-s*(hfsq+R)); 
            } else {
                return dk*ln2_hi-((hfsq-(s*(hfsq+R)+dk*ln2_lo))-f);
            }
        } else {
            if (k==0) {
                return f-s*(f-R); 
            } else {
                return dk*ln2_hi-((s*(f-R)-dk*ln2_lo)-f);
            }
        }
    }

    static final double sinh(double x) {
        if (x < 0) { return -sinh(-x); }
        double ex = exp(x);
        return (ex - one/ex) * half;
    }

    static final double cosh(double x) {
        x = Math.abs(x);
        double ex = exp(x);
        return (ex + one/ex) * half;
    }

    static final double tanh(double x) {
        return (x < zero) ? -tanh(-x) : (one - two/(exp(x+x) + 1));
    }

    static final double asinh(double x) {
        return (x < 0) ? -asinh(-x) : log(two * x + one / (Math.sqrt(x*x + one) + x));
    }
    
    static final double acosh(double x) {
        return log(two * x - one / (Math.sqrt(x*x - one) + x));
    }

    static final double atanh(double x) {
        return (x < 0) ? -atanh(-x) : 0.5 * log(one + (x + x) / (one - x));
    }

    private static final double oneThird = 1./3.;
    static final double cbrt(double x) {
        return (x < 0) ? -cbrt(-x) : exp(oneThird * log(x)); 
    }

    static final double floor(double x) {
        int i0,i1,j0;
        int i, j;
        i0 = HI(x);
        i1 = LO(x);
        j0 = ((i0>>20)&0x7ff)-0x3ff;
        if(j0<20) {
            if(j0<0) { 	/* raise inexact if x != 0 */
                if(huge+x>0.0) {/* return 0*sign(x) if |x|<1 */
                    if(i0>=0) {i0=i1=0;} 
                    else if(((i0&0x7fffffff)|i1)!=0)
                        { i0=0xbff00000;i1=0;}
                }
            } else {
                i = (0x000fffff)>>j0;
                if(((i0&i)|i1)==0) return x; /* x is integral */
                if(huge+x>0.0) {	/* raise inexact flag */
                    if(i0<0) i0 += (0x00100000)>>j0;
                    i0 &= (~i); i1=0;
                }
            }
        } else if (j0>51) {
            if(j0==0x400) return x+x;	/* inf or NaN */
            else return x;		/* x is integral */
        } else {
            i = 0xffffffff >>> (j0-20);
            if((i1&i)==0) return x;	/* x is integral */
            if(huge+x>0.0) { 		/* raise inexact flag */
                if(i0<0) {
                    if(j0==20) i0+=1; 
                    else {
                        j = i1+(1<<(52-j0));
                        if(j<i1) i0 +=1 ; 	/* got a carry */
                        i1=j;
                    }
                }
                i1 &= (~i);
            }
        }
        //HI(x) = i0;
        //LO(x) = i1;
        return Double.longBitsToDouble((((long)i0) << 32) | (((long)(i1)) & 0xffffffffL));
    }

    static final double mod(double x, double y) {
        //return Math.IEEEremainder(x, y);
        y = Math.abs(y);
        return (x < 0) ? -mod(-x, y) : (x - floor(x / y) * y);
    }

    static final double pow(double x, double y) {
        if (y == zero) { return one; }
        if (y == one)  { return x; }
        if (x == zero) { return zero; }
        if (x < 0) {
            if (floor(y) == y) {
                return (Math.abs(mod(y, two)) < .1) ? pow(-x, y) : -pow(-x, y);
            } else {
                return Double.NaN;
            }
        }
        return exp(y * log(x));
    }

    static final double log10(double x) {
        return log(x) * LOG10E;
    }

    static final double log2(double x) {
        return log(x) * LOG2E;
    }
}
