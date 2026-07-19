package AttendanceSystem.Service;

import AttendanceSystem.Model.CardDB;
import AttendanceSystem.Repository.CardRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class CardService {

    @Inject
    CardRepository repository;
 
    public List<CardDB> getAllCards() {
        try {
            return repository.getAllCardDB();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
 
    public List<CardDB> getAllCard(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return repository.getAllCardDB();
            }

            CardDB card = repository.getCardById(id);
            List<CardDB> result = new ArrayList<>();
            if (card != null) {
                result.add(card);
            }
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
 
    public List<CardDB> searchCardsByCardNo(String cardNo) {
        try {
            if (cardNo == null || cardNo.trim().isEmpty()) {
                return repository.getAllCardDB();
            }
            return repository.getCardsByCardNo(cardNo);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
 
    public boolean uploadImage(String cardId, byte[] imageData) {
        try {
            int result = repository.uploadImage(cardId, imageData);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
 
    public byte[] getImage(String cardId) {
        try {
            return repository.getImage(cardId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
 
    public CardDB getCardById(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return null;
            }
            return repository.getCardById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
 
    public boolean insertCard(CardDB card) {
        try {
            int result = repository.insertCard(card);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
 
    public boolean updateCard(CardDB card) {
        try {
            int result = repository.updateCard(card);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
 
    public boolean deleteCard(String id) {
        try {
            int result = repository.deleteCard(id);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}