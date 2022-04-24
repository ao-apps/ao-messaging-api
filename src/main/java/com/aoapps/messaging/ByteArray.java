/*
 * ao-messaging-api - Asynchronous bidirectional messaging over various protocols API.
 * Copyright (C) 2014, 2015, 2016, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.collections.AoArrays;
import com.aoapps.lang.EmptyArrays;

/**
 * Encapsulates a byte[] and the number of bytes used.
 */
public class ByteArray {

  public static final ByteArray EMPTY_BYTE_ARRAY = new ByteArray(EmptyArrays.EMPTY_BYTE_ARRAY);

  public final byte[] array;
  public final int size;

  public ByteArray(byte[] array) {
    this(array, array.length);
  }

  public ByteArray(byte[] array, int size) {
    this.array = array;
    this.size = size;
    assert size <= array.length;
  }

  /**
   * Two ByteArray are equal when they have the same size and each byte
   * within the first <code>size</code> bytes are equal.
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ByteArray)) {
      return false;
    }
    ByteArray other = (ByteArray) o;
    if (size != other.size) {
      return false;
    }
    return AoArrays.equals(array, other.array, 0, size);
  }

  /**
   * The hashCode is created from the first <code>size</code> bytes.
   */
  @Override
  public int hashCode() {
    return AoArrays.hashCode(array, 0, size);
  }
}
