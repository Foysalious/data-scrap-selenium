package scraper;

import com.google.gson.Gson;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// Class representing a node in the AVL tree
class AVLNode {
    String word;  // The word stored in the node
    int frequency; // Frequency of the word
    AVLNode left; // Left child
    AVLNode right; // Right child
    int height;   // Height of the node

    AVLNode(String word) {
        this.word = word;
        this.frequency = 1; // Initialize frequency to 1
        this.height = 1; // Initialize height
    }
}

// Class implementing the AVL tree for autocomplete functionality
class Assignment2 {
    private AVLNode root; // Root of the AVL tree

    // Insert a word into the AVL tree
    public AVLNode insert(AVLNode node, String word) {
        if (node == null) {
            return new AVLNode(word); // Create a new node
        }

        // Perform normal BST insert
        if (word.compareTo(node.word) < 0) {
            node.left = insert(node.left, word);
        } else if (word.compareTo(node.word) > 0) {
            node.right = insert(node.right, word);
        } else {
            node.frequency++; // Increment frequency if word already exists
            return node; // Return unchanged node
        }

        // Update the height of the ancestor node
        node.height = 1 + Math.max(getHeight(node.left), getHeight(node.right));

        // Get the balance factor
        int balance = getBalance(node);

        // Perform rotations to balance the tree
        if (balance > 1 && word.compareTo(node.left.word) < 0) {
            return rightRotate(node); // Left Left case
        }
        if (balance < -1 && word.compareTo(node.right.word) > 0) {
            return leftRotate(node); // Right Right case
        }
        if (balance > 1 && word.compareTo(node.left.word) > 0) {
            node.left = leftRotate(node.left); // Left Right case
            return rightRotate(node);
        }
        if (balance < -1 && word.compareTo(node.right.word) < 0) {
            node.right = rightRotate(node.right); // Right Left case
            return leftRotate(node);
        }

        return node; // Return the (potentially) new root
    }

    // Helper method to perform a right rotation
    private AVLNode rightRotate(AVLNode y) {
        AVLNode x = y.left;
        AVLNode T2 = x.right;

        // Perform rotation
        x.right = y;
        y.left = T2;

        // Update heights
        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;
        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;

        return x; // Return the new root
    }

    // Helper method to perform a left rotation
    private AVLNode leftRotate(AVLNode x) {
        AVLNode y = x.right;
        AVLNode T2 = y.left;

        // Perform rotation
        y.left = x;
        x.right = T2;

        // Update heights
        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;
        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;

        return y; // Return the new root
    }

    // Get the height of a node
    private int getHeight(AVLNode node) {
        return node == null ? 0 : node.height;
    }

    // Get the balance factor of a node
    private int getBalance(AVLNode node) {
        return node == null ? 0 : getHeight(node.left) - getHeight(node.right);
    }

    // Search for words with a given prefix
    public List<String> autocomplete(AVLNode node, String prefix) {
        List<String> results = new ArrayList<>();
        searchWithPrefix(node, prefix, results);
        return results;
    }

    // Recursive method to search for words with the given prefix
    private void searchWithPrefix(AVLNode node, String prefix, List<String> results) {
        if (node == null) {
            return;
        }

        // Check if the current word starts with the prefix
        if (node.word.startsWith(prefix)) {
            results.add(node.word); // Add the word to results
        }

        // Search left and right subtrees
        searchWithPrefix(node.left, prefix, results);
        searchWithPrefix(node.right, prefix, results);
    }

    // Method to initiate insertion of words
    public void insertWord(String word) {
        root = insert(root, word.toLowerCase()); // Insert word in lowercase
    }

    // Method to initiate autocomplete functionality
    public List<String> autocomplete(String prefix) {
        return autocomplete(root, prefix.toLowerCase()); // Get autocomplete suggestions
    }
}

// Class to represent the entire JSON structure
class ScrapedData {
    List<DictionaryEntry> packages; // List of package entries
}

// Class to represent each package entry
class DictionaryEntry {
    String title; // Title of the package
    String description; // Description of the package
    String price; // Price of the package
    String link; // Link to the package (optional)
}

class Main {
    public static void main(String[] args) {
        Assignment2 avlTree = new Assignment2(); // AVL tree for autocomplete
        Gson gson = new Gson(); // Gson instance for JSON parsing

        // Debugging the current directory
        System.out.println("Current working directory: " + System.getProperty("user.dir"));

        try (FileReader reader = new FileReader("scraped_data.json")) {
            // Deserialize JSON into ScrapedData object
            ScrapedData scrapedData = gson.fromJson(reader, ScrapedData.class);

            // Insert titles from packages into the AVL tree
            for (DictionaryEntry entry : scrapedData.packages) {
                if (entry.title != null) {
                    avlTree.insertWord(entry.title); // Insert lowercase for uniformity
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle IO exceptions
        } catch (com.google.gson.JsonSyntaxException e) {
            System.err.println("Malformed JSON: " + e.getMessage()); // Handle JSON syntax errors
        }

        // Create a scanner for user input
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a prefix to autocomplete (type 'exit' to quit):");

        // Loop to accept user input until 'exit' is entered
        while (true) {
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("exit")) {
                break; // Exit the loop if the user types 'exit'
            }

            // Get autocomplete results and display them
            List<String> results = avlTree.autocomplete(input);
            if (results.isEmpty()) {
                System.out.println("No suggestions found for: " + input);
            } else {
                System.out.println("Autocomplete results for '" + input + "': " + results);
            }
        }

        scanner.close(); // Close the scanner
    }
}