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

package com.hedera.mirror.importer.reader.balance;

import com.hedera.mirror.common.domain.balance.AccountBalance;
import com.hedera.mirror.common.domain.balance.AccountBalanceFile;
import com.hedera.mirror.importer.domain.StreamFileData;
import com.hedera.mirror.importer.reader.StreamFileReader;

/**
 * Reads an account balance file, parses the header to get the consensus timestamp, and extracts
 * <code>AccountBalance</code> objects, one such object per valid account balance line.
 */
public interface BalanceFileReader extends StreamFileReader<AccountBalanceFile, AccountBalance> {

    boolean supports(StreamFileData streamFileData);
}
