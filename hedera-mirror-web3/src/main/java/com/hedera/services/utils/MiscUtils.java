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

package com.hedera.services.utils;

import static com.hedera.node.app.service.evm.accounts.HederaEvmContractAliases.EVM_ADDRESS_LEN;
import static com.hedera.services.jproto.JKey.mapJKey;
import static com.hedera.services.jproto.JKey.mapKey;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.ContractCall;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.ContractCreate;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.ContractDelete;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.ContractUpdate;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.CryptoApproveAllowance;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.CryptoCreate;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.CryptoDelete;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.CryptoDeleteAllowance;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.CryptoTransfer;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.CryptoUpdate;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.EthereumTransaction;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.Freeze;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.NONE;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenAccountWipe;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenAssociateToAccount;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenBurn;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenCreate;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenDelete;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenDissociateFromAccount;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenFreezeAccount;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenGrantKycToAccount;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenMint;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenPause;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenRevokeKycFromAccount;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenUnfreezeAccount;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenUnpause;
import static com.hederahashgraph.api.proto.java.HederaFunctionality.TokenUpdate;
import static java.util.Objects.requireNonNull;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.services.jproto.JKey;
import com.hedera.services.utils.accessors.SignedTxnAccessor;
import com.hedera.services.utils.accessors.TxnAccessor;
import com.hederahashgraph.api.proto.java.HederaFunctionality;
import com.hederahashgraph.api.proto.java.Key;
import com.hederahashgraph.api.proto.java.SignatureMap;
import com.hederahashgraph.api.proto.java.SignedTransaction;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionBody.DataCase;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class MiscUtils {

    public static final Function<TransactionBody, HederaFunctionality> FUNCTION_EXTRACTOR = trans -> {
        try {
            return functionOf(trans);
        } catch (Exception ignore) {
            return NONE;
        }
    };

    private MiscUtils() {
        throw new UnsupportedOperationException("Utility Class");
    }

    /**
     * check TransactionBody and return the HederaFunctionality. This method was moved from MiscUtils. NODE_STAKE_UPDATE
     * is not checked in this method, since it is not a user transaction.
     *
     * @param txn the {@code TransactionBody}
     * @return one of HederaFunctionality
     * @throws Exception if all the check fails
     */
    @NonNull
    public static HederaFunctionality functionOf(@NonNull final TransactionBody txn) throws Exception {
        requireNonNull(txn);
        DataCase dataCase = txn.getDataCase();

        return switch (dataCase) {
            case CONTRACTCALL -> ContractCall;
            case CONTRACTCREATEINSTANCE -> ContractCreate;
            case CONTRACTUPDATEINSTANCE -> ContractUpdate;
            case CONTRACTDELETEINSTANCE -> ContractDelete;
            case ETHEREUMTRANSACTION -> EthereumTransaction;
            case CRYPTOAPPROVEALLOWANCE -> CryptoApproveAllowance;
            case CRYPTODELETEALLOWANCE -> CryptoDeleteAllowance;
            case CRYPTOCREATEACCOUNT -> CryptoCreate;
            case CRYPTODELETE -> CryptoDelete;
            case CRYPTOTRANSFER -> CryptoTransfer;
            case CRYPTOUPDATEACCOUNT -> CryptoUpdate;
            case FREEZE -> Freeze;
            case TOKENCREATION -> TokenCreate;
            case TOKENFREEZE -> TokenFreezeAccount;
            case TOKENUNFREEZE -> TokenUnfreezeAccount;
            case TOKENGRANTKYC -> TokenGrantKycToAccount;
            case TOKENREVOKEKYC -> TokenRevokeKycFromAccount;
            case TOKENDELETION -> TokenDelete;
            case TOKENUPDATE -> TokenUpdate;
            case TOKENMINT -> TokenMint;
            case TOKENBURN -> TokenBurn;
            case TOKENWIPE -> TokenAccountWipe;
            case TOKENASSOCIATE -> TokenAssociateToAccount;
            case TOKENDISSOCIATE -> TokenDissociateFromAccount;
            case TOKEN_PAUSE -> TokenPause;
            case TOKEN_UNPAUSE -> TokenUnpause;
            default -> throw new IllegalArgumentException("Unknown HederaFunctionality for " + txn);
        };
    }

    public static long perm64(long x) {
        // Shifts: {30, 27, 16, 20, 5, 18, 10, 24, 30}
        x += x << 30;
        x ^= x >>> 27;
        x += x << 16;
        x ^= x >>> 20;
        x += x << 5;
        x ^= x >>> 18;
        x += x << 10;
        x ^= x >>> 24;
        x += x << 30;
        return x;
    }

    public static Key asPrimitiveKeyUnchecked(final ByteString alias) {
        try {
            return Key.parseFrom(alias);
        } catch (final InvalidProtocolBufferException internal) {
            throw new IllegalStateException(internal);
        }
    }

    public static Key asKeyUnchecked(final JKey fcKey) {
        try {
            return mapJKey(fcKey);
        } catch (final Exception impossible) {
            return Key.getDefaultInstance();
        }
    }

    public static JKey asFcKeyUnchecked(final Key key) {
        try {
            return mapKey(key);
        } catch (final InvalidKeyException impermissible) {
            throw new IllegalArgumentException("Key " + key + " should have been decode-able!", impermissible);
        }
    }

    public static Optional<JKey> asUsableFcKey(final Key key) {
        try {
            final var fcKey = mapKey(key);
            if (!fcKey.isValid()) {
                return Optional.empty();
            }
            return Optional.of(fcKey);
        } catch (final InvalidKeyException ignore) {
            return Optional.empty();
        }
    }

    /**
     * Returns a {@link TxnAccessor} for the given in-progress synthetic op.
     *
     * @param syntheticOp the synthetic op
     * @return an accessor for the synthetic op
     */
    public static @NonNull TxnAccessor synthAccessorFor(@NonNull final TransactionBody.Builder syntheticOp) {
        final var signedTxn = SignedTransaction.newBuilder()
                .setBodyBytes(syntheticOp.build().toByteString())
                .setSigMap(SignatureMap.getDefaultInstance())
                .build();
        final var txn = Transaction.newBuilder()
                .setSignedTransactionBytes(signedTxn.toByteString())
                .build();
        return SignedTxnAccessor.uncheckedFrom(txn);
    }

    public static boolean isRecoveredEvmAddress(final byte[] address) {
        return address != null && address.length == EVM_ADDRESS_LEN;
    }

    public static long[] convertArrayToLong(final List<Long> list) {
        final var result = new long[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }
}
