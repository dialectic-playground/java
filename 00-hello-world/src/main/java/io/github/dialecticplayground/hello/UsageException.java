package io.github.dialecticplayground.hello;

/**
 * A usage error (CONV-13). The message is the diagnostic without the {@code dp-hello: } prefix;
 * {@link #diagnostic()} renders the complete two-line CONV-13/CONV-14 text.
 */
final class UsageException extends Exception {
  private static final long serialVersionUID = 1L;

  UsageException(String message) {
    super(message, null, false, false);
  }

  static UsageException invalidArgument(OptionValue option) {
    return new UsageException(
        "invalid argument '" + option.value() + "' for '" + option.option() + "'");
  }

  String diagnostic() {
    return Cli.PROGRAM
        + ": "
        + getMessage()
        + "\nTry '"
        + Cli.PROGRAM
        + " --help' for more information.\n";
  }
}
