/*
 * ao-messaging-api - Asynchronous bidirectional messaging over various protocols API.
 * Copyright (C) 2014, 2015, 2016, 2017, 2019, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.collections.AoCollections;
import com.aoapps.lang.io.AoByteArrayInputStream;
import com.aoapps.lang.io.AoByteArrayOutputStream;
import com.aoapps.tempfiles.TempFileContext;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * A message that is a combination of multiple messages.
 */
public class MultiMessage implements Message {

  private static final char DELIMITER = ',';

  public static final MultiMessage EMPTY_MULTI_MESSAGE = new MultiMessage(Collections.emptyList(), null);

  /**
   * Decodes the messages using the provided {@link TempFileContext temporary file context}.
   */
  public static MultiMessage decode(String encodedMessages, TempFileContext tempFileContext) throws IOException {
    if (encodedMessages.isEmpty()) {
      return EMPTY_MULTI_MESSAGE;
    }

    int pos = encodedMessages.indexOf(DELIMITER);
    if (pos == -1) {
      throw new IllegalArgumentException("Delimiter not found");
    }
    final int size = Integer.parseInt(encodedMessages.substring(0, pos++));
    List<Message> decodedMessages = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      MessageType type = MessageType.getFromTypeChar(encodedMessages.charAt(pos++));
      int nextPos = encodedMessages.indexOf(DELIMITER, pos);
      if (nextPos == -1) {
        throw new IllegalArgumentException("Delimiter not found");
      }
      final int capacity = Integer.parseInt(encodedMessages.substring(pos, nextPos++));
      pos = nextPos + capacity;
      decodedMessages.add(type.decode(encodedMessages.substring(nextPos, pos), tempFileContext));
    }
    if (pos != encodedMessages.length()) {
      throw new IllegalArgumentException("pos != encodedMessages.length()");
    }
    return new MultiMessage(AoCollections.optimalUnmodifiableList(decodedMessages), null);
  }

  /**
   * Decodes the messages, possibly using temporary files with {@link File#deleteOnExit()}.
   *
   * @see  #decode(java.lang.String, com.aoapps.tempfiles.TempFileContext)
   *
   * @deprecated  Please use {@link TempFileContext}
   *              as {@link File#deleteOnExit()} is prone to memory leaks in long-running applications.
   */
  @Deprecated
  public static MultiMessage decode(String encodedMessages) throws IOException {
    if (encodedMessages.isEmpty()) {
      return EMPTY_MULTI_MESSAGE;
    }

    int pos = encodedMessages.indexOf(DELIMITER);
    if (pos == -1) {
      throw new IllegalArgumentException("Delimiter not found");
    }
    final int size = Integer.parseInt(encodedMessages.substring(0, pos++));
    List<Message> decodedMessages = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      MessageType type = MessageType.getFromTypeChar(encodedMessages.charAt(pos++));
      int nextPos = encodedMessages.indexOf(DELIMITER, pos);
      if (nextPos == -1) {
        throw new IllegalArgumentException("Delimiter not found");
      }
      final int capacity = Integer.parseInt(encodedMessages.substring(pos, nextPos++));
      pos = nextPos + capacity;
      decodedMessages.add(type.decode(encodedMessages.substring(nextPos, pos)));
    }
    if (pos != encodedMessages.length()) {
      throw new IllegalArgumentException("pos != encodedMessages.length()");
    }
    return new MultiMessage(AoCollections.optimalUnmodifiableList(decodedMessages), null);
  }

  /**
   * Decodes the messages using the provided {@link TempFileContext temporary file context}.
   */
  public static MultiMessage decode(ByteArray encodedMessages, TempFileContext tempFileContext) throws IOException {
    if (encodedMessages.size == 0) {
      return EMPTY_MULTI_MESSAGE;
    }

    try (DataInputStream in = new DataInputStream(new AoByteArrayInputStream(encodedMessages.array))) {
      int totalRead = 0;
      final int size = in.readInt();
      totalRead += 4;
      List<Message> decodedMessages = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        final MessageType type = MessageType.getFromTypeByte(in.readByte());
        totalRead++;
        final int capacity = in.readInt();
        totalRead += 4;
        byte[] encodedMessage = new byte[capacity];
        in.readFully(encodedMessage, 0, capacity);
        totalRead += capacity;
        decodedMessages.add(type.decode(new ByteArray(encodedMessage, capacity), tempFileContext));
      }
      if (totalRead != encodedMessages.size) {
        throw new IllegalArgumentException("totalRead != encodedMessages.size");
      }
      return new MultiMessage(AoCollections.optimalUnmodifiableList(decodedMessages), null);
    }
  }

  /**
   * Decodes the messages, possibly using temporary files with {@link File#deleteOnExit()}.
   *
   * @see  #decode(com.aoapps.messaging.ByteArray, com.aoapps.tempfiles.TempFileContext)
   *
   * @deprecated  Please use {@link TempFileContext}
   *              as {@link File#deleteOnExit()} is prone to memory leaks in long-running applications.
   */
  @Deprecated
  public static MultiMessage decode(ByteArray encodedMessages) throws IOException {
    if (encodedMessages.size == 0) {
      return EMPTY_MULTI_MESSAGE;
    }

    try (DataInputStream in = new DataInputStream(new AoByteArrayInputStream(encodedMessages.array))) {
      int totalRead = 0;
      final int size = in.readInt();
      totalRead += 4;
      List<Message> decodedMessages = new ArrayList<>(size);
      for (int i = 0; i < size; i++) {
        final MessageType type = MessageType.getFromTypeByte(in.readByte());
        totalRead++;
        final int capacity = in.readInt();
        totalRead += 4;
        byte[] encodedMessage = new byte[capacity];
        in.readFully(encodedMessage, 0, capacity);
        totalRead += capacity;
        decodedMessages.add(type.decode(new ByteArray(encodedMessage, capacity)));
      }
      if (totalRead != encodedMessages.size) {
        throw new IllegalArgumentException("totalRead != encodedMessages.size");
      }
      return new MultiMessage(AoCollections.optimalUnmodifiableList(decodedMessages), null);
    }
  }

  private final Collection<? extends Message> unmodifiableMessages;

  /**
   * Creates a new multi-message.
   *
   * @param  messages  The unmodifiable messages, no defensive copy if performed
   */
  private MultiMessage(Collection<? extends Message> unmodifiableMessages, Void unused) {
    this.unmodifiableMessages = unmodifiableMessages;
  }

  /**
   * Creates a new multi-message.
   *
   * @param  messages  The messages or {@code null} for none, defensive copy if performed
   */
  public MultiMessage(Collection<? extends Message> messages) {
    this((messages == null) ? Collections.emptyList() : AoCollections.unmodifiableCopyCollection(messages), null);
  }

  @Override
  public String toString() {
    return "MultiMessage(" + unmodifiableMessages.size() + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MultiMessage)) {
      return false;
    }
    MultiMessage other = (MultiMessage) o;
    return AoCollections.equals(unmodifiableMessages, other.unmodifiableMessages);
  }

  @Override
  public int hashCode() {
    return AoCollections.hashCode(unmodifiableMessages);
  }

  @Override
  public MessageType getMessageType() {
    return MessageType.MULTI;
  }

  /**
   * Encodes the messages into a single string.
   */
  @Override
  public String encodeAsString() throws IOException {
    final int size = unmodifiableMessages.size();
    if (size == 0) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    sb.append(size).append(DELIMITER);
    int count = 0;
    for (Message message : unmodifiableMessages) {
      count++;
      String str = message.encodeAsString();
      sb
          .append(message.getMessageType().getTypeChar())
          .append(str.length())
          .append(DELIMITER)
          .append(str);
    }
    if (count != size) {
      throw new ConcurrentModificationException();
    }
    return sb.toString();
  }

  /**
   * Encodes the messages into a single ByteArray.
   * There is likely a more efficient implementation that reads-through, but this
   * is a simple implementation.
   */
  @Override
  public ByteArray encodeAsByteArray() throws IOException {
    final int size = unmodifiableMessages.size();
    if (size == 0) {
      return ByteArray.EMPTY_BYTE_ARRAY;
    }

    AoByteArrayOutputStream bout = new AoByteArrayOutputStream();
    try {
      try (DataOutputStream out = new DataOutputStream(bout)) {
        out.writeInt(size);
        int count = 0;
        for (Message message : unmodifiableMessages) {
          count++;
          ByteArray byteArray = message.encodeAsByteArray();
          final int capacity = byteArray.size;
          out.writeByte(message.getMessageType().getTypeByte());
          out.writeInt(capacity);
          out.write(byteArray.array, 0, capacity);
        }
        if (count != size) {
          throw new ConcurrentModificationException();
        }
      }
    } finally {
      bout.close();
    }
    return new ByteArray(bout.getInternalByteArray(), bout.size());
  }

  /**
   * Closes each of the underlying messages.
   */
  @Override
  public void close() throws IOException {
    for (Message message : unmodifiableMessages) {
      message.close();
    }
  }

  /**
   * Gets the messages or empty list for none.
   */
  @SuppressWarnings("ReturnOfCollectionOrArrayField") // Returning unmodifiable
  public Collection<? extends Message> getMessages() {
    return unmodifiableMessages;
  }
}
