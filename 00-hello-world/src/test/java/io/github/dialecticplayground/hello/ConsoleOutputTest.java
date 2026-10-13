package io.github.dialecticplayground.hello;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;

class ConsoleOutputTest {
  @Test
  void writesUtf8BytesWithLfOnly() {
    ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    int status = ConsoleOutput.emit(new Outcome("Hello, Zoë!\n", "", 0), stdout, stderr);
    assertEquals(0, status);
    assertArrayEquals(
        new byte[] {
          'H', 'e', 'l', 'l', 'o', ',', ' ', 'Z', 'o', (byte) 0xC3, (byte) 0xAB, '!', '\n'
        },
        stdout.toByteArray());
    assertEquals(0, stderr.size());
  }

  @Test
  void usageErrorGoesToStderrWithItsStatus() {
    ByteArrayOutputStream stdout = new ByteArrayOutputStream();
    ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    int status = ConsoleOutput.emit(new Outcome("", "dp-hello: x\n", 2), stdout, stderr);
    assertEquals(2, status);
    assertEquals(0, stdout.size());
    assertArrayEquals(
        "dp-hello: x\n".getBytes(java.nio.charset.StandardCharsets.UTF_8), stderr.toByteArray());
  }

  @Test
  void brokenPipeEndsQuietly() {
    OutputStream closedPipe =
        new OutputStream() {
          @Override
          public void write(int b) throws IOException {
            throw new IOException("Broken pipe");
          }
        };
    ByteArrayOutputStream stderr = new ByteArrayOutputStream();
    int status = ConsoleOutput.emit(new Outcome("Hello, World!\n", "", 0), closedPipe, stderr);
    assertEquals(ConsoleOutput.WRITE_FAILED, status);
    assertEquals(0, stderr.size());
  }
}
