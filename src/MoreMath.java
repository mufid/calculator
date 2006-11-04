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
        return Double.longBitsToDouble((Double.doubleToLongBits(x) & 0x00000000ffffffffL) | (((long)high)<<32));
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
        LOG2E  = 1.4426950408889634074,
        //iln2 = 1.44269504088896338700,
        LOG10E = 0.43429448190325182765,
        LN2    = 0.69314718055994530942,
        LN10   = 2.30258509299404568402,
        SQRT2  = 1.41421356237309504880,
        SQRT2PI = 2.50662827463100024157;

    private static final double
        zero = 0.0,
        one  = 1.0,
        two  = 2.0,
        half = .5,
        huge = 1.0e300,
        tiny = 1.0e-300,

        two53  = 9007199254740992.0,
        two54  = 1.80143985094819840000e+16,
        twom54 = 5.55111512312578270212e-17,

        //pow
        bp[]   = {1.0, 1.5,},
        dp_h[] = { 0.0, 5.84962487220764160156e-01,},
        dp_l[] = { 0.0, 1.35003920212974897128e-08,},

        L1 = 5.99999999999994648725e-01,
        L2 = 4.28571428578550184252e-01,
        L3 = 3.33333329818377432918e-01,
        L4 = 2.72728123808534006489e-01,
        L5 = 2.30660745775561754067e-01,
        L6 = 2.06975017800338417784e-01,

        lg2_h  =  6.93147182464599609375e-01,
        lg2_l  = -1.90465429995776804525e-09,
        ovt =  8.0085662595372944372e-0017,
        cp    =  9.61796693925975554329e-01, // 2/(3ln2)
        cp_h  =  9.61796700954437255859e-01,
        cp_l  = -7.02846165095275826516e-09,
        ivln2_h  =  1.44269502162933349609e+00,
        ivln2_l  =  1.92596299112661746887e-08,

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
               (ix==0x7ff00000&&(LO(x)!=0)))
                return x+x;		/* NaN */
            if(hx>0) return  atanhi[3]+atanlo[3];
            else     return -atanhi[3]-atanlo[3];
        } if (ix < 0x3fdc0000) {	/* |x| < 0.4375 */
            if (ix < 0x3e200000) {	/* |x| < 2^-29 */
                if(huge+x>one) return x;	/* raise inexact */
            }
            id = -1;
        } else {
            x = Math.abs(x);
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
        double s = Math.sqrt(t);
        if (ix >= 0x3FEF3333) { 	/* if |x| > 0.975 */
            w = p/q;
            t = pio2_hi-(2.0*(s+s*w)-pio2_lo);
        } else {
            w = setLO(s, 0);
            double c  = (t-w*w)/(s+w);
            double r  = p/q;
            p  = 2.0*s*r-(pio2_lo-2.0*c);
            q  = pio4_hi-2.0*w;
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

    static final double trunc(double x) {
        return x >= 0 ? Math.floor(x) : Math.ceil(x);
    }

    static final double gcd(double x, double y) {
        //double remainder = y;
        if (Double.isNaN(x) || Double.isNaN(y) ||
            Double.isInfinite(x) || Double.isInfinite(y)) {
            return Double.NaN;
        }
        x = Math.abs(x);
        y = Math.abs(y);
        double save;
        while (y > 1e-12) {
            save = y;
            y = x % y;
            x = save;
            //System.out.println(y);
        } 
        return x > 1e-10 ? x : 0;
    }
    
    // coefficients for gamma=7, kmax=8  Lanczos method
    static final double L[] = {
        0.99999999999980993227684700473478,
      //0.99999999999981800227684700473478, //mihai
        676.520368121885098567009190444019,
      //676.520368121884898567009190444019, //mihai        
        -1259.13921672240287047156078755283,
        771.3234287776530788486528258894,
        -176.61502916214059906584551354,
        12.507343278686904814458936853,
        -0.13857109526572011689554707,
        9.984369578019570859563e-6,
        1.50563273514931155834e-7
    };
    static final double SQRT2PI_E7 = 0.0022857491179850423937807; //sqrt(2*pi)/e**7

    static final double gammaFact(double x) {
        if (x <= -1) return Double.NaN;
        double a = L[0];
        for (int i = 1; i < 9; ++i) {
            a += L[i] / (x + i);
        }
        //double t = x + 7.5;
        //return SQRT2PI * pow(t, x+.5) * exp(-t) * a; 
        return (SQRT2PI_E7 * a) * Math.pow((x+7.5)/Math.E, x + .5);        
    }

    static final double FACT[] = {
        1.,
        40320.,
        20922789888000.,
        620448401733239439360000.,
        263130836933693530167218012160000000.,
        8.15915283247897734345611269600e47,
        1.24139155925360726708622890474e61,
        7.10998587804863451854045647464e74,
        1.268869321858841641034333893350e89,
        6.123445837688608686152407038530e103,
        7.156945704626380229481153372320e118,
        1.854826422573984391147968456460e134,
        9.916779348709496892095714015400e149,
        1.02990167451456276238485838648e166,
        1.974506857221074023536820372760e182,
        6.68950291344912705758811805409e198,
        3.85620482362580421735677065923e215,
        3.65904288195254865768972722052e232,
        5.55029383273930478955105466055e249,
        1.31133588568345254560672467123e267,
        4.71472363599206132240694321176e284,
        2.52607574497319838753801886917e302,
    };

    static final double factorial(double x) {
        if (x < 0) {
            return Double.NaN;
        }
        if (x > 170) {
            return Double.POSITIVE_INFINITY;
        }
        if (Math.floor(x) == x) {
            int n = (int)x;
            double extra = x;
            switch (n & 7) {
            case 7: extra *= --x;
            case 6: extra *= --x;
            case 5: extra *= --x;
            case 4: extra *= --x;
            case 3: extra *= --x;
            case 2: extra *= --x;
            case 1: return FACT[n >> 3] * extra;
            case 0: return FACT[n >> 3];
            }
        }
        return gammaFact(x);
    }

    /*
    static final double fact4(double x) {
        return SQRT2PI * Math.sqrt(x) * (1 + 1/(12*x) + 1/(288*x*x)) * Math.pow(x/Math.E, x);
    }
    */

    static final double
        a0  =  7.72156649015328655494e-02, /* 0x3FB3C467, 0xE37DB0C8 */
        a1  =  3.22467033424113591611e-01, /* 0x3FD4A34C, 0xC4A60FAD */
        a2  =  6.73523010531292681824e-02, /* 0x3FB13E00, 0x1A5562A7 */
        a3  =  2.05808084325167332806e-02, /* 0x3F951322, 0xAC92547B */
        a4  =  7.38555086081402883957e-03, /* 0x3F7E404F, 0xB68FEFE8 */
        a5  =  2.89051383673415629091e-03, /* 0x3F67ADD8, 0xCCB7926B */
        a6  =  1.19270763183362067845e-03, /* 0x3F538A94, 0x116F3F5D */
        a7  =  5.10069792153511336608e-04, /* 0x3F40B6C6, 0x89B99C00 */
        a8  =  2.20862790713908385557e-04, /* 0x3F2CF2EC, 0xED10E54D */
        a9  =  1.08011567247583939954e-04, /* 0x3F1C5088, 0x987DFB07 */
        a10 =  2.52144565451257326939e-05, /* 0x3EFA7074, 0x428CFA52 */
        a11 =  4.48640949618915160150e-05, /* 0x3F07858E, 0x90A45837 */
        tc  =  1.46163214496836224576e+00, /* 0x3FF762D8, 0x6356BE3F */
        tf  = -1.21486290535849611461e-01, /* 0xBFBF19B9, 0xBCC38A42 */
        /* tt = -(tail of tf) */
        tt  = -3.63867699703950536541e-18, /* 0xBC50C7CA, 0xA48A971F */
        t0  =  4.83836122723810047042e-01, /* 0x3FDEF72B, 0xC8EE38A2 */
        t1  = -1.47587722994593911752e-01, /* 0xBFC2E427, 0x8DC6C509 */
        t2  =  6.46249402391333854778e-02, /* 0x3FB08B42, 0x94D5419B */
        t3  = -3.27885410759859649565e-02, /* 0xBFA0C9A8, 0xDF35B713 */
        t4  =  1.79706750811820387126e-02, /* 0x3F9266E7, 0x970AF9EC */
        t5  = -1.03142241298341437450e-02, /* 0xBF851F9F, 0xBA91EC6A */
        t6  =  6.10053870246291332635e-03, /* 0x3F78FCE0, 0xE370E344 */
        t7  = -3.68452016781138256760e-03, /* 0xBF6E2EFF, 0xB3E914D7 */
        t8  =  2.25964780900612472250e-03, /* 0x3F6282D3, 0x2E15C915 */
        t9  = -1.40346469989232843813e-03, /* 0xBF56FE8E, 0xBF2D1AF1 */
        t10 =  8.81081882437654011382e-04, /* 0x3F4CDF0C, 0xEF61A8E9 */
        t11 = -5.38595305356740546715e-04, /* 0xBF41A610, 0x9C73E0EC */
        t12 =  3.15632070903625950361e-04, /* 0x3F34AF6D, 0x6C0EBBF7 */
        t13 = -3.12754168375120860518e-04, /* 0xBF347F24, 0xECC38C38 */
        t14 =  3.35529192635519073543e-04, /* 0x3F35FD3E, 0xE8C2D3F4 */
        u0  = -7.72156649015328655494e-02, /* 0xBFB3C467, 0xE37DB0C8 */
        u1  =  6.32827064025093366517e-01, /* 0x3FE4401E, 0x8B005DFF */
        u2  =  1.45492250137234768737e+00, /* 0x3FF7475C, 0xD119BD6F */
        u3  =  9.77717527963372745603e-01, /* 0x3FEF4976, 0x44EA8450 */
        u4  =  2.28963728064692451092e-01, /* 0x3FCD4EAE, 0xF6010924 */
        u5  =  1.33810918536787660377e-02, /* 0x3F8B678B, 0xBF2BAB09 */
        v1  =  2.45597793713041134822e+00, /* 0x4003A5D7, 0xC2BD619C */
        v2  =  2.12848976379893395361e+00, /* 0x40010725, 0xA42B18F5 */
        v3  =  7.69285150456672783825e-01, /* 0x3FE89DFB, 0xE45050AF */
        v4  =  1.04222645593369134254e-01, /* 0x3FBAAE55, 0xD6537C88 */
        v5  =  3.21709242282423911810e-03, /* 0x3F6A5ABB, 0x57D0CF61 */
        s0  = -7.72156649015328655494e-02, /* 0xBFB3C467, 0xE37DB0C8 */
        s1  =  2.14982415960608852501e-01, /* 0x3FCB848B, 0x36E20878 */
        s2  =  3.25778796408930981787e-01, /* 0x3FD4D98F, 0x4F139F59 */
        s3  =  1.46350472652464452805e-01, /* 0x3FC2BB9C, 0xBEE5F2F7 */
        s4  =  2.66422703033638609560e-02, /* 0x3F9B481C, 0x7E939961 */
        s5  =  1.84028451407337715652e-03, /* 0x3F5E26B6, 0x7368F239 */
        s6  =  3.19475326584100867617e-05, /* 0x3F00BFEC, 0xDD17E945 */
        r1  =  1.39200533467621045958e+00, /* 0x3FF645A7, 0x62C4AB74 */
        r2  =  7.21935547567138069525e-01, /* 0x3FE71A18, 0x93D3DCDC */
        r3  =  1.71933865632803078993e-01, /* 0x3FC601ED, 0xCCFBDF27 */
        r4  =  1.86459191715652901344e-02, /* 0x3F9317EA, 0x742ED475 */
        r5  =  7.77942496381893596434e-04, /* 0x3F497DDA, 0xCA41A95B */
        r6  =  7.32668430744625636189e-06, /* 0x3EDEBAF7, 0xA5B38140 */
        w0  =  4.18938533204672725052e-01, /* 0x3FDACFE3, 0x90C97D69 */
        w1  =  8.33333333333329678849e-02, /* 0x3FB55555, 0x5555553B */
        w2  = -2.77777777728775536470e-03, /* 0xBF66C16C, 0x16B02E5C */
        w3  =  7.93650558643019558500e-04, /* 0x3F4A019F, 0x98CF38B6 */
        w4  = -5.95187557450339963135e-04, /* 0xBF4380CB, 0x8C0FE741 */
        w5  =  8.36339918996282139126e-04, /* 0x3F4B67BA, 0x4CDAD5D1 */
        w6  = -1.63092934096575273989e-03; /* 0xBF5AB89D, 0x0B9E43E4 */
    
    static final double lgamma(double x) {
        double t,y,z,p,p1,p2,p3,q,r,w;
        int i;

        int hx = HI(x);
        int lx = LO(x);

        /* purge off +-inf, NaN, +-0, and negative arguments */
        int ix = hx&0x7fffffff;
        if (ix >= 0x7ff00000) return Double.POSITIVE_INFINITY;
        if ((ix|lx)==0 || hx < 0) return Double.NaN;
        if (ix<0x3b900000) {	/* |x|<2**-70, return -log(|x|) */
            return -log(x);
        }

        /* purge off 1 and 2 */
        if((((ix-0x3ff00000)|lx)==0)||(((ix-0x40000000)|lx)==0)) r = 0;
        /* for x < 2.0 */
        else if(ix<0x40000000) {
            if(ix<=0x3feccccc) { 	/* lgamma(x) = lgamma(x+1)-log(x) */
                r = -log(x);
                if(ix>=0x3FE76944) {y = one-x; i= 0;}
                else if(ix>=0x3FCDA661) {y= x-(tc-one); i=1;}
                else {y = x; i=2;}
            } else {
                r = zero;
                if(ix>=0x3FFBB4C3) {y=2.0-x;i=0;} /* [1.7316,2] */
                else if(ix>=0x3FF3B4C4) {y=x-tc;i=1;} /* [1.23,1.73] */
                else {y=x-one;i=2;}
            }
            
            switch(i) {
            case 0:
                z = y*y;
                p1 = a0+z*(a2+z*(a4+z*(a6+z*(a8+z*a10))));
                p2 = z*(a1+z*(a3+z*(a5+z*(a7+z*(a9+z*a11)))));
                p  = y*p1+p2;
                r  += (p-0.5*y); break;
            case 1:
                z = y*y;
                w = z*y;
                p1 = t0+w*(t3+w*(t6+w*(t9 +w*t12)));	/* parallel comp */
                p2 = t1+w*(t4+w*(t7+w*(t10+w*t13)));
                p3 = t2+w*(t5+w*(t8+w*(t11+w*t14)));
                p  = z*p1-(tt-w*(p2+y*p3));
                r += (tf + p); break;
            case 2:	
                p1 = y*(u0+y*(u1+y*(u2+y*(u3+y*(u4+y*u5)))));
                p2 = one+y*(v1+y*(v2+y*(v3+y*(v4+y*v5))));
                r += (-0.5*y + p1/p2);
            }
        }
        else if(ix<0x40200000) { 			/* x < 8.0 */
            i = (int)x;
            t = zero;
            y = x-(double)i;
            p = y*(s0+y*(s1+y*(s2+y*(s3+y*(s4+y*(s5+y*s6))))));
            q = one+y*(r1+y*(r2+y*(r3+y*(r4+y*(r5+y*r6)))));
            r = half*y+p/q;
            z = one;	/* lgamma(1+s) = log(s) + lgamma(s) */
            switch(i) {
            case 7: z *= (y+6.0);	/* FALLTHRU */
            case 6: z *= (y+5.0);	/* FALLTHRU */
            case 5: z *= (y+4.0);	/* FALLTHRU */
            case 4: z *= (y+3.0);	/* FALLTHRU */
            case 3: z *= (y+2.0);	/* FALLTHRU */
                r += log(z); break;
            }
            /* 8.0 <= x < 2**58 */
        } else if (ix < 0x43900000) {
            t = log(x);
            z = one/x;
            y = z*z;
            w = w0+z*(w1+y*(w2+y*(w3+y*(w4+y*(w5+y*w6)))));
            r = (x-half)*(t-one)+w;
        } else 
            /* 2**58 <= x <= inf */
            r =  x*(log(x)-one);
        return r;
    }
    

    static final double pow(double x, double y) {
        double z,ax,z_h,z_l,p_h,p_l;
        double y1,t1,t2,r,s,t,u,v,w;
        int i,j,k,yisint,n;
        //unsigned lx,ly;

        int hx = HI(x); 
        int lx = LO(x);
        int hy = HI(y); 
        int ly = LO(y);
        int ix = hx & 0x7fffffff;  
        int iy = hy & 0x7fffffff;

        /* y==zero: x**0 = 1 */
        if ((iy|ly) == 0) { return one; }
        
        /* +-NaN return x+y */
        if (ix > 0x7ff00000 || ((ix==0x7ff00000)&&(lx!=0)) ||
            iy > 0x7ff00000 || ((iy==0x7ff00000)&&(ly!=0))) {
            return x+y;	
        }

        /* determine if y is an odd int when x < 0
         * yisint = 0	... y is not an integer
         * yisint = 1	... y is an odd int
         * yisint = 2	... y is an even int
         */
        yisint  = 0;
        if (hx<0) {	
            if (iy>=0x43400000) yisint = 2; /* even integer y */
            else if (iy>=0x3ff00000) {
                k = (iy>>20)-0x3ff;	   /* exponent */
                if(k>20) {
                    j = ly>>(52-k);
                    if((j<<(52-k))==ly) yisint = 2-(j&1);
                } else if(ly==0) {
                    j = iy>>(20-k);
                    if((j<<(20-k))==iy) yisint = 2-(j&1);
                }
            }		
        } 
        
        /* special value of y */
        if(ly==0) { 	
            if (iy==0x7ff00000) {	/* y is +-inf */
                if(((ix-0x3ff00000)|lx)==0)
                    return  y - y;	/* inf**+-1 is NaN */
                else if (ix >= 0x3ff00000)/* (|x|>1)**+-inf = inf,0 */
                    return (hy>=0)? y: zero;
                else			/* (|x|<1)**-,+inf = inf,0 */
                    return (hy<0)?-y: zero;
            } 
            if(iy==0x3ff00000) {	/* y is  +-1 */
                if(hy<0) return one/x; else return x;
            }
            if(hy==0x40000000) return x*x; /* y is  2 */
            if(hy==0x3fe00000) {	/* y is  0.5 */
                if(hx>=0)	/* x >= +0 */
                    return Math.sqrt(x);	
            }
        }
        
        ax = Math.abs(x);
        /* special value of x */
        if(lx==0) {
            if(ix==0x7ff00000||ix==0||ix==0x3ff00000){
                z = ax;			/*x is +-0,+-inf,+-1*/
                if(hy<0) z = one/z;	/* z = (1/|x|) */
                if(hx<0) {
                    if(((ix-0x3ff00000)|yisint)==0) {
                        z = (z-z)/(z-z); /* (-1)**non-int is NaN */
                    } else if(yisint==1) 
                        z = -z;		/* (x<0)**odd = -(|x|**odd) */
                }
                return z;
            }
        }
        
        n = (hx>>31)+1;
        
        /* (x<0)**(non-int) is NaN */
        if((n|yisint)==0) return (x-x)/(x-x);
        
        s = one; /* s (sign of result -ve**odd) = -1 else = 1 */
        if((n|(yisint-1))==0) s = -one;/* (-ve)**(odd int) */
        
        /* |y| is huge */
        if(iy>0x41e00000) { /* if |y| > 2**31 */
            if(iy>0x43f00000){	/* if |y| > 2**64, must o/uflow */
                if(ix<=0x3fefffff) return (hy<0)? huge*huge:tiny*tiny;
                if(ix>=0x3ff00000) return (hy>0)? huge*huge:tiny*tiny;
            }
            /* over/underflow if x is not close to one */
            if(ix<0x3fefffff) return (hy<0)? s*huge*huge:s*tiny*tiny;
            if(ix>0x3ff00000) return (hy>0)? s*huge*huge:s*tiny*tiny;
            /* now |1-x| is tiny <= 2**-20, suffice to compute 
               log(x) by x-x^2/2+x^3/3-x^4/4 */
            t = ax-one;		/* t has 20 trailing zeros */
            w = (t*t)*(0.5-t*(0.3333333333333333333333-t*0.25));
            u = ivln2_h*t;	/* ivln2_h has 21 sig. bits */
            v = t*ivln2_l-w * LOG2E;
            t1 = u+v;
            t1 = setLO(t1, 0);
            t2 = v-(t1-u);
        } else {
            double ss,s2,s_h,s_l,t_h,t_l;
            n = 0;
            /* take care subnormal number */
            if (ix<0x00100000) {
                ax *= two53; 
                n -= 53; 
                ix = HI(ax); 
            }
            n  += ((ix)>>20)-0x3ff;
            j  = ix&0x000fffff;
            /* determine interval */
            ix = j|0x3ff00000;		/* normalize ix */
            if(j<=0x3988E) k=0;		/* |x|<sqrt(3/2) */
            else if (j<0xBB67A) k=1;	/* |x|<sqrt(3)   */
            else {
                k=0;n+=1;
                ix -= 0x00100000;
            }
            ax = setHI(ax, ix);
            
            /* compute ss = s_h+s_l = (x-1)/(x+1) or (x-1.5)/(x+1.5) */
            u = ax-bp[k];		/* bp[0]=1.0, bp[1]=1.5 */
            v = one/(ax+bp[k]);
            ss = u*v;
            s_h = ss;
            s_h = setLO(s_h, 0);
            /* t_h=ax+bp[k] High */
            t_h = zero;
            t_h = setHI(t_h, ((ix>>1)|0x20000000)+0x00080000+(k<<18)); 
            t_l = ax - (t_h-bp[k]);
            s_l = v*((u-s_h*t_h)-s_h*t_l);
            /* compute log(ax) */
            s2 = ss*ss;
            r = s2*s2*(L1+s2*(L2+s2*(L3+s2*(L4+s2*(L5+s2*L6)))));
            r += s_l*(s_h+ss);
            s2  = s_h*s_h;
            t_h = 3.0+s2+r;
            t_h = setLO(t_h, 0);
            t_l = r-((t_h-3.0)-s2);
            /* u+v = ss*(1+...) */
            u = s_h*t_h;
            v = s_l*t_h+t_l*ss;
            /* 2/(3log2)*(ss+...) */
            p_h = u+v;
            p_h = setLO(p_h, 0);
            p_l = v-(p_h-u);
            z_h = cp_h*p_h;		/* cp_h+cp_l = 2/(3*log2) */
            z_l = cp_l*p_h+p_l*cp+dp_l[k];
            /* log2(ax) = (ss+..)*2/(3*log2) = n + dp_h + z_h + z_l */
            t = (double)n;
            t1 = (((z_h+z_l)+dp_h[k])+t);
            t1 = setLO(t1, 0);
            t2 = z_l-(((t1-t)-dp_h[k])-z_h);
        }
        
        /* split up y into y1+y2 and compute (y1+y2)*(t1+t2) */
        y1 = setLO(y, 0);
        p_l = (y-y1)*t1+y*t2;
        p_h = y1*t1;
        z = p_l+p_h;
        j = HI(z);
        i = LO(z);
        if (j>=0x40900000) {				/* z >= 1024 */
            if(((j-0x40900000)|i)!=0)			/* if z > 1024 */
                return s*huge*huge;			/* overflow */
            else {
                if(p_l+ovt>z-p_h) return s*huge*huge;	/* overflow */
            }
        } else if((j&0x7fffffff)>=0x4090cc00 ) {	/* z <= -1075 */
            if(((j-0xc090cc00)|i)!=0) 		/* z < -1075 */
                return s*tiny*tiny;		/* underflow */
            else {
                if(p_l<=z-p_h) return s*tiny*tiny;	/* underflow */
            }
        }
        /*
         * compute 2**(p_h+p_l)
         */
        i = j&0x7fffffff;
        k = (i>>20)-0x3ff;
        n = 0;
        if(i>0x3fe00000) {		/* if |z| > 0.5, set n = [z+0.5] */
            n = j+(0x00100000>>(k+1));
            k = ((n&0x7fffffff)>>20)-0x3ff;	/* new k for n */
            t = zero;
            t = setHI(t, (n&~(0x000fffff>>k)));
            n = ((n&0x000fffff)|0x00100000)>>(20-k);
            if(j<0) n = -n;
            p_h -= t;
        } 
        t = p_l+p_h;
        t = setLO(t, 0);
        u = t*lg2_h;
        v = (p_l-(t-p_h)) * LN2 +t*lg2_l;
        z = u+v;
        w = v-(z-u);
        t  = z*z;
        t1  = z - t*(P1+t*(P2+t*(P3+t*(P4+t*P5))));
        r  = (z*t1)/(t1-two)-(w+z*w);
        z  = one-(r-z);
        j  = HI(z);
        j += (n<<20);
        if ((j>>20) <= 0) {
            z = scalbn(z,n);	/* subnormal output */
        } else {
            z = setHI(z, HI(z) + (n<<20));
        }
        return s*z;
    }
    
    /*
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
    */

    static final double copysign(double x, double y) {
        return Double.longBitsToDouble((Double.doubleToLongBits(x) & 0x7fffffffffffffffL) |
                                       (Double.doubleToLongBits(y) & 0x8000000000000000L));
    }

    static final double scalbn(double x, int n) {
        int hx = HI(x);
        int lx = LO(x);
        int k = (hx & 0x7ff00000) >> 20; /* extract exponent */
        if (k==0) {				/* 0 or subnormal x */
            if ((lx|(hx&0x7fffffff))==0) return x; /* +-0 */
            x *= two54; 
            hx = HI(x);
            k = ((hx&0x7ff00000)>>20) - 54; 
            if (n< -50000) return tiny*x; 	/*underflow*/
	    }
        if (k==0x7ff) return x+x;		/* NaN or Inf */
        k = k+n; 
        if (k >  0x7fe) return huge * copysign(huge, x); /* overflow  */
        if (k > 0) {
            setHI(x, (hx&0x800fffff)|(k<<20)); 
            return x;
        }
        if (k <= -54)
            if (n > 50000) 	/* in case integer overflow in n+k */
                return huge*copysign(huge,x);	/*overflow*/
            else return tiny*copysign(tiny,x); 	/*underflow*/
        k += 54;				/* subnormal result */
        setHI(x, (hx&0x800fffff)|(k<<20));
        return x * twom54;
    }

    static final double log10(double x) {
        return log(x) * LOG10E;
    }

    static final double log2(double x) {
        return log(x) * LOG2E;
    }

    static final boolean isPiMultiple(double x) {
        double npi = x / Math.PI;
        return Math.floor(npi) == npi;
    }

    /*
    static final double sin(double x) {
        if (isPiMultiple(x)) { return 0.0; }
        return Math.sin(x);
    }
    */

    
}
