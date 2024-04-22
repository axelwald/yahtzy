

import java.util.*;
//ba importerar alla util classer som typ scanner och random


public class yahtzee {
    static final Scanner scanner = new Scanner(System.in);
    static final Random random = new Random();
    static LinkedHashMap<String, Integer> list_of_players = new LinkedHashMap<>();
    //linked hashmap för att kunna associera spelarens namn med deras score, linked för att hålla kvar ordningen av spelarna

    public static void main(String[] args) {
        //init funktion för att starta igång spelet
        init();
        //main game loop, slutar ifall det finns bara en spelare kvar
        while (list_of_players.size() > 1) {
            update();
        }
        //self explanatory
        declareWinner();
        scanner.close();
    }
    //initialiserar spelet
    private static void init() {
        System.out.println("How many players: ");
        int number_of_players;
        //I en try catch loop för att fortsätta försöka få ett giltigt nummer, 
        while (true) {
            try {
                number_of_players = Integer.parseInt(scanner.nextLine());
                if (number_of_players > 0) {
                    break;
                }
                System.out.println("Please enter a positive number.");
            } catch (NumberFormatException e) {
                System.out.println("Please give a valid number");
            }
        }
        /*går över hur många spelare det skulle vara och göra samma namn prompt för att lägga till i linkedhashmapen
         * scanner.nextLine().trim() ger oss bara karaktärerna och inte extra space föra och efter stringen för att ge ett "clean" namn
         * samma sak som line 28 fast använder mig av isEmpty istället för försöka fånga ett error
         * 
        */
        for (int i = 0; i < number_of_players; i++) {
            System.out.format("Enter player %d name: ", i + 1);
            String name = scanner.nextLine().trim();
            while (name.isEmpty()) {
                System.out.println("Name cannot be empty. Please enter a valid name.");
                name = scanner.nextLine().trim();
            }
            list_of_players.put(name, 30);
        }
    }

    private static void update() {
        /*
         * main game loopen som är spelet
         * Gör en iterator som returnar typen Map.Entry<String, Integer> vilket är i det här fallet key och value från våran linked hashmap
         * iteratorn ger bara ett sätt att loopa över en hashmap, finns många andra alternativ som till exempel
         * ```java
         * list_of_players.forEach((key,value) -> vad som ska göras med datan)
         * ```
         * 
         * 
         */
        Iterator<Map.Entry<String, Integer>> iterator = list_of_players.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> player = iterator.next();

            //tar bort spelaren ifall den inte har några poäng kvar
            if (player.getValue() <= 0) {
                iterator.remove();
                continue;
            }
    
            System.out.format("Current player turn: %s\n", player.getKey());

            //skapar en list för att hålla alla våra valda dice i 
            List<Integer> pickedDice = new ArrayList<>();
            List<Integer> diceResults = rollDices(6); // börjar med att generera nyad ice
    
            while (!diceResults.isEmpty()) { //medans vi inte har 6 dice
                List<Integer> newPicks = pickDice(diceResults); // välja dice loopen
                pickedDice.addAll(newPicks);
    
                if (diceResults.isEmpty()) { // exit out ifall vi har inga mer dice
                    break;
                }
                diceResults = rollDices(diceResults.size()); //rerolla för nästa loop
            }
            /*  datastream för att få summan av våra dice 
                fungera som en typ som en lambda funktion
            */
            int sum = pickedDice.stream().mapToInt(Integer::intValue).sum(); 
            System.out.println("Total score from picked dice: " + sum);
            applyScoringRules(player, sum); //kollar vad som händer till spelaren baserad på poängen
    
            if (player.getValue() <= 0) {
                System.out.println(player.getKey() + " has been eliminated!");
                iterator.remove();
            } else {
                System.out.format("%s's total score: %d\n", player.getKey(), player.getValue());
            }
        }
    }
    
    private static void applyScoringRules(Map.Entry<String, Integer> player, int score) {
        // reglerna
        if (score == 30) {
            System.out.println("You hit 30 exactly! No points lost or gained.");
        } else if (score < 30) {
            int pointsLost = 30 - score;
            player.setValue(player.getValue() - pointsLost);
            System.out.format("You scored under 30. You lose %d points.\n", pointsLost);
        } else {
            int pointsOver = score - 30;
            System.out.format("You scored over 30. Try to get as many %d's as possible!\n", pointsOver);
            int count = rollForNumber(pointsOver); // kålla hur många tärningar som spelaren får som blir subtraherad till vald person
            System.out.format("You rolled %d %d's.\n", count, pointsOver);
            
            if (count > 0) {
                System.out.println("Choose an opponent to lose points:");
                String opponentName = chooseOpponent(player.getKey());
                int pointsToDeduct = count * pointsOver;
                int newScore = list_of_players.get(opponentName) - pointsToDeduct;
                list_of_players.put(opponentName, newScore);
                System.out.format("%s loses %d points.\n", opponentName, pointsToDeduct);
    
            }

        }
    }
    
    //enkel funktion för att få dices
    private static List<Integer> rollDices(int number) {
        List<Integer> dices = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            dices.add(random.nextInt(6) + 1); //+1 för att se till att numret är 1-6
        }
        return dices;
    }
    private static List<Integer> pickDice(List<Integer> diceResults) {
        List<Integer> pickedDice = new ArrayList<>();
        boolean validInput = false;
    
        while (!validInput) {
            System.out.println("Current dice results: " + diceResults);
            System.out.println("Enter indices of dice to keep (1-" + diceResults.size() + "), separated by commas:");
            String input = scanner.nextLine();
            String[] indices = input.split(",");
            //splitar numrerna med , så ifall man ger den en string "1,3,5" får du tillbaka en array av ["1","3","5"]
            Set<Integer> processedIndices = new HashSet<>();
            //hashset som håller i våra indices, eftersom vi kan inte ha samma index från samma rull, så använder vi av oss en hashset så man kan bara få en tärnign en gång
            List<Integer> indexList = new ArrayList<>();
            //skit jag behövde göra för att se till att indexen inte ändrades medans man valde

    
            try {
                for (String index : indices) {
                    int idx = Integer.parseInt(index.trim()) - 1; // konverterar till noll baserad index
                    //kollar limits så att värdet inte är mer eller mindre en arrayen, och att ifall vi lägger till den i hashsetten så är det ett nytt värde och inte ett redan existerande
                    if (idx >= 0 && idx < diceResults.size() && processedIndices.add(idx)) {
                        indexList.add(idx);
                    } else {
                        throw new IndexOutOfBoundsException("Index out of valid range."); // tvinga catch 
                    }
                }
                //ifall alla indexes är bra, ta bort dice från dice results och lägg till dem till våran picked dice array
                Collections.sort(indexList, Collections.reverseOrder());
                for (int idx : indexList) {
                    pickedDice.add(diceResults.get(idx));
                    diceResults.remove(idx);
                }
                validInput = true; // sant för att input är valid
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
                System.out.println("Invalid input. Please enter valid numeric indices within the given range, separated by commas.");
                // sätter inte till true, loopar
            }
        }
    
        return pickedDice;
    }
    
    

    private static int rollForNumber(int number) {
        int count = 0;
    
        while(true)  {
            boolean shouldContinue = false;  // Flag to determine whether to continue rerolling.

            System.out.println("Count: "+ count);
           
            List<Integer> dice = rollDices(6-count);  // Start by rolling 6 dice.
            System.out.println("Rolled: " + dice);  // Display dice roll results for transparency.

            int matchesThisRound = 0;
    
            // Count and determine if any dice show the target number.
            for (int die : dice) {
                System.out.format("%d %b",die,die==number);
                if (die == number) {
                    matchesThisRound++;
                    shouldContinue = true;  // Set flag to true if target number is found.
                }
            }
    
            count += matchesThisRound;  // Update total count of matches.
    
            // Check the reroll condition based on the last roll.

            if (shouldContinue) {
                System.out.format("Count: %d \n Should continue: %b",count,shouldContinue);
                dice = rollDices(dice.size() - matchesThisRound);  // Roll only non-matching dice.
                continue;
            } else {
                break;  // Exit loop if no target number was rolled or all dice matched.
            }
        } 

        return count;  // Return the total count of the target number obtained.
    }
    
    
    private static String chooseOpponent(String currentPlayer) {
        List<String> opponents = new ArrayList<>(list_of_players.keySet());
        opponents.remove(currentPlayer);
        if (opponents.size() == 1) {
            return opponents.get(0);
        } else {
            System.out.println("Available opponents: " + opponents);
            String selected;
            do {
                selected = scanner.nextLine();
                if (opponents.contains(selected)) {
                    return selected;
                }
                System.out.println("Please select a valid opponent.");
            } while (true);
        }
    }

    private static void declareWinner() {
        if (list_of_players.size() == 1) {
            String winner = list_of_players.keySet().iterator().next();
            System.out.format("Winner is %s\n", winner);
        } else {
            System.out.println("No winner could be determined.");
        }
    }
}
