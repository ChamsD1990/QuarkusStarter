package AttendanceSystem.Service;

import AttendanceSystem.Model.TransactionLive;
import AttendanceSystem.Repository.TransactionLiveRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class TransactionService {

    @Inject
    TransactionLiveRepository repository; 
    
    public List<TransactionLive> getAllTransactions(String id) {
        try { 
            if (id == null || id.trim().isEmpty()) {
                return repository.getAllTransactions();
            } 
            TransactionLive transaction = repository.getTransactionById(id);
            List<TransactionLive> result = new ArrayList<>(); 
            if (transaction != null) {
                result.add(transaction);
            } 
            return result; 
        } catch (Exception e) {
            // System.err.println("❌ Error fetching transactions: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
 
    public List<TransactionLive> getAllTransactions() {
        try {
            return repository.getAllTransactions();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
 
    public TransactionLive getTransactionById(String id) {
        try {
            return repository.getTransactionById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
 
    public List<TransactionLive> searchTransactions(String cardNo, String name) {
        try {
            if (cardNo != null && !cardNo.trim().isEmpty()) {
                return repository.getTransactionsByCardNo(cardNo);
            }

            if (name != null && !name.trim().isEmpty()) {
                return repository.getTransactionsByCardNo(cardNo);
            }

            return repository.getAllTransactions();

        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}