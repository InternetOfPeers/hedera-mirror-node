/*
 * Copyright (C) 2024-2025 Hedera Hashgraph, LLC
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

package com.hedera.mirror.importer.parser.record.transactionhandler;

import com.google.common.collect.Range;
import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.entity.Node;
import com.hedera.mirror.common.domain.transaction.RecordItem;
import com.hedera.mirror.common.domain.transaction.Transaction;
import com.hedera.mirror.common.domain.transaction.TransactionType;
import com.hedera.mirror.importer.parser.record.entity.EntityListener;
import com.hedera.mirror.importer.parser.record.entity.EntityProperties;
import jakarta.inject.Named;
import lombok.RequiredArgsConstructor;

@Named
@RequiredArgsConstructor
class NodeCreateTransactionHandler extends AbstractTransactionHandler {

    private final EntityListener entityListener;
    private final EntityProperties entityProperties;

    @Override
    public EntityId getEntity(RecordItem recordItem) {
        return EntityId.of(recordItem.getTransactionBody().getNodeCreate().getAccountId());
    }

    @Override
    public TransactionType getType() {
        return TransactionType.NODECREATE;
    }

    @Override
    protected void doUpdateTransaction(Transaction transaction, RecordItem recordItem) {
        if (!entityProperties.getPersist().isNodes()) {
            return;
        }
        transaction.setTransactionBytes(recordItem.getTransaction().toByteArray());
        transaction.setTransactionRecordBytes(recordItem.getTransactionRecord().toByteArray());
        parseNode(recordItem);
    }

    private void parseNode(RecordItem recordItem) {
        if (recordItem.isSuccessful()) {
            var nodeCreate = recordItem.getTransactionBody().getNodeCreate();
            long consensusTimestamp = recordItem.getConsensusTimestamp();
            entityListener.onNode(Node.builder()
                    .adminKey(nodeCreate.getAdminKey().toByteArray())
                    .createdTimestamp(consensusTimestamp)
                    .deleted(false)
                    .nodeId(recordItem.getTransactionRecord().getReceipt().getNodeId())
                    .timestampRange(Range.atLeast(consensusTimestamp))
                    .build());
        }
    }
}
