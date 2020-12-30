package test; /**
 * Copyright 2020 Splunk Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.pulsar.common.api.proto.BaseCommand;
import org.apache.pulsar.common.api.proto.MessageMetadata;
import org.apache.pulsar.common.api.proto.PulsarApi;
import org.apache.pulsar.common.util.protobuf.ByteBufCodedInputStream;
import org.apache.pulsar.common.util.protobuf.ByteBufCodedOutputStream;
import org.apache.pulsar.shaded.com.google.protobuf.v241.ByteString;
import org.apache.pulsar.shaded.com.google.protobuf.v241.ExtensionRegistryLite;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Benchmark)
@Warmup(iterations = 3)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Measurement(iterations = 3)
@Fork(value = 1)
public class SimpleBenchmark {

    private static final ByteBuf buffer = Unpooled.buffer(100 * 1024);
    private static final BaseCommand baseCommand = new BaseCommand();
    private static final MessageMetadata messageMetadata = new MessageMetadata();

    @Benchmark
    public void serialize_send_protobuf_241_patched(Blackhole bh) throws Exception {
        PulsarApi.BaseCommand.Builder baseCommandBuilder = PulsarApi.BaseCommand.newBuilder();
        baseCommandBuilder.setType(PulsarApi.BaseCommand.Type.SEND);

        PulsarApi.CommandSend.Builder sendCmdBuilder = PulsarApi.CommandSend.newBuilder();
        sendCmdBuilder.setProducerId(5)
                .setSequenceId(100)
                .setNumMessages(10);

        PulsarApi.CommandSend sendCmd = sendCmdBuilder.build();
        baseCommandBuilder.setSend(sendCmd);

        PulsarApi.BaseCommand baseCommand = baseCommandBuilder.build();

        buffer.clear();
        ByteBufCodedOutputStream outStream = ByteBufCodedOutputStream.get(buffer);
        baseCommand.writeTo(outStream);

        sendCmdBuilder.recycle();
        sendCmd.recycle();
        baseCommand.recycle();
        baseCommandBuilder.recycle();
        outStream.recycle();
    }

    @Benchmark
    public void serialize_send_lightproto(Blackhole bh) throws Exception {
        baseCommand.clear();
        buffer.clear();

        baseCommand.setType(BaseCommand.Type.SEND);
        baseCommand.setSend()
                .setProducerId(5)
                .setSequenceId(100)
                .setNumMessages(10);
        baseCommand.writeTo(buffer);
    }

    private static final byte[] schemaVersion = new byte[10];
    private static final byte[] orderingKey = new byte[50];

    @Benchmark
    public void serialize_metadata_protobuf_241_patched(Blackhole bh) throws Exception {
        PulsarApi.MessageMetadata.Builder metadataBuilder = PulsarApi.MessageMetadata.newBuilder();
        metadataBuilder
                .setProducerName("producer-name")
                .setPublishTime(1111222222)
                .setEventTime(1111233333)
                .setSequenceId(11111)
                .setPartitionKey("my-partition-key")
                .setOrderingKey(ByteString.copyFrom(orderingKey))
                .setSchemaVersion(ByteString.copyFrom(schemaVersion));

        PulsarApi.MessageMetadata metadata = metadataBuilder.build();

        buffer.clear();
        ByteBufCodedOutputStream outStream = ByteBufCodedOutputStream.get(buffer);
        metadata.writeTo(outStream);

        metadata.recycle();
        metadataBuilder.recycle();
        outStream.recycle();
    }

    @Benchmark
    public void serialize_metadata_lightproto(Blackhole bh) throws Exception {
        messageMetadata.clear();
        buffer.clear();

        messageMetadata
                .setProducerName("producer-name")
                .setPublishTime(1111222222)
                .setEventTime(1111233333)
                .setSequenceId(11111)
                .setPartitionKey("my-partition-key")
                .setOrderingKey(orderingKey)
                .setSchemaVersion(schemaVersion);

        messageMetadata.writeTo(buffer);
    }

    private static final ByteBuf serializedSend = Unpooled.buffer();
    private static final ByteBuf serializedMessageMetadata = Unpooled.buffer();

    static {
        baseCommand.clear();
        baseCommand.setType(BaseCommand.Type.SEND)
                .setSend()
                .setProducerId(5)
                .setSequenceId(100)
                .setNumMessages(10);
        baseCommand.writeTo(serializedSend);

        messageMetadata.clear();
        messageMetadata
                .setProducerName("producer-name")
                .setPublishTime(1111222222)
                .setEventTime(1111233333)
                .setSequenceId(11111)
                .setPartitionKey("my-partition-key")
                .setOrderingKey(orderingKey)
                .setSchemaVersion(schemaVersion);
        messageMetadata.writeTo(serializedMessageMetadata);g
    }

    @Benchmark
    public void deserialize_send_protobuf_241_patched(Blackhole bh) throws Exception {
        PulsarApi.BaseCommand.Builder baseCommandBuilder = PulsarApi.BaseCommand.newBuilder();
        ByteBufCodedInputStream inputStream = ByteBufCodedInputStream.get(serializedSend);
        baseCommandBuilder.mergeFrom(inputStream, ExtensionRegistryLite.getEmptyRegistry());

        serializedSend.resetReaderIndex();
        baseCommandBuilder.recycle();
        inputStream.recycle();
        bh.consume(baseCommandBuilder);
    }

    @Benchmark
    public void deserialize_send_lightproto(Blackhole bh) throws Exception {
        baseCommand.parseFrom(serializedSend, serializedSend.readableBytes());
        serializedSend.resetReaderIndex();
        bh.consume(baseCommand);
    }

    @Benchmark
    public void deserialize_metadata_protobuf_241_patched(Blackhole bh) throws Exception {
        PulsarApi.MessageMetadata.Builder metadataBuilder = PulsarApi.MessageMetadata.newBuilder();
        ByteBufCodedInputStream inputStream = ByteBufCodedInputStream.get(serializedMessageMetadata);
        metadataBuilder.mergeFrom(inputStream, ExtensionRegistryLite.getEmptyRegistry());

        serializedMessageMetadata.resetReaderIndex();
        metadataBuilder.recycle();
        inputStream.recycle();
        bh.consume(metadataBuilder);
    }

    @Benchmark
    public void deserialize_metadata_lightproto(Blackhole bh) throws Exception {
        messageMetadata.parseFrom(serializedMessageMetadata, serializedMessageMetadata.readableBytes());
        serializedMessageMetadata.resetReaderIndex();
        bh.consume(messageMetadata);
    }
}
