package com.dq.empportal.Service;

import com.dq.empportal.Model.Client;
import com.dq.empportal.Repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public List<Client> getClientsCreatedToday() {
        LocalDate today = LocalDate.now();  // Use LocalDate for today's date

        // Call the repository method with the correct type
        return clientRepository.findByCreatedDate(today);
    }



    public List<Client> getClientsCreatedInWeek() {
        // Get the current date
        LocalDate today = LocalDate.now();

        // Calculate the start of a week ago
        LocalDate oneWeekAgo = today.minusDays(7);

        // Fetch clients created between one week ago and today
        return clientRepository.findByCreatedDateBetween(oneWeekAgo, today);
    }

    public void deleteClientById(Integer id){
        if(clientRepository.existsById(id)){
            clientRepository.deleteById(id);
        }else {
            // throw new ClientNotFoundException("Client with id " + id + " not found");
        }
    }
}
