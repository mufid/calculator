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

    private static final double setHI(double x, int high) {
        return Double.longBitsToDouble((Double.doubleToLongBits(x) & 0x00000000ffffffffL) | (((long)low)<<32));
    }
    
    private static final double setLO(double x, int low) {
        return Double.longBitsToDouble((Double.doubleToLongBits(x) & 0xffffffff00000000L) | low);
    }

    /*
    //from MicroDouble 1.0
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
        PI_2   = 1.57079632679489661923,
        PI_4   = 0.78539816339744830962,
        LOG2E  =   1.4426950408889634074,
        //invln2 = 1.44269504088896338700,
        LOG10E = 0.43429448190325182765,
        LN2    = 0.69314718055994530942,
        LN10   = 2.30258509299404568402,
        SQRT2  = 1.41421356237309504880;

    private static final double
        zero = 0.0,
        one  = 1.0,
        two  = 2.0,
        half = .5,
        huge     = 1.0e300,
        
        //atan
        atanhi[] = {
            4.63647609000806093515e-01,
            7.85398163397448278999e-01,
            9.82793723247329054082e-01,
            1.57079632679489655800e+00,
        },
        atanlo[] = {
            2.26987774529616870924e-17,
            3.06161699786838301793e-17,
            1.39033110312309984516e-17,
            6.12323399573676603587e-17,
        },
        aT[] = {
            3.33333333333329318027e-01,
            -1.99999999998764832476e-01,
            1.42857142725034663711e-01,
            -1.11111104054623557880e-01,
            9.09088713343650656196e-02,
            -7.69187620504482999495e-02,
            6.66107313738753120669e-02,
            -5.83357013379057348645e-02,
            4.97687799461593236017e-02,
            -3.65315727442169155270e-02,
            1.62858201153657823623e-02,
        },

        //asin
        pio2_hi =  1.57079632679489655800e+00,
        pio2_lo =  6.12323399573676603587e-17,
        pio4_hi =  7.85398163397448278999e-01,
        /* coefficient for R(x^2) */
        pS0 =  1.66666666666666657415e-01,
        pS1 = -3.25565818622400915405e-01,
        pS2 =  2.01212532134862925881e-01,
        pS3 = -4.00555345006794114027e-02,
        pS4 =  7.91534994289814532176e-04,
        pS5 =  3.47933107596021167570e-05,
        qS1 = -2.40339491173441421878e+00,
        qS2 =  2.02094576023350569471e+00,
        qS3 = -6.88283971605453293030e-01,
        qS4 =  7.70381505559019352791e-02,

        //exp()
        twom1000= 9.33263618503218878990e-302,     /* 2**-1000=0x01700000,0*/
        o_threshold =  7.09782712893383973096e+02,
        u_threshold = -7.45133219101941108420e+02,

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
    
    static final double atan(double x) {
        double w,s1,s2,z;
        int ix,hx,id;

        hx = HI(x);
        ix = hx & 0x7fffffff;
        if(ix >= 0x44100000) {	/* if |x| >= 2^66 */
            if(ix>0x7ff00000||
               (ix==0x7ff00000&&(__LO(x)!=0)))
                return x+x;		/* NaN */
            if(hx>0) return  atanhi[3]+atanlo[3];
            else     return -atanhi[3]-atanlo[3];
        } if (ix < 0x3fdc0000) {	/* |x| < 0.4375 */
            if (ix < 0x3e200000) {	/* |x| < 2^-29 */
                if(huge+x>one) return x;	/* raise inexact */
            }
            id = -1;
        } else {
            x = fabs(x);
            if (ix < 0x3ff30000) {		/* |x| < 1.1875 */
                if (ix < 0x3fe60000) {	/* 7/16 <=|x|<11/16 */
                    id = 0; x = (2.0*x-one)/(2.0+x); 
                } else {			/* 11/16<=|x|< 19/16 */
                    id = 1; x  = (x-one)/(x+one); 
                }
            } else {
                if (ix < 0x40038000) {	/* |x| < 2.4375 */
                    id = 2; x  = (x-1.5)/(one+1.5*x);
                } else {			/* 2.4375 <= |x| < 2^66 */
                    id = 3; x  = -1.0/x;
                }
            }}
        /* end of argument reduction */
        z = x*x;
        w = z*z;
        /* break sum from i=0 to 10 aT[i]z**(i+1) into odd and even poly */
        s1 = z*(aT[0]+w*(aT[2]+w*(aT[4]+w*(aT[6]+w*(aT[8]+w*aT[10])))));
        s2 = w*(aT[1]+w*(aT[3]+w*(aT[5]+w*(aT[7]+w*aT[9]))));
        if (id<0) return x - x*(s1+s2);
        else {
            z = atanhi[id] - ((x*(s1+s2) - atanlo[id]) - x);
            return (hx<0)? -z:z;
        }
    }

    static final double asin(double x) {
        //double t,w,p,q,c,r,s;
        int hx = HI(x);
        int ix = hx&0x7fffffff;
        if (ix >= 0x3ff00000) {		/* |x|>= 1 */
            if (((ix - 0x3ff00000) | LO(x)) == 0) {
                /* asin(1)=+-pi/2 with inexact */
                return x*pio2_hi+x*pio2_lo;	
            }
            return (x-x)/(x-x);		/* asin(|x|>1) is NaN */   
        } else if (ix < 0x3fe00000) {	/* |x|<0.5 */
            if (ix < 0x3e400000) {		/* if |x| < 2**-27 */
                /*if (huge + x > one)*/ return x;/* return x with inexact if x!=0*/
            }
            double t = x*x;
            double p = t*(pS0+t*(pS1+t*(pS2+t*(pS3+t*(pS4+t*pS5)))));
            double q = one+t*(qS1+t*(qS2+t*(qS3+t*qS4)));
            double w = p/q;
            return x + x*w;
        }
        /* 1> |x|>= 0.5 */
        double w = one - Math.abs(x);
        double t = w * 0.5;
        double p = t*(pS0+t*(pS1+t*(pS2+t*(pS3+t*(pS4+t*pS5)))));
        double q = one+t*(qS1+t*(qS2+t*(qS3+t*qS4)));
        double s = sqrt(t);
        if (ix >= 0x3FEF3333) { 	/* if |x| > 0.975 */
            w = p/q;
            t = pio2_hi-(2.0*(s+s*w)-pio2_lo);
        } else {
            w = setLO(s, 0);
            double c  = (t-w*w)/(s+w);
            double r  = p/q;
            double p  = 2.0*s*r-(pio2_lo-2.0*c);
            double q  = pio4_hi-2.0*w;
            t  = pio4_hi-(p-q);
        }
        return (hx>0) ? t : -t;
    }

    static final double acos(double x) {
        return (x < 0) ? PI_2 + asin(-x) : PI_2 - asin(x);
    }

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
