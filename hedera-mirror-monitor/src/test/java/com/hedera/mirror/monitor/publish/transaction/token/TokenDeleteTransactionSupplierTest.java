/*
 * Copyright (C) 2021-2025 Hedera Hashgraph, LLC
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

package com.hedera.mirror.monitor.publish.transaction.token;

import static org.assertj.core.api.Assertions.assertThat;

import com.hedera.hashgraph.sdk.TokenDeleteTransaction;
import com.hedera.mirror.monitor.publish.transaction.AbstractTransactionSupplierTest;
import com.hedera.mirror.monitor.publish.transaction.TransactionSupplier;
import org.junit.jupiter.api.Test;

class TokenDeleteTransactionSupplierTest extends AbstractTransactionSupplierTest {

    @Test
    void createWithMinimumData() {
        TokenDeleteTransactionSupplier tokenDeleteTransactionSupplier = new TokenDeleteTransactionSupplier();
        tokenDeleteTransactionSupplier.setTokenId(TOKEN_ID.toString());
        TokenDeleteTransaction actual = tokenDeleteTransactionSupplier.get();

        assertThat(actual)
                .returns(MAX_TRANSACTION_FEE_HBAR, TokenDeleteTransaction::getMaxTransactionFee)
                .returns(TOKEN_ID, TokenDeleteTransaction::getTokenId);
    }

    @Test
    void createWithCustomData() {
        TokenDeleteTransactionSupplier tokenDeleteTransactionSupplier = new TokenDeleteTransactionSupplier();
        tokenDeleteTransactionSupplier.setMaxTransactionFee(1);
        tokenDeleteTransactionSupplier.setTokenId(TOKEN_ID.toString());
        TokenDeleteTransaction actual = tokenDeleteTransactionSupplier.get();

        assertThat(actual)
                .returns(ONE_TINYBAR, TokenDeleteTransaction::getMaxTransactionFee)
                .returns(TOKEN_ID, TokenDeleteTransaction::getTokenId);
    }

    @Override
    protected Class<? extends TransactionSupplier<?>> getSupplierClass() {
        return TokenDeleteTransactionSupplier.class;
    }
}
