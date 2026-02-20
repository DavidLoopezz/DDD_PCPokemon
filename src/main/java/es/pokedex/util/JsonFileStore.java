package es.pokedex.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class JsonFileStore {

    // ObjectMapper configurado para trabajar con JSON y tipos de fecha/hora Java
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    /**
     * Lee un fichero JSON y lo convierte en una lista del tipo indicado.
     * Si el archivo no existe o está vacío → crea "[] y devuelve lista vacía.
     */
    public <T> List<T> readList(Path path, TypeReference<List<T>> ref) {
        try {
            // Si no existe, lo crea con un array vacío
            if (!Files.exists(path)) {
                writeString(path, "[]");
                return Collections.emptyList();
            }

            // Lee todo el contenido del archivo
            byte[] bytes = Files.readAllBytes(path);
            String s = new String(bytes);

            // Si está vacío, lo inicializa como "[]"
            if (s.isBlank()) {
                writeString(path, "[]");
                return Collections.emptyList();
            }

            // Convierte JSON a lista usando Jackson
            return mapper.readValue(s, ref);

        } catch (IOException e) {
            throw new RuntimeException("Error reading JSON file " + path + ": " + e.getMessage(), e);
        }
    }

    /**
     * Escribe una lista en JSON en el archivo indicado.
     * Hace backup automático, y la escritura es atómica (usa .tmp y move).
     */
    public <T> void writeList(Path path, List<T> list) {
        try {
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(list);

            // Si el archivo ya existe, crea copia en /backups/
            if (Files.exists(path)) {
                Path backupDir = path.getParent().resolve("backups");

                if (!Files.exists(backupDir))
                    Files.createDirectories(backupDir);

                // Nombre del backup con fecha-hora
                String stamp = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss")
                );

                Path backup = backupDir.resolve(path.getFileName().toString() + ".bak-" + stamp);

                Files.copy(path, backup, StandardCopyOption.REPLACE_EXISTING);

            } else {
                // Si no existe el directorio destino, se crea
                if (!Files.exists(path.getParent()))
                    Files.createDirectories(path.getParent());
            }

            //  primero crea un archivo temporal
            Path tmp = path.resolveSibling(path.getFileName().toString() + ".tmp");

            writeString(tmp, json);

            // Reemplaza el archivo original de forma segura
            Files.move(tmp, path,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );

        } catch (IOException e) {
            throw new RuntimeException("Error writing JSON file " + path + ": " + e.getMessage(), e);
        }
    }

    /**
     * Escribe un String directamente a un archivo.
     * Usado por lectura vacía y escritura temporal.
     */
    private void writeString(Path path, String content) throws IOException {
        Files.write(path, content.getBytes(),
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }
}
