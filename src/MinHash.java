import java.io.IOException;

/**
 * Created by fd_huseynov on 20.12.2016.
 */
public class MinHash {

    public static void main(String[] args) {
        if (args.length != 0) {
            int number_of_docs = Integer.parseInt(args[0]);
            if (number_of_docs == 100 || number_of_docs == 1000 || number_of_docs == 10000) {
                Indexer indexer = new Indexer(number_of_docs);
                int[] randomsA = indexer.getRandomNumbersA();
                int[] randomsB = indexer.getRandomNumbersB();
                try {
                    indexer.index();
                    Parser parser = new Parser("test.txt", randomsA, randomsB);
                    parser.prepareDocument();
                    parser.findSimilarDocuments();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Usage: java MinHash 100/1000/10000");
            }
        } else {
            System.out.println("Usage: java MinHash <number_of_documents>");
        }
    }

}
