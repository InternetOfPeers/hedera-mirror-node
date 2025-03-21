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

import {InvalidArgumentError} from '../errors';

class TokenType {
  static TYPE = ['FUNGIBLE_COMMON', 'NON_FUNGIBLE_UNIQUE'];

  constructor(id) {
    this._id = Number(id);
    if (Number.isNaN(this._id) || this._id < 0 || this._id > 2) {
      throw new InvalidArgumentError(`Invalid token type id ${id}`);
    }
  }

  getId() {
    return this._id;
  }

  toJSON() {
    return this.toString();
  }

  toString() {
    return TokenType.TYPE[this._id];
  }
}

export default TokenType;
