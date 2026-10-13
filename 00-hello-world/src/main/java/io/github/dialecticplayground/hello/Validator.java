package io.github.dialecticplayground.hello;

import java.util.Optional;

/**
 * Turns the recorded scan results into a {@link Config} (FR-03, FR-05, FR-09). It runs only after
 * the whole scan (CONV-12) and checks only the winning values (FR-06): operands first, then the
 * name, then the count.
 */
final class Validator {
  private static final int MAX_COUNT_DIGITS = String.valueOf(Config.MAX_COUNT).length();

  private Validator() {}

  static Config validate(ParsedArguments parsed) throws UsageException {
    Optional<String> operand = parsed.firstOperand();
    if (operand.isPresent()) {
      throw new UsageException("extra operand '" + operand.get() + "'");
    }
    String name = parsed.name().isPresent() ? name(parsed.name().get()) : Config.DEFAULT_NAME;
    int count = parsed.count().isPresent() ? count(parsed.count().get()) : Config.DEFAULT_COUNT;
    return new Config(name, count);
  }

  /** The name may be anything except the empty string (FR-03). */
  static String name(OptionValue option) throws UsageException {
    if (option.value().isEmpty()) {
      throw UsageException.invalidArgument(option);
    }
    return option.value();
  }

  /** ASCII digits only, leading zeros allowed, value 1 to 1000 (FR-04, FR-05). */
  static int count(OptionValue option) throws UsageException {
    String text = option.value();
    if (text.isEmpty() || !text.chars().allMatch(c -> c >= '0' && c <= '9')) {
      throw UsageException.invalidArgument(option);
    }
    // Leading zeros are stripped before parsing, so an arbitrarily long digit string never
    // overflows: anything with more significant digits than MAX_COUNT is out of range.
    String significant = text.replaceFirst("^0+", "");
    if (significant.length() > MAX_COUNT_DIGITS) {
      throw UsageException.invalidArgument(option);
    }
    int value = significant.isEmpty() ? 0 : Integer.parseInt(significant);
    if (value < Config.MIN_COUNT || value > Config.MAX_COUNT) {
      throw UsageException.invalidArgument(option);
    }
    return value;
  }
}
