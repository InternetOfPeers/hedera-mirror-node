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

package com.hedera.mirror.importer.domain;

import com.hedera.mirror.common.domain.entity.EntityId;
import com.hedera.mirror.common.domain.transaction.RecordItem;
import java.util.Collection;
import java.util.Set;
import lombok.Value;

/**
 * Collection of fields that can be used by Transaction Filter to filter on.
 */
@Value
@SuppressWarnings("java:S6548") // Class is not a singleton
public class TransactionFilterFields {
    public static final TransactionFilterFields EMPTY = new TransactionFilterFields(Set.of(), null);
    /**
     * entities contains: (1) Main entity associated with the transaction (2) Transaction payer account (when present)
     * (3) crypto transfer receivers/senders
     */
    Collection<EntityId> entities;

    RecordItem recordItem;
}
