/*
 * Copyright 2016-2018 Leon Chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moilioncircle.redis.replicator;

import com.moilioncircle.redis.replicator.cmd.Command;
import com.moilioncircle.redis.replicator.cmd.CommandListener;
import com.moilioncircle.redis.replicator.cmd.impl.SetCommand;
import com.moilioncircle.redis.replicator.io.RateLimitInputStream;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
@SuppressWarnings("resource")
public class RedisAofReplicatorTest {

    @Test
    public void open() throws Exception {
        Replicator replicator = new RedisReplicator(
                new RateLimitInputStream(RedisSocketReplicatorTest.class.getClassLoader().getResourceAsStream("appendonly1.aof"), 1000), FileType.AOF,
                Configuration.defaultSetting());
        final AtomicInteger acc = new AtomicInteger(0);
        replicator.addCommandListener(new CommandListener() {
            @Override
            public void handle(Replicator replicator, Command command) {
                System.out.println(command);
                acc.incrementAndGet();
            }
        });
        replicator.addCloseListener(new CloseListener() {
            @Override
            public void handle(Replicator replicator) {
                System.out.println("close open");
            }
        });
        replicator.open();
        assertEquals(4, acc.get());
    }

    @Test
    public void open2() throws Exception {
        Replicator replicator = new RedisReplicator(
                new RateLimitInputStream(RedisSocketReplicatorTest.class.getClassLoader().getResourceAsStream("appendonly2.aof"), 1024 * 1000), FileType.AOF,
                Configuration.defaultSetting());
        final AtomicInteger acc = new AtomicInteger(0);
        replicator.addCommandListener(new CommandListener() {
            @Override
            public void handle(Replicator replicator, Command command) {
                System.out.println(command);
                if (command instanceof SetCommand && ((SetCommand) command).getKey().startsWith("test_")) {
                    acc.incrementAndGet();
                }
            }
        });
        replicator.addCloseListener(new CloseListener() {
            @Override
            public void handle(Replicator replicator) {
                System.out.println("close open2");

            }
        });
        replicator.open();
        assertEquals(48000, acc.get());
    }

    @Test
    public void open3() throws Exception {
        Replicator replicator = new RedisReplicator(
                new RateLimitInputStream(RedisSocketReplicatorTest.class.getClassLoader().getResourceAsStream("appendonly3.aof"), 1024 * 1000), FileType.AOF,
                Configuration.defaultSetting());
        final AtomicInteger acc = new AtomicInteger(0);
        replicator.addCommandListener(new CommandListener() {
            @Override
            public void handle(Replicator replicator, Command command) {
                System.out.println(command);
                acc.incrementAndGet();
            }
        });
        replicator.addCloseListener(new CloseListener() {
            @Override
            public void handle(Replicator replicator) {
                System.out.println("close open3");
            }
        });
        replicator.open();
        assertEquals(92539, acc.get());
    }

    @Test
    public void open4() throws Exception {
        Replicator replicator = new RedisReplicator(
                new RateLimitInputStream(RedisSocketReplicatorTest.class.getClassLoader().getResourceAsStream("appendonly5.aof"), 1000), FileType.AOF,
                Configuration.defaultSetting());
        final AtomicInteger acc = new AtomicInteger(0);
        replicator.addCommandListener(new CommandListener() {
            @Override
            public void handle(Replicator replicator, Command command) {
                System.out.println(command);
                acc.incrementAndGet();
            }
        });
        replicator.addCloseListener(new CloseListener() {
            @Override
            public void handle(Replicator replicator) {
                System.out.println("close open4");
            }
        });
        replicator.open();
        assertEquals(71, acc.get());
    }

}