package io.github.dialecticplayground.hello;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OptionParserTest {
  private static ParsedArguments parse(String... arguments) throws UsageException {
    return OptionParser.parse(List.of(arguments));
  }

  private static String syntaxError(String... arguments) {
    return assertThrows(UsageException.class, () -> parse(arguments)).getMessage();
  }

  private static ParsedArguments greet(OptionValue name, OptionValue count, String firstOperand) {
    return new ParsedArguments(
        ParsedArguments.Action.GREET,
        Optional.ofNullable(name),
        Optional.ofNullable(count),
        Optional.ofNullable(firstOperand));
  }

  @Test
  void noArgumentsRecordsNothing() throws UsageException {
    assertEquals(greet(null, null, null), parse());
  }

  @ParameterizedTest
  @CsvSource({"-n,Ada,-n", "--name,Ada,--name"})
  void separateValue(String option, String value, String spelled) throws UsageException {
    assertEquals(greet(new OptionValue(spelled, value), null, null), parse(option, value));
  }

  @Test
  void attachedValues() throws UsageException {
    assertEquals(
        greet(new OptionValue("-n", "Ken"), new OptionValue("-c", "2"), null),
        parse("-c2", "-nKen"));
    assertEquals(
        greet(new OptionValue("--name", "Linus"), new OptionValue("--count", "3"), null),
        parse("--name=Linus", "--count=3"));
  }

  @Test
  void valuesMayStartWithDashOrBeEmpty() throws UsageException {
    assertEquals(greet(new OptionValue("--name", "-x"), null, null), parse("--name=-x"));
    assertEquals(greet(new OptionValue("-n", "-x"), null, null), parse("-n-x"));
    assertEquals(greet(new OptionValue("--name", "-x"), null, null), parse("--name", "-x"));
    assertEquals(greet(new OptionValue("-n", "--help"), null, null), parse("-n", "--help"));
    assertEquals(greet(new OptionValue("--name", ""), null, null), parse("--name="));
    assertEquals(greet(new OptionValue("-n", ""), null, null), parse("-n", ""));
    assertEquals(greet(new OptionValue("--name", "a=b"), null, null), parse("--name=a=b"));
  }

  @Test
  void bundledShortOptionConsumesRestOfToken() throws UsageException {
    assertEquals(greet(null, new OptionValue("-c", "n5"), null), parse("-cn5"));
  }

  @Test
  void lastOccurrenceWins() throws UsageException {
    assertEquals(
        greet(new OptionValue("--name", "B"), new OptionValue("-c", "0"), null),
        parse("-n", "A", "-c", "0", "--name", "B", "-c0"));
  }

  @Test
  void operandsAreCollectedNotRejected() throws UsageException {
    assertEquals(greet(null, null, "extra"), parse("extra", "more"));
    assertEquals(greet(null, null, "-"), parse("-"));
  }

  @Test
  void terminatorMakesEverythingAfterItAnOperand() throws UsageException {
    assertEquals(greet(null, null, "-n"), parse("--", "-n", "--help"));
    assertEquals(greet(null, null, "--"), parse("--", "--"));
    assertEquals(greet(null, null, null), parse("--"));
  }

  @Test
  void helpAndVersionStopTheScan() throws UsageException {
    assertEquals(ParsedArguments.help(), parse("--help", "extra", "-x"));
    assertEquals(ParsedArguments.version(), parse("--version", "--help"));
    assertEquals(ParsedArguments.help(), parse("extra", "--help"));
    assertEquals(ParsedArguments.help(), parse("--count", "0", "--help"));
    assertEquals(ParsedArguments.version(), parse("-n", "", "--version"));
  }

  @Test
  void syntaxErrorBeforeHelpWins() {
    assertEquals("invalid option -- 'x'", syntaxError("-x", "--help"));
  }

  @Test
  void unknownShortOption() {
    assertEquals("invalid option -- 'x'", syntaxError("-x"));
    assertEquals("invalid option -- 'h'", syntaxError("-h"));
    assertEquals("invalid option -- 'x'", syntaxError("-xn", "Ada"));
    assertEquals("invalid option -- 'ë'", syntaxError("-ë"));
    assertEquals("invalid option -- '😀'", syntaxError("-😀"));
  }

  @Test
  void unknownLongOptionQuotesWholeToken() {
    assertEquals("unrecognized option '--nme'", syntaxError("--nme", "Ada"));
    assertEquals("unrecognized option '--nam'", syntaxError("--nam"));
    assertEquals("unrecognized option '--x=1'", syntaxError("--x=1"));
    assertEquals("unrecognized option '---name'", syntaxError("---name"));
    assertEquals("unrecognized option '--=x'", syntaxError("--=x"));
  }

  @Test
  void missingArguments() {
    assertEquals("option requires an argument -- 'n'", syntaxError("-n"));
    assertEquals("option requires an argument -- 'c'", syntaxError("-c"));
    assertEquals("option '--name' requires an argument", syntaxError("--name"));
    assertEquals("option '--count' requires an argument", syntaxError("--count"));
  }

  @Test
  void flagsRejectValues() {
    assertEquals("option '--help' doesn't allow an argument", syntaxError("--help="));
    assertEquals("option '--version' doesn't allow an argument", syntaxError("--version=1"));
  }
}
