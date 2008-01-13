/*
 * Copyright (C) 2006-2008 Mihai Preda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * Portions derived from FDLIBM 5.3
 * http://www.netlib.org/fdlibm/
 * FDLIBM comes with this copyright notice:
 *
 * ====================================================
 * Copyright (C) 2004 by Sun Microsystems, Inc. All rights reserved.
 *
 * Permission to use, copy, modify, and distribute this
 * software is freely granted, provided that this notice 
 * is preserved.
 * ====================================================
 */

package org.javia.eval;

public class BaseMath {
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

    private static final double
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
    
    public static final double atan(double x) {
        double w,s1,s2,z;
        int ix,hx,id;

        hx = HI(x);
        ix = hx & 0x7fffffff;
        if(ix >= 0x44100000) {  /* if |x| >= 2^66 */
            if(ix>0x7ff00000||
               (ix==0x7ff00000&&(LO(x)!=0)))
                return x+x;     /* NaN */
            if(hx>0) return  atanhi[3]+atanlo[3];
            else     return -atanhi[3]-atanlo[3];
        } if (ix < 0x3fdc0000) {    /* |x| < 0.4375 */
            if (ix < 0x3e200000) {  /* |x| < 2^-29 */
                return x;
            }
            id = -1;
        } else {
            x = Math.abs(x);
            if (ix < 0x3ff30000) {      /* |x| < 1.1875 */
                if (ix < 0x3fe60000) {  /* 7/16 <=|x|<11/16 */
                    id = 0; 
                    x = (2.0*x-1.)/(2.0+x); 
                } else {            /* 11/16<=|x|< 19/16 */
                    id = 1; 
                    x  = (x-1.)/(x+1.); 
                }
            } else {
                if (ix < 0x40038000) {  /* |x| < 2.4375 */
                    id = 2; x  = (x-1.5)/(1.+1.5*x);
                } else {            /* 2.4375 <= |x| < 2^66 */
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

    public static final double asin(double x) {
        //double t,w,p,q,c,r,s;
        int hx = HI(x);
        int ix = hx&0x7fffffff;
        if (ix >= 0x3ff00000) {     /* |x|>= 1 */
            if (((ix - 0x3ff00000) | LO(x)) == 0) {
                /* asin(1)=+-pi/2 with inexact */
                return x*pio2_hi+x*pio2_lo; 
            }
            return (x-x)/(x-x);     /* asin(|x|>1) is NaN */   
        } else if (ix < 0x3fe00000) {   /* |x|<0.5 */
            if (ix < 0x3e400000) {      /* if |x| < 2**-27 */
                return x;
            }
            double t = x*x;
            double p = t*(pS0+t*(pS1+t*(pS2+t*(pS3+t*(pS4+t*pS5)))));
            double q = 1.+t*(qS1+t*(qS2+t*(qS3+t*qS4)));
            double w = p/q;
            return x + x*w;
        }
        /* 1> |x|>= 0.5 */
        double w = 1. - Math.abs(x);
        double t = w * 0.5;
        double p = t*(pS0+t*(pS1+t*(pS2+t*(pS3+t*(pS4+t*pS5)))));
        double q = 1.+t*(qS1+t*(qS2+t*(qS3+t*qS4)));
        double s = Math.sqrt(t);
        if (ix >= 0x3FEF3333) {     /* if |x| > 0.975 */
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

    public static final double acos(double x) {
        return (x < 0) ? PI_2 + asin(-x) : PI_2 - asin(x);
    }

    public static final double exp(double x) {
        int shx = HI(x);
        int xsb = shx >>> 31;
        int hx = shx & 0x7fffffff;
        
        if (hx >= 0x40862E42) {         /* if |x|>=709.78... */
            if(hx >= 0x7ff00000) {
                if(((hx & 0xfffff) | LO(x)) != 0) { 
                    return Double.NaN;
                } else {
                    return shx < 0 ? 0. : Double.POSITIVE_INFINITY; 
                        //(xsb == 0) ? x : 0.0; /* exp(+-inf)={inf,0} */
                }
            }
            if (x > o_threshold) { return Double.POSITIVE_INFINITY; }
            if (x < u_threshold) { return 0.; }
        }

        double hi = 0, lo = 0, c, t, y;
        int k = 0;
        if (hx > 0x3fd62e42) {      /* if  |x| > 0.5 ln2 */ 
            if(hx < 0x3FF0A2B2) {   /* and |x| < 1.5 ln2 */
                hi = x - ln2HI[xsb]; 
                lo = ln2LO[xsb]; 
                k = 1 - xsb - xsb;
            } else {
                k  = (int)(LOG2E * x + halF[xsb]);
                t  = k;
                hi = x - t * ln2HI[0];  /* t*ln2HI is exact here */
                lo = t*ln2LO[0];
            }
            x  = hi - lo;
        } else if (hx < 0x3e300000)  {  /* when |x|<2**-28 */
            return 1. + x;
        } else {
            k = 0;
        }

        /* x is now in primary range */
        t  = x * x;
        c  = x - t * (P1 + t * (P2 + t * (P3 +t * (P4 + t * P5))));
        if (k==0) {
            return 1.-((x*c)/(c-2.0)-x); 
        } else { 
            y = 1.-((lo-(x*c)/(2.0-c))-hi);
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

    public static final double log(double x) {
        double hfsq, f, s, z, R, w, t1, t2, dk;
        int k, i, j;

        int hx = HI(x);
        int lx = LO(x);

        k=0;
        if (hx < 0x00100000) {          /* x < 2**-1022  */
            if (((hx&0x7fffffff) | lx) == 0) {
                return Double.NEGATIVE_INFINITY;
            }
            if (hx < 0) {
                return Double.NaN;
            }
            k -= 54; 
            x *= two54; /* subnormal number, scale up x */
            hx = HI(x);     /* high word of x */
        } 
        if (hx >= 0x7ff00000) {
            return x + x;
        }
        k  += (hx >> 20) - 1023;

        //Log.log("k= " + k);

        hx &= 0x000fffff;
        i = (hx + 0x95f64) & 0x100000;
        //Log.log("hx= " + hx + "; i= " + i);
        long bits = (((long)(hx | (i ^ 0x3ff00000))) << 32) | 
            (Double.doubleToLongBits(x) & 0xffffffffL);
        x = Double.longBitsToDouble(bits);
        //HI(x) = hx | (i ^ 0x3ff00000);    /* normalize x or x/2 */
        //Log.log("x= " + x);
        k += (i >> 20);
        f = x - 1.0;
        if ((0x000fffff & (2 + hx)) < 3) {  /* |f| < 2**-20 */
            if(f==0.) {
                if(k==0) {
                    return 0.;
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

    public static final double sinh(double x) {
        if (x < 0) { return -sinh(-x); }
        double ex = exp(x);
        return (ex - 1./ex) * half;
    }

    public static final double cosh(double x) {
        x = Math.abs(x);
        double ex = exp(x);
        return (ex + 1./ex) * half;
    }

    public static final double tanh(double x) {
        return (x < 0) ? -tanh(-x) : (1 - 2/(exp(x+x) + 1));
    }

    public static final double cbrt(double x) {
        return (x < 0) ? -pow(-x, 1/3.) : pow(x, 1/3.);
    }
         
    public static final double pow(double x, double y) {
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
        if ((iy|ly) == 0) { return 1.; }
        
        /* +-NaN return x+y */
        if (ix > 0x7ff00000 || ((ix==0x7ff00000)&&(lx!=0)) ||
            iy > 0x7ff00000 || ((iy==0x7ff00000)&&(ly!=0))) {
            return x+y; 
        }

        /* determine if y is an odd int when x < 0
         * yisint = 0   ... y is not an integer
         * yisint = 1   ... y is an odd int
         * yisint = 2   ... y is an even int
         */
        yisint  = 0;
        if (hx<0) { 
            if (iy>=0x43400000) yisint = 2; /* even integer y */
            else if (iy>=0x3ff00000) {
                k = (iy>>20)-0x3ff;    /* exponent */
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
            if (iy==0x7ff00000) {   /* y is +-inf */
                if(((ix-0x3ff00000)|lx)==0)
                    return  y - y;  /* inf**+-1 is NaN */
                else if (ix >= 0x3ff00000)/* (|x|>1)**+-inf = inf,0 */
                    return (hy>=0)? y: 0.;
                else            /* (|x|<1)**-,+inf = inf,0 */
                    return (hy<0)?-y: 0.;
            } 
            if(iy==0x3ff00000) {    /* y is  +-1 */
                if(hy<0) return 1./x; else return x;
            }
            if(hy==0x40000000) return x*x; /* y is  2 */
            if(hy==0x3fe00000) {    /* y is  0.5 */
                if(hx>=0)   /* x >= +0 */
                    return Math.sqrt(x);    
            }
        }
        
        ax = Math.abs(x);
        /* special value of x */
        if(lx==0) {
            if(ix==0x7ff00000||ix==0||ix==0x3ff00000){
                z = ax;         /*x is +-0,+-inf,+-1*/
                if(hy<0) z = 1./z;  /* z = (1/|x|) */
                if(hx<0) {
                    if(((ix-0x3ff00000)|yisint)==0) {
                        z = (z-z)/(z-z); /* (-1)**non-int is NaN */
                    } else if(yisint==1) 
                        z = -z;     /* (x<0)**odd = -(|x|**odd) */
                }
                return z;
            }
        }
        
        n = (hx>>31)+1;
        
        /* (x<0)**(non-int) is NaN */
        if((n|yisint)==0) return (x-x)/(x-x);
        
        s = 1.; /* s (sign of result -ve**odd) = -1 else = 1 */
        if((n|(yisint-1))==0) s = -1.;/* (-ve)**(odd int) */
        
        /* |y| is huge */
        if(iy>0x41e00000) { /* if |y| > 2**31 */
            if(iy>0x43f00000){  /* if |y| > 2**64, must o/uflow */
                if(ix<=0x3fefffff) return (hy<0)? huge*huge:tiny*tiny;
                if(ix>=0x3ff00000) return (hy>0)? huge*huge:tiny*tiny;
            }
            /* over/underflow if x is not close to one */
            if(ix<0x3fefffff) return (hy<0)? s*huge*huge:s*tiny*tiny;
            if(ix>0x3ff00000) return (hy>0)? s*huge*huge:s*tiny*tiny;
            /* now |1-x| is tiny <= 2**-20, suffice to compute 
               log(x) by x-x^2/2+x^3/3-x^4/4 */
            t = ax-1.;      /* t has 20 trailing zeros */
            w = (t*t)*(0.5-t*(0.3333333333333333333333-t*0.25));
            u = ivln2_h*t;  /* ivln2_h has 21 sig. bits */
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
            ix = j|0x3ff00000;      /* normalize ix */
            if(j<=0x3988E) k=0;     /* |x|<sqrt(3/2) */
            else if (j<0xBB67A) k=1;    /* |x|<sqrt(3)   */
            else {
                k=0;n+=1;
                ix -= 0x00100000;
            }
            ax = setHI(ax, ix);
            
            /* compute ss = s_h+s_l = (x-1)/(x+1) or (x-1.5)/(x+1.5) */
            u = ax-bp[k];       /* bp[0]=1.0, bp[1]=1.5 */
            v = 1./(ax+bp[k]);
            ss = u*v;
            s_h = ss;
            s_h = setLO(s_h, 0);
            /* t_h=ax+bp[k] High */
            t_h = 0.;
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
            z_h = cp_h*p_h;     /* cp_h+cp_l = 2/(3*log2) */
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
        if (j>=0x40900000) {                /* z >= 1024 */
            if(((j-0x40900000)|i)!=0)           /* if z > 1024 */
                return s*huge*huge;         /* overflow */
            else {
                if(p_l+ovt>z-p_h) return s*huge*huge;   /* overflow */
            }
        } else if((j&0x7fffffff)>=0x4090cc00 ) {    /* z <= -1075 */
            if(((j-0xc090cc00)|i)!=0)       /* z < -1075 */
                return s*tiny*tiny;     /* underflow */
            else {
                if(p_l<=z-p_h) return s*tiny*tiny;  /* underflow */
            }
        }
        /*
         * compute 2**(p_h+p_l)
         */
        i = j&0x7fffffff;
        k = (i>>20)-0x3ff;
        n = 0;
        if(i>0x3fe00000) {      /* if |z| > 0.5, set n = [z+0.5] */
            n = j+(0x00100000>>(k+1));
            k = ((n&0x7fffffff)>>20)-0x3ff; /* new k for n */
            t = 0.;
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
        r  = (z*t1)/(t1-2)-(w+z*w);
        z  = 1.-(r-z);
        j  = HI(z);
        j += (n<<20);
        if ((j>>20) <= 0) {
            z = scalbn(z,n);    /* subnormal output */
        } else {
            z = setHI(z, HI(z) + (n<<20));
        }
        return s*z;
    }
    
    /*
    static final double pow(double x, double y) {
        if (y == zero) { return 1.; }
        if (y == 1.)  { return x; }
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

    private static final double copysign(double x, double y) {
        return Double.longBitsToDouble((Double.doubleToLongBits(x) & 0x7fffffffffffffffL) |
                                       (Double.doubleToLongBits(y) & 0x8000000000000000L));
    }

    private static final double scalbn(double x, int n) {
        int hx = HI(x);
        int lx = LO(x);
        int k = (hx & 0x7ff00000) >> 20; /* extract exponent */
        if (k==0) {             /* 0 or subnormal x */
            if ((lx|(hx&0x7fffffff))==0) return x; /* +-0 */
            x *= two54; 
            hx = HI(x);
            k = ((hx&0x7ff00000)>>20) - 54; 
            if (n< -50000) return tiny*x;   /*underflow*/
        }
        if (k==0x7ff) return x+x;       /* NaN or Inf */
        k = k+n; 
        if (k >  0x7fe) return huge * copysign(huge, x); /* overflow  */
        if (k > 0) {
            setHI(x, (hx&0x800fffff)|(k<<20)); 
            return x;
        }
        if (k <= -54)
            if (n > 50000)  /* in case integer overflow in n+k */
                return huge*copysign(huge,x);   /*overflow*/
            else return tiny*copysign(tiny,x);  /*underflow*/
        k += 54;                /* subnormal result */
        setHI(x, (hx&0x800fffff)|(k<<20));
        return x * twom54;
    }

    public static final double log10(double x) {
        return log(x) * LOG10E;
    }
}
