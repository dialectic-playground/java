package io.github.dialecticplayground.hello;

/**
 * A validated greeting request.
 *
 * @param name the greeted name, never empty (FR-03)
 * @param count how many greeting lines to print, from {@link #MIN_COUNT} to {@link #MAX_COUNT}
 *     (FR-04)
 */
record Config(String name, int count) {
  static final String DEFAULT_NAME = "World";
  static final int DEFAULT_COUNT = 1;
  static final int MIN_COUNT = 1;
  static final int MAX_COUNT = 1000;

  Config {
    if (name.isEmpty() || count < MIN_COUNT || count > MAX_COUNT) {
      throw new IllegalArgumentException("invalid Config: " + name + ", " + count);
    }
  }
}
