package perudo_backend.perudo_backend.dto;

import perudo_backend.perudo_backend.Dice;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DiceRollDTO {
    private List<Integer> values;
    private String playerId;  // Changed from int to String to match entity ID type

    public static DiceRollDTO fromDiceList(List<Dice> dice) {
        List<Integer> values = dice.stream()
            .map(Dice::getValue)
            .collect(Collectors.toList());
        return new DiceRollDTO(values);
    }

    public DiceRollDTO(List<Integer> values) {
        this.values = values;
    }

    public DiceRollDTO(int[] values) {
        this.values = Arrays.stream(values).boxed().collect(Collectors.toList());
    }

    public DiceRollDTO(String playerId, List<Integer> values) {  // Updated constructor
        this.playerId = playerId;
        this.values = values;
    }

    // Getters and setters
    public List<Integer> getValues() {
        return values;
    }

    public void setValues(List<Integer> values) {
        this.values = values;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    // Utility method to convert List<Integer> to int[]
    public int[] getValuesAsArray() {
        return values.stream().mapToInt(Integer::intValue).toArray();
    }
}
