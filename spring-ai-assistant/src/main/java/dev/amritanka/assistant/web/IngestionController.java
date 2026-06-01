package dev.amritanka.assistant.web;

import dev.amritanka.assistant.rag.IngestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class IngestionController {

    private final IngestionService ingestionService;

    @PostMapping("/upload")
    public Mono<IngestResult> upload(@RequestPart("file") FilePart file,
                                     @RequestPart(value = "tenantId", required = false) String tenantId) {
        String resolvedTenant = tenantId != null ? tenantId : "default";
        return DataBufferUtilsHelper.toBytes(file)
                .map(bytes -> {
                    int chunks = ingestionService.ingest(
                            new ByteArrayResource(bytes) {
                                @Override public String getFilename() { return file.filename(); }
                            },
                            resolvedTenant,
                            file.filename());
                    return new IngestResult(file.filename(), chunks);
                });
    }

    public record IngestResult(String filename, int chunks) {}
}
