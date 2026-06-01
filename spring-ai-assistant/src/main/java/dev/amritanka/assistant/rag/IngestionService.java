package dev.amritanka.assistant.rag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * RAG ingestion: extract → chunk → embed → store in pgvector.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final VectorStore vectorStore;

    public int ingest(Resource resource, String tenantId, String documentTitle) {
        log.info("Ingesting {} for tenant {}", documentTitle, tenantId);

        // 1) Extract text from any format Tika supports (PDF/DOCX/MD/HTML/...)
        List<Document> raw = new TikaDocumentReader(resource).get();

        // 2) Chunk into ~800-token windows with 120-token overlap
        TokenTextSplitter splitter = new TokenTextSplitter(800, 120, 5, 10000, true);
        List<Document> chunks = splitter.apply(raw);

        // 3) Tag every chunk with tenant + source for filtering and citations
        chunks.forEach(c -> {
            c.getMetadata().put("tenantId", tenantId);
            c.getMetadata().put("title", documentTitle);
        });

        // 4) Embed + persist
        vectorStore.add(chunks);
        log.info("Ingested {} chunks", chunks.size());
        return chunks.size();
    }
}
