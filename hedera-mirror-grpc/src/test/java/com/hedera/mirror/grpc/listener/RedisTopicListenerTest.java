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

package com.hedera.mirror.grpc.listener;

import com.hedera.mirror.common.domain.topic.TopicMessage;
import jakarta.annotation.Resource;
import lombok.CustomLog;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import reactor.core.publisher.Flux;

@CustomLog
@SuppressWarnings("java:S2187") // Ignore no tests in file warning
class RedisTopicListenerTest extends AbstractSharedTopicListenerTest {

    @Resource
    private ReactiveRedisOperations<String, TopicMessage> redisOperations;

    @Override
    protected ListenerProperties.ListenerType getType() {
        return ListenerProperties.ListenerType.REDIS;
    }

    @Override
    protected void publish(Flux<TopicMessage> publisher) {
        publisher.concatMap(t -> redisOperations.convertAndSend(getTopic(t), t)).blockLast();
    }

    private String getTopic(TopicMessage topicMessage) {
        return "topic." + topicMessage.getTopicId().getId();
    }
}
