/*
 * Copyright (C) 2020-2025 Hedera Hashgraph, LLC
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

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.TokenGrantKycTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.mirror.monitor.publish.transaction.TransactionSupplier;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenGrantKycTransactionSupplier implements TransactionSupplier<TokenGrantKycTransaction> {

    @NotBlank
    private String accountId;

    @Min(1)
    private long maxTransactionFee = 1_000_000_000;

    @NotBlank
    private String tokenId;

    @Override
    public TokenGrantKycTransaction get() {

        return new TokenGrantKycTransaction()
                .setAccountId(AccountId.fromString(accountId))
                .setMaxTransactionFee(Hbar.fromTinybars(maxTransactionFee))
                .setTokenId(TokenId.fromString(tokenId));
    }
}
