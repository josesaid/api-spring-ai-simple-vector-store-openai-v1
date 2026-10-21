package com.mx.development.jose.said.runner;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.mx.development.jose.said.dto.Resultado;

import java.util.ArrayList;
import java.util.List;

//@Component
public class SemanticSearchRunner {

    @Bean
    CommandLineRunner embedding4(EmbeddingModel embeddingModel) {

        return args -> {

            //String consulta = "Automóvil";
            String consulta = "Pinsa";

            List<String> documentos = List.of(
                    "Vehículo",
                    "Coche",
                    "Carro",
                    "Bicicleta",
                    "Avión",
                    "Pizza"
            );

            float[] consultaVector = embeddingModel.embed(consulta);

            System.out.println("\nConsulta: " + consulta);
            System.out.println("--------------------------------");

            documentos.stream()
                    .map(doc -> {
                        float[] vector = embeddingModel.embed(doc);
                        double score = cosineSimilarity(consultaVector, vector);
                        return new Resultado(doc, score);
                    })
                    .sorted((a, b) -> Double.compare(b.score(), a.score()))
                    .forEach(r ->
                            System.out.printf("%-12s -> %.4f%n",
                                    r.texto(),
                                    r.score()));
        };
    }



    //@Bean
    CommandLineRunner embedding3(EmbeddingModel embeddingModel) {

        return args -> {

            List<String> textos = List.of(
                    "Automóvil",
                    "Vehículo",
                    "Coche",
                    "Carro",
                    "Bicicleta",
                    "Avión",
                    "Pizza"
            );

            List<float[]> vectores = new ArrayList<>();

            // Generar embeddings
            for (String texto : textos) {
                vectores.add(embeddingModel.embed(texto));
            }

            System.out.println("\n===== MATRIZ DE SIMILITUD =====\n");

            for (int i = 0; i < textos.size(); i++) {

                for (int j = i + 1; j < textos.size(); j++) {

                    double similitud = cosineSimilarity(
                            vectores.get(i),
                            vectores.get(j)
                    );

                    System.out.printf(
                            "%-12s ↔ %-12s = %.4f%n",
                            textos.get(i),
                            textos.get(j),
                            similitud
                    );
                }
            }

        };
    }

    //@Bean
    CommandLineRunner embedding2(EmbeddingModel embeddingModel) {

        return args -> {

            List<String> textos = List.of(
                    "Inteligencia Artificial",
                    "Artificial Intelligence",
                    "Machine Learning",
                    "Deep Learning",
                    "Perro"
            );

            List<float[]> vectores = new ArrayList<>();

            // Generar embeddings
            for (String texto : textos) {

                float[] vector = embeddingModel.embed(texto);

                vectores.add(vector);
            }

            System.out.println("===== MATRIZ DE SIMILITUD =====");

            for (int i = 0; i < textos.size(); i++) {

                for (int j = i + 1; j < textos.size(); j++) {

                    double similitud = cosineSimilarity(
                            vectores.get(i),
                            vectores.get(j)
                    );

                    System.out.printf(
                            "%-25s ↔ %-25s = %.4f%n",
                            textos.get(i),
                            textos.get(j),
                            similitud
                    );
                }
            }

        };
    }


    //@Bean
    CommandLineRunner embeddingCompare(EmbeddingModel embeddingModel) {
        return args -> {

            float[] springAi =embeddingModel.embed("Spring AI integra modelos de inteligencia artificial con aplicaciones Java.");
            float[] frameworkJava =embeddingModel.embed("Framework para desarrollar aplicaciones inteligentes en Java.");

            float[] pizza =embeddingModel.embed("La pizza hawaiana contiene piña y jamón.");
            float[] perro =embeddingModel.embed("Perro Peruano.");

            double similitudTecnica =cosineSimilarity(springAi, frameworkJava);

            double similitudPizza =cosineSimilarity(springAi, pizza);
            double similitudPerro =cosineSimilarity(springAi, perro);
            double similitudsimilitudPizzaPerro =cosineSimilarity(pizza, perro);

            System.out.printf(
                    "Spring AI vs Framework Java: %.4f%n",
                    similitudTecnica
            );

            System.out.printf(
                    "Spring AI vs Pizza: %.4f%n",
                    similitudPizza
            );
            System.out.printf(
                    "Spring AI vs Perro: %.4f%n",
                    similitudPerro
            );

            System.out.printf(
                    "Pizza vs Perro: %.4f%n",
                    similitudsimilitudPizzaPerro
            );
        };
    }

    private static double cosineSimilarity(float[] a, float[] b) {

        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "Los vectores deben tener la misma dimensión."
            );
        }

        double productoPunto = 0.0;
        double normaA = 0.0;
        double normaB = 0.0;

        for (int i = 0; i < a.length; i++) {
            productoPunto += a[i] * b[i];
            normaA += a[i] * a[i];
            normaB += b[i] * b[i];
        }

        if (normaA == 0.0 || normaB == 0.0) {
            return 0.0;
        }

        return productoPunto /
                (Math.sqrt(normaA) * Math.sqrt(normaB));
    }


    //@Bean
    CommandLineRunner embedding(EmbeddingModel embeddingModel) {
        return args -> {

            EmbeddingResponse response =embeddingModel.embedForResponse(
                            //List.of("¿Qué es Spring AI?")
                            List.of("¿Qué es Arquitectura Hexagonal?")
                    );
            float[] vector = response
                    .getResults()
                    .get(0)
                    .getOutput();

            System.out.println("\nEMBEDDING GENERADO");
            System.out.println("Dimensión: " + vector.length);
            System.out.println("Primeros 10 valores:");

            for (int i = 0; i < Math.min(10, vector.length); i++) {
                System.out.printf(
                        "[%d] %.6f%n",
                        i,
                        vector[i]
                );
            }
        };
    }


}
