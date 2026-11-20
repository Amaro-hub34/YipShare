package com.startupidea.yipshare.loan;

import com.startupidea.yipshare.R;
import com.startupidea.yipshare.error.BadRequestException;
import com.startupidea.yipshare.error.ConflictException;
import com.startupidea.yipshare.error.ResourceNotFound;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.startupidea.yipshare.item.Item;
import com.startupidea.yipshare.item.ItemAvailability;
import com.startupidea.yipshare.item.ItemPayload;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoanServiceTest {

    private final List<Loan> stored = new ArrayList<>();
    private LoanStatus savedStatus;
    private String sentCode;
    private String listedCondominium;
    private Integer requestedDays;

    private final LoanService service = new LoanService(new LoanRepository() {
        @Override
        public List<Loan> getAllLoansByCondominiumId(String condominiumId) {
            listedCondominium = condominiumId;
            return new ArrayList<>(stored);
        }

        @Override
        public List<LoanRequest> getAllLoanRequestsByCondominiumId(String condominiumId) {
            listedCondominium = condominiumId;
            return new ArrayList<>();
        }

        @Override
        public void createLoanRequest(String articleId, String requestText, int days) {
            requestedDays = days;
        }

        @Override
        public void modifyLoanStatus(String loanId, LoanStatus answer) {
            savedStatus = answer;
        }

        @Override
        public String createCode(String loanId) {
            return "ABC234";
        }

        @Override
        public Loan confirmCode(String loanId, String code) {
            sentCode = code;
            return loan(LoanStatus.ON_LOAN, false);
        }
    });

    private static Loan loan(LoanStatus status, boolean lentByMe) {
        return new Loan("loan", "Drill", "Works well", null, "other", "ana", lentByMe, null, "08/11/2026", status, "+525500000000");
    }

    private static Item item(String ownerId, ItemAvailability availability) {
        return new Item("item", ownerId, "condo", availability,
                new ItemPayload("Drill", "Tools", "d", "Good", null, Collections.singletonList("1 day")));
    }

    @Test
    public void borrowerSendsTheCodeNormalized() {
        Loan updated = service.confirmCode("user", loan(LoanStatus.ACCEPTED, false), " abc234 ");

        assertEquals("ABC234", sentCode);
        assertEquals(LoanStatus.ON_LOAN, updated.status());
    }

    @Test
    public void malformedCodeIsNotSent() {
        assertThrows(BadRequestException.class, () -> service.confirmCode("user", loan(LoanStatus.ACCEPTED, false), "12a4"));
        assertNull(sentCode);
    }

    @Test
    public void onlyActiveLoansTakeACode() {
        assertThrows(ConflictException.class, () -> service.confirmCode("user", loan(LoanStatus.PENDING, false), "ABC234"));
        assertThrows(ConflictException.class, () -> service.confirmCode("user", loan(LoanStatus.FINISHED, false), "ABC234"));
        assertNull(sentCode);
    }

    @Test
    public void lenderGeneratesTheCodeAndBorrowerTypesIt() {
        assertEquals("ABC234", service.createCode(loan(LoanStatus.ON_LOAN, true)));
        assertThrows(ConflictException.class, () -> service.createCode(loan(LoanStatus.ACCEPTED, false)));
        assertThrows(ConflictException.class, () -> service.confirmCode("user", loan(LoanStatus.ACCEPTED, true), "ABC234"));
    }

    @Test
    public void serverStatusesMapToTheAppOnes() {
        assertEquals(LoanStatus.ON_LOAN, LoanStatus.parse("on_loan"));
        assertEquals(LoanStatus.FINISHED, LoanStatus.parse("returned"));
    }

    @Test
    public void lenderAcceptsOrRejectsPendingRequest() {
        service.acceptLoanRequest("user", "loan");
        assertEquals(LoanStatus.ACCEPTED, savedStatus);

        service.rejectLoanRequest("user", "loan");
        assertEquals(LoanStatus.REJECTED, savedStatus);
    }

    @Test
    public void neighborRequestsAvailableItem() {
        service.requestLoan("user", item("owner", ItemAvailability.AVAILABLE), "text", 7);

        assertEquals(Integer.valueOf(7), requestedDays);
    }

    @Test
    public void cannotRequestOwnOrLentItems() {
        assertThrows(ResourceNotFound.class, () -> service.requestLoan("user", item("user", ItemAvailability.AVAILABLE), "t", 1));
        assertThrows(ConflictException.class, () -> service.requestLoan("user", item("owner", ItemAvailability.NOT_AVAILABLE), "t", 1));
        assertNull(requestedDays);
    }

    @Test
    public void listsActiveAndFinishedLoansOfTheSelectedCondominium() {
        stored.addAll(Arrays.asList(loan(LoanStatus.PENDING, true), loan(LoanStatus.ACCEPTED, true),
                loan(LoanStatus.REJECTED, false), loan(LoanStatus.FINISHED, false)));

        assertEquals(2, service.getAllLoansByUserId("user", "condo").size());
        assertEquals("condo", listedCondominium);

        listedCondominium = null;
        assertTrue(service.getAllLoansByUserId("user", null).isEmpty());
        assertTrue(service.getAllLoanRequests("user", null).isEmpty());
        assertNull(listedCondominium);
    }
}
