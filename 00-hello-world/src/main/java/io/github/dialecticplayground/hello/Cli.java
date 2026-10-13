package io.github.dialecticplayground.hello;

import java.util.List;

/** The pure core of dp-hello: arguments in, {@link Outcome} out. */
final class Cli {
  /** The canonical name used in every message (CONV-01). */
  static final String PROGRAM = "dp-hello";

  /** CONV-02. */
  static final String VERSION = PROGRAM + " (dialectic-playground) 1.0.0\n";

  /** The help text of the user manual, byte for byte (FR-07, CONV-23). */
  static final String HELP =
      """
      Usage: dp-hello [OPTION]...
      Print a greeting to standard output.

      Options:
        -n, --name=NAME     greet NAME instead of World
        -c, --count=COUNT   print the greeting COUNT times (1-1000, default 1)
            --help          display this help and exit
            --version       output version information and exit
      """;

  private Cli() {}

  static Outcome run(List<String> arguments) {
    try {
      ParsedArguments parsed = OptionParser.parse(arguments);
      return switch (parsed.action()) {
        case HELP -> Outcome.success(HELP);
        case VERSION -> Outcome.success(VERSION);
        case GREET -> Outcome.success(Greeting.text(Validator.validate(parsed)));
      };
    } catch (UsageException error) {
      return Outcome.usageError(error);
    }
  }
}
