/*
 * RomanNumeralTest.java        1.1 12/16/2020 (TODO version info)
 * 
 * No copyright
 */

package com.github.tjuve.romannumeral;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opentest4j.TestAbortedException;

import com.github.tjuve.romannumeral.RomanNumeral.Symbol;

/**
 * TODO Class description goes here.
 *
 * @version 1.1 16 Dec 2020  
 * @author Taylor Juve
 */
public class RomanNumeralTest {
    /*
     * TODO Class implementation comment can go here
     */
    private static class TestNumeral {
        private final String expectedSymbols;
        private final int expectedValue;
        private final RomanNumeral actualNumeral;
        
        private TestNumeral(String expectedSymbols, int expectedValue) {
            this.expectedSymbols = expectedSymbols;
            this.expectedValue = expectedValue;
            this.actualNumeral = createRomanNumeral(expectedSymbols,
                                                    expectedValue);
        }
        
        private static RomanNumeral createRomanNumeral(String symbols,
                int value) {
            RomanNumeral numeral;
            try {
                numeral = RomanNumeral.of(value); // faster than parse
            } catch (Exception ex) {
                numeral = null;
            }
            
            if (numeral == null) {
                try {
                    return RomanNumeral.parse(symbols);
                } catch (Exception ex) {
                    numeral = null;
                }
            }
            return numeral;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof TestNumeral) {
                TestNumeral another = (TestNumeral) obj;
                return expectedValue == another.expectedValue
                        && Objects.equals(expectedSymbols, 
                                          another.expectedSymbols)
                        && Objects.equals(actualNumeral, 
                                          another.actualNumeral);
            } else if (obj instanceof RomanNumeral) {
                RomanNumeral numeral = (RomanNumeral) obj;
                if (actualNumeral == null) {
                    return expectedValue == numeral.value
                            && Objects.equals(expectedSymbols, numeral.symbols);
                } else {
                    return Objects.equals(actualNumeral, numeral);
                }
            } else {
                return false;
            }
        }
        
        @Override
        public String toString() {
            if (actualNumeral == null) {
                return expectedSymbols;
            } else {
                return actualNumeral.toString();
            }
        }
    }
    
    /*
     * Pathname is relative to project root
     * Reference format: One numeral per line in increasing numerical order
     * (eg. I\nII\rIII\r\nIV), (see: BufferedReader.readLine())
     */
    public static final String REFERENCE_PATHNAME = "test/com/github/tjuve/"
            + "romannumeral/tests/references/ExpectedRomanNumerals1.txt";
    public static final int NUM_REPEATS_TO_CHECK_FOR_CONSISTENY = 5;

    private static final int MAX_NUM_CONSECUTIVE = 3;
    
    private static List<Arguments> expectedSymbolsValuePairs;
    private static List<String> invalidSymbols;
    private static List<TestNumeral> testNumerals;
    
    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    public class SymbolTests {
        @DisplayName(".value of each Symbol")
        @ParameterizedTest(name = "{1}.value")
        @MethodSource("valueTestArgsProvider")
        public void valueTest(int expectedValue, Symbol symbol) {
            assertEquals(expectedValue, symbol.value);
        }
        
        @DisplayName("Symbol.valueOf(char)")
        @ParameterizedTest(name = "Symbol.valueOf({1})")
        @MethodSource("valueOfTestArgsProvider")
        public void valueOfTest(Symbol expectedSymbol, char c) {
            assertEquals(expectedSymbol,
                         assertDoesNotThrow(() -> Symbol.valueOf(c)));
        }
        
        private Stream<Arguments> valueTestArgsProvider() {
            Builder<Arguments> builder = Stream.builder();
            initExpectedSymbolsValuePairs();
            
            for (Symbol symbol : Symbol.values()) {
                builder.accept(Arguments.of(
                        (int) expectedSymbolsValuePairs.get(symbol.value - 1)
                                                       .get()[1],
                        symbol));
            }
            
            return builder.build();
        }
    
        private Stream<Arguments> valueOfTestArgsProvider() {
            Builder<Arguments> builder = Stream.builder();
            
            for (Symbol symbol : Symbol.values()) {
                String name = symbol.name();
                
                if (name.length() == 1) {
                    builder.accept(Arguments.of(symbol, name.charAt(0)));
                }
            }
            
            return builder.build();
        }
    }
    
    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    public class FactoryTests {
        @Nested
        @DisplayName("of(int)")
        public class ofTests {
            @DisplayName("of(valid int)")
            @ParameterizedTest(name = "of({1})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#expectedSymbolsValuePairsProvider()")
            public void ofValidTest(String expectedSymbols, int expectedValue) {
                RomanNumeral numeral = assertDoesNotThrow(
                        () -> RomanNumeral.of(expectedValue));
                assertDataEquals(expectedSymbols, expectedValue, numeral);
            }
            
            @DisplayName("of(invalid int)")
            @ParameterizedTest(name = "of({0})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#invalidValueTestArgsProvider()")
            public void ofInvalidTest(int invalidValue) {
                assertThrows(IllegalArgumentException.class, 
                             () -> RomanNumeral.of(invalidValue));
            }
        }
        
        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("parse(CharSequence, int, int)")
        public class parseCharSequenceTests {
            /*
             * All non-empty substrings of valid symbols are valid symbols so
             * this tests only the whole CharSequence
             */
            @DisplayName("parse(valid CharSequence, int, int)")
            @ParameterizedTest(name = "parse(\"{0}\", 0, \"{0}\".length())")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#expectedSymbolsValuePairsProvider()")
            public void parseValidCharSequenceTest(String expectedSymbols,
                                                   int expectedValue) {
                RomanNumeral numeral = assertDoesNotThrow(
                        () -> RomanNumeral.parse(expectedSymbols, 0,
                                                 expectedSymbols.length()));
                assertDataEquals(expectedSymbols, expectedValue, numeral);
            }
            
            @DisplayName("parse(invalid CharSequence, int, int)")
            @ParameterizedTest(name = "parse(\"{0}\", 0, \"{0}\".length())")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#invalidSymbolsProvider()")
            public void parseInvalidCharSequenceTest(String invalidSymbols) {
                int endIndex;
                if (invalidSymbols == null) {
                    endIndex = 0;
                } else {
                    endIndex = invalidSymbols.length();
                }
                assertThrows(NumberFormatException.class,
                             () -> RomanNumeral.parse(invalidSymbols, 0, 
                                                      endIndex));
            }
            
            @DisplayName("parse(null, 0, 1)")
            @Test
            public void parseNullCharSequenceTest() {
                assertThrows(NumberFormatException.class,
                             () -> RomanNumeral.parse(null, 0, 1));
            }
        
            @DisplayName("parse(CharSequence, valid pair of ints)")
            @ParameterizedTest(name = "parse(\"{2}\", {3}, {4})")
            @MethodSource("parseCharSequenceValidBoundsTestArgsProvider")
            public void parseCharSequenceValidBoundsTest(String expectedSymbols,
                                                         int expectedValue,
                                                         String validSymbols,
                                                         int beginIndex,
                                                         int endIndex) {
                RomanNumeral numeral = assertDoesNotThrow(
                        () -> RomanNumeral.parse(validSymbols, beginIndex,
                                                 endIndex));
                assertDataEquals(expectedSymbols, expectedValue, numeral);
            }
            
            @DisplayName("parse(CharSequence, invalid pair of ints)")
            @ParameterizedTest(name = "parse(\"{0}\", {1}, {2})")
            @MethodSource("parseCharSequenceInvalidBoundsTestArgsProvider")
            public void parseCharSequenceInvalidBoundsTest(String symbols,
                                                           int beginIndex,
                                                           int endIndex) {
                assertThrows(IndexOutOfBoundsException.class,
                             () -> RomanNumeral.parse(symbols, beginIndex,
                                                      endIndex));
            }
            
            private Stream<Arguments> 
                    parseCharSequenceValidBoundsTestArgsProvider() {
                // Build longest valid String of symbols ("MMMDCCCLXXXVIII")
                Symbol[] symbols = Symbol.values();
                StringBuilder strBuilder = new StringBuilder(MAX_NUM_CONSECUTIVE
                                                             * symbols.length);
                int[] values = new int[strBuilder.capacity()];
                int valuesIndex = 0; 
                
                Arrays.sort(symbols, new Comparator<Symbol>() {
                    @Override
                    public int compare(Symbol s1, Symbol s2) {
                        return s1.value - s2.value;
                    }
                });
                
                for (int i = symbols.length - 1; i >= 0; i--) {
                    Symbol symbol = symbols[i];
                    
                    int numRepeats;
                    if (String.valueOf(symbol.value).startsWith("1")) {
                        numRepeats = MAX_NUM_CONSECUTIVE;
                    } else {
                        numRepeats = 1;
                    }
                    
                    String name = symbol.name();
                    for (int j = 0; j < numRepeats; j++) {
                        strBuilder.append(name);
                        values[valuesIndex] = symbol.value;
                        valuesIndex++;
                    }
                }
                String symbolsStr = strBuilder.toString();
                
                Builder<Arguments> argsBuilder = Stream.builder();
                initExpectedSymbolsValuePairs();
                
                int numConsecutive = 1;
                int prevValue = RomanNumeral.MIN_VALUE - 1;
                for (int beginIndex = symbolsStr.length() - 1; beginIndex >= 0;
                        beginIndex--) {
                    if (values[beginIndex] == prevValue) {
                        numConsecutive++;
                    } else {
                        numConsecutive = 1;
                    }
                    
                    int value = 0;
                    for (int endIndex = beginIndex + 1;
                            endIndex <= symbolsStr.length(); endIndex++) {
                        value += values[endIndex - 1];
                        
                        if (endIndex >= beginIndex + numConsecutive) {
                            Object[] symbolsValuePair
                                    = expectedSymbolsValuePairs
                                      .get(value - 1).get();
                            
                            argsBuilder.accept(
                                    Arguments.of((String) symbolsValuePair[0], 
                                                 (int) symbolsValuePair[1],
                                                 symbolsStr, beginIndex,
                                                 endIndex));
                        }
                    }
                    
                    prevValue = values[beginIndex];
                }
                
                return argsBuilder.build();
            }
            
            private Stream<Arguments> 
                    parseCharSequenceInvalidBoundsTestArgsProvider() {
                String symbol = Symbol.I.name();
                return Stream.of(Arguments.of(symbol, -1, 1), // negative
                                 Arguments.of(symbol, 0, -1), // negative
                                 // beyond end
                                 Arguments.of(symbol, 0, symbol.length() + 1),
                                 // end less than begin
                                 Arguments.of(symbol, 1, 0)); 
            }
        }
        
        @Nested
        @DisplayName("parse(String)")
        public class parseStringTests {
            @DisplayName("parse(valid String)")
            @ParameterizedTest(name = "parse(\"{0}\")")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#expectedSymbolsValuePairsProvider()")
            public void parseValidStringTest(String expectedSymbols,
                                             int expectedValue) {
                RomanNumeral numeral = assertDoesNotThrow(
                        () -> RomanNumeral.parse(expectedSymbols));
                assertDataEquals(expectedSymbols, expectedValue, numeral);
            }
            
            @DisplayName("parse(invalid String)")
            @ParameterizedTest(name = "parse(\"{0}\")")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#invalidSymbolsProvider()")
            public void parseInvalidStringTest(String invalidSymbols) {
                assertThrows(NumberFormatException.class, 
                             () -> RomanNumeral.parse(invalidSymbols));
            }
            
            @DisplayName("parse(null)")
            @Test
            public void parseNullStringTest() {
                assertThrows(NumberFormatException.class, 
                             () -> RomanNumeral.parse(null));
            }
        }
        
        private void assertDataEquals(String expectedSymbols,
                                      int expectedValue,
                                      RomanNumeral numeral) {
            assertFalse(numeral == null);
            assertEquals(expectedSymbols, numeral.symbols);
            assertEquals(expectedValue, numeral.value);
        }
    }
    
    @Nested
    public class ConversionMethodTests {
        @Nested
        @DisplayName("toString(int)")
        public class ToStringTests {
            @DisplayName("toString(valid int)")
            @ParameterizedTest(name = "toString({1})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#expectedSymbolsValuePairsProvider()")
            public void toStringValidTest(String expectedSymbols, int value) {
                assertEquals(expectedSymbols, assertDoesNotThrow(
                        () -> RomanNumeral.toString(value)));
            }
            
            @DisplayName("toString(invalid int)")
            @ParameterizedTest(name = "toString({0})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#invalidValueTestArgsProvider()")
            public void toStringInvalidTest(int invalidValue) {
                assertThrows(IllegalArgumentException.class, 
                             () -> RomanNumeral.toString(invalidValue));
            }
        }
        
        @Nested
        @DisplayName("valueOf(String)")
        public class ValueOfTests {
            @DisplayName("valueOf(valid String)")
            @ParameterizedTest(name = "valueOf(\"{0}\")")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#expectedSymbolsValuePairsProvider()")
            public void valueOfValidTest(String symbols, int expectedValue) {
                assertTrue(expectedValue == assertDoesNotThrow(
                        () -> RomanNumeral.valueOf(symbols)));
            }
            
            @DisplayName("valueOf(invalid String)")
            @ParameterizedTest(name = "valueOf(\"{0}\")")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#invalidSymbolsProvider()")
            public void valueOfInvalidTest(String invalidSymbols) {
                assertThrows(NumberFormatException.class,
                             () -> RomanNumeral.valueOf(invalidSymbols));
            }
            
            @DisplayName("valueOf(null)")
            @Test
            public void valueOfNullTest() {
                assertThrows(NumberFormatException.class,
                             () -> RomanNumeral.valueOf(null));
            }
        }
        
        @Nested
        @DisplayName("isValid(int)")
        public class isValidIntTests {
            @DisplayName("isValid(valid int)")
            @ParameterizedTest(name = "isValid({1})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#expectedSymbolsValuePairsProvider()")
            public void isValidValidIntTest(String symbols, int value) {
                assertTrue(assertDoesNotThrow(
                        () -> RomanNumeral.isValid(value)));
            }  
            
            @DisplayName("isValid(invalid int)")
            @ParameterizedTest(name = "isValid({0})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#invalidValueTestArgsProvider()")
            public void isValidInvalidIntTest(int invalidValue) {
                assertFalse(assertDoesNotThrow(
                        () -> RomanNumeral.isValid(invalidValue)));
            }  
        }
          
        @Nested
        @DisplayName("isValid(String)")
        public class isValidStringTests {
            @DisplayName("isValid(valid String)")
            @ParameterizedTest(name = "isValid(\"{0}\")")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#expectedSymbolsValuePairsProvider()")
            public void isValidValidStringTest(String validSymbols) {
                assertTrue(assertDoesNotThrow(
                        () -> RomanNumeral.isValid(validSymbols)));
            }
            
            @DisplayName("isValid(invalid String)")
            @ParameterizedTest(name = "isValid(\"{0}\")")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#invalidSymbolsProvider()")
            public void isValidInvalidStringTest(String invalidSymbols) {
                assertFalse(assertDoesNotThrow(
                        () -> RomanNumeral.isValid(invalidSymbols)));
            }
            
            @DisplayName("isValid(null)")
            @Test
            public void isValidNullStringTest() {
                assertFalse(assertDoesNotThrow(
                        () -> RomanNumeral.isValid(null)));
            }
        }
    }
    
    @Nested
    @TestInstance(Lifecycle.PER_CLASS)
    public class ArithmeticMethodTests {
        @Nested
        @DisplayName("addExact(RomanNumeral, RomanNumeral)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class addExactTests {
            private final IntBinaryOperator ADDITION = (x, y) -> x + y;
            
            @DisplayName(
                    "addExact(RomanNumeral, smallest addable RomanNumeral)")
            @ParameterizedTest(name = "addExact({1}, {2})")
            @MethodSource("addExactSmallestTestArgsProvider")
            public void addExactSmallestTest(TestNumeral expected,
                                             TestNumeral testNumeral,
                                             RomanNumeral smallest) {
                assertNumeralsAreEqual(expected, testNumeral, 
                        (numeral) -> RomanNumeral.addExact(numeral, smallest));
            }
            
            @DisplayName(
                    "addExact(RomanNumeral, largest addable RomanNumeral)")
            @ParameterizedTest(name = "addExact({1}, {2})")
            @MethodSource("addExactLargestTestArgsProvider")
            public void addExactLargestTest(TestNumeral expected,
                                            TestNumeral testNumeral,
                                            RomanNumeral largest) {
                assertNumeralsAreEqual(expected, testNumeral, 
                        (numeral) -> RomanNumeral.addExact(numeral, largest));
            }
            
            @DisplayName("addExact(RomanNumeral, same RomanNumeral)")
            @ParameterizedTest(name = "addExact({1}, same {1})")
            @MethodSource("addExactSameArgsProvider")
            public void addExactSameTest(TestNumeral expected, 
                                         TestNumeral testNumeral) {
                assertNumeralsAreEqual(expected, testNumeral, 
                        (numeral) -> RomanNumeral.addExact(numeral, numeral));
            }
            
            @DisplayName("addExact(x, y) ?= addExact(y, x)")
            @ParameterizedTest(
                    name = "addExact({1}, {2}) ?= addExact({2}, {1})")
            @MethodSource("addExactSmallestTestArgsProvider")
            public void addExactCommutativeTest(TestNumeral expected,
                                                TestNumeral testNumeral,
                                                RomanNumeral smallest) {
                assertCommutativeProperty(testNumeral, smallest,
                        (x, y) -> RomanNumeral.addExact(x, y));
            }
            
            @DisplayName(
                    "addExact(RomanNumeral, too large of a RomanNumeral)")
            @ParameterizedTest(name = "addExact({0}, {1})")
            @MethodSource("addExactOverflowTestArgsProvider")
            public void addExactOverflowTest(TestNumeral testNumeral,
                                             RomanNumeral tooLarge,
                                             boolean operandNotFound) {
                assertOverflows(
                        (numeral) -> RomanNumeral.addExact(numeral, tooLarge),
                        testNumeral, operandNotFound);
                             
            }
            
            private Stream<Arguments> addExactSmallestTestArgsProvider() {
                return smallestTestArgsProvider(1, ADDITION);
            }
            
            private Stream<Arguments> addExactLargestTestArgsProvider() {
                // numeral + largest = MAX
                // largest = MAX - numeral
                return largestTestArgsProvider(1, ADDITION,
                        (numeral) -> RomanNumeral.MAX_VALUE - numeral);
            }
            
            private Stream<Arguments> addExactSameArgsProvider() {
                return sameTestArgProvider((numeral) -> numeral + numeral);
            }
        
            private Stream<Arguments> addExactOverflowTestArgsProvider() {
                /* 
                 * value + x = MAX + 1
                 * x = MAX + 1 - value
                 */
                return overflowTestArgsProvider(ADDITION, 
                        (value) -> RomanNumeral.MAX_VALUE + 1 - value,
                        RomanNumeral.MIN_VALUE - 1);  // skip no value
            }
        }

        @Nested
        @DisplayName("decrementExact(RomanNumeral)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class decrementExactTests {
            @DisplayName("decrementExact(RomanNumeral)")
            @ParameterizedTest(name = "decrementExact({1})")
            @MethodSource("decrementExactTestArgsProvider")
            public void decrementExactTest(TestNumeral expected,
                                           TestNumeral testNumeral) {
                assertNumeralsAreEqual(expected, testNumeral,
                        (numeral) -> RomanNumeral.decrementExact​(numeral));
            }
            
            @DisplayName("decrementExact(too small of a RomanNumeral)")
            @ParameterizedTest(name = "decrementExact({0})")
            @MethodSource("decrementExactOverflowTestArgsProvider")
            public void decrementExactOverflowTest(TestNumeral tooSmall) {
                assertOverflows(
                        (numeral) -> RomanNumeral.decrementExact​(numeral),
                        tooSmall, false);
            }
            
            private Stream<Arguments> decrementExactTestArgsProvider() {
                return unaryTestArgsProvider((numeral) -> numeral - 1);
            }
            
            private Stream<TestNumeral>
                    decrementExactOverflowTestArgsProvider() {
                initTestNumerals();
                
                return Stream.of(testNumerals.get(RomanNumeral.MIN_VALUE - 1));
            }
        }
        
        @Nested
        @DisplayName("divideExact(RomanNumeral, RomanNumeral")
        @TestInstance(Lifecycle.PER_CLASS)
        public class divideExactTests {
            private final IntBinaryOperator DIVISION = (x, y) -> x / y;
            
            @DisplayName("divideExact(RomanNumeral, "
                         + "smallest divisible RomanNumeral)")
            @ParameterizedTest(name = "divideExact({1}, {2})")
            @MethodSource("divideExactSmallestTestArgsProvider")
            public void divideExactSmallestTest(TestNumeral expected,
                                                TestNumeral testNumeral,
                                                RomanNumeral smallest) {
                assertNumeralsAreEqual(expected, testNumeral, 
                        (numeral) -> RomanNumeral.divideExact(numeral,
                                                              smallest));
            }
            
            @DisplayName(
                    "divideExact(RomanNumeral, largest divisible RomanNumeral)")
            @ParameterizedTest(name = "divideExact({1}, {2})")
            @MethodSource("divideExactLargestTestArgsProvider")
            public void divideExactLargestTest(TestNumeral expected,
                                               TestNumeral testNumeral,
                                               RomanNumeral largest) {
                assertNumeralsAreEqual(expected, testNumeral,
                        (numeral) -> RomanNumeral.divideExact(numeral,
                                                              largest));
            }
            
            @DisplayName(
                    "divideExact(RomanNumeral, same RomanNumeral)")
            @ParameterizedTest(name = "divideExact({1}, same {1})")
            @MethodSource("divideExactSameTestArgsProvider")
            public void divideExactSameTest(TestNumeral expected, 
                                            TestNumeral testNumeral) {
                assertNumeralsAreEqual(expected, testNumeral,
                        (numeral) -> RomanNumeral.divideExact(numeral,
                                                              numeral));
            }
        
            @DisplayName("divideExact(RomanNumeral, I)")
            @ParameterizedTest(name = "divideExact({1}, {0})")
            @MethodSource("divideExactOneTestArgsProvider")
            public void divideExactOneTest(RomanNumeral one,
                                           TestNumeral testNumeral) {
                assertNumeralsAreEqual(testNumeral, testNumeral,
                        (numeral) -> RomanNumeral.divideExact(numeral, one));
            }
            
            @DisplayName(
                    "divideExact(RomanNumeral, too large of a RomanNumeral)")
            @ParameterizedTest(name = "divideExact({0}, {1})")
            @MethodSource("divideExactOverflowTestArgsProvider")
            public void divideExactOverflowTest(TestNumeral testNumeral,
                                                RomanNumeral tooLarge,
                                                boolean operandNotFound) {
                assertOverflows(
                        (numeral) -> RomanNumeral.divideExact(numeral, 
                                                              tooLarge),
                        testNumeral, operandNotFound);
            }
            
            private Stream<Arguments> divideExactSmallestTestArgsProvider() {
                return smallestTestArgsProvider(2, DIVISION);
            }
            
            private Stream<Arguments> divideExactLargestTestArgsProvider() {
                /*
                 * numeral / largest = MIN
                 * largest = numeral / MIN
                 * (largest != numeral to avoid duplicate tests, so largest--)
                 */
                return largestTestArgsProvider(2, DIVISION, 
                        (numeral) -> numeral / RomanNumeral.MIN_VALUE - 1);
            }
        
            private Stream<Arguments> divideExactSameTestArgsProvider() {
                Builder<Arguments> builder = Stream.builder();
                initTestNumerals();
                
                TestNumeral one = testNumerals.get(0);
                assumeTrue(one.expectedValue == 1);
                
                for (TestNumeral testNumeral : testNumerals) {
                    builder.accept(Arguments.of(one, testNumeral));
                }
                
                return builder.build();
            }
       
            private Stream<Arguments> divideExactOneTestArgsProvider() {
                return oneTestArgsProvider();
            }
        
            private Stream<Arguments> divideExactOverflowTestArgsProvider() {
                assumeTrue(RomanNumeral.MIN_VALUE == 1);
                
                return overflowTestArgsProvider(DIVISION, (value) -> value + 1,
                                                RomanNumeral.MAX_VALUE);
            }
        }
        
        @Nested
        @DisplayName("incrementExact(RomanNumeral)")
        @TestInstance(Lifecycle.PER_CLASS)
        public class incrementExactTests {
            @DisplayName("incrementExact(RomanNumeral)")
            @ParameterizedTest(name = "incrementExact({1})")
            @MethodSource("incrementExactTestArgsProvider")
            public void incrementExactTest(TestNumeral expected,
                                           TestNumeral testNumeral) {
                assertNumeralsAreEqual(expected, testNumeral,
                        (numeral) -> RomanNumeral.incrementExact​(numeral));
            }
            
            @DisplayName("incrementExact(too large of a RomanNumeral)")
            @ParameterizedTest(name = "incrementExact({0})")
            @MethodSource("incrementExactOverflowTestArgsProvider")
            public void incrementExactOverflowTest(TestNumeral tooLarge) {
                assertOverflows(
                        (numeral) -> RomanNumeral.incrementExact​(numeral),
                        tooLarge, false);
            }
            
            private Stream<Arguments> incrementExactTestArgsProvider() {
                return unaryTestArgsProvider((numeral) -> numeral + 1);
            }
            
            private Stream<TestNumeral> 
                    incrementExactOverflowTestArgsProvider() {
                initTestNumerals();
                
                return Stream.of(testNumerals.get(RomanNumeral.MAX_VALUE - 1));
            }
        }
        
        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("modExact(RomanNumeral, RomanNumeral)")
        public class modExactTests { 
            private final IntBinaryOperator MODULO = (x, y) -> x % y;
            
            @DisplayName(
                    "modExact(RomanNumeral, smallest moddable RomanNumeral)")
            @ParameterizedTest(name = "modExact({1}, {2})")
            @MethodSource("modExactSmallestTestArgsProvider")
            public void modExactSmallestTest(TestNumeral expected,
                                             TestNumeral testNumeral,
                                             RomanNumeral smallest,
                                             boolean smallerNotConstructable) {
                assumeFalse(smallerNotConstructable,
                        "Couldn't construct smaller RomanNumeral");
                
                assertNumeralsAreEqual(expected, testNumeral, 
                        (numeral) -> RomanNumeral.modExact(numeral, smallest));
            }
            
            @DisplayName("modExact(RomanNumeral, "
                         + "largest smaller moddable RomanNumeral)")
            @ParameterizedTest(name = "modExact({1}, {2})")
            @MethodSource("modExactLargestTestArgsProvider")
            public void modExactLargestTest(TestNumeral expected,
                                            TestNumeral testNumeral,
                                            RomanNumeral largest,
                                            boolean largestNotConstructable) {
                assumeFalse(largestNotConstructable,
                        "Couldn't construct largest smaller RomanNumeral");
                
                assertNumeralsAreEqual(expected, testNumeral, 
                        (numeral) -> RomanNumeral.modExact(numeral, largest));
            }
            
            @DisplayName("modExact(RomanNumeral, larger RomanNumeral)")
            @ParameterizedTest(name = "modExact({0}, {1})")
            @MethodSource("modExactLargerTestArgsProvider")
            public void modExactLargerTest(TestNumeral testNumeral, 
                                           RomanNumeral larger,
                                           boolean largerNotConstructable) {
                assumeFalse(largerNotConstructable,
                            "Couldn't construct larger RomanNumeral");
                
                assertNumeralsAreEqual(testNumeral, testNumeral, 
                        (numeral) -> RomanNumeral.modExact(numeral, larger));
            }
            
            @DisplayName("modExact(RomanNumeral, same RomanNumeral)")
            @ParameterizedTest(name = "modExact({0}, same {0})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#testNumeralsProvider()")
            public void modExactSameOverflowTest(TestNumeral testNumeral) {
                RomanNumeral numeral = getActualNumeral(testNumeral);
                
                assertThrows(ArithmeticException.class,
                             () -> RomanNumeral.modExact(numeral, numeral));
            }
            
            @DisplayName(
                    "modExact(RomanNumeral, RomanNumeral that divides evenly)")
            @ParameterizedTest(name = "modExact({0}, {1})")
            @MethodSource("modExactOverflowTestArgsProvider")
            public void modExactOverflowTest(TestNumeral testNumeral,
                                             RomanNumeral divisor,
                                             boolean operandNotFound) {
                assertOverflows(
                        (numeral) -> RomanNumeral.modExact(numeral, divisor),
                        testNumeral, operandNotFound);
            }
            
            private Stream<Arguments> modExactSmallestTestArgsProvider() {
                return modExactSmallestOrLargestTestArgsProvider(true);
            }
        
            private Stream<Arguments> modExactLargestTestArgsProvider() {
                return modExactSmallestOrLargestTestArgsProvider(false);
            }
            
            private Stream<Arguments> modExactSmallestOrLargestTestArgsProvider(
                    boolean smaller) {
                Builder<Arguments> builder = Stream.builder();
                initTestNumerals();

                for (int i = 2; i < testNumerals.size(); i++) {
                    TestNumeral testNumeral = testNumerals.get(i);
                    
                    if (testNumeral.actualNumeral == null) {
                        builder.accept(Arguments.of(null, testNumeral, null,
                                                    false));
                    } else {
                        int value;
                        if (smaller) {
                            value = 2;
                        } else {
                            value = testNumeral.expectedValue - 1;
                        }
                        
                        RomanNumeral operand = null;
                        int result = 1;
                        while (operand == null && value > 1
                                && value < testNumeral.expectedValue) {
                            result = testNumeral.expectedValue % value;
                            
                            if (result != 0) {
                                operand = testNumerals
                                          .get(value - 1).actualNumeral;
                            }
                            
                            if (smaller) {
                                value++;
                            } else {
                                value--;
                            }
                        }
                        
                        if (operand == null) {
                            builder.accept(Arguments.of(null, testNumeral, null,
                                                        true));
                        } else {
                            builder.accept(Arguments.of(
                                    testNumerals.get(result - 1), testNumeral,
                                    operand, false));
                        }
                    }
                }
                
                return builder.build();
            }
        
            private Stream<Arguments> modExactLargerTestArgsProvider() {
                Builder<Arguments> builder = Stream.builder();
                initTestNumerals();
                
                for (int i = 0; i < testNumerals.size() - 1; i++) {
                    TestNumeral testNumeral = testNumerals.get(i);
                    
                    if (testNumeral.actualNumeral == null) {
                        builder.accept(Arguments.of(testNumeral, null, false));
                    } else {
                        int value = testNumeral.expectedValue + 1;
                        
                        RomanNumeral larger = null;
                        while (larger == null
                                && value <= RomanNumeral.MAX_VALUE) {
                            larger = testNumerals.get(value - 1).actualNumeral;
                            value++;
                        }
                        
                        if (larger == null) {
                            builder.accept(Arguments.of(testNumeral, null,
                                                        true));
                        } else {
                            builder.accept(Arguments.of(testNumeral, larger, 
                                                        false));
                        }
                    }
                }
                
                return builder.build();
            }
        
            private Stream<Arguments> modExactOverflowTestArgsProvider() {
                return overflowTestArgsProvider(MODULO, (value) -> 1, 
                        RomanNumeral.MIN_VALUE - 1); // skip none
            }
        }
        
        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("multiplyExact(RomanNumeral, RomanNumeral)")
        public class multiplyExactTests {
            private final IntBinaryOperator MULTIPLICATION = (x, y) -> x * y;
            
            @DisplayName("multiplyExact(RomanNumeral, "
                                     + "smallest multipliable RomanNumeral)")
            @ParameterizedTest(name = "multiplyExact({1}, {2})")
            @MethodSource("multiplyExactSmallestTestArgsProvider")
            public void multiplyExactSmallestTest(TestNumeral expected,
                                                  TestNumeral testNumeral,
                                                  RomanNumeral smallest) {
                assertNumeralsAreEqual(expected, testNumeral,
                        (numeral) -> RomanNumeral.multiplyExact​(numeral,
                                                                smallest));
            }
            
            @DisplayName("multiplyExact(RomanNumeral, "
                    + "largest multipliable RomanNumeral)")
            @ParameterizedTest(name = "multiplyExact({1}, {2})")
            @MethodSource("multiplyExactLargestTestArgsProvider")
            public void multiplyExactLargestTest(TestNumeral expected,
                                                 TestNumeral testNumeral,
                                                 RomanNumeral largest) {
                assertNumeralsAreEqual(expected, testNumeral,
                        (numeral) -> RomanNumeral.multiplyExact​(numeral,
                                                                largest));
            }
            
            @DisplayName("multiplyExact(RomanNumeral, I)")
            @ParameterizedTest(name = "multiplyExact({1}, {0})")
            @MethodSource("multiplyExactOneTestArgsProvider")
            public void multiplyExactOneTest(RomanNumeral one,
                                             TestNumeral testNumeral) {
                assertNumeralsAreEqual(testNumeral, testNumeral,
                        (numeral) -> RomanNumeral.multiplyExact​(numeral, one));
            }
            
            @DisplayName("multiplyExact(RomanNumeral, same RomanNumeral)")
            @ParameterizedTest(name = "multiplyExact({1}, same {1})")
            @MethodSource("multipleExactSameTestArgsProvider")
            public void multiplyExactSameTest(TestNumeral expected,
                                              TestNumeral testNumeral) {
                assertNumeralsAreEqual(expected, testNumeral,
                        (numeral) -> RomanNumeral.multiplyExact​(numeral,
                                                                numeral));
            }
            
            @DisplayName("multiplyExact(x, y) ?= multiplyExact(y, x)")
            @ParameterizedTest(
                    name = "multiplyExact({1}, {2}) ?= multiplyExact({2}, {1})")
            @MethodSource("multiplyExactSmallestTestArgsProvider")
            public void multiplyExactCommutativeTest(TestNumeral expected,
                                                     TestNumeral testNumeral,
                                                     RomanNumeral smallest) {
                assertCommutativeProperty(testNumeral, smallest,
                        (x, y) -> RomanNumeral.addExact(x, y));
            }
        
            @DisplayName(
                    "multiplyExact(RomanNumeral, too large of a RomanNumeral)")
            @ParameterizedTest(name = "multiplyExact({0}, {1})")
            @MethodSource("multiplyExactOverflowTestArgsProvider")
            public void multiplyExactOverflowTest(TestNumeral testNumeral,
                                                  RomanNumeral tooLarge,
                                                  boolean operandNotFound) {
                assertOverflows(
                        (numeral) -> RomanNumeral.multiplyExact​(numeral,
                                                                tooLarge),
                        testNumeral, operandNotFound);
            }
            
            private Stream<Arguments> multiplyExactSmallestTestArgsProvider() {
                return smallestTestArgsProvider(2, MULTIPLICATION);
            }
        
            private Stream<Arguments> multiplyExactLargestTestArgsProvider() {
                /*
                 * numeral * largest = MAX
                 * largest = MAX / numeral;
                 */
                return largestTestArgsProvider(2, MULTIPLICATION, 
                        (numeral) -> RomanNumeral.MAX_VALUE / numeral);
            }
        
            private Stream<Arguments> multiplyExactOneTestArgsProvider() {
                return oneTestArgsProvider();
            }
        
            private Stream<Arguments> multipleExactSameTestArgsProvider() {
                return sameTestArgProvider((numeral) -> numeral * numeral);
            }
        
            private Stream<Arguments> multiplyExactOverflowTestArgsProvider() {
                /*
                 * value * tooLarge = MAX + 1
                 * tooLarge = (MAX + 1) / value
                 */
                return overflowTestArgsProvider(MULTIPLICATION,
                        (value) -> (RomanNumeral.MAX_VALUE + 1) / value, 1);
            }
        }
        
        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("powExact(RomanNumeral, RomanNumeral)")
        public class powExactTests {
            private final IntBinaryOperator EXPONENTIATION 
                    = (x, y) -> (int) Math.pow(x, y);
            
            @DisplayName(
                    "powExact(RomanNumeral, smallest powerable RomanNumeral)")
            @ParameterizedTest(name = "powExact({1}, {2})")
            @MethodSource("powExactSmallestTestArgsProvider")
            public void powExactSmallestTest(TestNumeral expected,
                                             TestNumeral testNumeral,
                                             RomanNumeral smallest) {
                assertNumeralsAreEqual(expected, testNumeral,
                        (numeral) -> RomanNumeral.powExact(numeral, smallest));
            }

            @DisplayName(
                    "powExact(RomanNumeral, largest powerable RomanNumeral)")
            @ParameterizedTest(name = "powExact({1}, {2})")
            @MethodSource("powExactLargestTestArgsProvider")
            public void powExactLargestTest(TestNumeral expected,
                                            TestNumeral testNumeral,
                                            RomanNumeral largest) {
                assertNumeralsAreEqual(expected, testNumeral,
                        (numeral) -> RomanNumeral.powExact(numeral, largest));
            }

            @DisplayName("powExact(RomanNumeral, I)")
            @ParameterizedTest(name = "powExact({1}, {0})")
            @MethodSource("powExactOneTestArgsProvider")
            public void powExactOneTest(RomanNumeral one,
                                        TestNumeral testNumeral) {
                assertNumeralsAreEqual(testNumeral, testNumeral,
                        (numeral) -> RomanNumeral.powExact(numeral, one));
            }
            
            @DisplayName("powExact(RomanNumeral, too large of a RomanNumeral)")
            @ParameterizedTest(name = "powExact({0}, {1})")
            @MethodSource("powExactOverflowTestArgsProvider")
            public void powExactOverflowTest(TestNumeral testNumeral,
                                             RomanNumeral tooLarge,
                                             boolean operandNotFound) {
                assertOverflows((numeral) -> RomanNumeral.powExact(numeral,
                                                                   tooLarge),
                                testNumeral, operandNotFound);
            }
            
            private Stream<Arguments> powExactSmallestTestArgsProvider() {
                return smallestTestArgsProvider(2, EXPONENTIATION);
            }
        
            private Stream<Arguments> powExactLargestTestArgsProvider() {
                /*
                 * value pow largest = MAX
                 * largest = logMAX(value)
                 */
                return largestTestArgsProvider(2, EXPONENTIATION, 
                        (value) -> (int) (Math.log(RomanNumeral.MAX_VALUE)
                                          / Math.log(value)));
            }
        
            private Stream<Arguments> powExactOneTestArgsProvider() {
                return oneTestArgsProvider();
            }
        
            private Stream<Arguments> powExactOverflowTestArgsProvider() {
                return overflowTestArgsProvider(EXPONENTIATION, (value) -> 2,
                                                1);
            }
        }
        
        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("subtractExact(RomanNumeral, RomanNumeral)")
        public class subtractExactTests {
            private final IntBinaryOperator SUBTRACTION = (x, y) -> x - y;
            
            @DisplayName("subtractExact(RomanNumeral, "
                                     + "smallest subtractable RomanNumeral)")
            @ParameterizedTest(name = "subtractExact({1}, {2})")
            @MethodSource("subtractExactSmallestTestArgsProvider")
            public void subtractExactSmallestTest(TestNumeral expected,
                                                  TestNumeral testNumeral,
                                                  RomanNumeral smallest) {
                assertNumeralsAreEqual(expected, testNumeral, 
                        (numeral) -> RomanNumeral.subtractExact​(numeral,
                                                                smallest));
            }
            
            @DisplayName("subtractExact(RomanNumeral, "
                                     + "largest subtractable RomanNumeral)")
            @ParameterizedTest(name = "subtractExact({1}, {2})")
            @MethodSource("subtractExactLargestTestArgsProvider")
            public void subtractExactLargestTest(TestNumeral expected,
                                                 TestNumeral testNumeral,
                                                 RomanNumeral largest) {
                assertNumeralsAreEqual(expected, testNumeral, 
                            (numeral) -> RomanNumeral.subtractExact​(numeral,
                                                                    largest));
            }
            
            @DisplayName("subtractExact(RomanNumeral, same RomanNumeral)")
            @ParameterizedTest(name = "subtractExact({0}, same {0})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#testNumeralsProvider()")
            public void subtractExactSameOverflowTest(TestNumeral testNumeral) {
                assertOverflows(
                        (numeral) -> RomanNumeral.subtractExact​(numeral,
                                                                numeral),
                        testNumeral, false);
            }
            
            @DisplayName(
                    "subtractExact(RomanNumeral, too large of a RomanNumeral)")
            @ParameterizedTest(name = "subtractExact({0}, {1})")
            @MethodSource("subtractExactOverflowTestArgsProvider")
            public void subtractExactOverflowTest(TestNumeral testNumeral,
                                                  RomanNumeral tooLarge,
                                                  boolean operandNotFound) {
                assertOverflows((numeral)
                        -> RomanNumeral.subtractExact​(numeral, tooLarge),
                        testNumeral, operandNotFound);
            }
            
            private Stream<Arguments> subtractExactSmallestTestArgsProvider() {
                return smallestTestArgsProvider(1, SUBTRACTION);
            }
       
            private Stream<Arguments> subtractExactLargestTestArgsProvider() {
                /*
                 * value - largest = MIN
                 * largest = value - MIN
                 */
                return largestTestArgsProvider(2, SUBTRACTION, 
                        (value) -> value - RomanNumeral.MIN_VALUE);
            }
        
            private Stream<Arguments> subtractExactOverflowTestArgsProvider() {
                return overflowTestArgsProvider(SUBTRACTION, 
                        (value) -> value + 1, RomanNumeral.MAX_VALUE);
            }
        }
        
        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("max(RomanNumeral, RomanNumeral)")
        public class maxTests {
            @DisplayName("max(RomanNumeral, smaller RomanNumeral)")
            @ParameterizedTest(name = "max({0}, {1})")
            @MethodSource("maxSmallerTestArgsProvider")
            public void maxSmallerTest(TestNumeral testNumeral, 
                                       TestNumeral smaller,
                                       boolean smallerNotConstructable) {
                assumeFalse(smallerNotConstructable, 
                            "No constructable smaller RomanNumeral");
                
                assertNumeralsAreEqual(testNumeral, testNumeral, 
                        (numeral) -> RomanNumeral.max(numeral,
                                                      smaller.actualNumeral));
            }
            
            @DisplayName("max(RomanNumeral, larger RomanNumeral)")
            @ParameterizedTest(name = "max({0}, {1})")
            @MethodSource("maxLargerTestArgsProvider")
            public void maxLargerTest(TestNumeral testNumeral, 
                                      TestNumeral larger,
                                      boolean largerNotConstructable) {
                assumeFalse(largerNotConstructable, 
                            "No constructable larger RomanNumeral");
                
                assertNumeralsAreEqual(larger, testNumeral, 
                        (numeral) -> RomanNumeral.max(numeral,
                                                      larger.actualNumeral));
            }
            
            @DisplayName("max(RomanNumeral, same RomanNumeral)")
            @ParameterizedTest(name = "max({0}, same {0})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#testNumeralsProvider()")
            public void maxSameTest(TestNumeral testNumeral) {
                assertNumeralsAreEqual(testNumeral, testNumeral, 
                                       (numeral) -> RomanNumeral.max(numeral,
                                                                     numeral));
            }
        
            private Stream<Arguments> maxSmallerTestArgsProvider() {
                return smallerOrLargerTestArgsProvider(true);
            }
            
            private Stream<Arguments> maxLargerTestArgsProvider() {
                return smallerOrLargerTestArgsProvider(false);
            }
        }
        
        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("min(RomanNumeral, RomanNumeral)")
        public class minTests {
            @DisplayName("min(RomanNumeral, smaller RomanNumeral)")
            @ParameterizedTest(name = "min({0}, {1})")
            @MethodSource("minSmallerTestArgsProvider")
            public void minSmallerTest(TestNumeral testNumeral, 
                                       TestNumeral smaller,
                                       boolean smallerNotConstructable) {
                assumeFalse(smallerNotConstructable, 
                            "No constructable smaller RomanNumeral");
                
                assertNumeralsAreEqual(smaller, testNumeral, 
                        (numeral) -> RomanNumeral.min(numeral,
                                                      smaller.actualNumeral));
            }
            
            @DisplayName("min(RomanNumeral, larger RomanNumeral)")
            @ParameterizedTest(name = "min({0}, {1})")
            @MethodSource("minLargerTestArgsProvider")
            public void minLargerTest(TestNumeral testNumeral, 
                                      TestNumeral larger,
                                      boolean largerNotConstructable) {
                assumeFalse(largerNotConstructable, 
                            "No constructable larger RomanNumeral");
                
                assertNumeralsAreEqual(testNumeral, testNumeral, 
                        (numeral) -> RomanNumeral.min(numeral,
                                                      larger.actualNumeral));
            }
            
            @DisplayName("min(RomanNumeral, same RomanNumeral)")
            @ParameterizedTest(name = "min({0}, same {0})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#testNumeralsProvider()")
            public void minSameTest(TestNumeral testNumeral) {
                assertNumeralsAreEqual(testNumeral, testNumeral, 
                        (numeral) -> RomanNumeral.min(numeral, numeral));
            }
        
            private Stream<Arguments> minSmallerTestArgsProvider() {
                return smallerOrLargerTestArgsProvider(true);
            }

            private Stream<Arguments> minLargerTestArgsProvider() {
                return smallerOrLargerTestArgsProvider(false);
            }
        }
        
        private Stream<Arguments> smallestTestArgsProvider(int minValue,
                IntBinaryOperator op) {
            initTestNumerals();
            
            for (int i = minValue - 1; i < testNumerals.size(); i++) {
                TestNumeral testNumeral = testNumerals.get(i);
                
                if (testNumeral.actualNumeral != null) {
                    return operandTestArgsProvider(op,
                                                   testNumeral.actualNumeral);
                }
            }
            
            throw new TestAbortedException("No constructable RomanNumerals");
        }
        
        private Stream<Arguments> operandTestArgsProvider(IntBinaryOperator op,
                RomanNumeral operand) {
            Builder<Arguments> builder = Stream.builder();
            initTestNumerals();
            
            for (TestNumeral testNumeral : testNumerals) {
                if (testNumeral.actualNumeral == null) {
                    // For displaying test was skipped
                    builder.accept(Arguments.of(null, testNumeral, null));
                } else {
                    int resultValue = op.applyAsInt(testNumeral.expectedValue,
                                                    operand.value);
                    
                    if (isWithinBounds(resultValue)) {
                        builder.accept(
                                Arguments.of(testNumerals.get(resultValue - 1),
                                             testNumeral, operand));
                    }
                }
            }
            
            return builder.build();
        }
   
        private Stream<Arguments> largestTestArgsProvider(int minValue,
                IntBinaryOperator op, IntUnaryOperator calcLargestValue) {
            Builder<Arguments> builder = Stream.builder();
            initTestNumerals();
            
            for (TestNumeral testNumeral : testNumerals) {
                if (testNumeral.actualNumeral == null) {
                    // For displaying test was skipped
                    builder.accept(Arguments.of(null, testNumeral, null));
                } else {
                    int largestValue = calcLargestValue.applyAsInt(
                            testNumeral.expectedValue); 
                    
                    boolean found = false;
                    while (!found && largestValue >= minValue
                            && largestValue <= RomanNumeral.MAX_VALUE) {
                        RomanNumeral largest = testNumerals
                                .get(largestValue - 1).actualNumeral;
                        
                        if (largest == null) {
                            largestValue--;
                        } else {
                            int resultValue = op.applyAsInt(
                                    testNumeral.expectedValue, largest.value);
                            
                            if (isWithinBounds(resultValue)) {
                                builder.accept(Arguments.of(
                                        testNumerals.get(resultValue - 1),
                                        testNumeral, largest));
                                found = true;
                            } else {
                                largestValue--;
                            }
                        }
                    }
                }
            }
            
            return builder.build();
        }
        
        private Stream<Arguments> sameTestArgProvider(IntUnaryOperator op) {
            Builder<Arguments> builder = Stream.builder();
            initTestNumerals();
            
            for (TestNumeral testNumeral : testNumerals) {
                if (testNumeral.actualNumeral == null) {
                    builder.accept(Arguments.of(null, testNumeral));
                } else {
                    int resultValue = op.applyAsInt(testNumeral.expectedValue);
                    
                    if (isWithinBounds(resultValue)) {
                        builder.accept(Arguments.of(
                                testNumerals.get(resultValue - 1),
                                testNumeral));
                    }
                }
            }
            
            return builder.build();
        }
        
        private Stream<Arguments> unaryTestArgsProvider(IntUnaryOperator op) {
            Builder<Arguments> builder = Stream.builder();
            initTestNumerals();
            
            for (TestNumeral testNumeral : testNumerals) {
                if (testNumeral.actualNumeral == null) {
                    builder.accept(Arguments.of(null, testNumeral));
                } else {
                    int result = op.applyAsInt(testNumeral.expectedValue);
                    
                    if (isWithinBounds(result)) {
                        builder.accept(Arguments.of(
                                testNumerals.get(result - 1), testNumeral));
                    }
                }
            }
            
            return builder.build();
        }
        
        private Stream<Arguments> oneTestArgsProvider() {
            initTestNumerals();
            
            RomanNumeral one = getActualNumeral(testNumerals.get(0));
            assumeTrue(one.value == 1);
            
            Builder<Arguments> builder = Stream.builder();
            for (TestNumeral testNumeral : testNumerals) {
                builder.accept(Arguments.of(one, testNumeral));
            }
            
            return builder.build();
        }
        
        private void assertNumeralsAreEqual(TestNumeral expected,
                                            TestNumeral testNumeral,
                                            UnaryOperator<RomanNumeral> op) {
            RomanNumeral numeral = getActualNumeral(testNumeral);
            assertEquals(expected, assertDoesNotThrow(() -> op.apply(numeral)));
        }
    
        private void assertCommutativeProperty(TestNumeral testNumeral,
                RomanNumeral numeral, BinaryOperator<RomanNumeral> op) {
            RomanNumeral another = getActualNumeral(testNumeral);
            
            assertEquals(assertDoesNotThrow(() -> op.apply(numeral, another)),
                         assertDoesNotThrow(() -> op.apply(another, numeral)));
        }
    
        private void assertOverflows(Function<RomanNumeral, RomanNumeral> func,
                                     TestNumeral testNumeral,
                                     boolean operandNotFound) {
            assumeFalse(operandNotFound, "Failed to construct operand");
            
            RomanNumeral numeral = getActualNumeral(testNumeral);
            assertThrows(ArithmeticException.class, () -> func.apply(numeral));
        }
    }

    @Nested
    public class InstanceMethodTests {
        @Nested
        @TestInstance(Lifecycle.PER_CLASS)
        @DisplayName("RomanNumeral.compareTo(RomanNumeral)")
        public class compareToTests {
            @DisplayName("RomanNumeral.compareTo(smaller RomanNumeral)")
            @ParameterizedTest(name = "{0}.compareTo({1})")
            @MethodSource("compareToSmallerTestArgsProvider")
            public void compareToSmallerTest(TestNumeral testNumeral,
                                             TestNumeral smaller) {
                RomanNumeral numeral = getActualNumeral(testNumeral);
                assertTrue(0 < numeral.compareTo(smaller.actualNumeral));
            }
            
            @DisplayName("RomanNumeral.compareTo(larger RomanNumeral)")
            @ParameterizedTest(name = "{0}.compareTo({1})")
            @MethodSource("compareToLargerTestArgsProvider")
            public void compareToLargerTest(TestNumeral testNumeral,
                                            TestNumeral larger) {
                RomanNumeral numeral = getActualNumeral(testNumeral);
                assertTrue(0 > numeral.compareTo(larger.actualNumeral));
            }
        
            @DisplayName("RomanNumeral.compareTo(same RomanNumeral)")
            @ParameterizedTest(name = "{0}.compareTo(same {0})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#testNumeralsProvider()")
            public void compareToSameTest(TestNumeral testNumeral) {
                RomanNumeral numeral = getActualNumeral(testNumeral);
                assertTrue(0 == numeral.compareTo(numeral));
            }
        
            private Stream<Arguments> compareToSmallerTestArgsProvider() {
                return smallerOrLargerTestArgsProvider(true);
            }
            
            private Stream<Arguments> compareToLargerTestArgsProvider() {
                return smallerOrLargerTestArgsProvider(false);
            }
        }
    
        @Nested
        @DisplayName("RomanNumeral.equals(Object)")
        public class equalsTests {
            @DisplayName("RomanNumeral.equals(unequivalent RomanNumeral)")
            @ParameterizedTest(name = "{0}.equals({1})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#unequivalentTestArgsProvider()")
            public void equalsUnequivalentTest(TestNumeral testNumeral,
                                               RomanNumeral unequivalentNumeral,
                                               boolean notFound) {
                assumeFalse(notFound,
                            "Failed to construct unequivalent Roman Numeral");
                
                RomanNumeral numeral = getActualNumeral(testNumeral);
                assertFalse(assertDoesNotThrow(
                        () -> numeral.equals(unequivalentNumeral)));
            }
            
            @DisplayName("RomanNumeral.equals(same RomanNumeral)")
            @ParameterizedTest(name = "{0}.equals({0})")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#testNumeralsProvider()")
            public void equalsSameTest(TestNumeral testNumeral) {
                RomanNumeral numeral = getActualNumeral(testNumeral);
                assertTrue(assertDoesNotThrow(() -> numeral.equals(numeral)));
            }
            
            @DisplayName("RomanNumeral.equals(unequivalent RomanNumeral)"
                       + " is consistent?")
            @ParameterizedTest(name = "{0}.equals({1}) is consistent?")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#unequivalentTestArgsProvider()")
            public void equalsUnequivalentConsistencyTest(
                    TestNumeral testNumeral, RomanNumeral unequivalentNumeral,
                    boolean notFound) {
                assumeFalse(notFound,
                            "Failed to construct unequivalent Roman Numeral");
                
                RomanNumeral numeral = getActualNumeral(testNumeral);
                for (int i = 0; i < NUM_REPEATS_TO_CHECK_FOR_CONSISTENY; i++) {
                    assertFalse(assertDoesNotThrow(
                            () -> numeral.equals(unequivalentNumeral)));
                }
            }
            
            @DisplayName(
                    "RomanNumeral.equals(same RomanNumeral) is consistent?")
            @ParameterizedTest(name = "{0}.equals({0}) is consistent?")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#testNumeralsProvider()")
            public void equalsSameConsistencyTest(TestNumeral testNumeral) {
                RomanNumeral numeral = getActualNumeral(testNumeral);
                for (int i = 0; i < NUM_REPEATS_TO_CHECK_FOR_CONSISTENY; i++) {
                    assertTrue(assertDoesNotThrow(
                            () -> numeral.equals(numeral)));
                }
            }
            
            @DisplayName("RomanNumeral.equals(null)")
            @ParameterizedTest(name = "{0}.equals(null)")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#testNumeralsProvider()")
            public void equalsNullTest(TestNumeral testNumeral) {
                RomanNumeral numeral = getActualNumeral(testNumeral);
                assertFalse(assertDoesNotThrow(() -> numeral.equals(null)));
            }
        }
        
        @Nested
        @DisplayName("RomanNumeral.hashCode()")
        public class hashCodeTests {
            @DisplayName("RomanNumeral.hashCode() is consistent?")
            @ParameterizedTest(name = "{0}.hashCode() is consistent?")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#testNumeralsProvider()")
            public void hashCodeConsistencyTest(TestNumeral testNumeral) {
                RomanNumeral numeral = getActualNumeral(testNumeral);
                int hashCode = assertDoesNotThrow(() -> numeral.hashCode());
                for (int i = 1; i < NUM_REPEATS_TO_CHECK_FOR_CONSISTENY; i++) {
                    assertEquals(hashCode,
                                 assertDoesNotThrow(() -> numeral.hashCode()));
                }
            }
            
            @Tag("WarningTest")
            @DisplayName("RomanNumeral.hashCode()"
                       + " ?= (unequivalent RomanNumeral).hashCode()")
            @ParameterizedTest(name = "{0}.hashCode() ?= {1}.hashCode()")
            @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                          + "#unequivalentTestArgsProvider()")
            public void hashCodeUnequalWarningTest(TestNumeral testNumeral,
                                                   RomanNumeral unequivalent,
                                                   boolean notFound) {
                assumeFalse(notFound,
                            "Failed to construct unequivalent Roman Numeral");
                
                RomanNumeral numeral = getActualNumeral(testNumeral);
                assertNotEquals(assertDoesNotThrow(() -> numeral.hashCode()),
                        assertDoesNotThrow(() -> unequivalent.hashCode()));
            }
        }
        
        @DisplayName("RomanNumeral.toString()")
        @ParameterizedTest(name = "{0}.toString()")
        @MethodSource("com.github.tjuve.romannumeral.RomanNumeralTest"
                      + "#testNumeralsProvider()")
        public void toStringTest(TestNumeral testNumeral) {
            RomanNumeral numeral = getActualNumeral(testNumeral);
            assertEquals(testNumeral.expectedSymbols,
                         assertDoesNotThrow(() -> numeral.toString()));
        }
    }
    
    @SuppressWarnings("unused")
    private static Stream<Arguments> expectedSymbolsValuePairsProvider() {
        initExpectedSymbolsValuePairs();
        return expectedSymbolsValuePairs.stream();
    }
    
    @SuppressWarnings("unused")
    private static Stream<TestNumeral> testNumeralsProvider() {
        initTestNumerals();
        return testNumerals.stream();
    }
    
    @SuppressWarnings("unused")
    private static Stream<Integer> invalidValueTestArgsProvider() {
        return Stream.of(RomanNumeral.MIN_VALUE - 1,
                         RomanNumeral.MAX_VALUE + 1);
    }
    
    @SuppressWarnings("unused")
    private static Stream<String> invalidSymbolsProvider() {
        if (invalidSymbols != null) {
            return invalidSymbols.stream();
        }
        
        invalidSymbols = new LinkedList<String>();
        
        // Null tested separately
        invalidSymbols.add("");
        
        Symbol[] symbols = Symbol.values();
        char[] oneLetterSymbolNames = new char[symbols.length];
        int numOneLetterSymbolNames = 0;
        for (int i = 0; i < symbols.length; i++) {
            Symbol symbol = symbols[i];
            String name = symbols[i].name();
            
            if (name.length() == 1) {
                oneLetterSymbolNames[numOneLetterSymbolNames] = name.charAt(0);
                numOneLetterSymbolNames++;
            }
            
            if (String.valueOf(symbol.value).startsWith("1")) {
                // forms like "IIII"
                invalidSymbols.add(name + name + name + name);
                
                // "II(larger)"
                for (int j = i + 1; j < symbols.length; j++) {
                    invalidSymbols.add(name + name + symbols[j].name());
                }
                
                // "I(symbol greater than X)
                for (int j = i + 3; j < symbols.length; j++) {
                    invalidSymbols.add(name + symbols[j].name());
                }
                
                if (i + 1 < symbols.length) {
                    String next = symbols[i + 1].name();
                    
                    // "IVI"
                    invalidSymbols.add(name + next + name);
                    
                    if (i + 2 < symbols.length) {
                        String nextNext = symbols[i + 2].name();
                        
                        // "IXI"
                        invalidSymbols.add(name + nextNext + name);
                        // "IXV"
                        invalidSymbols.add(name + nextNext + next);
                        // "IXX"
                        invalidSymbols.add(name + nextNext + nextNext);
                        
                        if (i + 4 < symbols.length) {
                            // "IXC"
                            invalidSymbols.add(name + nextNext
                                               + symbols[i + 4].name());
                        }
                    }
                }
            } else  {
                // forms like "VV"
                invalidSymbols.add(name + name);
                
                // "V(greater symbol)"
                for (int j = i + 1; j < symbols.length; j++) {
                    invalidSymbols.add(name + symbols[j].name());
                }
                
                if (i - 1 > 0) {
                    String prev = symbols[i - 1].name();
                    
                    // "VIV"
                    invalidSymbols.add(name + prev + name);
                    
                    if (i + 1 < symbols.length) {
                        // "VIX"
                        invalidSymbols.add(name + prev + symbols[i + 1].name());
                    }
                }
            }
        }
        
        Arrays.sort(oneLetterSymbolNames);   
        int i = 0;
        // Invalid characters (eg. "3", "i", "+")
        for (char c = ' '; c <= '~'; c++) {
            if (i < numOneLetterSymbolNames
                    && c == oneLetterSymbolNames[i]) {
                i++;
            } else {
                invalidSymbols.add("" + c);
            }
        }
        
        return invalidSymbols.stream();
    }
    
    private static Stream<Arguments> overflowTestArgsProvider(
            IntBinaryOperator op, IntUnaryOperator overflowPointOp,
            int skipValue) {
        Builder<Arguments> builder = Stream.builder();
        initTestNumerals();
        
        for (TestNumeral testNumeral : testNumerals) {
            if (testNumeral.expectedValue == skipValue) {
                continue;
            }
            
            if (testNumeral.actualNumeral == null) {
                builder.accept(Arguments.of(testNumeral, null, false));
            } else {
                int value = overflowPointOp.applyAsInt(
                        testNumeral.expectedValue);
                
                boolean found = false;
                TestNumeral operand = null;
                while (!found && value <= RomanNumeral.MAX_VALUE) {
                    operand = testNumerals.get(value - 1);
                    
                    if (operand.actualNumeral != null) {
                        int result = op.applyAsInt(testNumeral.expectedValue,
                                                   operand.expectedValue);
                        
                        found = !isWithinBounds(result);
                    }
                    
                    value++;
                }
                
                if (found) {
                    builder.accept(Arguments.of(testNumeral, 
                                                operand.actualNumeral, false));
                } else {
                    builder.accept(Arguments.of(testNumeral, null, true));
                }
            }
        }
        
        return builder.build();
    }
    
    private static Stream<Arguments> smallerOrLargerTestArgsProvider(
            boolean smaller) {
        Builder<Arguments> builder = Stream.builder();
        initTestNumerals();
        
        int i = 0;
        if (smaller) {
            i++;
        }
        int max = testNumerals.size();
        if (!smaller) {
            max--;
        }
        while (i < max) {
            TestNumeral testNumeral = testNumerals.get(i);
            
            if (testNumeral.actualNumeral == null) {
                builder.accept(Arguments.of(testNumeral, null, false));
            } else {
                int j = i;
                if (smaller) {
                    j--;
                } else {
                    j++;
                }
                
                TestNumeral operand = null;
                boolean found = false;
                while (!found && j >= 0 && j < testNumerals.size()) {
                    operand = testNumerals.get(j);
                    
                    if (operand.actualNumeral == null) {
                        if (smaller) {
                            j--;
                        } else {
                            j++;
                        }
                    } else {
                        found = true;
                    }
                }
                
                if (found) {
                    builder.accept(Arguments.of(testNumeral, operand, false));
                } else {
                    builder.accept(Arguments.of(testNumeral, null, true));
                }
            }
            
            i++;
        }
        
        return builder.build();
    }

    @SuppressWarnings("unused")
    private static Stream<Arguments> unequivalentTestArgsProvider() {
        Builder<Arguments> builder = Stream.builder();
        initTestNumerals();
        
        for (TestNumeral testNumeral : testNumerals) {
            if (testNumeral.actualNumeral == null) {
                builder.accept(Arguments.of(testNumeral, null, false));
            } else {
                int value = testNumeral.expectedValue - 1;
                RomanNumeral unequivalent = null;
                while (unequivalent == null
                        && value >= RomanNumeral.MIN_VALUE) {
                    unequivalent = testNumerals.get(value - 1).actualNumeral;
                    value--;
                }
                
                value = testNumeral.expectedValue + 1;
                while (unequivalent == null
                        && value <= RomanNumeral.MAX_VALUE) {
                    unequivalent = testNumerals.get(value - 1).actualNumeral;
                    value++;
                }
                
                if (unequivalent == null) {
                    builder.accept(Arguments.of(testNumeral, null, true));
                } else {
                    builder.accept(Arguments.of(testNumeral, unequivalent,
                                                false));
                }
            }
        }
        
        return builder.build();
    }

    private static void initExpectedSymbolsValuePairs() {
        if (expectedSymbolsValuePairs == null) {
            expectedSymbolsValuePairs
                    = new ArrayList<Arguments>(RomanNumeral.MAX_VALUE);
            
            if (testNumerals == null) {
                readReference((expectedSymbols, expectedValue) -> 
                        expectedSymbolsValuePairs.add(
                                Arguments.of(expectedSymbols, expectedValue)));
            } else {
                for (TestNumeral testNumeral : testNumerals) {
                    expectedSymbolsValuePairs.add(
                            Arguments.of(testNumeral.expectedSymbols,
                                         testNumeral.expectedValue));
                }
            }
        }
    }
    
    private static void initTestNumerals() {
        if (testNumerals == null) {
            testNumerals = new ArrayList<TestNumeral>(RomanNumeral.MAX_VALUE);
            
            if (expectedSymbolsValuePairs == null) {
                readReference(
                        (expectedSymbols, expectedValue) -> testNumerals.add(
                                new TestNumeral(expectedSymbols,
                                                expectedValue)));
            } else {
                for (Arguments args : expectedSymbolsValuePairs) {
                    Object[] symbolsValuePair = args.get();
                    testNumerals.add(
                            new TestNumeral((String) symbolsValuePair[0],
                                            (int) symbolsValuePair[1]));
                }
            }
        }
    }
    
    private static void readReference(BiConsumer<String, Integer> consumer) {
        try {
            BufferedReader referenceReader
                    = Files.newBufferedReader(Paths.get(REFERENCE_PATHNAME));
            
            String expectedSymbols;
            int expectedValue = RomanNumeral.MIN_VALUE;
            while ((expectedSymbols = referenceReader.readLine()) != null) {
                consumer.accept(expectedSymbols, expectedValue);
                expectedValue++;
            }
        } catch (IOException ioEx) {
            TestAbortedException testAbortedEx
                    = new TestAbortedException("Failed to read reference file");
            testAbortedEx.initCause(ioEx);
            throw testAbortedEx;
        }
    }
    
    private static boolean isWithinBounds(int value) {
        return value <= RomanNumeral.MAX_VALUE
                && value >= RomanNumeral.MIN_VALUE;
    }
    
    private static RomanNumeral getActualNumeral(TestNumeral testNumeral) {
        assumeFalse(testNumeral == null);
        assumeFalse(testNumeral.actualNumeral == null,
                    "Failed to construct: " + testNumeral);
        return testNumeral.actualNumeral;
    }
}
