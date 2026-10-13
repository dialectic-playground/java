package io.github.dialecticplayground.hello;

import java.io.FileDescriptor;
import java.io.FileOutputStream;

/** Entry point: the thin I/O shell around {@link Cli#run}. */
public final class DpHello {
  private DpHello() {}

  /**
   * Runs dp-hello.
   *
   * @param args the arguments as decoded by the java launcher
   */
  public static void main(String[] args) {
    Outcome outcome = Cli.run(WindowsCommandLine.arguments(args));
    int status =
        ConsoleOutput.emit(
            outcome,
            new FileOutputStream(FileDescriptor.out),
            new FileOutputStream(FileDescriptor.err));
    System.exit(status);
  }
}
