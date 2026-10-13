package io.github.dialecticplayground.hello;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class GreetingTest {
  @Test
  void lineEndsInLf() {
    assertEquals("Hello, World!\n", Greeting.line("World"));
    assertEquals("Hello, Ada Lovelace!\n", Greeting.line("Ada Lovelace"));
    assertEquals("Hello, Zoë!\n", Greeting.line("Zoë"));
  }

  @Test
  void textRepeatsTheLine() {
    assertEquals("Hello, Ken!\nHello, Ken!\n", Greeting.text(new Config("Ken", 2)));
    assertEquals("Hello, A!\n".repeat(1000), Greeting.text(new Config("A", 1000)));
  }

  @Test
  void configRejectsInvalidValues() {
    assertThrows(IllegalArgumentException.class, () -> new Config("", 1));
    assertThrows(IllegalArgumentException.class, () -> new Config("A", 0));
    assertThrows(IllegalArgumentException.class, () -> new Config("A", 1001));
  }
}
