package io.github.dialecticplayground.hello;

/**
 * A value given to an option, with the option spelled as the user wrote it ({@code -n} or {@code
 * --name}), which the CONV-13 "invalid argument" diagnostic quotes.
 */
record OptionValue(String option, String value) {}
