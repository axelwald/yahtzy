

import java.util.*;

public class yahtzee {
    static final Scanner scanner = new Scanner(System.in);
    static final Random random = new Random();
    static LinkedHashMap<String, Integer> list_of_players = new LinkedHashMap<>();

    public static void main(String[] args) {
        init();
        while (list_of_players.size() > 1) {
            update();
        }
        declareWinner();
        scanner.close();
    }

    private static void init() {
        System.out.println("How many players: ");
        int number_of_players;
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
        Iterator<Map.Entry<String, Integer>> iterator = list_of_players.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> player = iterator.next();
            if (player.getValue() <= 0) {
                iterator.remove();
                continue;
            }
    
            System.out.format("Current player turn: %s\n", player.getKey());
            List<Integer> pickedDice = new ArrayList<>();
            List<Integer> diceResults = rollDices(6); // Start by rolling 6 dice.
    
            while (!diceResults.isEmpty()) { // Continue while there are dice left to pick.
                System.out.println("Dice results: " + diceResults);
                System.out.println("Pick dice to keep (enter indices separated by commas):");
                List<Integer> newPicks = pickDice(diceResults); // Pick dice based on user input.
                pickedDice.addAll(newPicks);
    
                if (diceResults.isEmpty()) { // No more dice left to roll.
                    break;
                }
                diceResults = rollDices(diceResults.size()); // Re-roll the remaining dice.
            }
    
            int sum = pickedDice.stream().mapToInt(Integer::intValue).sum(); // Sum of picked dice.
            System.out.println("Total score from picked dice: " + sum);
            applyScoringRules(player, sum);
    
            if (player.getValue() <= 0) {
                System.out.println(player.getKey() + " has been eliminated!");
                iterator.remove();
            } else {
                System.out.format("%s's total score: %d\n", player.getKey(), player.getValue());
            }
        }
    }
    
    private static void applyScoringRules(Map.Entry<String, Integer> player, int score) {
        // Implement scoring rules based on game specifications.
        if (score == 30) {
            System.out.println("You hit 30 exactly! No points lost or gained.");
        } else if (score < 30) {
            int pointsLost = 30 - score;
            player.setValue(player.getValue() - pointsLost);
            System.out.format("You scored under 30. You lose %d points.\n", pointsLost);
        } else {
            int pointsOver = score - 30;
            System.out.format("You scored over 30. Try to get as many %d's as possible!\n", pointsOver);
            int count = rollForNumber(pointsOver);
            System.out.format("You rolled %d %d's.\n", count, pointsOver);
    
            System.out.println("Choose an opponent to lose points:");
            String opponentName = chooseOpponent(player.getKey());
            int pointsToDeduct = count * pointsOver;
            int newScore = list_of_players.get(opponentName) - pointsToDeduct;
            list_of_players.put(opponentName, newScore);
            System.out.format("%s loses %d points.\n", opponentName, pointsToDeduct);
        }
    }
    

    private static List<Integer> rollDices(int number) {
        List<Integer> dices = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            dices.add(random.nextInt(6) + 1);
        }
        return dices;
    }

    private static List<Integer> pickDice(List<Integer> diceResults) {
        String[] indices = scanner.nextLine().split(",");
        Set<Integer> uniqueIndices = new HashSet<>();
        List<Integer> pickedDice = new ArrayList<>();
        try {
            for (String index : indices) {
                int idx = Integer.parseInt(index.trim()) - 1;
                if (idx >= 0 && idx < diceResults.size() && uniqueIndices.add(idx)) {
                    pickedDice.add(diceResults.get(idx));
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. No dice picked this round.");
        }
        diceResults.removeAll(pickedDice);
        return pickedDice;
    }

    private static int rollForNumber(int number) {
        int count = 0;
        int dieResult;
        do {
            dieResult = random.nextInt(6) + 1; // Roll a die
            if (dieResult == number) {
                count++; // Increment count if the die shows the target number
            }
        } while (dieResult == number); // Continue rolling while the result is the target number
        return count; // Return the total count of the target number obtained
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
