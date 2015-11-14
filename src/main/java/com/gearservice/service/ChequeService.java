package com.gearservice.service;

import com.gearservice.model.cheque.Cheque;
import com.gearservice.model.cheque.ChequeMin;
import com.gearservice.model.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class ChequeService {

    @Autowired ChequeRepository chequeRepository;
    @Autowired DiagnosticRepository diagnosticRepository;
    @Autowired NoteRepository noteRepository;
    @Autowired PhotoRepository photoRepository;
    @Autowired UserRepository userRepository;
    @Autowired PaymentRepository paymentRepository;
    @Autowired EntityManager em;

    /**
     * Method getCheques call by client-side and return all cheques from database
     * Native query use for create partial object of Cheque � ChequeMin, that has only necessary for client-side fields
     * Call with value "/cheques" and request method GET
     * @return list of all cheques, that database has
     */
    public List<ChequeMin> getMinChequesList() {
        return chequeRepository.getListOfCompactCheques();
    }

    /**
     * Method saveCheque call by client-side with data for cheque.class
     * Call with value "/cheque" and request method POST
     * @param cheque is data for Cheque.class, that was create on client-side
     * @return Cheque, that added
     */
    public Cheque synchronizeCheque(@RequestBody Cheque cheque) {
        Long ID = cheque.getId();

        chequeRepository.save(cheque);

        if(ID == null)
            return chequeRepository.findFirstByOrderByIdDesc();
        else
            return chequeRepository.findOne(ID);
    }

    /**
     * Method getCheque call by client-side, when it needs in one cheque for represent
     * Call with value of request "/cheques/{chequeID}" and request method GET
     * @param chequeID is ID of cheque in database, that client-side wants
     * @return Cheque, that client-side was request
     */
    public Cheque getCheque(@PathVariable Long chequeID) {
        return chequeRepository.findOne(chequeID);
    }

    /**
     * Method deleteCheque call by client-side, when it needs to delete one cheque
     * Call with value of request "/cheques/{chequeID}" and request method DELETE
     * @param chequeID is ID of cheque in database, that client-side wants to delete
     * @return redirect to main page
     */
    public void deleteCheque(@PathVariable Long chequeID) {
        chequeRepository.delete(chequeID);
        photoRepository.deleteByChequeId(chequeID.toString());
    }

    /**
     * Method addCheques call by client-side, when it needs to fill database few sample cheques
     * Call with value of request "/add" and in default request method GET
     * Should send in response OK status, if code works correct
     * @return redirect to main page
     */
    public ModelAndView addSampleCheques() {
        IntStream.range(0, 5).forEach(i -> chequeRepository.save(new Cheque().withRandomData()));
        return new ModelAndView("redirect:/");
    }

    public List<Cheque> attentionCheques() {
        return chequeRepository.findByDiagnosticsIsNull();
    }

//    @Transactional(readOnly = true)
//    public List<ChequeMin> attentionChequesByDelay() {
//        Long[] IDs = chequeRepository.findIdOfChequesWithDelay(OffsetDateTime.now().minusDays(3).toString());
//        return chequeRepository.getListOfCompactChequesWithIDs(IDs);
//    }

    public List<String> getAutocompleteData(String itemName) {
        switch (itemName) {
            case "customerName": return chequeRepository.listOfCustomerNames();
            case "productName": return chequeRepository.listOfProductNames();
            case "modelName": return chequeRepository.listOfModelNames();
            default: throw new IllegalArgumentException();
        }
    }

}
