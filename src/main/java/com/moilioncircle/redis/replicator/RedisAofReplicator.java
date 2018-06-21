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

import static com.moilioncircle.redis.replicator.Status.CONNECTED;
import static com.moilioncircle.redis.replicator.Status.DISCONNECTED;
import com.moilioncircle.redis.replicator.cmd.Command;
import com.moilioncircle.redis.replicator.cmd.CommandName;
import com.moilioncircle.redis.replicator.cmd.CommandParser;
import com.moilioncircle.redis.replicator.cmd.ReplyParser;
import com.moilioncircle.redis.replicator.io.RedisInputStream;
import com.moilioncircle.redis.replicator.util.Arrays;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * @author Leon Chen
 * @since 2.1.0
 */
public class RedisAofReplicator extends AbstractReplicator {

    protected static final Log logger = LogFactory.getLog(RedisAofReplicator.class);
    protected final ReplyParser replyParser;

    public RedisAofReplicator(File file, Configuration configuration) throws FileNotFoundException {
        this(new FileInputStream(file), configuration);
    }

    public RedisAofReplicator(InputStream in, Configuration configuration) {
        Objects.requireNonNull(in);
        Objects.requireNonNull(configuration);
        this.configuration = configuration;
        this.inputStream = new RedisInputStream(in, this.configuration.getBufferSize());
        this.inputStream.setRawByteListeners(this.rawByteListeners);
        this.replyParser = new ReplyParser(inputStream);
        this.builtInCommandParserRegister();
        if (configuration.isUseDefaultExceptionListener())
            addExceptionListener(new DefaultExceptionListener());
    }

    @Override
    public void open() throws IOException {
        if (!this.connected.compareAndSet(DISCONNECTED, CONNECTED)) return;
        try {
            this.doOpen();
        } catch (EOFException ignore) {
        } catch (UncheckedIOException e) {
            if (!(e.getCause() instanceof EOFException)) throw e.getCause();
        } finally {
            doClose();
            doCloseListener(this);
        }
    }

    protected void doOpen() throws IOException {
        while (getStatus() == CONNECTED) {
            Object obj = replyParser.parse();

            if (obj instanceof Object[]) {
                if (verbose() && logger.isDebugEnabled())
                    logger.debug(Arrays.deepToString((Object[]) obj));
                Object[] raw = (Object[]) obj;
                CommandName name = CommandName.name(new String((byte[]) raw[0], UTF_8));
                final CommandParser<? extends Command> parser;
                if ((parser = commands.get(name)) == null) {
                    logger.warn("command [" + name + "] not register. raw command:[" + Arrays.deepToString(raw) + "]");
                    continue;
                }
                submitEvent(parser.parse(raw));
            } else {
                logger.info("unexpected redis reply:" + obj);
            }
        }
    }
}