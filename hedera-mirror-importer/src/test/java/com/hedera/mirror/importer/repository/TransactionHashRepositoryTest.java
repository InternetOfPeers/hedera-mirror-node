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

package com.hedera.mirror.importer.repository;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class TransactionHashRepositoryTest extends AbstractRepositoryTest {

    private final TransactionHashRepository transactionHashRepository;

    @Test
    void save() {
        var transactionHash = domainBuilder.transactionHash().get();
        transactionHashRepository.save(transactionHash);
        assertThat(transactionHashRepository.findById(transactionHash.getHash()))
                .contains(transactionHash);
    }
}
