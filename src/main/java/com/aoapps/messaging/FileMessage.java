/*
 * ao-messaging-api - Asynchronous bidirectional messaging over various protocols API.
 * Copyright (C) 2014, 2015, 2016, 2017, 2019, 2020, 2021, 2022, 2024, 2025, 2026  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-messaging-api.
 *
 * ao-messaging-api is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-messaging-api is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-messaging-api.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.aoapps.messaging;

import com.aoapps.hodgepodge.util.Base64Coder;
import com.aoapps.lang.io.AoByteArrayOutputStream;
import com.aoapps.lang.io.FileUtils;
import com.aoapps.lang.io.IoUtils;
import com.aoapps.tempfiles.TempFileContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;

/**
 * A message that is a file.
 */
public class FileMessage implements Message {

  /**
   * Base-64 decodes the message into the provided file.
   *
   * @see #decode(com.aoapps.messaging.ByteArray, java.io.File)
   */
  public static FileMessage decode(String encodedMessage, File file) throws IOException {
    return decode(
        encodedMessage.isEmpty()
            ? ByteArray.EMPTY_BYTE_ARRAY
            : new ByteArray(
            Base64Coder.decode(
                encodedMessage
            )
        ),
        file
    );
  }

  /**
   * Base-64 decodes the message into a temp file.
   *
   * @see  #decode(java.lang.String, java.io.File)
   *
   * @deprecated  Please use {@link TempFileContext} supplier since {@link File#deleteOnExit()} is prone to memory leaks
   *              in long-running applications.
   */
  @Deprecated(forRemoval = true)
  public static FileMessage decode(String encodedMessage) throws IOException {
    return decode(
        encodedMessage.isEmpty()
            ? ByteArray.EMPTY_BYTE_ARRAY
            : new ByteArray(
            Base64Coder.decode(
                encodedMessage
            )
        )
    );
  }

  /**
   * Restores this message into the provided file.
   *
   * @see  #decode(java.lang.String, java.io.File)
   */
  public static FileMessage decode(ByteArray encodedMessage, File file) throws IOException {
    try (OutputStream out = new FileOutputStream(file)) {
      out.write(encodedMessage.array, 0, encodedMessage.size);
    }
    return new FileMessage(true, file);
  }

  /**
   * Restores this message into a temp file.
   *
   * @see  #decode(com.aoapps.messaging.ByteArray, java.io.File)
   *
   * @deprecated  Please use {@link TempFileContext} supplier since {@link File#deleteOnExit()} is prone to memory leaks
   *              in long-running applications.
   */
  @Deprecated(forRemoval = true)
  public static FileMessage decode(ByteArray encodedMessage) throws IOException {
    File file = Files.createTempFile("FileMessage.", null).toFile();
    file.deleteOnExit();
    return decode(encodedMessage, file);
  }

  private final boolean isTemp;
  private final Object lock = new Object();
  private File file;

  /**
   * Creates a new {@link FileMessage}.
   */
  public FileMessage(File file) {
    this(false, file);
  }

  private FileMessage(boolean isTemp, File file) {
    this.isTemp = isTemp;
    this.file = file;
  }

  @Override
  public String toString() {
    return "FileMessage(\"" + file.getPath() + "\")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FileMessage)) {
      return false;
    }
    FileMessage other = (FileMessage) o;
    try {
      return FileUtils.contentEquals(file, other.file);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public int hashCode() {
    try {
      return FileUtils.contentHashCode(file);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.FILE;
  }

  /**
   * Base-64 encodes the message.
   */
  @Override
  public String encodeAsString() throws IOException {
    ByteArray byteArray = encodeAsByteArray();
    if (byteArray.size == 0) {
      return "";
    }
    return new String(Base64Coder.encode(byteArray.array, byteArray.size));
  }

  @Override
  public ByteArray encodeAsByteArray() throws IOException {
    long len = file.length();
    try (InputStream in = new FileInputStream(file)) {
      AoByteArrayOutputStream bout = new AoByteArrayOutputStream(len > 0 && len <= Integer.MAX_VALUE ? (int) len : 32);
      try {
        IoUtils.copy(in, bout);
      } finally {
        bout.close();
      }
      return new ByteArray(bout.getInternalByteArray(), bout.size());
    }
  }

  @Override
  public void close() throws IOException {
    synchronized (lock) {
      if (isTemp && file != null) {
        Files.delete(file.toPath());
        file = null;
      }
    }
  }

  public boolean isTemp() {
    return isTemp;
  }

  public File getMessage() {
    return file;
  }
}
