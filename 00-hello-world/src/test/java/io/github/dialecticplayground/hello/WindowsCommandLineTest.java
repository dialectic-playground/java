package io.github.dialecticplayground.hello;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

class WindowsCommandLineTest {
  @Test
  void takesTheProgramArgumentsFromTheEndOfTheWideVector() {
    List<String> wide = List.of("C:\\java.exe", "-jar", "C:\\dp-hello", "-n", "日本語");
    assertEquals(List.of("-n", "日本語"), WindowsCommandLine.reconcile(List.of("-n", "???"), wide));
  }

  @Test
  void keepsTheLauncherArgumentsWhenTheVectorsDisagree() {
    List<String> decoded = List.of("-n", "a.txt", "b.txt");
    List<String> wide = List.of("java.exe", "-jar", "dp-hello", "-n", "*");
    assertEquals(decoded, WindowsCommandLine.reconcile(decoded, wide));
    assertEquals(decoded, WindowsCommandLine.reconcile(decoded, List.of("-n", "a.txt", "b")));
  }

  @Test
  void noArguments() {
    assertEquals(List.of(), WindowsCommandLine.reconcile(List.of(), List.of("java.exe")));
  }

  @Test
  void argumentsPassThroughUnchangedOffWindows() {
    if (!System.getProperty("os.name").startsWith("Windows")) {
      assertEquals(List.of("-n", "Zoë"), WindowsCommandLine.arguments(new String[] {"-n", "Zoë"}));
    }
  }

  /** End to end on Windows: a name outside the ANSI code page survives (CONV-05). */
  @Test
  @EnabledOnOs(OS.WINDOWS)
  void unicodeNameSurvivesTheWindowsLauncher() throws Exception {
    String java = Path.of(System.getProperty("java.home"), "bin", "java.exe").toString();
    Process process =
        new ProcessBuilder(
                java,
                "--enable-native-access=ALL-UNNAMED",
                "-cp",
                Path.of(DpHello.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .toString(),
                DpHello.class.getName(),
                "--name",
                "Zoë 日本語 😀")
            .start();
    byte[] stdout = process.getInputStream().readAllBytes();
    byte[] stderr = process.getErrorStream().readAllBytes();
    assertEquals(0, process.waitFor());
    assertArrayEquals("Hello, Zoë 日本語 😀!\n".getBytes(StandardCharsets.UTF_8), stdout);
    assertEquals("", new String(stderr, StandardCharsets.UTF_8));
  }
}
