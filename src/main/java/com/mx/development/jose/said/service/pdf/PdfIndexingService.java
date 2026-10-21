package com.mx.development.jose.said.service.pdf;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import com.mx.development.jose.said.dto.IndexingResult;

import java.util.List;

@Service
public class PdfIndexingService {

    private final VectorStore vectorStore;

    public PdfIndexingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    // Paso 01: Se ejecuta una sola vez
    public IndexingResult indexar(Resource pdfResource) {

        PagePdfDocumentReader pdfReader =new PagePdfDocumentReader(pdfResource);

        List<Document> documentos = pdfReader.get();

        TokenTextSplitter textSplitter = TokenTextSplitter.builder().build();

        List<Document> fragmentos =textSplitter.apply(documentos);

        vectorStore.add(fragmentos);

        return new IndexingResult(
                documentos.size(),
                fragmentos.size()
        );
    }

    // Paso 02: Uso -> n veces
    public List<Document> buscar(String consulta) {

        SearchRequest searchRequest = SearchRequest.builder()
                .query(consulta)
                .topK(3)
                .similarityThreshold(0.50)
                .build();

        return vectorStore.similaritySearch(searchRequest);
    }
}