/*
 * Copyright (C) 2022-2025 Hedera Hashgraph, LLC
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

package com.hedera.mirror.importer.downloader.provider;

import static com.hedera.mirror.importer.domain.StreamFilename.EPOCH;
import static com.hedera.mirror.importer.domain.StreamFilename.FileType.SIGNATURE;
import static org.apache.commons.lang3.StringUtils.isNumeric;

import com.hedera.mirror.common.domain.StreamType;
import com.hedera.mirror.importer.addressbook.ConsensusNode;
import com.hedera.mirror.importer.domain.StreamFileData;
import com.hedera.mirror.importer.domain.StreamFilename;
import com.hedera.mirror.importer.downloader.CommonDownloaderProperties;
import com.hedera.mirror.importer.downloader.CommonDownloaderProperties.PathType;
import com.hedera.mirror.importer.exception.InvalidDatasetException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import lombok.CustomLog;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.RequestPayer;
import software.amazon.awssdk.services.s3.model.S3Object;

@CustomLog
@RequiredArgsConstructor
public final class S3StreamFileProvider implements StreamFileProvider {

    public static final String SEPARATOR = "/";
    private static final String RANGE_PREFIX = "bytes=0-";
    private static final String TEMPLATE_ACCOUNT_ID_PREFIX = "%s/%s%s/";
    private static final String TEMPLATE_NODE_ID_PREFIX = "%s/%d/%d/%s/";

    private final CommonDownloaderProperties properties;
    private final Map<PathKey, PathResult> paths = new ConcurrentHashMap<>();
    private final S3AsyncClient s3Client;

    public Mono<StreamFileData> get(ConsensusNode node, StreamFilename streamFilename) {

        var s3Key = streamFilename.getFilePath();
        var request = GetObjectRequest.builder()
                .bucket(properties.getBucketName())
                .key(s3Key)
                .requestPayer(RequestPayer.REQUESTER)
                .range(RANGE_PREFIX + (properties.getMaxSize() - 1))
                .build();

        var responseFuture = s3Client.getObject(request, AsyncResponseTransformer.toBytes());
        return Mono.fromFuture(responseFuture)
                .map(r -> toStreamFileData(streamFilename, r))
                .timeout(properties.getTimeout())
                .onErrorMap(NoSuchKeyException.class, TransientProviderException::new)
                .doOnSuccess(s -> log.debug("Finished downloading {}", s3Key));
    }

    @Override
    public Flux<StreamFileData> list(ConsensusNode node, StreamFilename lastFilename) {
        // Number of items we plan do download in a single batch times 2 for file + sig.
        int batchSize = properties.getBatchSize() * 2;

        var key = new PathKey(node, lastFilename.getStreamType());
        var pathResult = paths.computeIfAbsent(key, k -> new PathResult());
        var prefix = getPrefix(key, pathResult.getPathType());
        var startAfter = prefix + lastFilename.getFilenameAfter();

        var listRequest = ListObjectsV2Request.builder()
                .bucket(properties.getBucketName())
                .prefix(prefix)
                .delimiter(SEPARATOR)
                .startAfter(startAfter)
                .maxKeys(batchSize)
                .requestPayer(RequestPayer.REQUESTER)
                .build();

        return Mono.fromFuture(s3Client.listObjectsV2(listRequest))
                .timeout(properties.getTimeout())
                .doOnNext(l -> {
                    pathResult.update(!l.contents().isEmpty());
                    log.debug("Returned {} s3 objects", l.contents().size());
                })
                .flatMapIterable(ListObjectsV2Response::contents)
                .filter(r -> r.size() <= properties.getMaxSize())
                .map(this::toStreamFilename)
                .filter(s -> s != EPOCH && s.getFileType() == SIGNATURE)
                .flatMapSequential(streamFilename -> get(node, streamFilename))
                .doOnSubscribe(s -> log.debug(
                        "Searching for the next {} files after {}/{}",
                        batchSize,
                        properties.getBucketName(),
                        startAfter))
                .switchIfEmpty(Flux.defer(() -> pathResult.fallback() ? list(node, lastFilename) : Flux.empty()));
    }

    private String getAccountIdPrefix(PathKey key) {
        var streamType = key.type();
        var nodeAccount = key.node().getNodeAccountId().toString();
        return TEMPLATE_ACCOUNT_ID_PREFIX.formatted(streamType.getPath(), streamType.getNodePrefix(), nodeAccount);
    }

    private String getNodeIdPrefix(PathKey key) {
        var network = properties.getImporterProperties().getNetwork();
        var shard = properties.getImporterProperties().getShard();
        var streamFolder = key.type().getNodeIdBasedSuffix();
        return TEMPLATE_NODE_ID_PREFIX.formatted(network, shard, key.node().getNodeId(), streamFolder);
    }

    private String getPrefix(PathKey key, PathType pathType) {
        return switch (pathType) {
            case ACCOUNT_ID, AUTO -> getAccountIdPrefix(key);
            case NODE_ID -> getNodeIdPrefix(key);
        };
    }

    private StreamFileData toStreamFileData(StreamFilename streamFilename, ResponseBytes<GetObjectResponse> r) {
        var response = r.response();
        var contentLength = StringUtils.substringAfterLast(response.contentRange(), '/');
        long size = isNumeric(contentLength) ? Long.parseLong(contentLength) : response.contentLength();

        if (size > properties.getMaxSize()) {
            throw new InvalidDatasetException("Stream file " + streamFilename + " size " + size + " exceeds limit");
        }

        return new StreamFileData(streamFilename, r::asByteArrayUnsafe, response.lastModified());
    }

    private StreamFilename toStreamFilename(S3Object s3Object) {
        var key = s3Object.key();

        try {
            return StreamFilename.from(key, SEPARATOR);
        } catch (Exception e) {
            log.warn("Unable to parse stream filename for {}", key, e);
            return EPOCH; // Reactor doesn't allow null return values for map(), so use a sentinel that we filter later
        }
    }

    record PathKey(ConsensusNode node, StreamType type) {}

    @Data
    private class PathResult {

        @Nullable
        private volatile Instant expiration;

        private volatile PathType pathType = properties.getPathType();

        private PathResult() {
            if (properties.getPathType() == PathType.AUTO) {
                this.expiration = Instant.now().plus(properties.getPathRefreshInterval());
                this.pathType = PathType.ACCOUNT_ID;
            }
        }

        void update(boolean found) {
            // Path is statically configured or has permanently transitioned from ACCOUNT_ID to NODE_ID
            if (expiration == null) {
                return;
            }

            // Permanently switch to NODE_ID
            if (found && pathType == PathType.NODE_ID) {
                expiration = null;
                return;
            }

            // NODE_ID attempt failed so revert back to ACCOUNT_ID for now
            if (!found && pathType == PathType.NODE_ID) {
                pathType = PathType.ACCOUNT_ID;
                return;
            }

            // If ACCOUNT_ID auto mode interval has expired, try NODE_ID if no files were found
            var now = Instant.now();
            if (now.isAfter(expiration)) {
                expiration = now.plus(properties.getPathRefreshInterval());
                if (!found) {
                    pathType = PathType.NODE_ID;
                }
            }
        }

        boolean fallback() {
            return expiration != null && pathType == PathType.NODE_ID;
        }
    }
}
