package io.github.dialecticplayground.hello;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class CliTest {
  private static final String TRY = "Try 'dp-hello --help' for more information.\n";

  private static Outcome run(String... arguments) {
    return Cli.run(List.of(arguments));
  }

  private static Outcome usage(String message) {
    return new Outcome("", "dp-hello: " + message + "\n" + TRY, 2);
  }

  @Test
  void greetings() {
    assertEquals(new Outcome("Hello, World!\n", "", 0), run());
    assertEquals(new Outcome("Hello, Zoë!\n", "", 0), run("--name", "Zoë"));
    assertEquals(new Outcome("Hello, Ken!\nHello, Ken!\n", "", 0), run("-c2", "-n", "Ken"));
  }

  @Test
  void helpMatchesTheUserManual() {
    String help =
        "Usage: dp-hello [OPTION]...\n"
            + "Print a greeting to standard output.\n"
            + "\n"
            + "Options:\n"
            + "  -n, --name=NAME     greet NAME instead of World\n"
            + "  -c, --count=COUNT   print the greeting COUNT times (1-1000, default 1)\n"
            + "      --help          display this help and exit\n"
            + "      --version       output version information and exit\n";
    assertEquals(new Outcome(help, "", 0), run("--help"));
    assertEquals(new Outcome(help, "", 0), run("extra", "--help"));
    assertEquals(new Outcome(help, "", 0), run("--count", "0", "--help"));
  }

  @Test
  void version() {
    assertEquals(new Outcome("dp-hello (dialectic-playground) 1.0.0\n", "", 0), run("--version"));
  }

  @Test
  void usageErrorsWriteNothingToStdout() {
    assertEquals(usage("invalid option -- 'x'"), run("-x", "--help"));
    assertEquals(usage("unrecognized option '--nme'"), run("--nme", "Ada"));
    assertEquals(usage("option '--name' requires an argument"), run("--name"));
    assertEquals(usage("option '--version' doesn't allow an argument"), run("--version=1"));
    assertEquals(usage("invalid argument '' for '--name'"), run("--name", ""));
    assertEquals(usage("invalid argument '1001' for '-c'"), run("-c", "1001"));
    assertEquals(usage("extra operand 'extra'"), run("extra"));
    assertEquals(usage("extra operand '-n'"), run("--", "-n"));
  }
}
