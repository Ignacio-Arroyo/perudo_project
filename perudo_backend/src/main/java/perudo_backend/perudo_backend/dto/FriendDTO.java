package perudo_backend.perudo_backend.dto;

import java.util.Objects;

public class FriendDTO {
    private Long id;
    private String username;
    private String friendCode; // Utile pour que l'utilisateur puisse le partager
    // Ajoutez d'autres champs pertinents si nécessaire, comme un statut en ligne, etc.
    // MAIS PAS la List<FriendDTO> friends de cet ami pour éviter la récursion.

    public FriendDTO(Long id, String username, String friendCode) {
        this.id = id;
        this.username = username;
        this.friendCode = friendCode;
    }

    // Getters (et Setters si nécessaires, mais souvent les DTOs sont immuables après création)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFriendCode() {
        return friendCode;
    }

    public void setFriendCode(String friendCode) {
        this.friendCode = friendCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendDTO friendDTO = (FriendDTO) o;
        return Objects.equals(id, friendDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
