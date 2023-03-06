import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BooleanSearchEngine implements SearchEngine {
    protected Map<String, List<PageEntry>> indexing = new HashMap<>();


    public BooleanSearchEngine(File pdfsDir) throws IOException {

        File[] listPdfFiles = getListFiles(pdfsDir, ".pdf");
        if (listPdfFiles != null) {
            for (File pdfFile : listPdfFiles) {
                scan(pdfFile);
            }
            indexing.values().forEach(Collections::sort);
        }
    }


    private void scan(File pdfFile) throws IOException {
        try (var doc = new PdfDocument(new PdfReader(pdfFile))) {
            for (int i = 1; i < doc.getNumberOfPages() + 1; i++) {
                var pdfPage = doc.getPage(i);
                var text = PdfTextExtractor.getTextFromPage(pdfPage);
                var words = text.split("\\P{IsAlphabetic}+");
                Map<String, Integer> freqs = new HashMap<>();

                for (var word : words) {
                    if (word.isEmpty()) {
                        continue;
                    }
                    word = word.toLowerCase();
                    freqs.put(word, freqs.getOrDefault(word, 0) + 1);
                }


                for (Map.Entry<String, Integer> entry : freqs.entrySet()) {
                    List<PageEntry> pageEntries;
                    String key = entry.getKey();
                    Integer value = entry.getValue();
                    if (indexing.containsKey(key)) {
                        pageEntries = indexing.get(key);
                    } else {
                        pageEntries = new ArrayList<>();
                    }
                    pageEntries.add(new PageEntry(pdfFile.getName(), i, value));
                    indexing.put(key, pageEntries);

                }
            }
        }
    }


    @Override
    public List<PageEntry> search(String word) {
        List<PageEntry> list = indexing.get(word.toLowerCase());
        return Objects.requireNonNullElse(list, Collections.emptyList());

    }

    private File[] getListFiles(File pdfsDir, String typeOfFile) {
        File[] tempListFiles = pdfsDir.listFiles();
        if (tempListFiles == null) return null;
        List<File> files = new ArrayList<>();
        for (File file : tempListFiles) {
            if (file.isDirectory()) {
                File[] listFiles = getListFiles(file, typeOfFile);
                if (listFiles != null) files.addAll(List.of(listFiles));
            } else {
                if (file.getName().toLowerCase().endsWith(typeOfFile)) {
                    files.add(file);
                }
            }
        }
        return files.toArray(new File[0]);
    }
}
