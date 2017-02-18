import java.io.*;
import java.util.*;
import java.util.zip.CRC32;
/**
 * Created by Fuad Huseynov on 20.12.2016.
 */
public class Indexer {

    private int NUM_OF_DOCS;
    private static int NUM_OF_HASHES = 10;
    ArrayList<long[]> signatures;

    int[] randomNumbersA, randomNumbersB;

    //Constructor
    public Indexer(int numOfDocs) {
        NUM_OF_DOCS = numOfDocs;
        randomNumbersA = getRandomNumbers(NUM_OF_HASHES);
        randomNumbersB = getRandomNumbers(NUM_OF_HASHES);
        signatures = new ArrayList<>();
    }

    public int[] getRandomNumbersA() {
        return randomNumbersA;
    }

    public int[] getRandomNumbersB() {
        return randomNumbersB;
    }

    //Shingles and creates MinHash signatures for each document
    public void index() throws FileNotFoundException, IOException {
        /*
        * Producing a shingle set for all documents
        * CRC32 hash function is applied to each shingle in order to get a unique integer value for each
        * */
        System.out.println("\nShingling documents...");

        HashMap<String, Set<Long>> docsAsShingles = new LinkedHashMap<String, Set<Long>>();
        ArrayList<String> docNames = new ArrayList<>();

        //Create the file direction string according to the given command line argument
        String fileDir = "../data/articles_" + NUM_OF_DOCS + ".train";
        BufferedReader bf = new BufferedReader(new FileReader(fileDir));

        long start = System.currentTimeMillis();

        int totalShingles = 0;
        String docId;
        Set<Long> shinglesInDoc;
        String shingle;
        CRC32 crc32 = new CRC32();

        //divide each documents into shingles
        for (int i = 0; i < NUM_OF_DOCS; i++) {
            String[] words = bf.readLine().split(" ");
            docId = words[0];
            docNames.add(docId);

            shinglesInDoc = new LinkedHashSet<>();
            //create shingleId for each shingle and store in ShinglesInDoc
            for (int j = 1; j < words.length - 2; j++) {
                //create 3-shingle
                shingle = words[j] + " "  + words[j + 1] + " " + words[j + 2];
                //get corresponging crc32 value of the shingle
                byte[] bytes = shingle.getBytes();
                crc32.update(bytes, 0, bytes.length);
                long crcValue = crc32.getValue();
                //System.out.print(shingle + ": ");
                //System.out.println(crcValue);
                //store the value in the set
                shinglesInDoc.add(crcValue);
                crc32.reset();
            }
            //map the shingle set to its corresponding document id
            docsAsShingles.put(docId, shinglesInDoc);
            //System.out.println(docId + "\t" + docsAsShingles.get(docId));
            totalShingles = totalShingles + (words.length - 2);
        }

        long finish = System.currentTimeMillis() - start;

        System.out.println("Shingling " + NUM_OF_DOCS + " docs took " + finish + " milliseconds");
        System.out.println("Average shingles per document: " + (totalShingles / NUM_OF_DOCS));

        /*
        * Create MinHash signatures for all documents
        * */
        System.out.println("\nGenerating MinHash signatures for all documents...");
        long start1 = System.currentTimeMillis();

        long nextPrime = 4294967311L;

        Iterator iterator;

        //set of signatures for all documents
        String theLine ="";

        Set<Long> shingleIDSet;
        long shingleId;
        long hashCode;
        long minHashCode;
        int counter = 1;

        File fout = new File("out.txt");
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        Set theEntrySet = docsAsShingles.entrySet();
        Iterator iter = theEntrySet.iterator();

        while (iter.hasNext()) {
            theLine = "";
            Map.Entry mapEntry = (Map.Entry)iter.next();
            shingleIDSet = (Set<Long>) mapEntry.getValue();
            theLine = theLine + "" + mapEntry.getKey().toString();
            //System.out.println("\n\n" + "" + mapEntry.getKey().toString() +"\n" + shingleIDSet);

            for (int j = 0; j < NUM_OF_HASHES; j++) {
                minHashCode = nextPrime + 1;
                //System.out.println(minHashCode);
                iterator = shingleIDSet.iterator();
                while(iterator.hasNext()) {
                    shingleId = Long.parseLong(iterator.next().toString());
                    hashCode = (randomNumbersA[j] * shingleId + randomNumbersB[j]) % nextPrime;
                    if (hashCode < minHashCode)
                        minHashCode = hashCode;
                }
                theLine = theLine + " " + minHashCode;
            }
            bw.write(theLine);
            bw.newLine();
        }
        bw.close();

        finish = System.currentTimeMillis() - start1;
        System.out.println("Generating MinHash signatures took " + finish + " milliseconds");
    }


    private int[] getRandomNumbers(int number_of_hashes) {
        int maxShingleId = (int)Math.pow(2, 32);
        maxShingleId = maxShingleId - 1;
        int[] randomList = new int[number_of_hashes];
        int randomNumber;
        int count;

        for (int k = 0; k < number_of_hashes; k++) {
            count = 0;
            Random random = new Random();
            randomNumber = random.nextInt(maxShingleId) + 0;
            for (int l = 0; l < k; l++) {
                if (randomList[l] == randomNumber)
                    count++;
            }
            if (count == 0)
                randomList[k] = randomNumber;
            else
                k--;
        }

        return randomList;

    }

}
