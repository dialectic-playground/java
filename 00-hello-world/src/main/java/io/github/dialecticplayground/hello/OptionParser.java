package io.github.dialecticplayground.hello;

import java.util.List;
import java.util.Optional;

/**
 * Hand-written option scanner (CONV-07 to CONV-12). It only checks option syntax: values are
 * recorded and operands collected for {@link Validator}, so that {@code --help} and {@code
 * --version} win over every value or operand error (CONV-12).
 */
final class OptionParser {
  private final List<String> arguments;
  private int index;
  private Optional<OptionValue> name = Optional.empty();
  private Optional<OptionValue> count = Optional.empty();
  private Optional<String> firstOperand = Optional.empty();

  private OptionParser(List<String> arguments) {
    this.arguments = arguments;
  }

  /**
   * Scans {@code arguments} from left to right.
   *
   * @return {@link ParsedArguments.Action#HELP} or {@link ParsedArguments.Action#VERSION} as soon
   *     as that option is reached, otherwise the last value of each option and the first operand
   * @throws UsageException on the first option syntax error before any {@code --help} or {@code
   *     --version}
   */
  static ParsedArguments parse(List<String> arguments) throws UsageException {
    return new OptionParser(arguments).scan();
  }

  private ParsedArguments scan() throws UsageException {
    boolean optionsEnded = false;
    for (index = 0; index < arguments.size(); index++) {
      String token = arguments.get(index);
      if (optionsEnded || isOperand(token)) {
        firstOperand = firstOperand.or(() -> Optional.of(token));
      } else if (token.equals("--")) {
        optionsEnded = true;
      } else if (token.startsWith("--")) {
        Optional<ParsedArguments> immediate = longOption(token);
        if (immediate.isPresent()) {
          return immediate.get();
        }
      } else {
        shortOption(token);
      }
    }
    return new ParsedArguments(ParsedArguments.Action.GREET, name, count, firstOperand);
  }

  /** CONV-10: a token that does not start with {@code -}, or is exactly {@code -}. */
  private static boolean isOperand(String token) {
    return !token.startsWith("-") || token.equals("-");
  }

  /**
   * Handles {@code --long[=value]} (CONV-08: no abbreviations).
   *
   * @return the help or version result when the scan must stop here
   */
  private Optional<ParsedArguments> longOption(String token) throws UsageException {
    int equals = token.indexOf('=');
    String optionName = equals < 0 ? token.substring(2) : token.substring(2, equals);
    String attached = equals < 0 ? null : token.substring(equals + 1);
    switch (optionName) {
      case "help", "version" -> {
        if (attached != null) {
          throw new UsageException("option '--" + optionName + "' doesn't allow an argument");
        }
        return Optional.of(
            optionName.equals("help") ? ParsedArguments.help() : ParsedArguments.version());
      }
      case "name", "count" -> {
        if (attached == null && !hasNext()) {
          throw new UsageException("option '--" + optionName + "' requires an argument");
        }
        record(optionName.charAt(0), "--" + optionName, attached != null ? attached : next());
        return Optional.empty();
      }
      default -> throw new UsageException("unrecognized option '" + token + "'");
    }
  }

  /**
   * Handles {@code -nNAME}, {@code -n NAME}, {@code -cCOUNT}, {@code -c COUNT} (CONV-07). Both
   * short options take a value, so the rest of the token is always that value.
   */
  private void shortOption(String token) throws UsageException {
    int letter = token.codePointAt(1);
    String spelled = "-" + Character.toString(letter);
    String rest = token.substring(spelled.length());
    if (letter != 'n' && letter != 'c') {
      throw new UsageException("invalid option -- '" + Character.toString(letter) + "'");
    }
    if (rest.isEmpty() && !hasNext()) {
      throw new UsageException(
          "option requires an argument -- '" + Character.toString(letter) + "'");
    }
    record((char) letter, spelled, rest.isEmpty() ? next() : rest);
  }

  private boolean hasNext() {
    return index + 1 < arguments.size();
  }

  private String next() {
    return arguments.get(++index);
  }

  /** Records a value; a later occurrence replaces an earlier one (CONV-11). */
  private void record(char key, String spelled, String value) {
    OptionValue optionValue = new OptionValue(spelled, value);
    if (key == 'n') {
      name = Optional.of(optionValue);
    } else {
      count = Optional.of(optionValue);
    }
  }
}
