/*
 * Copyright (C) 2019-2025 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hedera.mirror.web3.evm.exception;

import java.io.Serial;

@SuppressWarnings("java:S110")
public class WrongTypeException extends EvmException {
    @Serial
    private static final long serialVersionUID = -6844339452754420145L;

    public WrongTypeException(String message) {
        super(message);
    }
}
