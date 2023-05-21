import models.Pair;
import models.Point;
import models.Question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CrosswordGenerator {
    Crossword crossword;
    Dictionary dict;
    Map<String, Pair<Point, Character>> possibleVHash;
    Map<String, Pair<Point, Character>> possibleHHash;
    List<Pair<Point, Character>> possibleV;
    List<Pair<Point, Character>> possibleH;

    public CrosswordGenerator(String dictPath){
        this.dict = new Dictionary(dictPath);
    }

    public void initialize() throws Exception{
        dict.initialize();
        possibleVHash = new HashMap<>();
        possibleHHash = new HashMap<>();
        possibleV = new ArrayList<>();
        possibleH = new ArrayList<>();
    }

    public void addQuestionH(Question q, String answer, Set<String> toBeIntersections){
        crossword.addQuestionH(q, answer);

        for(int i = 0; i < q.length; i++){
            String key = "(" + q.y + "," + (q.x + i) + ")";
            if(toBeIntersections.contains(key)) {
                if(possibleVHash.containsKey(key)){
                    possibleV.remove(possibleVHash.get(key));
                    possibleVHash.remove(key);
                }
                if(possibleHHash.containsKey(key)){
                    possibleH.remove(possibleHHash.get(key));
                    possibleHHash.remove(key);
                }
                continue;
            }
            Pair<Point, Character> newPair = new Pair<Point,Character>(new Point(q.x + i, q.y), answer.charAt(i));
            possibleV.add(newPair);
            possibleVHash.put(key, newPair);
        }

    }

    public void addQuestionV(Question q, String answer, Set<String> toBeIntersections){
        crossword.addQuestionV(q, answer);

        for(int i = 0; i < q.length; i++){
            String key = "(" + (q.y + i) + "," + q.x + ")";
            if(toBeIntersections.contains(key)) {
                if(possibleVHash.containsKey(key)){
                    possibleV.remove(possibleVHash.get(key));
                    possibleVHash.remove(key);
                }
                if(possibleHHash.containsKey(key)){
                    possibleH.remove(possibleHHash.get(key));
                    possibleHHash.remove(key);
                }
                continue;
            };
            Pair<Point, Character> newPair = new Pair<Point,Character>(new Point(q.x, q.y + i), answer.charAt(i));
            possibleH.add(newPair);
            possibleHHash.put(key, newPair);
        }
    }

    public void generate(){
        crossword = new Crossword();

        List<String> unlocatedStrings = new ArrayList<>();
        for(String word : dict.wordlist){
            unlocatedStrings.add(word);
        }

        String starter = unlocatedStrings.remove(0);
        Set<String> toBeIntersections = new HashSet<>();
        addQuestionH(new Question(0, 0, starter.length()), starter, toBeIntersections);
        crossword.maxWidth = starter.length();
        crossword.maxHeight = 1;
        System.out.println("Initialized with starter: " + starter + " at (0,0)");        
        
        boolean success = false;
        boolean possible = true;
        boolean possible2 = true;
        boolean vertical = true;
        while(true){
            possible = false;
            success = false;

            // System.out.println("Remaining: " + unlocatedStrings.size());
            for(String word : unlocatedStrings){
                // System.out.println("Checking on: " + word);
                if(vertical){
                    for(Pair<Point, Character> p : possibleV){
                        int ypoint = p.first.loc.first;
                        int xpoint = p.first.loc.second;
                    
                        Character v = p.second;
                        success = false;

                        for(int i = 0; i < word.length(); i++){
                            if(v == word.charAt(i)){
                                int y = ypoint - i;
                                int x = xpoint;

                                Boolean canAdd = true;
                                toBeIntersections = new HashSet<>();
                                for(int j = 0; j < word.length(); j++){
                                    if(j == i) {
                                        toBeIntersections.add("(" + (y + j) + "," + x + ")");
                                        continue;
                                    };
                                    if(crossword.matrix.get("("+ (y + j) +","+ (x) +")") != null){
                                        if(crossword.matrix.get("("+ (y + j) +","+ (x) +")") != word.charAt(j)) {
                                            canAdd = false;
                                            break;
                                        };
                                        toBeIntersections.add("(" + (y + j) + "," + x + ")");
                                    }
                                    else{
                                        if(crossword.matrix.get("("+ (y + j) +","+ (x + 1) +")") != null){
                                            canAdd = false;
                                            break;
                                        }
                                        if(crossword.matrix.get("("+ (y + j) +","+ (x - 1) +")") != null){
                                            canAdd = false;
                                            break;
                                        }
                                    }
                                }
                                if(crossword.matrix.get("("+ (y + word.length()) +","+ (x) +")") != null) canAdd = false;
                                if(crossword.matrix.get("("+ (y - 1) +","+ (x) +")") != null) canAdd = false;

                                if(!canAdd) continue;

                                Question addedQuestion = new Question(y, x, word.length());
                                addQuestionV(addedQuestion, word, toBeIntersections);
                                vertical = false;

                                crossword.maxHeight = Math.max(crossword.maxHeight, y + word.length());
                                crossword.minHeight = Math.min(crossword.minHeight, y);

                                // System.out.println("Added vertical question: " + addedQuestion.toString() + " with word: " + word);
                                success = true;
                                break;
                            }
                        }

                        if(success) break;
                    }
                }
                else{
                    for(Pair<Point, Character> p : possibleH){
                        int ypoint = p.first.loc.first;
                        int xpoint = p.first.loc.second;

                        Character v = p.second;
                        success = false;

                        for(int i = 0; i < word.length(); i++){    
                            if(v == word.charAt(i)){
                                int y = ypoint;
                                int x = xpoint - i;

                                Boolean canAdd = true;
                                toBeIntersections = new HashSet<>();
                                for(int j = 0; j < word.length(); j++){
                                    if(j == i) {
                                        toBeIntersections.add("(" + (y) + "," + (x + j) + ")");
                                        continue;
                                    };
                                    if(crossword.matrix.get("("+ (y) +","+ (x + j) +")") != null){
                                        if(crossword.matrix.get("("+ (y) +","+ (x + j) +")") != word.charAt(j)) {
                                            canAdd = false;
                                            break;
                                        };
                                        toBeIntersections.add("(" + (y) + "," + (x + j) + ")");
                                    }
                                    else{
                                        if(crossword.matrix.get("("+ (y + 1) +","+ (x + j) +")") != null){
                                            canAdd = false;
                                            break;
                                        }
                                        if(crossword.matrix.get("("+ (y - 1) +","+ (x + j) +")") != null){
                                            canAdd = false;
                                            break;
                                        }
                                    }
                                }
                                if(crossword.matrix.get("("+ (y) +","+ (x + word.length()) +")") != null) canAdd = false;
                                if(crossword.matrix.get("("+ (y) +","+ (x - 1) +")") != null) canAdd = false;

                                if(!canAdd) continue;

                                Question addedQuestion = new Question(y, x, word.length());
                                addQuestionH(addedQuestion, word, toBeIntersections);
                                vertical = true;

                                // System.out.println("Added horizontal question: " + addedQuestion.toString() + " with word: " + word);
                                success = true;
                                break;
                            }
                        }

                        if(success) break;
                    }
                }

                if (success) {
                    possible = true;
                    possible2 = true;
                    unlocatedStrings.remove(word);
                    break;
                };
            }

            
            if(unlocatedStrings.isEmpty()){
                System.out.println("Every word has been added successfully");
                break;
            }

            if (!possible){
                if(possible2){
                    possible2 = false;
                    vertical = !vertical;
                }
                else{
                    System.out.println("Not possible to add more words");
                    break;
                }
            }
        }

        System.out.println("Done generating\n");
    }

    public static void main(String[] args) {
        long startTime = System.nanoTime();

        CrosswordGenerator cg = new CrosswordGenerator("dictionaryShorter.json");

        try{
            cg.initialize();
            cg.generate();
        }catch (Exception e){
            e.printStackTrace();
        }

        long elapsedTime = System.nanoTime() - startTime;
        System.out.println("Total execution time in millis: " + elapsedTime/1000000);

        if(cg.crossword.maxWidth - cg.crossword.minWidth > 200){
            System.out.println("Width: " + (cg.crossword.maxWidth - cg.crossword.minWidth));
            System.out.println("Height: " + (cg.crossword.maxHeight - cg.crossword.minHeight));
            System.out.println("Crossword is too big to print");
        }
        else{
            System.out.println("Width: " + (cg.crossword.maxWidth - cg.crossword.minWidth));
            System.out.println("Height: " + (cg.crossword.maxHeight - cg.crossword.minHeight));
            System.out.println("Density: " + cg.crossword.matrix.size() + "/" + ((cg.crossword.maxWidth - cg.crossword.minWidth) * (cg.crossword.maxHeight - cg.crossword.minHeight)));
            System.out.println("Score: " + (Double.parseDouble(((Integer) cg.crossword.matrix.size()).toString())/Double.parseDouble(((Integer)((cg.crossword.maxWidth - cg.crossword.minWidth) * (cg.crossword.maxHeight - cg.crossword.minHeight))).toString())));
            System.out.println("result: \n");
            cg.crossword.print();
        }
    }
}