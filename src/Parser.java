import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.zip.CRC32;

/**
 * Created by fd_huseynov on 20.12.2016.
 */
public class Parser {

    private static int NUMBER_OF_HASHES = 10;

    String path;
    String fileContent;
    Set<Long> shingles;
    ArrayList<Long> signature;
    int[] randomA, randomB;

    public Parser(String path, int[] a, int[] b) {
        this.path = path;
        randomA = a;
        randomB = b;
        shingles = new LinkedHashSet<>();
        signature = new ArrayList<Long>();
        try {
            fileContent = readFile();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private String readFile() throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        String str = new String(encoded, Charset.defaultCharset());
        return str;
    }

    private void shingleFileContent() {
        String shingle;
        long crcValue;
        String[] allWords = fileContent.split(" ");
        CRC32 crc32 = new CRC32();
        for (int i = 0; i < allWords.length - 2; i++) {
            shingle = allWords[i] + " "  + allWords[i + 1] + " " + allWords[i + 2];
            byte[] bytes = shingle.getBytes();
            crc32.update(bytes, 0, bytes.length);
            crcValue = crc32.getValue();
            shingles.add(crcValue);
            crc32.reset();
        }
    }

    private void createMinHashSignature() {
        long nextPrime = 4294967311L;
        long shingleId;
        long hashCode, minHashCode;
        Iterator iterator;
        for (int i = 0; i < NUMBER_OF_HASHES; i++) {
            minHashCode = nextPrime + 1;
            iterator= shingles.iterator();
            while (iterator.hasNext()) {
                shingleId = Long.parseLong(iterator.next().toString());
                hashCode = (randomA[i] * shingleId + randomB[i]) % nextPrime;
                if (hashCode < minHashCode)
                    minHashCode = hashCode;
            }
            signature.add(minHashCode);
        }
    }

    public void prepareDocument() {
        shingleFileContent();
        createMinHashSignature();
    }

    public void findSimilarDocuments() throws IOException {
        System.out.println("Finding similar documents...");
        long t0 = System.currentTimeMillis();
        String[] wordSet;
        String documentId;
        ArrayList<Long> documentSignature = new ArrayList<>();
        FileInputStream fis = new FileInputStream(new File("out.txt"));
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String document = null;
        int count;
        double jSimilarity;

        while ((document = br.readLine()) != null) {
            count = 0;
            wordSet = document.split(" ");
            documentId = wordSet[0];
            for (int i = 1; i < wordSet.length; i++) {
                documentSignature.add(Long.parseLong(wordSet[i]));
                if (Long.parseLong(wordSet[i]) == signature.get(i-1))
                    count = count + 1;
            }
            jSimilarity = (double)count / (double)NUMBER_OF_HASHES;
            if (jSimilarity > 0.5) {
                System.out.println(documentId + ": " + jSimilarity);
            }
        }
        long finalTime = System.currentTimeMillis() - t0;
        System.out.println("Similar documents found in " + finalTime + " milliseconds");
    }
}
