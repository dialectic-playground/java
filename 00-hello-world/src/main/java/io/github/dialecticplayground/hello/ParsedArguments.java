package io.github.dialecticplayground.hello;

import java.util.Optional;

/**
 * The result of the left-to-right option scan (CONV-12): what to do, plus the recorded but not yet
 * validated values. Only the last {@code --name} and {@code --count} are kept (CONV-11).
 */
record ParsedArguments(
    Action action,
    Optional<OptionValue> name,
    Optional<OptionValue> count,
    Optional<String> firstOperand) {

  /** What the scan decided: print the help, print the version, or greet. */
  enum Action {
    HELP,
    VERSION,
    GREET
  }

  static ParsedArguments help() {
    return new ParsedArguments(Action.HELP, Optional.empty(), Optional.empty(), Optional.empty());
  }

  static ParsedArguments version() {
    return new ParsedArguments(
        Action.VERSION, Optional.empty(), Optional.empty(), Optional.empty());
  }
}
