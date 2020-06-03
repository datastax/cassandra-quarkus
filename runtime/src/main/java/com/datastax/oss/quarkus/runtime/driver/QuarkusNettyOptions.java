/*
 * Copyright DataStax, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.quarkus.runtime.driver;

import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.internal.core.context.NettyOptions;
import com.datastax.oss.driver.internal.core.util.concurrent.BlockingOperation;
import com.datastax.oss.driver.shaded.guava.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.PlatformDependent;
import java.time.Duration;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuarkusNettyOptions implements NettyOptions {
  private static final Logger LOG = LoggerFactory.getLogger(QuarkusNettyOptions.class);

  private final DriverExecutionProfile config;
  private final EventLoopGroup ioEventLoopGroup;
  private final EventLoopGroup adminEventLoopGroup;
  private final Timer timer;

  public QuarkusNettyOptions(
      QuarkusDriverContext context,
      EventLoopGroup ioEventLoopGroup,
      EventLoopGroup adminEventLoopGroup) {
    this.config = context.getConfig().getDefaultProfile();
    boolean daemon = config.getBoolean(DefaultDriverOption.NETTY_DAEMON);
    // The following options are ignored when using Quarkus Netty event loops:
    // NETTY_IO_SHUTDOWN_QUIET_PERIOD
    // NETTY_IO_SHUTDOWN_TIMEOUT
    // NETTY_IO_SHUTDOWN_UNIT
    // NETTY_ADMIN_SHUTDOWN_QUIET_PERIOD
    // NETTY_ADMIN_SHUTDOWN_TIMEOUT
    // NETTY_ADMIN_SHUTDOWN_UNIT

    ThreadFactory safeFactory = new BlockingOperation.SafeThreadFactory();

    this.ioEventLoopGroup = ioEventLoopGroup;
    this.adminEventLoopGroup = adminEventLoopGroup;

    // setup the Timer
    ThreadFactory timerThreadFactory =
        new ThreadFactoryBuilder()
            .setThreadFactory(safeFactory)
            .setNameFormat(context.getSessionName() + "-timer-%d")
            .setDaemon(daemon)
            .build();

    Duration tickDuration = config.getDuration(DefaultDriverOption.NETTY_TIMER_TICK_DURATION);
    // JAVA-2264: tick durations on Windows cannot be less than 100 milliseconds,
    // see https://github.com/netty/netty/issues/356.
    if (PlatformDependent.isWindows() && tickDuration.toMillis() < 100) {
      LOG.warn(
          "Timer tick duration was set to a value too aggressive for Windows: {} ms; "
              + "doing so is known to cause extreme CPU usage. "
              + "Please set advanced.netty.timer.tick-duration to 100 ms or higher.",
          tickDuration.toMillis());
    }
    timer =
        new HashedWheelTimer(
            timerThreadFactory,
            tickDuration.toNanos(),
            TimeUnit.NANOSECONDS,
            config.getInt(DefaultDriverOption.NETTY_TIMER_TICKS_PER_WHEEL));
  }

  @Override
  public EventLoopGroup ioEventLoopGroup() {
    return ioEventLoopGroup;
  }

  @Override
  public EventExecutorGroup adminEventExecutorGroup() {
    return adminEventLoopGroup;
  }

  @Override
  public Class<? extends Channel> channelClass() {
    return NioSocketChannel.class;
  }

  @Override
  public ByteBufAllocator allocator() {
    return ByteBufAllocator.DEFAULT;
  }

  @Override
  public void afterBootstrapInitialized(Bootstrap bootstrap) {
    boolean tcpNoDelay = config.getBoolean(DefaultDriverOption.SOCKET_TCP_NODELAY);
    bootstrap.option(ChannelOption.TCP_NODELAY, tcpNoDelay);
    if (config.isDefined(DefaultDriverOption.SOCKET_KEEP_ALIVE)) {
      boolean keepAlive = config.getBoolean(DefaultDriverOption.SOCKET_KEEP_ALIVE);
      bootstrap.option(ChannelOption.SO_KEEPALIVE, keepAlive);
    }
    if (config.isDefined(DefaultDriverOption.SOCKET_REUSE_ADDRESS)) {
      boolean reuseAddress = config.getBoolean(DefaultDriverOption.SOCKET_REUSE_ADDRESS);
      bootstrap.option(ChannelOption.SO_REUSEADDR, reuseAddress);
    }
    if (config.isDefined(DefaultDriverOption.SOCKET_LINGER_INTERVAL)) {
      int lingerInterval = config.getInt(DefaultDriverOption.SOCKET_LINGER_INTERVAL);
      bootstrap.option(ChannelOption.SO_LINGER, lingerInterval);
    }
    if (config.isDefined(DefaultDriverOption.SOCKET_RECEIVE_BUFFER_SIZE)) {
      int receiveBufferSize = config.getInt(DefaultDriverOption.SOCKET_RECEIVE_BUFFER_SIZE);
      bootstrap
          .option(ChannelOption.SO_RCVBUF, receiveBufferSize)
          .option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(receiveBufferSize));
    }
    if (config.isDefined(DefaultDriverOption.SOCKET_SEND_BUFFER_SIZE)) {
      int sendBufferSize = config.getInt(DefaultDriverOption.SOCKET_SEND_BUFFER_SIZE);
      bootstrap.option(ChannelOption.SO_SNDBUF, sendBufferSize);
    }
    if (config.isDefined(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT)) {
      Duration connectTimeout = config.getDuration(DefaultDriverOption.CONNECTION_CONNECT_TIMEOUT);
      bootstrap.option(
          ChannelOption.CONNECT_TIMEOUT_MILLIS, Long.valueOf(connectTimeout.toMillis()).intValue());
    }
  }

  @Override
  public void afterChannelInitialized(Channel channel) {
    // nothing to do
  }

  @Override
  public Future<Void> onClose() {
    // return immediately completed future. We should not close even loops because they are
    // managed by the quarkus.
    DefaultPromise<Void> promise = new DefaultPromise<>(GlobalEventExecutor.INSTANCE);
    promise.setSuccess(null);
    return promise;
  }

  @Override
  public synchronized Timer getTimer() {
    return timer;
  }
}
