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

package com.hedera.mirror.monitor.subscribe.controller;

import com.hedera.mirror.monitor.ScenarioProperties;
import com.hedera.mirror.monitor.ScenarioProtocol;
import com.hedera.mirror.monitor.ScenarioStatus;
import com.hedera.mirror.monitor.subscribe.MirrorSubscriber;
import com.hedera.mirror.monitor.subscribe.Scenario;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.CustomLog;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@CustomLog
@RequestMapping("/api/v1/subscriber")
@RequiredArgsConstructor
@RestController
class SubscriberController {

    private final MirrorSubscriber mirrorSubscriber;

    @GetMapping
    public <T extends ScenarioProperties> Flux<Scenario<T, Object>> subscriptions(
            @RequestParam("protocol") Optional<ScenarioProtocol> protocol,
            @RequestParam("status") Optional<List<ScenarioStatus>> status) {
        return mirrorSubscriber
                .<Scenario<T, Object>>getSubscriptions()
                .filter(s -> !protocol.isPresent() || protocol.get() == s.getProtocol())
                .filter(s -> !status.isPresent() || status.get().contains(s.getStatus()))
                .switchIfEmpty(Mono.error(new NoSuchElementException()));
    }

    @GetMapping("/{name}")
    public <T extends ScenarioProperties> Flux<Scenario<T, Object>> subscriptions(
            @PathVariable("name") String name, @RequestParam("status") Optional<List<ScenarioStatus>> status) {
        Flux<Scenario<T, Object>> subscriptions = subscriptions(Optional.empty(), status);
        return subscriptions
                .filter(subscription -> subscription.getName().equals(name))
                .switchIfEmpty(Mono.error(new NoSuchElementException()));
    }

    @GetMapping("/{name}/{id}")
    public <T extends ScenarioProperties> Mono<Scenario<T, Object>> subscription(
            @PathVariable("name") String name, @PathVariable("id") int id) {
        Flux<Scenario<T, Object>> subscriptions = subscriptions(name, Optional.empty());
        return subscriptions.filter(s -> s.getId() == id).last();
    }

    @ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Not found")
    @ExceptionHandler(NoSuchElementException.class)
    void notFound() {
        // Error logging is done generically in LoggingFilter
    }
}
