/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.rulii.lib.spring.util;

import java.util.Locale;

import org.rulii.lib.spring.lang.Nullable;

/**
 * Miscellaneous {@link String} utility methods.
 *
 * <p>
 * Mainly for internal use within the framework; consider
 * <a href="http://commons.apache.org/proper/commons-lang/">Apache's Commons Lang</a> for a more
 * comprehensive suite of {@code String} utilities.
 *
 * <p>
 * This class delivers some simple functionality that should really be provided by the core Java
 * {@link String} and {@link StringBuilder} classes. It also provides easy-to-use methods to convert
 * between delimited strings, such as CSV strings, and collections and arrays.
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Keith Donald
 * @author Rob Harrop
 * @author Rick Evans
 * @author Arjen Poutsma
 * @author Sam Brannen
 * @author Brian Clozel
 * @since 16 April 2001
 */

public abstract class StringUtils {

  // ---------------------------------------------------------------------
  // General convenience methods for working with Strings
  // ---------------------------------------------------------------------

  /**
   * Check that the given {@code String} is neither {@code null} nor of length 0.
   * <p>
   * Note: this method returns {@code true} for a {@code String} that purely consists of whitespace.
   *
   * @param str the {@code String} to check (may be {@code null})
   * @return {@code true} if the {@code String} is not {@code null} and has length
   * @see #hasLength(CharSequence)
   * @see #hasText(String)
   */
  public static boolean hasLength(@Nullable String str) {
    return (str != null && !str.isEmpty());
  }

  /**
   * Check whether the given {@code String} contains actual <em>text</em>.
   * <p>
   * More specifically, this method returns {@code true} if the {@code String} is not {@code null},
   * its length is greater than 0, and it contains at least one non-whitespace character.
   *
   * @param str the {@code String} to check (may be {@code null})
   * @return {@code true} if the {@code String} is not {@code null}, its length is greater than 0,
   *         and it does not contain whitespace only
   * @see #hasText(CharSequence)
   */
  public static boolean hasText(@Nullable String str) {
    return (str != null && !str.isEmpty() && containsText(str));
  }

  private static boolean containsText(CharSequence str) {
    int strLen = str.length();
    for (int i = 0; i < strLen; i++) {
      if (!Character.isWhitespace(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

  /**
   * Trim leading whitespace from the given {@code String}.
   *
   * @param str the {@code String} to check
   * @return the trimmed {@code String}
   * @see Character#isWhitespace
   */
  public static String trimLeadingWhitespace(String str) {
    if (!hasLength(str)) {
      return str;
    }

    StringBuilder sb = new StringBuilder(str);
    while (sb.length() > 0 && Character.isWhitespace(sb.charAt(0))) {
      sb.deleteCharAt(0);
    }
    return sb.toString();
  }

  /**
   * Trim all occurrences of the supplied leading character from the given {@code String}.
   *
   * @param str the {@code String} to check
   * @param leadingCharacter the leading character to be trimmed
   * @return the trimmed {@code String}
   */
  public static String trimLeadingCharacter(String str, char leadingCharacter) {
    if (!hasLength(str)) {
      return str;
    }

    StringBuilder sb = new StringBuilder(str);
    while (sb.length() > 0 && sb.charAt(0) == leadingCharacter) {
      sb.deleteCharAt(0);
    }
    return sb.toString();
  }

  // ---------------------------------------------------------------------
  // Convenience methods for working with formatted Strings
  // ---------------------------------------------------------------------

  @Nullable
  private static Locale parseLocaleTokens(String localeString, String[] tokens) {
    String language = (tokens.length > 0 ? tokens[0] : "");
    String country = (tokens.length > 1 ? tokens[1] : "");
    validateLocalePart(language);
    validateLocalePart(country);

    String variant = "";
    if (tokens.length > 2) {
      // There is definitely a variant, and it is everything after the country
      // code sans the separator between the country code and the variant.
      int endIndexOfCountryCode = localeString.indexOf(country, language.length()) + country.length();
      // Strip off any leading '_' and whitespace, what's left is the variant.
      variant = trimLeadingWhitespace(localeString.substring(endIndexOfCountryCode));
      if (variant.startsWith("_")) {
        variant = trimLeadingCharacter(variant, '_');
      }
    }
    return (language.length() > 0 ? new Locale(language, country, variant) : null);
  }

  private static void validateLocalePart(String localePart) {
    for (int i = 0; i < localePart.length(); i++) {
      char ch = localePart.charAt(i);
      if (ch != ' ' && ch != '_' && ch != '#' && !Character.isLetterOrDigit(ch)) {
        throw new IllegalArgumentException(
            "Locale part \"" + localePart + "\" contains invalid characters");
      }
    }
  }

  // ---------------------------------------------------------------------
  // Convenience methods for working with String arrays
  // ---------------------------------------------------------------------

  /**
   * Convert a {@code String} array into a delimited {@code String} (e.g. CSV).
   * <p>
   * Useful for {@code toString()} implementations.
   *
   * @param arr the array to display
   * @param delim the delimiter to use (typically a ",")
   * @return the delimited {@code String}
   */
  public static String arrayToDelimitedString(@Nullable Object[] arr, String delim) {
    if (ObjectUtils.isEmpty(arr)) {
      return "";
    }
    if (arr.length == 1) {
      return ObjectUtils.nullSafeToString(arr[0]);
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < arr.length; i++) {
      if (i > 0) {
        sb.append(delim);
      }
      sb.append(arr[i]);
    }
    return sb.toString();
  }

  /**
   * Capitalize a {@code String}, changing the first letter to
   * upper case as per {@link Character#toUpperCase(char)}.
   * No other letters are changed.
   * @param str the {@code String} to capitalize
   * @return the capitalized {@code String}
   */
  public static String capitalize(String str) {
    return changeFirstCharacterCase(str, true);
  }

  /**
   * Uncapitalize a {@code String}, changing the first letter to
   * lower case as per {@link Character#toLowerCase(char)}.
   * No other letters are changed.
   * @param str the {@code String} to uncapitalize
   * @return the uncapitalized {@code String}
   */
  public static String uncapitalize(String str) {
    return changeFirstCharacterCase(str, false);
  }

  private static String changeFirstCharacterCase(String str, boolean capitalize) {
    if (!hasLength(str)) {
      return str;
    }

    char baseChar = str.charAt(0);
    char updatedChar;
    if (capitalize) {
      updatedChar = Character.toUpperCase(baseChar);
    }
    else {
      updatedChar = Character.toLowerCase(baseChar);
    }
    if (baseChar == updatedChar) {
      return str;
    }

    char[] chars = str.toCharArray();
    chars[0] = updatedChar;
    return new String(chars, 0, chars.length);
  }

  /**
   * Convert a {@code String} array into a comma delimited {@code String}
   * (i.e., CSV).
   * <p>Useful for {@code toString()} implementations.
   * @param arr the array to display (potentially {@code null} or empty)
   * @return the delimited {@code String}
   */
  public static String arrayToCommaDelimitedString(@Nullable Object[] arr) {
    return arrayToDelimitedString(arr, ",");
  }
}
