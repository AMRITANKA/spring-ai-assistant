package dev.amritanka.assistant.web;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;

final class DataBufferUtilsHelper {

    private DataBufferUtilsHelper() {}

    static Mono<byte[]> toBytes(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .map(buf -> {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream(buf.readableByteCount())) {
                        buf.toByteBuffer();
                        byte[] bytes = new byte[buf.readableByteCount()];
                        buf.read(bytes);
                        return bytes;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to read upload", e);
                    } finally {
                        DataBufferUtils.release(buf);
                    }
                });
    }
}
