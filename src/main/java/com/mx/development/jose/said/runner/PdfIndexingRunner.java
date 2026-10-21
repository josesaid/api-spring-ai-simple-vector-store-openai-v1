package com.mx.development.jose.said.runner;

import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import com.mx.development.jose.said.service.pdf.PdfIndexingService;

import java.util.List;

@Component
public class PdfIndexingRunner implements CommandLineRunner {

    private final PdfIndexingService indexingService;


    public PdfIndexingRunner(
            PdfIndexingService indexingService
    ) {
        this.indexingService = indexingService;
    }

    @Override
    public void run(String... args) {

        var pdfResource =
                new ClassPathResource(
                        "documents/spring_ai.pdf"
                );

        var resultado = indexingService.indexar(pdfResource);


        System.out.println();
        System.out.println("INDEXACIÓN DEL DOCUMENTO");
        System.out.println("------------------------------");
        System.out.println("Documentos leídos  : " + resultado.documentosLeidos());
        System.out.println("Chunks indexados   : " + resultado.fragmentosIndexados());
        System.out.println("Estado             : Finalizado");


        //String consulta ="¿Por qué se divide un documento en chunks?";
        String consulta ="¿Por qué usar RAG?";
        //String consulta = "¿Qué es Spring AI?";
        List<Document> resultados = indexingService.buscar(consulta);

        System.out.println();
        System.out.println("BÚSQUEDA SEMÁNTICA");
        System.out.println("------------------------------");
        System.out.println("Consulta: " + consulta);
        System.out.println("Resultados encontrados: " + resultados.size());

        for (int i = 0; i < resultados.size(); i++) {

            Document documento = resultados.get(i);

            System.out.println();
            System.out.println("RESULTADO " + (i + 1));
            System.out.println("------------------------------");
            System.out.printf(
                    "Similitud : %.4f%n",
                    documento.getScore()
            );
            System.out.println(
                    "Metadatos : "
                            + documento.getMetadata()
            );
            System.out.println(
                    "Contenido : " +
                            // + documento.getText()
                            resumirTexto(documento.getText(), 200)
            );
        }
    }

    private String resumirTexto(String texto, int limite) {

        String limpio = texto
                .replaceAll("\\s+", " ")
                .trim();

        if (limpio.length() <= limite) {
            return limpio;
        }

        return limpio.substring(0, limite) + "...";
    }


}