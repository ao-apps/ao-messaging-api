/*
 * ao-messaging-api - Asynchronous bidirectional messaging over various protocols API.
 * Copyright (C) 2014, 2015, 2016, 2017, 2018, 2020, 2021, 2022, 2024, 2025  AO Industries, Inc.
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

import com.aoapps.lang.io.function.IOSupplier;
import com.aoapps.tempfiles.TempFileContext;
import java.io.File;
import java.io.IOException;

/**
 * The types of serializations.
 */
public enum MessageType {

  BYTE_ARRAY {
    @Override
    public byte getTypeByte() {
      return 0;
    }

    @Override
    public char getTypeChar() {
      return 'b';
    }

    @Override
    public ByteArrayMessage decode(String encodedMessage, IOSupplier<TempFileContext> tempFileContextSupplier) {
      return ByteArrayMessage.decode(encodedMessage);
    }

    @Override
    @Deprecated(forRemoval = true)
    public ByteArrayMessage decode(String encodedMessage) {
      return ByteArrayMessage.decode(encodedMessage);
    }

    @Override
    public ByteArrayMessage decode(ByteArray encodedMessage, IOSupplier<TempFileContext> tempFileContextSupplier) {
      return new ByteArrayMessage(encodedMessage);
    }

    @Override
    @Deprecated(forRemoval = true)
    public ByteArrayMessage decode(ByteArray encodedMessage) {
      return new ByteArrayMessage(encodedMessage);
    }
  },
  FILE {
    @Override
    public byte getTypeByte() {
      return 1;
    }

    @Override
    public char getTypeChar() {
      return 'f';
    }

    @Override
    public FileMessage decode(String encodedMessage, IOSupplier<TempFileContext> tempFileContextSupplier) throws IOException {
      if (tempFileContextSupplier == null) {
        throw new IllegalArgumentException(
            "No " + IOSupplier.class.getSimpleName() + "<" + TempFileContext.class.getSimpleName() + ">");
      }
      TempFileContext tempFileContext = tempFileContextSupplier.get();
      if (tempFileContext == null) {
        throw new IllegalStateException("No " + TempFileContext.class.getSimpleName());
      }
      return FileMessage.decode(
          encodedMessage,
          tempFileContext.createTempFile("FileMessage_").getFile()
      );
    }

    @Override
    @Deprecated(forRemoval = true)
    public FileMessage decode(String encodedMessage) throws IOException {
      return FileMessage.decode(encodedMessage);
    }

    @Override
    public FileMessage decode(ByteArray encodedMessage, IOSupplier<TempFileContext> tempFileContextSupplier) throws IOException {
      if (tempFileContextSupplier == null) {
        throw new IllegalArgumentException(
            "No " + IOSupplier.class.getSimpleName() + "<" + TempFileContext.class.getSimpleName() + ">");
      }
      TempFileContext tempFileContext = tempFileContextSupplier.get();
      if (tempFileContext == null) {
        throw new IllegalStateException("No " + TempFileContext.class.getSimpleName());
      }
      return FileMessage.decode(
          encodedMessage,
          tempFileContext.createTempFile("FileMessage_").getFile()
      );
    }

    @Override
    @Deprecated(forRemoval = true)
    public FileMessage decode(ByteArray encodedMessage) throws IOException {
      return FileMessage.decode(encodedMessage);
    }
  },
  STRING {
    @Override
    public byte getTypeByte() {
      return 2;
    }

    @Override
    public char getTypeChar() {
      return 's';
    }

    @Override
    public StringMessage decode(String encodedMessage, IOSupplier<TempFileContext> tempFileContextSupplier) {
      return new StringMessage(encodedMessage);
    }

    @Override
    @Deprecated(forRemoval = true)
    public StringMessage decode(String encodedMessage) {
      return new StringMessage(encodedMessage);
    }

    @Override
    public StringMessage decode(ByteArray encodedMessage, IOSupplier<TempFileContext> tempFileContextSupplier) {
      return StringMessage.decode(encodedMessage);
    }

    @Override
    @Deprecated(forRemoval = true)
    public StringMessage decode(ByteArray encodedMessage) {
      return StringMessage.decode(encodedMessage);
    }
  },
  MULTI {
    @Override
    public byte getTypeByte() {
      return 3;
    }

    @Override
    public char getTypeChar() {
      return 'm';
    }

    @Override
    public MultiMessage decode(String encodedMessage, IOSupplier<TempFileContext> tempFileContextSupplier) throws IOException {
      return MultiMessage.decode(encodedMessage, tempFileContextSupplier);
    }

    @Override
    @Deprecated(forRemoval = true)
    public MultiMessage decode(String encodedMessage) throws IOException {
      return MultiMessage.decode(encodedMessage);
    }

    @Override
    public MultiMessage decode(ByteArray encodedMessage, IOSupplier<TempFileContext> tempFileContextSupplier) throws IOException {
      return MultiMessage.decode(encodedMessage, tempFileContextSupplier);
    }

    @Override
    @Deprecated(forRemoval = true)
    public MultiMessage decode(ByteArray encodedMessage) throws IOException {
      return MultiMessage.decode(encodedMessage);
    }
  };

  /**
   * Gets the message type for its numeric code.
   */
  public static MessageType getFromTypeByte(byte typeByte) {
    switch (typeByte) {
      case 0:
        return BYTE_ARRAY;
      case 1:
        return FILE;
      case 2:
        return STRING;
      case 3:
        return MULTI;
      default:
        throw new IllegalArgumentException("Invalid type byte: " + typeByte);
    }
  }

  /**
   * Gets the message type for its character code.
   */
  public static MessageType getFromTypeChar(char typeChar) {
    switch (typeChar) {
      case 'b':
        return BYTE_ARRAY;
      case 'f':
        return FILE;
      case 's':
        return STRING;
      case 'm':
        return MULTI;
      default:
        throw new IllegalArgumentException("Invalid type char: " + typeChar);
    }
  }

  /**
   * Gets the single byte that uniquely represents this message type.
   */
  public abstract byte getTypeByte();

  /**
   * Gets the single character that uniquely represents this message type.
   */
  public abstract char getTypeChar();

  /**
   * Constructs a message of this type from its string encoding using the provided
   * {@link TempFileContext temporary file context} supplier as-needed.
   */
  public abstract Message decode(String encodedMessage, IOSupplier<TempFileContext> tempFileContextSupplier)
      throws IOException;

  /**
   * Constructs a message of this type from its string encoding using the provided
   * {@link TempFileContext temporary file context} as-needed.
   *
   * @see  #decode(java.lang.String, com.aoapps.lang.io.function.IOSupplier)
   *
   * @deprecated  Please use {@link TempFileContext} supplier which may defer creation until first needed.
   */
  public final Message decode(String encodedMessage, TempFileContext tempFileContext) throws IOException {
    return decode(encodedMessage, () -> tempFileContext);
  }

  /**
   * Constructs a message of this type from its string encoding, possibly using temporary files with
   * {@link File#deleteOnExit()} as-needed.
   *
   * @see  #decode(java.lang.String, com.aoapps.lang.io.function.IOSupplier)
   *
   * @deprecated  Please use {@link TempFileContext} supplier since {@link File#deleteOnExit()} is prone to memory leaks
   *              in long-running applications.
   */
  @Deprecated(forRemoval = true)
  public abstract Message decode(String encodedMessage) throws IOException;

  /**
   * Constructs a message of this type from its byte array encoding using the provided
   * {@link TempFileContext temporary file context} supplier as-needed.
   */
  public abstract Message decode(ByteArray encodedMessage, IOSupplier<TempFileContext> tempFileContextSupplier)
      throws IOException;

  /**
   * Constructs a message of this type from its byte array encoding using the provided
   * {@link TempFileContext temporary file context} as-needed.
   *
   * @see  #decode(com.aoapps.messaging.ByteArray, com.aoapps.lang.io.function.IOSupplier)
   *
   * @deprecated  Please use {@link TempFileContext} supplier which may defer creation until first needed.
   */
  public final Message decode(ByteArray encodedMessage, TempFileContext tempFileContext) throws IOException {
    return decode(encodedMessage, () -> tempFileContext);
  }

  /**
   * Constructs a message of this type from its byte array encoding, possibly using temporary files with
   * {@link File#deleteOnExit()} as-needed.
   *
   * @see  #decode(com.aoapps.messaging.ByteArray, com.aoapps.lang.io.function.IOSupplier)
   *
   * @deprecated  Please use {@link TempFileContext} supplier since {@link File#deleteOnExit()} is prone to memory leaks
   *              in long-running applications.
   */
  @Deprecated(forRemoval = true)
  public abstract Message decode(ByteArray encodedMessage) throws IOException;
}
