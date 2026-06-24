/*
 * Copyright 2019 Web3 Labs Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.web3j.protocol.ipc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.web3j.protocol.Service;

/** Ipc service implementation. */
public class IpcService extends Service {

    private static final Logger log = LoggerFactory.getLogger(IpcService.class);

    public IpcService(boolean includeRawResponses) {
        super(includeRawResponses);
    }

    public IpcService() {
        this(false);
    }

    protected IOFacade getIO() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    protected InputStream performIO(String payload) throws IOException {
        IOFacade io = getIO();
        io.write(payload);
        log.debug(">> {}", payload);

        String result = io.read();
        log.debug("<< {}", result);
        io.close();

        // It's not ideal converting back into an inputStream, but we want
        // to be consistent with the HTTPService API.
        // UTF-8 (the default encoding for JSON) is explicitly used here.
        return new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void close() {}
}
