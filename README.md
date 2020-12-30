

## Benchmark results

This is a microbenchmark comparing the Pulsar data serialization with the existing patched Protobuf 2.4.1 and the 
LightProto based serializer


```
Benchmark                                                   Mode  Cnt   Score    Error   Units
SimpleBenchmark.deserialize_metadata_lightproto            thrpt    3  18.891 ±  1.675  ops/us
SimpleBenchmark.deserialize_metadata_protobuf_241_patched  thrpt    3   6.187 ±  3.399  ops/us
SimpleBenchmark.deserialize_send_lightproto                thrpt    3  23.830 ±  8.318  ops/us
SimpleBenchmark.deserialize_send_protobuf_241_patched      thrpt    3   7.926 ± 14.269  ops/us
SimpleBenchmark.serialize_metadata_lightproto              thrpt    3   6.699 ± 12.016  ops/us
SimpleBenchmark.serialize_metadata_protobuf_241_patched    thrpt    3   2.943 ±  0.662  ops/us
SimpleBenchmark.serialize_send_lightproto                  thrpt    3  22.905 ± 18.748  ops/us
SimpleBenchmark.serialize_send_protobuf_241_patched        thrpt    3   4.614 ±  1.750  ops/us
```