/*
 * ao-messaging-api - Asynchronous bidirectional messaging over various protocols API.
 * Copyright (C) 2014, 2015, 2016, 2019, 2020, 2021, 2022  AO Industries, Inc.
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

import com.aoapps.concurrent.ConcurrentListenerManager;
import com.aoapps.security.Identifier;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * Each socket, regardless or protocol and whether client or server, has a
 * context.
 */
public interface SocketContext extends Closeable {

  /**
   * Gets a snapshot of all active sockets.
   * If context is closed will be an empty map.
   */
  Map<Identifier, ? extends Socket> getSockets();

  /**
   * Gets the socket of the given ID or <code>null</code> if not found.
   */
  Socket getSocket(Identifier id);

  /**
   * Closes this context.  When the context is closed, all active sockets are
   * closed and all related persistent resources are freed.
   */
  @Override
  void close() throws IOException;

  boolean isClosed();

  /**
   * See {@link ConcurrentListenerManager#addListener(java.lang.Object, boolean)}.
   */
  void addSocketContextListener(SocketContextListener listener, boolean synchronous);

  /**
   * See {@link ConcurrentListenerManager#removeListener(java.lang.Object)}.
   */
  boolean removeSocketContextListener(SocketContextListener listener);
}
