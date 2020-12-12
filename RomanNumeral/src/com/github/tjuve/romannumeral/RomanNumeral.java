/*
 * RomanNumeral.java        1.0 12/11/2020 (TODO version info)
 * 
 * No copyright
 */

package com.github.tjuve.romannumeral;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntBinaryOperator;

/**
 * TODO Class description goes here.
 *
 * @version 1.0 11 Dec 2020  
 * @author Taylor Juve
 */
public final class RomanNumeral extends Number 
                                implements Comparable<RomanNumeral> {
    /*
     * TODO Class implementation comment can go here
     */
    public enum Symbol {
        I(1),
        V(5),
        X(10),
        L(50),
        C(100),
        D(500),
        M(1_000);

        public final int value;
        
        private final int maxNumConsecutive;
        
        Symbol(int value) {
            this.value = value;
            
            int n = value;
            while (n >= 10) {
                n /= 10;
            }
            if (n == 1) {
                // leftmost digit is 1
                maxNumConsecutive = 3;
            } else {
                maxNumConsecutive = 1;
            }
        }
        
        // Much faster than implicitly defined valueOf(String)
        public static Symbol valueOf(char c) {
            /*
             * Could use if/else if/else instead
             * Will need to update manually after adding/removing a Symbol
             * Faster than automatically updating (using values())
             */
            switch(c) {
                case 'I': return Symbol.I;
                case 'V': return Symbol.V;
                case 'X': return Symbol.X;
                case 'L': return Symbol.L;
                case 'C': return Symbol.C;
                case 'D': return Symbol.D;
                case 'M': return Symbol.M;
                default: return null;
            }
        }
    }
    
    // MMMCMXCIX = (non-standard) IMMMM = 3,999
    public static final int MAX_VALUE = Symbol.M.value 
                                        * (Symbol.M.maxNumConsecutive + 1)
                                        - Symbol.I.value;
    public static final int MIN_VALUE = Symbol.I.value; //  = 1
    
    private static final int NUM_UNIQUE_NUMERALS = MAX_VALUE - MIN_VALUE + 1;
                                              // = 3999
    // index of a RomanNumeral is equal to it's value
    private static final RomanNumeral[] NUMERAL_CACHE
            = new RomanNumeral[NUM_UNIQUE_NUMERALS + MIN_VALUE]; // 4000
    // Symbols map to their value
    private static final Map<String, Integer> VALUE_CACHE
                                        // this initalCapacity ensures no resize
            = new HashMap<String, Integer>(NUM_UNIQUE_NUMERALS / 3 * 4 + 1); 
    private static final String[] THOUSANDS = {"", "M", "MM", "MMM"};
    private static final String[] HUNDREDS = 
            {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"}; 
    private static final String[] TENS = 
            {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    private static final String[] ONES = 
            {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
    private static final int INVALID_SYMBOLS_INDICATOR = MIN_VALUE - 1;
    // generated by Eclipse
    private static final long serialVersionUID = 1991808113664446373L;
    
    private String symbols;
    private int value; // Could be short if needed
    
    private RomanNumeral(int value) {
        this(toString(value), value);
    }
    
    private RomanNumeral(String symbols) {
        this(symbols, valueOf(symbols));
    }
    
    // Caution! Could make valueOf(this.symbols) != this.value
    private RomanNumeral(String symbols, int value) {
        this.symbols = symbols;
        this.value = value; 
    }
    
    public static RomanNumeral of(int value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException(forInput(value));
        }
        RomanNumeral numeral = NUMERAL_CACHE[value];
        if (numeral == null) {
            numeral = constructAndCache(toString(value), value);
        }
        return numeral;
    }
    
    public static RomanNumeral parse(CharSequence symbols, int beginIndex,
                                     int endIndex) 
                                             throws NumberFormatException {
        if (symbols == null) {
            throw new NumberFormatException(forNullInput());
        }
        return parse((String) symbols.subSequence(beginIndex, endIndex));
    }
    
    public static RomanNumeral parse(String symbols) {
        Integer value = VALUE_CACHE.get(symbols);
        RomanNumeral numeral;
        if (value == null) {
            // throws NumberFormatException
            numeral = constructAndCache(symbols, valueOf(symbols, true));
        } else {
            numeral = NUMERAL_CACHE[value];
        }
        return numeral;
    }
    
    public static String toString(int value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException(forInput(value));
        }
        return THOUSANDS[value / 1000] + HUNDREDS[value / 100 % 10]
                + TENS[value / 10 % 10] + ONES[value % 10];
    }
    
    public static int valueOf(String symbols) {
        return valueOf(symbols, true);
    }
    
    public static boolean isValid(int value) {
        return MAX_VALUE >= value && MIN_VALUE <= value;
    }
    
    public static boolean isValid(String symbols) {
        return valueOf(symbols, false) != INVALID_SYMBOLS_INDICATOR;
    }
    
    public static RomanNumeral addExact(RomanNumeral x, RomanNumeral y) {
        return of(calculate((a, b) -> a + b, x.value, y.value));
    }
    
    public static RomanNumeral decrementExact​(RomanNumeral a) {
        return of(calculate((x, y) -> x - y, a.value, 1));
    }
    
    public static RomanNumeral divideExact(RomanNumeral x, RomanNumeral y) {
        return of(calculate((a, b) -> a / b, x.value, y.value));
    }
    
    public static RomanNumeral incrementExact​(RomanNumeral a) {
        return of(calculate((x, y) -> x + y, a.value, 1));
    }
    
    public static RomanNumeral modExact(RomanNumeral x, RomanNumeral y) {
        return of(calculate((a, b) -> a % b, x.value, y.value));
    }
    
    public static RomanNumeral multiplyExact​(RomanNumeral x, RomanNumeral y) {
        return of(calculate((a, b) -> a * b, x.value, y.value));
    }
    
    public static RomanNumeral powExact(RomanNumeral x, RomanNumeral y) {  
        return of(calculate((a, b) -> (int) Math.pow(a, b), x.value, y.value));
    }

    public static RomanNumeral subtractExact​(RomanNumeral x, RomanNumeral y) {
        return of(calculate((a, b) -> a - b, x.value, y.value));
    }
    
    public static RomanNumeral max(RomanNumeral a, RomanNumeral b) {
        return maxOrMin((x, y) -> Math.max(x, y), a, b);
    }
    
    public static RomanNumeral min(RomanNumeral a, RomanNumeral b) {
        return maxOrMin((x, y) -> Math.min(x, y), a, b);
    }
    
    private static RomanNumeral constructAndCache(String symbols, int value) {
        RomanNumeral numeral = new RomanNumeral(symbols, value);
        VALUE_CACHE.put(symbols, value);
        NUMERAL_CACHE[value] = numeral;
        return numeral;
    }
    
    /*
     * symbols must be non-empty and exactly (from left-to-right):
    * 0-3 M's before 
    * CM, CD, or 0-1 D and 0-3 C's before
    * XC, XL, or 0-1 L and 0-3 X's before
    * IX, IV, or 0-1 V and 0-3 I's
    * eg. "MMCDLXXXIV"
    */
    private static int valueOf(String s, boolean throwing) {
        if (s == null) {
            if (throwing) {
                throw new NumberFormatException(forNullInput());
            }
            return INVALID_SYMBOLS_INDICATOR;
        } 
        int length = s.length();
        if (length == 0 || length > 15) {
            if (throwing) {
                throw new NumberFormatException(forInput(s));
            }
            return INVALID_SYMBOLS_INDICATOR;
        }
        
        Integer value = VALUE_CACHE.get(s);
        if (value != null) {
            return value;
        }
        
        int totalValue = 0;
        int prevValue = RomanNumeral.MIN_VALUE - 1; // primed
        int numConsecutiveSame = 1;
        int minValue = RomanNumeral.MIN_VALUE - 1; // primed
        for (int i = length - 1; i >= 0; i--) {
            Symbol symbol = Symbol.valueOf(s.charAt(i));
            if (symbol == null) {
                // invalid chars like "i"
                if (throwing) {
                    throw new NumberFormatException(forInput(s));
                }
                return INVALID_SYMBOLS_INDICATOR;
            }
             
            int curValue = symbol.value;
            if (curValue == prevValue) {
                if (numConsecutiveSame == symbol.maxNumConsecutive) {
                    // invalid forms like "IIII" or "VV"
                    if (throwing) {
                        throw new NumberFormatException(forInput(s));
                    }
                    return INVALID_SYMBOLS_INDICATOR;
                }
                
                numConsecutiveSame++;
            } else {
                numConsecutiveSame = 1;
            }
            
            if (curValue >= prevValue && curValue >= minValue) {
                // valid forms of standard addition notation
                totalValue += curValue;
                minValue = prevValue;
            } else if (symbol.maxNumConsecutive == 3 && curValue > minValue
                       && curValue * 10 >= prevValue) {
                // valid forms of standard subtraction notation
                totalValue -= curValue;
                minValue = 10 * curValue;
            } else {
                /*
                 * invalid addition forms like "IIV" and "VIV", also
                 * invalid subtraction forms like "IXC", "VX", "IVI", "IXX",
                 * "IL", and "IC"
                 */
                if (throwing) {
                    throw new NumberFormatException(forInput(s));
                }
                return INVALID_SYMBOLS_INDICATOR;
            }
            
            prevValue = curValue;
        }
        
        return totalValue;
    }
    
    private static String forInput(int value) {
        return "For input int: " + value;
    }
    
    private static String forNullInput() {
        return "null";
    }
    
    private static String forInput(String symbols) {
        return "For input String: \"" + symbols + "\"";
    }
    
    private static int calculate(IntBinaryOperator op, int x, int y) {
        int result = op.applyAsInt(x, y);
        if (isValid(result)) {
            return result;
        } else {
            throw new ArithmeticException("RomanNumeral overflow");
        }
    }
    
    private static RomanNumeral maxOrMin(IntBinaryOperator op, RomanNumeral a,
                                         RomanNumeral b) {
        if (op.applyAsInt(a.value, b.value) == a.value) {
            return a;
        } else {
            return b;
        }
    }
    
    public int compareTo(RomanNumeral anotherRomanNumeral) {
        return value - anotherRomanNumeral.value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RomanNumeral) {
            // faster but more dangerous than checking String equality also
            return value == ((RomanNumeral) obj).value; 
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        /*
         * Faster than hashing both. Hashing only int field would cause
         * collisions with int (as Numbers)
         */
        return symbols.hashCode(); 
    }

    @Override
    public String toString() {
        return symbols;
    }
    
    @Override
    public byte byteValue() {
        return (byte) value; // narrowing primitive conversion
    }
    
    @Override
    public short shortValue() {
        // lossless conversion b/c MAX_VALUE (3999) < Short.MAX_VALUE
        return (short) value;
    }

    @Override
    public int intValue() {
        return value;
    }
    
    @Override
    public long longValue() {
        return value;
    }
    
    @Override
    public double doubleValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }
}   
