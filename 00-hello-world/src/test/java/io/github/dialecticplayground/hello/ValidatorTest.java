package io.github.dialecticplayground.hello;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ValidatorTest {
  private static ParsedArguments greet(OptionValue name, OptionValue count, String operand) {
    return new ParsedArguments(
        ParsedArguments.Action.GREET,
        Optional.ofNullable(name),
        Optional.ofNullable(count),
        Optional.ofNullable(operand));
  }

  @Test
  void defaults() throws UsageException {
    assertEquals(new Config("World", 1), Validator.validate(greet(null, null, null)));
  }

  @Test
  void nameIsUsedVerbatim() throws UsageException {
    for (String name : new String[] {"Ada Lovelace", "Zoë", "-x", " ", "日本語", "a'b"}) {
      assertEquals(name, Validator.name(new OptionValue("-n", name)));
    }
  }

  @Test
  void emptyNameIsRejectedWithOptionAsWritten() {
    UsageException error =
        assertThrows(UsageException.class, () -> Validator.name(new OptionValue("--name", "")));
    assertEquals("invalid argument '' for '--name'", error.getMessage());
  }

  @ParameterizedTest
  @ValueSource(strings = {"1", "2", "999", "1000", "007", "0001000", "0000000000000000000001"})
  void validCounts(String text) throws UsageException {
    assertEquals(Integer.parseInt(text), Validator.count(new OptionValue("-c", text)));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "0",
        "000",
        "1001",
        "10000",
        "",
        "abc",
        "-1",
        "+1",
        " 1",
        "1 ",
        "1.0",
        "1e3",
        "٣",
        "99999999999999999999999999"
      })
  void invalidCounts(String text) {
    UsageException error =
        assertThrows(UsageException.class, () -> Validator.count(new OptionValue("-c", text)));
    assertEquals("invalid argument '" + text + "' for '-c'", error.getMessage());
  }

  @Test
  void operandIsRejectedBeforeValues() {
    UsageException error =
        assertThrows(
            UsageException.class,
            () ->
                Validator.validate(
                    greet(new OptionValue("-n", ""), new OptionValue("-c", "0"), "extra")));
    assertEquals("extra operand 'extra'", error.getMessage());
  }

  @Test
  void nameIsCheckedBeforeCount() {
    UsageException error =
        assertThrows(
            UsageException.class,
            () ->
                Validator.validate(
                    greet(new OptionValue("-n", ""), new OptionValue("--count", "x"), null)));
    assertEquals("invalid argument '' for '-n'", error.getMessage());
  }

  @Test
  void validRequest() throws UsageException {
    assertEquals(
        new Config("Ken", 2),
        Validator.validate(greet(new OptionValue("-n", "Ken"), new OptionValue("-c", "2"), null)));
  }
}
