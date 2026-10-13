package io.github.dialecticplayground.hello;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Writes an {@link Outcome} as UTF-8 bytes. The streams are raw byte streams, so the bytes reach
 * the file descriptors unchanged on every OS: no platform charset, no CRLF translation (CONV-03,
 * CONV-05).
 */
final class ConsoleOutput {
  /**
   * Exit status after a failed write, such as a closed pipe (CONV-25: no diagnostic, status
   * unspecified). 141 is what shells report for a process killed by SIGPIPE.
   */
  static final int WRITE_FAILED = 141;

  private ConsoleOutput() {}

  /** Writes the outcome and returns the exit status to use. */
  static int emit(Outcome outcome, OutputStream stdout, OutputStream stderr) {
    try {
      write(stdout, outcome.stdout());
      write(stderr, outcome.stderr());
      return outcome.exitStatus();
    } catch (IOException writeFailed) {
      return WRITE_FAILED;
    }
  }

  private static void write(OutputStream stream, String text) throws IOException {
    if (!text.isEmpty()) {
      stream.write(text.getBytes(StandardCharsets.UTF_8));
      stream.flush();
    }
  }
}
