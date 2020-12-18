/*
 * RomanNumeral.java        1.0.0 12/17/2020
 * 
 * No copyright
 */

package com.github.tjuve.romannumeral;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntBinaryOperator;

/**
 * A Roman numeral in 
 * <a href="https://en.wikipedia.org/wiki/Roman_numerals#Standard_form">
 * Standard form</a>, such as {@code MMMCMXCIX}.
 * <p>
 * {@code RomanNumeral} is an immutable number object that represents a Roman
 * numeral in Standard form. An object of type {@code RomanNumeral} contains
 * two fields, a field whose type is {@code String} and a field whose type is 
 * {@code int}. These fields represent a Roman numeral's symbols and value,
 * respectively, such as the RomanNumeral representing {@code XIV} would contain
 * {@code "XIV"} and {@code 14}.
 * <p>
 * In addition, this class provides methods for converting an {@code int} to a 
 * {@code String} of Roman numeral symbols and a {@code String} of Roman
 * numeral symbols to an {@code int}, as well as other constants and methods
 * useful when dealing with an {@code RomanNumeral}.
 * <p>
 * This is a <a href="https://docs.oracle.com/javase/8/docs/api/java/lang/
doc-files/ValueBased.html">value-based</a> class; use of identity-sensitive
 * operations (including reference equality ({@code ==}), identity hash code,
 * or synchronization) on instances of {@code RomanNumeral} may have
 * unpredictable results and should be avoided.
 * 
 * @since   12-17-2020
 * @version 1.0.0 
 * @author  Taylor Juve
 */ 
public final class RomanNumeral implements Serializable,
                                           Comparable<RomanNumeral> {
    /*
     * Built to run on Java SE Runtime Environment 8u271
     */
    
    /**
     * Roman numerals in Standard form are represented by combinations of these 
     * letters. Each symbol has a fixed {@code int} value:
     * <p>
     * <table>
     *   <tbody>
     *     <tr>
     *       <th>Symbol</th>
     *       <td>I</td>
     *       <td>V</td>
     *       <td>X</td>
     *       <td>L</td>
     *       <td>C</td>
     *       <td>D</td>
     *       <td>M</td>
     *     </tr>
     *     <tr>
     *       <th>Value</th>
     *       <td>1</td>
     *       <td>5</td>
     *       <td>10</td>
     *       <td>50</td>
     *       <td>100</td>
     *       <td>500</td>
     *       <td>1000</td>
     *     </tr>
     *   </tbody>
     * </table>
     */
    public enum Symbol  {
        I(1),
        V(5),
        X(10),
        L(50),
        C(100),
        D(500),
        M(1_000);

        /**
         * The value of this enum constant
         */
        public final int value;
        
        /**
         * The maximum number of consecutive occurrences of this Symbol in
         * Standard form
         */
        private final int maxNumConsecutive; 
        
        /**
         * Constructs an enum constant to represent the given value.
         *
         * @param   value   the value to be represented by the enum constant.
         */
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
        
        /**
         * Returns the enum constant with the specified name. The name must 
         * match exactly an identifier used to declare an enum constant of
         * type {@code Symbol}.
         * 
         * @param   name   
         * @return  the enum constant with the specified name if declared;
         *          {@code null} otherwise.
         * @implNote Much faster than implicitly defined valueOf(String)
         */
        public static Symbol valueOf(char name) {
            /*
             * Could use if/else if/else instead
             * Will need to update manually after adding/removing a Symbol
             * Faster than automatically updating (using values())
             */
            switch(name) {
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
    
    /**
     * Cache to store each unique {@code RomanNumeral}.
     * <p>
     * The cache is initialized on first usage.
     */
    private static class NumeralCache {
        /**
         * Stores each unique {@code RomanNumeral}.
         * <p>
         * The index of a {@code RomanNumeral} is equal to it's {@code value}
         * field (eg. {@code VI} goes at index {@code 6}.)
         * 
         * @implNote Must manually ensure RomanNumerals are properly indexed
         */
        private static final RomanNumeral[] cache
                = new RomanNumeral[NUM_UNIQUE_NUMERALS + MIN_VALUE];
    }
    
    /**
     * Cache to store each unique {@code RomanNumeral}'s {@code value} field.
     * <p>
     * The cache is initialized on first usage.
     */
    private static class ValueCache {
        /**
         * Maps from each unique {@code RomanNumeral}'s {@code symbols} field
         * to it's {@code value} field (eg. {@code "VI"} maps to {@code 6}.)
         * 
         * @implNote Must manually ensure to associate each {@code value} with
         *           the specified correct {@code symbols}
         * @implNote {@link RomanNumeral#NUM_UNIQUE_NUMERALS} can be stored
         *           without needed to resize.
         */
        private static final Map<String, Integer> cache
                = new HashMap<String, Integer>(NUM_UNIQUE_NUMERALS / 3 * 4 + 1);
    }
    
    /**
     * A constant holding the maximum value a {@code RomanNumeral} can 
     * represent, 3999.
     */
    public static final int MAX_VALUE = Symbol.M.value 
                                        * (Symbol.M.maxNumConsecutive + 1)
                                        - Symbol.I.value;
    /**
     * A constant holding the minimum value a {@code RomanNumeral} can 
     * represent, 1.
     */
    public static final int MIN_VALUE = Symbol.I.value;
    
    /**
     * A constant holding the number of unique Roman numerals in Standard form,
     *  3999.
     */
    private static final int NUM_UNIQUE_NUMERALS = MAX_VALUE - MIN_VALUE + 1;
    /**
     * A constant holding the maximum length of a Roman numeral, 15 (from
     * "MMMDCCCLXXXVIII".length())
     */                                          
    private static final int MAX_SYMBOLS_LENGTH = 15;
    /**
     * A constant holding the maximum length of a Roman numeral, 15 (from
     * "MMMDCCCLXXXVIII".length())
     */ 
    /**
     * All possible symbols for representing the thousandths place of an
     * {@code int} (up to 3).
     * 
     * @implNote The element at index {@code i} is the representation of 
     * {@code i * 1000}; An empty {@code String} is at index 0 because there
     * is no representation for 0.
     */
    private static final String[] thousands = {"", "M", "MM", "MMM"};
    /**
     * All possible symbols for representing the hundredths place of an
     * {@code int}.
     * 
     * @implNote The element at index {@code i} is the representation of 
     * {@code i * 100}; An empty {@code String} is at index 0 because there
     * is no representation for 0.
     */
    private static final String[] hundreds =
            {"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"}; 
    /**
     * All possible symbols for representing the tens place of an {@code int}.
     * 
     * @implNote The element at index {@code i} is the representation of 
     * {@code i * 10}; An empty {@code String} is at index 0 because there
     * is no representation for 0.
     */
    private static final String[] tens = 
            {"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    /**
     * All possible symbols for representing the ones place of an
     * {@code int}.
     * 
     * @implNote The element at index {@code i} is the representation of 
     * {@code i}; An empty {@code String} is at index 0 because there
     * is no representation for 0.
     */
    private static final String[] ones = 
            {"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};
    /**
     * A constant holding a value used to indicate a {@code String} does not
     * represent a Roman Numeral in Standard form.
     */
    private static final int INVALID_SYMBOLS_INDICATOR = MIN_VALUE - 1;
    /**
     * Serialization version.
     * 
     * @implNote Generated by Eclipse IDE for Java Developers (4.17.0)
     */
    private static final long serialVersionUID = 1991808113664446373L;
    
    /**
     * Used to Efficiently build {@code String} objects
     */
    private static StringBuilder strBuilder;
    
    /**
     * The symbols of the {@code RomanNumeral}.
     */
    public final String symbols;
    /**
     * The value of the {@code RomanNumeral}.
     * 
     * @implNote Could be {@code short}.
     */
    public final int value;
    
    private RomanNumeral(int value) {
        this(toString(value), value);
    }
    
    private RomanNumeral(String symbols) {
        this(symbols, valueOf(symbols, true, false));
    }
    
    
    /*
     * Caution: valueOf(symbols) must equal value and
     * toString(value) must equal symbols for this to be valid!
     */
    private RomanNumeral(String symbols, int value) {
        this.symbols = symbols;
        this.value = value;
        
        if (NumeralCache.cache[value] == null) {
            ValueCache.cache.put(symbols, value);
            NumeralCache.cache[value] = this;
        }
    }
    
    public static RomanNumeral of(int value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException(forInput(value));
        }
        
        RomanNumeral numeral = NumeralCache.cache[value];
        if (numeral == null) {
            numeral = new RomanNumeral(value);
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
        Integer value = ValueCache.cache.get(symbols);
        
        RomanNumeral numeral;
        if (value == null) {
            numeral = new RomanNumeral(symbols); // throws NumberFormatException
        } else {
            numeral = NumeralCache.cache[value];
        }
        return numeral;
    }
    
    public static String toString(int value) {
        if (!isValid(value)) {
            throw new IllegalArgumentException(forInput(value));
        }
        
        RomanNumeral numeral = NumeralCache.cache[value];
        if (numeral != null) {
            return numeral.symbols;
        }
        
        if (strBuilder == null) {
            strBuilder = new StringBuilder(MAX_SYMBOLS_LENGTH);
        } else {
            strBuilder.delete(0, MAX_SYMBOLS_LENGTH);
        }
        
        strBuilder.append(thousands[value / 1000]);
        strBuilder.append(hundreds[value / 100 % 10]);
        strBuilder.append(tens[value / 10 % 10]);
        strBuilder.append(ones[value % 10]);
        
        return strBuilder.toString();
    }
    
    public static int valueOf(String symbols) {
        return valueOf(symbols, true, true);
    }
    
    public static boolean isValid(int value) {
        return MAX_VALUE >= value && MIN_VALUE <= value;
    }
    
    public static boolean isValid(String symbols) {
        return valueOf(symbols, false, true) != INVALID_SYMBOLS_INDICATOR;
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
    
    /*
     * s must be non-empty and exactly (from left-to-right):
     * 0-3 M's before 
     * CM, CD, or 0-1 D and 0-3 C's before
     * XC, XL, or 0-1 L and 0-3 X's before
     * IX, IV, or 0-1 V and 0-3 I's
     * eg. "MMCDLXXXIV"
     */
    private static int valueOf(String s, boolean throwing, boolean construct) {
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
        
        Integer value = ValueCache.cache.get(s);
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
        
        if (construct) {
            new RomanNumeral(s, totalValue);
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
}