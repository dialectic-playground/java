package io.github.dialecticplayground.hello;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The command-line arguments as Unicode text on every OS (CONV-05).
 *
 * <p>On Windows the java launcher decodes the command line with the ANSI code page, so characters
 * outside it arrive as {@code ?}. There the arguments are read again from the UTF-16 command line
 * ({@code GetCommandLineW} and {@code CommandLineToArgvW}, through java.lang.foreign). Elsewhere,
 * and whenever that fails, the launcher's arguments are used as they are.
 */
final class WindowsCommandLine {
  private WindowsCommandLine() {}

  static List<String> arguments(String[] launcherArguments) {
    List<String> decoded = List.of(launcherArguments);
    if (!System.getProperty("os.name", "").startsWith("Windows")) {
      return decoded;
    }
    return wideArguments().map(wide -> reconcile(decoded, wide)).orElse(decoded);
  }

  /**
   * Selects the program's arguments from the full UTF-16 argument vector: they are its last {@code
   * decoded.size()} entries, after the java executable and its options. Falls back to {@code
   * decoded} unless every pure-ASCII wide argument equals the launcher's version of it, which would
   * not hold if the two parsers had split the command line differently.
   */
  static List<String> reconcile(List<String> decoded, List<String> wide) {
    int launcherTokens = wide.size() - decoded.size();
    if (launcherTokens < 1) {
      return decoded;
    }
    List<String> tail = wide.subList(launcherTokens, wide.size());
    for (int i = 0; i < decoded.size(); i++) {
      String candidate = tail.get(i);
      if (isAscii(candidate) && !candidate.equals(decoded.get(i))) {
        return decoded;
      }
    }
    return List.copyOf(tail);
  }

  private static boolean isAscii(String text) {
    return text.chars().allMatch(c -> c < 0x80);
  }

  /** The process's UTF-16 command line split by {@code CommandLineToArgvW}, if available. */
  @SuppressWarnings("restricted")
  private static Optional<List<String>> wideArguments() {
    Linker linker = Linker.nativeLinker();
    try (Arena arena = Arena.ofConfined()) {
      SymbolLookup kernel32 = SymbolLookup.libraryLookup("kernel32.dll", arena);
      SymbolLookup shell32 = SymbolLookup.libraryLookup("shell32.dll", arena);
      MethodHandle getCommandLine =
          linker.downcallHandle(
              kernel32.findOrThrow("GetCommandLineW"), FunctionDescriptor.of(ADDRESS));
      MethodHandle commandLineToArgv =
          linker.downcallHandle(
              shell32.findOrThrow("CommandLineToArgvW"),
              FunctionDescriptor.of(ADDRESS, ADDRESS, ADDRESS));
      MethodHandle localFree =
          linker.downcallHandle(
              kernel32.findOrThrow("LocalFree"), FunctionDescriptor.of(ADDRESS, ADDRESS));

      MemorySegment count = arena.allocate(JAVA_INT);
      MemorySegment commandLine = (MemorySegment) getCommandLine.invokeExact();
      MemorySegment argv = (MemorySegment) commandLineToArgv.invokeExact(commandLine, count);
      if (argv.equals(MemorySegment.NULL)) {
        return Optional.empty();
      }
      try {
        int argc = count.get(JAVA_INT, 0);
        MemorySegment pointers = argv.reinterpret(ADDRESS.byteSize() * argc);
        List<String> arguments = new ArrayList<>(argc);
        for (int i = 0; i < argc; i++) {
          MemorySegment argument = pointers.getAtIndex(ADDRESS, i).reinterpret(Long.MAX_VALUE);
          arguments.add(argument.getString(0, StandardCharsets.UTF_16LE));
        }
        return Optional.of(arguments);
      } finally {
        MemorySegment unused = (MemorySegment) localFree.invokeExact(argv);
      }
    } catch (Throwable unavailable) {
      // invokeExact declares Throwable. Any failure here (no native access, missing symbol)
      // leaves the launcher's decoding in place rather than crashing (CONV-24).
      return Optional.empty();
    }
  }
}
