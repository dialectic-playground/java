package io.github.dialecticplayground.hello;

/**
 * Everything one invocation produces: the text for standard output and standard error, and the exit
 * status. {@link Cli#run} computes it without side effects; {@link ConsoleOutput} emits it.
 */
record Outcome(String stdout, String stderr, int exitStatus) {
  static final int SUCCESS = 0;
  static final int USAGE_ERROR = 2;

  static Outcome success(String stdout) {
    return new Outcome(stdout, "", SUCCESS);
  }

  static Outcome usageError(UsageException error) {
    return new Outcome("", error.diagnostic(), USAGE_ERROR);
  }
}
