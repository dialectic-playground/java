package io.github.dialecticplayground.hello;

/** Greeting construction: pure functions with no I/O (NFR-05). */
final class Greeting {
  private Greeting() {}

  /** {@code Hello, NAME!} followed by LF (SRS §1.3). */
  static String line(String name) {
    return "Hello, " + name + "!\n";
  }

  /** The complete standard output for {@code config}: COUNT identical greeting lines (FR-04). */
  static String text(Config config) {
    return line(config.name()).repeat(config.count());
  }
}
