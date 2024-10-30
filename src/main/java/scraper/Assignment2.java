// File: Assignment2.java
package scraper;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

// Node class to represent each word and frequency in the AVL Tree
class WordNode {
    String word;
    int frequency;
    WordNode left;
    WordNode right;
    int height;

    WordNode(String word, int frequency) {
        this.word = word;
        this.frequency = frequency;
        this.height = 1;
    }
}

// AVL Tree for storing and retrieving words with autocomplete functionality
class AutocompleteTree {
    private WordNode root;

    // Insert a word into the AVL tree with given frequency
    public WordNode insert(WordNode node, String word, int frequency) {
        if (node == null) return new WordNode(word, frequency);

        if (word.compareTo(node.word) < 0) {
            node.left = insert(node.left, word, frequency);
        } else if (word.compareTo(node.word) > 0) {
            node.right = insert(node.right, word, frequency);
        } else {
            node.frequency += frequency;
            return node;
        }

        node.height = 1 + Math.max(getHeight(node.left), getHeight(node.right));
        int balance = getBalance(node);

        if (balance > 1 && word.compareTo(node.left.word) < 0) return rotateRight(node);
        if (balance < -1 && word.compareTo(node.right.word) > 0) return rotateLeft(node);
        if (balance > 1 && word.compareTo(node.left.word) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }
        if (balance < -1 && word.compareTo(node.right.word) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    // Perform right rotation
    private WordNode rotateRight(WordNode y) {
        WordNode x = y.left;
        WordNode temp = x.right;
        x.right = y;
        y.left = temp;

        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;
        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;

        return x;
    }

    // Perform left rotation
    private WordNode rotateLeft(WordNode x) {
        WordNode y = x.right;
        WordNode temp = y.left;
        y.left = x;
        x.right = temp;

        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;
        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;

        return y;
    }

    private int getHeight(WordNode node) {
        return node == null ? 0 : node.height;
    }

    private int getBalance(WordNode node) {
        return node == null ? 0 : getHeight(node.left) - getHeight(node.right);
    }

    public void addWord(String word, int frequency) {
        root = insert(root, word.toLowerCase(), frequency);
    }

    public PriorityQueue<WordNode> getSuggestions(String prefix) {
        PriorityQueue<WordNode> suggestions = new PriorityQueue<>((a, b) -> b.frequency - a.frequency);
        searchPrefix(root, prefix.toLowerCase(), suggestions);
        return suggestions;
    }

    // Recursive search method to find nodes with words starting with the given prefix
    private void searchPrefix(WordNode node, String prefix, PriorityQueue<WordNode> suggestions) {
        if (node == null) return;

        if (node.word.startsWith(prefix)) {
            suggestions.add(node);
        }
        searchPrefix(node.left, prefix, suggestions);
        searchPrefix(node.right, prefix, suggestions);
    }
}

// Main class for JSON processing and autocomplete interaction
public class Assignment2 {
    public static void main(String[] args) {
        AutocompleteTree avlTree = new AutocompleteTree();
        Gson gson = new Gson();

        try (FileReader reader = new FileReader("scraped_data.json")) {
            ScrapedData scrapedData = gson.fromJson(reader, ScrapedData.class);
            for (DictionaryEntry entry : scrapedData.packages) {
                if (entry.title != null) {
                    avlTree.addWord(entry.title, 1);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Malformed JSON: " + e.getMessage());
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a prefix to autocomplete (type 'exit' to quit):");

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) break;

            PriorityQueue<WordNode> results = avlTree.getSuggestions(input);
            if (results.isEmpty()) {
                System.out.println("No suggestions found for: " + input);
            } else {
                System.out.println("Autocomplete results for '" + input + "': ");
                for (WordNode node : results) {
                    System.out.println(node.word + " (frequency: " + node.frequency + ")");
                }
            }
        }

        scanner.close();
    }
}

// Supporting classes for JSON structure
class ScrapedData {
    List<DictionaryEntry> packages;
}

class DictionaryEntry {
    String title;
    String description;
    String price;
    String link;
}